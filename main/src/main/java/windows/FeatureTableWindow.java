package windows;

import handlers.FeatureTableHandler;
import handlers.MapHandler;
import handlers.SelectHandler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import listeners.featureTableWindow.AddLayersFromMapToAttributeTableListener;
import listeners.featureTableWindow.AddSelectedFeaturesAsNewLayerListener;
import listeners.featureTableWindow.FilterTableContentMenuItemListener;
import listeners.featureTableWindow.OpenFileMenuItemListener;
import listeners.featureTableWindow.SelectInMapSelectedInTableListener;
import listeners.mainWindow.DisplaySelectedFeaturesMenuItemListener;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.FeatureCollectionTableModelExtended;
import org.geotools.main.Roles;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.geotools.swing.action.SafeAction;
import org.opengis.feature.simple.SimpleFeature;

public class FeatureTableWindow extends JFrame {

    private SelectHandler selectHandler;
    private MapHandler mapHandler;

    private List<DataStore> dataStoreList;
    private DataStore selectedDataStore;
    private JComboBox<String> featureTypeComboBox;
    private JTable table;
    private JTextField text;
    private FeatureTableHandler featureTableHandler;
    private JMenu fileMenu;

    public FeatureTableWindow(SelectHandler selectHandler, MapHandler mapHandler) {

        this.setTitle("Feature table");
        
        if (selectHandler == null) {
            throw new IllegalArgumentException(
                    "selectHandler must not be null!");
        }

        this.selectHandler = selectHandler;
        this.mapHandler = mapHandler;

        this.dataStoreList = new ArrayList<DataStore>();

        getContentPane().setLayout(new BorderLayout());

        text = new JTextField(80);
        text.setText("include"); // include selects everything!
        getContentPane().add(text, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(5, 5));
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        fileMenu = new JMenu("File");
        menubar.add(fileMenu);

        featureTypeComboBox = new JComboBox();
        menubar.add(featureTypeComboBox);

        JMenu dataMenu = new JMenu("Data");
        menubar.add(dataMenu);
        pack();

        featureTableHandler = new FeatureTableHandler(this.mapHandler, this);

        addJMenuItem(Roles.OpenFile.label, new OpenFileMenuItemListener(this),
                fileMenu);

        // add layers from map menu item
        fileMenu.addSeparator();

        addJMenuItem(Roles.AddLayersFromMap.label,
                new AddLayersFromMapToAttributeTableListener(this), fileMenu);

        fileMenu.addSeparator();

        addJMenuItem(Roles.GetFeatures.label,
                new FilterTableContentMenuItemListener(this), dataMenu);

        // JMenuItem countRowsButton = new JMenuItem(Roles.CountRows.label);
        // countRowsButton.addActionListener(new CountRowsListener(
        // this));
        // dataMenu.add(countRowsButton);

        addJMenuItem(Roles.DisplaySelectedFeatures.label,
                new DisplaySelectedFeaturesMenuItemListener(this), dataMenu);

        addJMenuItem(Roles.SelectInMapSelectedInTable.label,
                new SelectInMapSelectedInTableListener(this), dataMenu);

        addJMenuItem("Add to map selected features as layer",
                new AddSelectedFeaturesAsNewLayerListener(
                        this.featureTableHandler), dataMenu);

    }

    // add jmenu items (buttons)
    private int addJMenuItem(String label, ActionListener actionListener,
            JMenu jMenu) {

        if (jMenu == null || label == null) {

            throw new IllegalArgumentException("Arguments must not be null!");
        }

        JMenuItem jMenuItem = new JMenuItem(label);
        jMenuItem.addActionListener(actionListener);
        jMenu.add(jMenuItem);

        return 1;
    }

    public int clearDataStoresAndSelectedDataStore() {

        try {

            this.dataStoreList.clear();
            this.selectedDataStore = null;

        } catch (NullPointerException e) {

            e.printStackTrace();
        }
        return 1;
    }

    public void addLayersThatAreInMap() {

        List<Layer> mapLayers = this.mapHandler.getLayers();

        for (Layer layer : mapLayers) {

            addLayer(layer);
        }
    }

