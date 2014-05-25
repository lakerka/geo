package handlers;

import interfaces.ICommonOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Geometry;

import setsRelated.GeometrySet;
import views.panels.LayerJListPanel;
import views.windows.GroupFeaturesWindow;

public class GroupFeaturesHandler implements ICommonOperations {

    public LayerJListPanel layerJListPanel;
    public GroupFeaturesWindow groupFeaturesWindow;
    public MapHandler mapHandler;

    public GroupFeaturesHandler() {
        super();
    }

    public GroupFeaturesHandler(GroupFeaturesWindow groupFeaturesWindow,
            LayerJListPanel layerJListPanel, MapHandler mapHandler) {

        super();

        if (groupFeaturesWindow == null || layerJListPanel == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.groupFeaturesWindow = groupFeaturesWindow;
        this.layerJListPanel = layerJListPanel;
        this.mapHandler = mapHandler;
    }

    public int addLayer(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            this.layerJListPanel.addLayer(layer);

            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public int removeSelectedFromJpanel() {

        try {

            return this.layerJListPanel.removeSelectedLayers();

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public Layer intersectSelected(int threadCount) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must not be null!");
        }

        try {

            List<Layer> layerList = this.layerJListPanel.getSelectedLayers();

            if (layerList == null || layerList.isEmpty()) {
                return null;
            }

            GeometrySet geometrySet = new GeometrySet();

            Layer interSectLayer = layerList.get(0);

            for (int i = 1; i < layerList.size(); i++) {

                Layer layer = layerList.get(i);
                interSectLayer = geometrySet.intersect(interSectLayer, layer,
                        threadCount);

            }

            return interSectLayer;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return null;
    }

    public int addSelectedLayersToMap() {

        int noError = 1;

        try {

            List<Layer> layerList = this.layerJListPanel.getSelectedLayers();

            for (Layer layer : layerList) {

                noError = noError & this.mapHandler.addLayerToMapContent(layer);
            }

            return noError;

        } catch (Exception exception) {

            noError = 0;

            exception.printStackTrace();
        }

        return noError;
    }

    public int exportSelectedLayers() {

        int noErrors = 1;

        try {

            List<Layer> layerList = this.layerJListPanel.getSelectedLayers();

            for (Layer layer : layerList) {

                File directory = Support.getFileDirectory();

                String schemaTypeName = layer.getFeatureSource().getName()
                        .toString();

                if (null == Support.exportToShapeFile(layer, schemaTypeName,
                        directory.getParentFile(), directory.getName())) {
                    noErrors = 0;
                }
            }

            return noErrors;

        } catch (Exception e) {

            noErrors = 0;

            e.printStackTrace();
        }
        return noErrors;
    }

    public int addSelectedLayersFromMap() {

        int noErrors = 1;

        try {

            List<Layer> layerList = this.mapHandler.getSelectedOrVisibleLayer(
                    true, false);

            for (Layer layer : layerList) {

                noErrors = noErrors & addLayer(layer);
            }

            return noErrors;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return (noErrors = 0);
    }

    public int removeSelected() {

        try {

            return this.layerJListPanel.removeSelectedLayers();

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public Layer groupFeaturesByAttribute(Layer layer, String attributeName) {

        try {

            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(layer);

            if (simpleFeatureList.isEmpty()) {

                return layer;
            }

            SimpleFeature tmpSimpleFeature = simpleFeatureList.get(0);

            HashMap<Object, ArrayList<Geometry>> hashMap = new HashMap<Object, ArrayList<Geometry>>();

            for (SimpleFeature simpleFeature : simpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object attribute = simpleFeature.getAttribute(attributeName);

                if (!hashMap.containsKey(attribute)) {

                    hashMap.put(attribute, new ArrayList<Geometry>());

                }

                hashMap.get(attribute).add(geometry);

            }

            Set<Object> attributeSet = hashMap.keySet();

            SimpleFeatureTypeImpl simpleFeatureTypeImpl = null;
            SimpleFeatureBuilder simpleFeatureBuilder = null;

            int id = 0;

            List<SimpleFeature> resultSimpleFeatureList = new ArrayList<SimpleFeature>();

            for (Object attribute : attributeSet) {

                // suformuojame feature
                Collection<Geometry> collectionGeometry = hashMap
                        .get(attribute);

                Geometry geometry = Support
                        .combineIntoOneGeometry(collectionGeometry);

                if (simpleFeatureTypeImpl == null) {

                    AttributeDescriptor attributeToGroupByDescriptor = tmpSimpleFeature
                            .getFeatureType().getDescriptor(attributeName);

                    simpleFeatureTypeImpl = buildSimpleFeatureTypeImpl(
                            geometry, tmpSimpleFeature,
                            attributeToGroupByDescriptor);

                    simpleFeatureBuilder = new SimpleFeatureBuilder(
                            simpleFeatureTypeImpl);

                }

                simpleFeatureBuilder.add(geometry);
                simpleFeatureBuilder.add(attribute);
                SimpleFeature newSimpleFeature = simpleFeatureBuilder
                        .buildFeature(String.valueOf(id));
                resultSimpleFeatureList.add(newSimpleFeature);
                id++;
            }

            SimpleFeatureCollection resultSimpleFeatureCollection = DataUtilities
                    .collection(resultSimpleFeatureList);

            Layer resultLayer = Support
                    .simpleFeatureCollectionToLayer(resultSimpleFeatureCollection);

            return resultLayer;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    private SimpleFeatureTypeImpl buildSimpleFeatureTypeImpl(Geometry geometry,
            SimpleFeature simpleFeature,
            AttributeDescriptor attributeToGroupByDescriptor) {

        if (geometry == null || simpleFeature == null
                || attributeToGroupByDescriptor == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        // start building geometry
        AttributeTypeBuilder build = new AttributeTypeBuilder();
        build.setNillable(true);
        build.setCRS(simpleFeature.getType().getCoordinateReferenceSystem());
        build.setBinding(geometry.getClass());

        GeometryType geometryType = build.buildGeometryType();
        GeometryDescriptor geometryDescriptor = build.buildDescriptor(
                "the_geom", geometryType);

        List<AttributeDescriptor> attributeDescriptorList = new ArrayList<AttributeDescriptor>();
        attributeDescriptorList.add(geometryDescriptor);
        attributeDescriptorList.add(attributeToGroupByDescriptor);

        SimpleFeatureType simpleFeatureType = simpleFeature.getFeatureType();

        String newSimpleFeatureTypeNameString = new String(
                simpleFeature.getName() + "_BY_"
                        + attributeToGroupByDescriptor.getLocalName());

        SimpleFeatureTypeImpl simpleFeatureTypeImpl = new SimpleFeatureTypeImpl(
                new NameImpl(newSimpleFeatureTypeNameString),
                attributeDescriptorList, geometryDescriptor,
                simpleFeatureType.isAbstract(),
                simpleFeatureType.getRestrictions(),
                simpleFeatureType.getSuper(),
                simpleFeatureType.getDescription());

        return simpleFeatureTypeImpl;
    }

    public int groupFeatures() {

        try {

            List<Layer> layerList = this.layerJListPanel.getSelectedLayers();

            if (layerList.size() > 1) {
                this.groupFeaturesWindow
                        .displayPopUpBox("Single layer must be selected!");
                return 0;
            }

            if (layerList.isEmpty()) {
                this.groupFeaturesWindow.displayPopUpBox("Select layer!");
                return 0;
            }

            Layer layer = layerList.get(0);

            String attributeName = this.groupFeaturesWindow.getAttributeName();

            Layer resultLayer = groupFeaturesByAttribute(layer, attributeName);

            return addLayer(resultLayer);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;

    }

    public Layer groupFeaturesByAttributeOptimised(Layer layer,
            String attributeName) {

        try {

            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(layer);

            if (simpleFeatureList.isEmpty()) {

                return layer;
            }

            SimpleFeature tmpSimpleFeature = simpleFeatureList.get(0);

            HashMap<Object, ArrayList<Geometry>> hashMap = new HashMap<Object, ArrayList<Geometry>>();

            // get attribute index we are grouping by
            int attributeIndex = 0;
            List<AttributeDescriptor> tmpAttributeDescriptorList = tmpSimpleFeature
                    .getFeatureType().getAttributeDescriptors();
            for (int i = 0; i < tmpAttributeDescriptorList.size(); i++) {
                
                String curAttributeName = tmpAttributeDescriptorList.get(i)
                        .getName().getLocalPart();
                
                if (curAttributeName.equals(attributeName)) {
                    attributeIndex = i;
                    break;
                }
            }

           //collect different attribute values
            for (SimpleFeature simpleFeature : simpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object attribute = simpleFeature.getAttribute(attributeIndex);

                if (!hashMap.containsKey(attribute)) {

                    hashMap.put(attribute, new ArrayList<Geometry>());

                }

                hashMap.get(attribute).add(geometry);

            }
            Set<Object> attributeSet = hashMap.keySet();

            SimpleFeatureTypeImpl simpleFeatureTypeImpl = null;
            SimpleFeatureBuilder simpleFeatureBuilder = null;

            int id = 0;

            List<SimpleFeature> resultSimpleFeatureList = new ArrayList<SimpleFeature>();
            

            //put geometry ant attribute we grouped by to new feature
            for (Object attribute : attributeSet) {

                // suformuojame feature
                Collection<Geometry> collectionGeometry = hashMap
                        .get(attribute);
                Geometry geometry = Support
                        .combineIntoOneGeometry(collectionGeometry);
                if (simpleFeatureTypeImpl == null) {

                    AttributeDescriptor attributeToGroupByDescriptor = tmpSimpleFeature
                            .getFeatureType().getDescriptor(attributeName);

                    simpleFeatureTypeImpl = buildSimpleFeatureTypeImpl(
                            geometry, tmpSimpleFeature,
                            attributeToGroupByDescriptor);

                    simpleFeatureBuilder = new SimpleFeatureBuilder(
                            simpleFeatureTypeImpl);

                }

                simpleFeatureBuilder.add(geometry);
                simpleFeatureBuilder.add(attribute);
                SimpleFeature newSimpleFeature = simpleFeatureBuilder
                        .buildFeature(String.valueOf(id));
                resultSimpleFeatureList.add(newSimpleFeature);
                id++;
            }
            SimpleFeatureCollection resultSimpleFeatureCollection = DataUtilities
                    .collection(resultSimpleFeatureList);

            Layer resultLayer = Support
                    .simpleFeatureCollectionToLayer(resultSimpleFeatureCollection);

            return resultLayer;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}
