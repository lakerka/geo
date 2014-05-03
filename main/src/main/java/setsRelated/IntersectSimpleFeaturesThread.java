package setsRelated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.main.Support;
import org.geotools.process.vector.IntersectionFeatureCollection;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class IntersectSimpleFeaturesThread implements Runnable {

    // private Thread thread;
    private String runnableName;
    private SimpleFeatureCollection simpleFeatureList1;

    private SimpleFeatureCollection simpleFeatureList2;
    private List<SimpleFeature> localSimpleFeatureList;
    private List<SimpleFeature> globalIntersectSimpleFeatureList;
    private SimpleFeatureType simpleFeatureType;
    private long id;
    private InverseSemaphore inverseSemaphore;

    public IntersectSimpleFeaturesThread(String name,
            SimpleFeatureCollection simpleFeatureList1,
            SimpleFeatureCollection simpleFeatureList2,
            List<SimpleFeature> intersectSimpleFeatureList, long id, InverseSemaphore inverseSemaphore) {

        this.runnableName = name;
        this.simpleFeatureList1 = simpleFeatureList1;
        this.simpleFeatureList2 = simpleFeatureList2;
        this.globalIntersectSimpleFeatureList = intersectSimpleFeatureList;
        this.localSimpleFeatureList = new ArrayList<SimpleFeature>();
        this.id = id;
        this.inverseSemaphore = inverseSemaphore;
        System.out.println("Creating " + this.runnableName);
    }

    public String getThreadName() {
        return this.runnableName;
    }

    private long getId() {
        return this.id++;
    }

    public void run() {

        try {
            System.out.println("Starting " + this.runnableName);
            // intersection of empty set and something will not produce any
            // results
            if (simpleFeatureList1.isEmpty() || simpleFeatureList2.isEmpty()) {
                return;
            }

            this.simpleFeatureList2 = filterSimpleFeatureCollection(
                    simpleFeatureList1, simpleFeatureList2);

            IntersectionFeatureCollection ifc = new IntersectionFeatureCollection();
            SimpleFeatureIterator iter;

            SimpleFeatureCollection kk = ifc.execute(simpleFeatureList1,
                    simpleFeatureList2, null, null, null, null, null);
            iter = kk.features();

            for (; iter.hasNext();) {

                SimpleFeature sf = iter.next();

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
                        sf.getFeatureType());

                builder.addAll(sf.getAttributes());
                localSimpleFeatureList.add(builder.buildFeature("" + getId()));
            }
            this.globalIntersectSimpleFeatureList
                    .addAll(localSimpleFeatureList);
        } catch (Exception exception) {

            exception.printStackTrace();
            
        } finally {
            
            this.inverseSemaphore.taskCompleted();
            
            System.out.println("Ending " + this.runnableName);
        }
    }

    // public void run() {
    //
    // System.out.println("Running " + threadName);
    //
    // try {
    //
    // List<Geometry> geometryList1 = Support
    // .simpleFeatureListToGeometryList(this.simpleFeatureList1);
    // List<Geometry> geometryList2 = Support
    // .simpleFeatureListToGeometryList(this.simpleFeatureList2);
    // List<SimpleFeature> localIntersectSimpleFeatureList = new
    // ArrayList<SimpleFeature>();
    //
    // List<Object> newSimpleFeatureList = new ArrayList<Object>();
    // System.out.println("Starting main loop " + threadName);
    // for (int i = 0; i < geometryList1.size(); i++) {
    //
    // for (int j = 0; j < geometryList2.size(); j++) {
    //
    // Geometry geometry1 = geometryList1.get(i);
    // Geometry geometry2 = geometryList2.get(j);
    //
    // if (geometry1.intersects(geometry2)) {
    //
    // Geometry intersectionOfGeometries = geometry1
    // .intersection(geometry2);
    //
    // SimpleFeature simpleFeature1 = simpleFeatureList1
    // .get(i);
    // SimpleFeature simpleFeature2 = simpleFeatureList2
    // .get(j);
    //
    // if (simpleFeatureType == null) {
    //
    // simpleFeatureType = buildSimpleFeatureType(
    // simpleFeature1, simpleFeature2,
    // intersectionOfGeometries);
    // }
    //
    // newSimpleFeatureList.clear();
    //
    // // add new geometry
    // newSimpleFeatureList.add(intersectionOfGeometries);
    //
    // // coonstruct attributes according to type
    // addAllAttributesExceptGeometry(newSimpleFeatureList,
    // simpleFeature1);
    // addAllAttributesExceptGeometry(newSimpleFeatureList,
    // simpleFeature2);
    // // SimpleFeatureBuilder b; b.ad
    // SimpleFeature newSimpleFeature = SimpleFeatureBuilder
    // .build(simpleFeatureType, newSimpleFeatureList,
    // null);
    //
    // //localIntersectSimpleFeatureList.add(newSimpleFeature);
    // }
    // }
    // }
    //
    // this.globalIntersectSimpleFeatureList
    // .addAll(localIntersectSimpleFeatureList);
    //
    // } catch (Exception e) {
    //
    // e.printStackTrace();
    // }
    //
    // System.out.println("Thread " + threadName + " exiting.");
    //
    // }

    private int addAllAttributesExceptGeometry(List<Object> target,
            SimpleFeature simpleFeature) {

        if (target == null || simpleFeature == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }

        Object attribute;
        int attributeCount = simpleFeature.getAttributeCount();

        for (int j = 0; j < attributeCount; j++) {

            attribute = simpleFeature.getAttribute(j);

            if (!(attribute instanceof Geometry)) {

                target.add(attribute);
            }
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

        // simpleFeatureTypeBuilder.add("INTERSECTION_ID", Integer.class);
        simpleFeatureTypeBuilder.setAbstract(simpleFeature1.getFeatureType()
                .isAbstract());
        simpleFeatureTypeBuilder.setSuperType(simpleFeature1.getFeatureType());

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

            Collection<Property> propertiesCollection = simpleFeature
                    .getProperties();
            Iterator<Property> propertiesCollectionIterator = propertiesCollection
                    .iterator();
            int attributeCount = simpleFeature.getAttributeCount();

            for (int i = 0; i < attributeCount; i++) {

                Object attribute = simpleFeature.getAttribute(i);

                // avoid including previous geometries
                if (attribute instanceof Geometry) {
                    propertiesCollectionIterator.next();
                    continue;
                }

                simpleFeatureTypeBuilder.add(marker
                        + propertiesCollectionIterator.next().getName()
                                .toString(), attribute.getClass());

            }

            return 1;

        } catch (IndexOutOfBoundsException exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public void setThreadName(String threadName) {
        this.runnableName = threadName;
    }

    public SimpleFeatureCollection filterSimpleFeatureCollection(
            SimpleFeatureCollection simpleFeatureCollection,
            SimpleFeatureCollection collectionToFilter) {

        try {

            ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(
                    simpleFeatureCollection.getBounds(),
                    simpleFeatureCollection.getSchema()
                            .getCoordinateReferenceSystem());

            FilterFactory2 filterFactory2 = CommonFactoryFinder
                    .getFilterFactory2();

            String geometryDescriptorLocalName = simpleFeatureCollection
                    .getSchema().getGeometryDescriptor().getLocalName();

            Filter filter = filterFactory2.bbox(
                    filterFactory2.property(geometryDescriptorLocalName),
                    referencedEnvelope);

            SimpleFeatureCollection filteredCollection = collectionToFilter
                    .subCollection(filter);

            if (filteredCollection == null) {
                throw new IllegalArgumentException("Filtering returned null!");
            }

            return filteredCollection;

        } catch (Exception exception) {

            return collectionToFilter;
        }
    }

}
