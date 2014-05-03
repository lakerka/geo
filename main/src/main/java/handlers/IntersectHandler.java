package handlers;

import interfaces.ICommonOperations;

import java.io.File;
import java.util.List;

import org.geotools.main.Main;
import org.geotools.main.Support;
import org.geotools.map.Layer;

import setsRelated.GeometrySet;
import windows.FeatureTableWindow;
import windows.intersect.IntersectWindow;
import windows.intersect.LayerJListPanel;

public class IntersectHandler implements ICommonOperations {

    public FeatureTableWindow featureTableWindow;
    public LayerJListPanel layerJListPanel;
    public IntersectWindow intersectWindow;
    public MapHandler mapHandler;

    public IntersectHandler(FeatureTableWindow featureTableWindow,
            LayerJListPanel layerJListPanel, IntersectWindow intersectWindow,
            MapHandler mapHandler) {

        super();

        if (featureTableWindow == null || layerJListPanel == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.featureTableWindow = featureTableWindow;
        this.layerJListPanel = layerJListPanel;
        this.intersectWindow = intersectWindow;
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

    public int removeSelected() {

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

                String schemaTypeName = layer.getFeatureSource().getName().toString();

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
            
            List<Layer> layerList = this.mapHandler.getSelectedOrVisibleLayer(true, false);
            
            for (Layer layer : layerList) {
                
                noErrors = noErrors & addLayer(layer);
            }
            
            return noErrors;
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        
        return (noErrors=0);
    }
}