    // /////////// add new layer by selecting file/////////////////
    public void addLayer() {

        try {

            DataStore dataStore = this.featureTableHandler
                    .loadShapeFileDataStore();

            if (dataStore != null) {

                this.dataStoreList.add(dataStore);
                updateUI();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // /////////// add new layer /////////////////
    public void addLayer(Layer layer) {

        try {

            DataStore dataStore = Support.layerToDataStore(layer);

            if (dataStore != null) {

                this.dataStoreList.add(dataStore);
                updateUI();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public FeatureTableHandler getFeatureTableHandler() {

        return this.featureTableHandler;
    }

    // /////////// update user interface /////////////////
    private void updateUI() throws Exception {

        try {

            Vector<String> layerNames = new Vector<String>();

            // getting type names of shapefiles we have loaded
            for (DataStore dataStore : this.dataStoreList) {

                String[] typeNamesArray = dataStore.getTypeNames();

                if (typeNamesArray.length > 0) {

                    String layerTypeName = typeNamesArray[0];

                    SimpleFeatureSource simpleFeatureSource = dataStore
                            .getFeatureSource(layerTypeName);

                    Layer layer = Support
                            .simpleFeatureSourceToLayer(simpleFeatureSource);
                    
                    //try to use layer title if possible
                    if (layer.getTitle() != null && !layer.getTitle().isEmpty()) {

                        layerNames.add(layer.getTitle());

                    } else {

                        layerNames.add(layerTypeName);

                    }
                }

            }

            ComboBoxModel<String> cbm = new DefaultComboBoxModel<String>(
                    layerNames);

            featureTypeComboBox.setModel(cbm);

            table.setModel(new DefaultTableModel(5, 5));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // /////////// filter features given filter phraze /////////////////
    public int filterTableContent() {

        try {

            if (featureTypeComboBox.getSelectedItem() == null
                    || this.dataStoreList.isEmpty()) {
                return 0;
            }

            String typeName = (String) featureTypeComboBox.getSelectedItem();

            SimpleFeatureSource source = getFirstDataStoreByTypeName(
                    this.dataStoreList, typeName).getFeatureSource(typeName);

            String cqlPredicate = this.text.getText();

            FeatureCollectionTableModelExtended model = this.featureTableHandler
                    .filterFeatures(typeName, source, cqlPredicate);

            setTableModel(model);

            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();

        }
        return 0;
    }

    private DataStore getFirstDataStoreByTypeName(
            List<DataStore> dataStoreList, String typeName) {

        if (dataStoreList == null) {
            throw new IllegalArgumentException(
                    "shapefileDataStoreList must not be null!");
        }

        try {

            for (DataStore dataStore : dataStoreList) {

                String[] typeNames = dataStore.getTypeNames();

                if (typeNames.length > 0 && typeNames[0].equals(typeName)) {

                    return dataStore;
                }
            }
        } catch (Exception exception) {

            exception.printStackTrace();
        }

        // not found
        return null;

    }

    // method that should help prevent to set
    // invalid model
    private void setTableModel(FeatureCollectionTableModelExtended tableModel) {

        if (tableModel == null) {
            throw new IllegalArgumentException("tableModel must not be null!");
        }
        this.table.setModel(tableModel);
    }

    // display selected featured from map to table
    public int displaySelectedFeatures() {

        try {

            List<Layer> layersOpenForSelect = this.selectHandler
                    .getLayersOpenForSelect();

            List<SimpleFeature> selectedFeatures = new ArrayList<SimpleFeature>();
            selectedFeatures.addAll(this.selectHandler.getSelectedFeatures());


            FeatureCollectionTableModelExtended tableModel = new FeatureCollectionTableModelExtended(
                    DataUtilities.collection(selectedFeatures));

            setTableModel(tableModel);
            
            return 1;

        } catch (Exception exception) {
            
            exception.printStackTrace();
        }
        
        return 0;
    }

    public List<SimpleFeature> getSelectedTableFeatures() {

        List<SimpleFeature> simpleFeatureList = null;

        if (this.table.getModel() instanceof FeatureCollectionTableModelExtended) {

            try {

                FeatureCollectionTableModelExtended features = (FeatureCollectionTableModelExtended) this.table
                        .getModel();

                int[] selectedIndexes = this.table.getSelectedRows();

                simpleFeatureList = features.getSimpleFeatures(selectedIndexes);

            } catch (Exception exception) {

                exception.printStackTrace();
                simpleFeatureList = null;
            }

            return simpleFeatureList;

        } else {

            throw new ClassCastException(
                    "table model instance must be of type: FeatureCollectionTableModelExtended!");
        }
    }

    public int selectInMapFromSelectedInTable() {

        List<SimpleFeature> simpleFeaturesList = getSelectedTableFeatures();

        if (simpleFeaturesList == null || simpleFeaturesList.isEmpty()
                || this.selectHandler == null) {

            return 0;
        }

        return this.selectHandler.selectByFeatures(simpleFeaturesList);
    }

    public DataStore getSelectedDataStore() {
        return this.selectedDataStore;
    }

    public String getNewLayerName() {

        String layerName = JOptionPane.showInputDialog("Enter a layer name");
        return layerName;

    }
    
    public void displayPopUpBox(String message) {

        JOptionPane.showMessageDialog(this, message);

    }

}
