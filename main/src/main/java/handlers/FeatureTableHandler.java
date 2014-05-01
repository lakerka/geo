package handlers;

import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.main.FeatureCollectionTableModelExtended;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import windows.FeatureTableWindow;

public class FeatureTableHandler {

    private MapHandler mapHandler;
    private FeatureTableWindow featureTableWindow;
    
    public FeatureTableHandler(MapHandler mapHandler, FeatureTableWindow featureTableWindow) {

        super();
        
        if (mapHandler == null || featureTableWindow == null) {
            throw new IllegalArgumentException(
                    "Arguments must not be null!");
        }
        
        
        this.mapHandler = mapHandler;
        this.featureTableWindow = featureTableWindow;
        
    }

    public ShapefileDataStore loadShapeFileDataStore() throws Exception {
        
        ShapefileDataStore dataStore;
        
        try {
            
            SimpleFeatureSource simpleFeatureSource = Support.loadShapeFile();
            dataStore = (ShapefileDataStore) simpleFeatureSource.getDataStore();
            
        } catch (Exception exception) {

            dataStore = null;
            exception.printStackTrace();
        }
        
        return dataStore;
    }
    

    public FeatureCollectionTableModelExtended filterFeatures(String typeName, SimpleFeatureSource source, String cqlPredicate) throws Exception {
   
        try {
            
            Filter filter = CQL.toFilter(cqlPredicate);
            SimpleFeatureCollection features = source.getFeatures(filter);
            FeatureCollectionTableModelExtended model = new FeatureCollectionTableModelExtended(
                    features);
            
            return model;
            
        }catch(Exception exception) {
            
            exception.printStackTrace();
            return null;
        }
        
    }

//    public void countFeatures() throws Exception {
//        String typeName = (String) featureTypeCBox.getSelectedItem();
//        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
//
//        Filter filter = CQL.toFilter(text.getText());
//        SimpleFeatureCollection features = source.getFeatures(filter);
//
//        int count = features.size();
//        JOptionPane.showMessageDialog(text, "Number of selected features:"
//                + count);
//    }
//
//    public void queryFeatures() throws Exception {
//        String typeName = (String) featureTypeCBox.getSelectedItem();
//        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
//
//        FeatureType schema = source.getSchema();
//        String name = schema.getGeometryDescriptor().getLocalName();
//
//        Filter filter = CQL.toFilter(text.getText());
//
//        Query query = new Query(typeName, filter, new String[] { name });
//
//        SimpleFeatureCollection features = source.getFeatures(query);
//
//        FeatureCollectionTableModel model = new FeatureCollectionTableModel(
//                features);
//        table.setModel(model);
//    }
    
    public int addSelectedFeaturesAsNewLayerToMap() {
        
        try {
            //features are selected only from one layer
            List<SimpleFeature> simpleFeatureList = this.featureTableWindow.getSelectedTableFeatures();
            
            Layer layer = Support.simpleFeatureListToLayer(simpleFeatureList);
            
            this.mapHandler.addLayerToMapContent(layer);
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
        }
        
        return 1;
    }
    
    public int addLayerToMap(Layer layer) {
        
        if (layer == null) {
            throw new IllegalArgumentException(
                    "layer must not be null!");
        }
        
        try {
            
            return this.mapHandler.addLayerToMapContent(layer);
            
        } catch (Exception e) {
            
            e.printStackTrace();
        
        }
        
        return 0;
    }

}
