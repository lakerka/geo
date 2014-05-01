package SetsRelated;

import handlers.MapHandler;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.main.Support;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.geotools.resources.BoundingBoxes;
import org.geotools.styling.SLD;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class IntersectTest {
    
    public SimpleFeatureSource intersect(SimpleFeatureSource simpleFeatureSource1, SimpleFeatureSource simpleFeatureSource2, int threadCount) {

        if (simpleFeatureSource1 == null || simpleFeatureSource2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {
            List<SimpleFeature> simpleFeatureList1 = DataUtilities.list(simpleFeatureSource1.getFeatures());
            List<SimpleFeature> simpleFeatureList2 = DataUtilities.list(simpleFeatureSource2.getFeatures());;

            List<SimpleFeature> intersectionList = this.intersect(
                    simpleFeatureList1, simpleFeatureList2, threadCount);

            // if intersection result is null then return empty result
            if (intersectionList == null) {
                return null;
            }

            SimpleFeatureCollection intersectCollection = DataUtilities
                    .collection(intersectionList);
            
            SimpleFeatureSource intersectSimpleFeatureSource = DataUtilities.source(intersectCollection);
            
            return intersectSimpleFeatureSource;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }
    
    public Layer intersect(Layer layer1, Layer layer2, int threadCount) {

        if (layer1 == null || layer2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {
            List<SimpleFeature> simpleFeatureList1 = Support
                    .layerToSimpleFeatureList(layer1);
            List<SimpleFeature> simpleFeatureList2 = Support
                    .layerToSimpleFeatureList(layer2);

            List<SimpleFeature> intersectionList = this.intersect(
                    simpleFeatureList1, simpleFeatureList2, threadCount);

            // if intersection result is null then return empty result
            if (intersectionList == null) {
                return null;
            }
            
            SimpleFeatureCollection intersectionCollection = DataUtilities
                    .collection(intersectionList);

            Layer intersectionLayer = new FeatureLayer(intersectionCollection,
                    SLD.createSimpleStyle(intersectionCollection.getSchema()));
            
//do not skip
            return intersectionLayer;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }

    public List<SimpleFeature> intersect(
            List<SimpleFeature> simpleFeatureList1,
            List<SimpleFeature> simpleFeatureList2, int threadCount) {

        if (simpleFeatureList1 == null || simpleFeatureList2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            List<Geometry> geometryList1 = Support
                    .simpleFeatureListToGeometryList(simpleFeatureList1);
            List<Geometry> geometryList2 = Support
                    .simpleFeatureListToGeometryList(simpleFeatureList2);
            List<SimpleFeature> localIntersectSimpleFeatureList = new ArrayList<SimpleFeature>();

            SimpleFeatureType simpleFeatureType = null;

            int simpleFeatureId = -1;

            for (int i = 0; i < geometryList1.size(); i++) {

                for (int j = 0; j < geometryList2.size(); j++) {

                    Geometry geometry1 = geometryList1.get(i);
                    Geometry geometry2 = geometryList2.get(j);

                    if (geometry1.intersects(geometry2)) {

                        Geometry intersectionOfGeometries = geometry1
                                .intersection(geometry2);
                        
                        SimpleFeature simpleFeature1 = simpleFeatureList1
                                .get(i);
                        SimpleFeature simpleFeature2 = simpleFeatureList2
                                .get(j);

                        if (simpleFeatureType == null) {

                            simpleFeatureType = buildSimpleFeatureType(
                                    simpleFeature1, simpleFeature2,
                                    intersectionOfGeometries);
                        }
                        List<Object> newSimpleFeatureList = new ArrayList<Object>();

                        // add new geometry
                        newSimpleFeatureList.add(intersectionOfGeometries);

                        // coonstruct attributes according to type
                        addAllAttributesExceptGeometry(newSimpleFeatureList,
                                simpleFeature1);
                        addAllAttributesExceptGeometry(newSimpleFeatureList,
                                simpleFeature2);

                        // id of newly generated feature
                        simpleFeatureId += 1;

                        // new combined simpleFeature
                        // SimpleFeature newSimpleFeature = SimpleFeatureBuilder
                        // .build(simpleFeatureType, newSimpleFeatureList,
                        // String.valueOf(simpleFeatureId));

                        SimpleFeature newSimpleFeature = SimpleFeatureBuilder
                                .build(simpleFeatureType, newSimpleFeatureList,
                                        null);

                        localIntersectSimpleFeatureList.add(newSimpleFeature);
                    }
                }
            }

            return localIntersectSimpleFeatureList;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    private void removeGeometries(List<Object> objectList) {

        if (objectList == null) {
            throw new IllegalArgumentException("objectList must not be null!");
        }

        for (int k = objectList.size() - 1; k >= 0; k--) {

            if (objectList.get(k) instanceof Geometry) {

                objectList.remove(k);
            }
        }
    }

    private int addAllAttributesExceptGeometry(List<Object> target,
            SimpleFeature simpleFeature) {

        if (target == null || simpleFeature == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }

        for (int j = 0; j < simpleFeature.getAttributeCount(); j++) {

            Object attribute = simpleFeature.getAttribute(j);
            // TODO uncomment
            // if (attribute instanceof Geometry) {
            // continue;
            // }

            target.add(attribute);
        }

        return 1;
    }

    // type of form: geometry | simpleFeature1 attributes | simpleFeature2
    // attributes |
    private SimpleFeatureType buildSimpleFeatureType(
            SimpleFeature simpleFeature1, SimpleFeature simpleFeature2,
            Geometry geometry) {

        if (simpleFeature1 == null || simpleFeature2 == null
                || geometry == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }

        SimpleFeatureTypeBuilder simpleFeatureTypeBuilder = new SimpleFeatureTypeBuilder();

        /*
         * set the name of simplefeature (layer)
         */
        simpleFeatureTypeBuilder.setName(simpleFeature1.getName().toString()
                + "_INTERSECT_" + simpleFeature2.getName().toString());

        /*
         * get coordinate reference system
         */
        BoundingBox boundingBox = simpleFeature1.getBounds();
        boundingBox.include(simpleFeature2.getBounds());
        CoordinateReferenceSystem coordinateReferenceSystem = boundingBox
                .getCoordinateReferenceSystem();
        /*
         * add geometry
         */
        // set default geometry name
        simpleFeatureTypeBuilder.setDefaultGeometry("the_geom");
        simpleFeatureTypeBuilder.add("the_geom", geometry.getClass(),
                coordinateReferenceSystem);

        // add all attributes except geometries
        addAttributesToSimpleFeatureTypeBuilder(simpleFeature1,
                simpleFeatureTypeBuilder, "_1_");
        addAttributesToSimpleFeatureTypeBuilder(simpleFeature2,
                simpleFeatureTypeBuilder, "_2_");

        // build the type
        SimpleFeatureType simpleFeatureType = simpleFeatureTypeBuilder
                .buildFeatureType();

        return simpleFeatureType;
    }

    // adds all attributes except geometry attribute
    private int addAttributesToSimpleFeatureTypeBuilder(
            SimpleFeature simpleFeature,
            SimpleFeatureTypeBuilder simpleFeatureTypeBuilder, String marker) {

        if (simpleFeature == null || simpleFeatureTypeBuilder == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            int attributeCount = simpleFeature.getAttributeCount();
            AttributeDescriptor attributeDescriptor = simpleFeature
                    .getDescriptor();

            Collection<Property> propertiesCollection = simpleFeature
                    .getProperties();
            Iterator<Property> propertiesCollectionIterator = propertiesCollection
                    .iterator();

            for (int i = 0; i < attributeCount; i++) {

                Object attribute = simpleFeature.getAttribute(i);
                // TODO uncomment
                // // avoid including geometries
                // if (attribute instanceof Geometry) {
                // continue;
                // }

                if (attribute instanceof Geometry) {

                    simpleFeatureTypeBuilder.add(marker
                            + propertiesCollectionIterator.next().getName()
                                    .toString(), attribute.getClass(),
                            simpleFeature.getFeatureType()
                                    .getCoordinateReferenceSystem());

                } else {

                    simpleFeatureTypeBuilder.add(marker
                            + propertiesCollectionIterator.next().getName()
                                    .toString(), attribute.getClass());
                }
            }

            return 1;

        } catch (IndexOutOfBoundsException exception) {

            exception.printStackTrace();
        }

        return 0;
    }

}
