package views.panels;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import listeners.featureTableWindow.AddLayersFromMapToAttributeTableListener;
import listeners.featureTableWindow.FilterTableContentMenuItemListener;
import listeners.featureTableWindow.OpenFileMenuItemListener;
import listeners.featureTableWindow.SelectInMapSelectedInTableListener;
import listeners.mainWindow.DisplaySelectedFeaturesMenuItemListener;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Roles;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.geotools.swing.MapLayerTable;
import org.geotools.swing.MapLayerTableCellRenderer.LayerControlItem;
import org.geotools.swing.action.SafeAction;

import handlers.FeatureTableHandler;
import handlers.MapHandler;
import handlers.SelectHandler;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;
import org.geotools.map.Layer;

import views.other.LayerListCellRenderer;

public class LayerJListPanel extends JPanel {

    private JList<Layer> layerJList = null;
    private DefaultListModel<Layer> layerJListModel = null;

    public LayerJListPanel(String paneLabelText) {

        this.layerJListModel = new DefaultListModel<Layer>();
        this.layerJList = new JList<Layer>(this.layerJListModel);
        init(paneLabelText);

    }

    public LayerJListPanel(DefaultListModel<Layer> layerJListModel,
            String paneLabelText) {

        if (layerJListModel == null) {
            throw new IllegalArgumentException(
                    "layerJListModel must not be null!");
        }

        this.layerJListModel = layerJListModel;
        this.layerJList = new JList<Layer>(this.layerJListModel);
        init(paneLabelText);

    }

    private void init(String paneLabelText) {

        // set our custom cell renderer
        layerJList.setCellRenderer(new LayerListCellRenderer());

        layerJList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        layerJList.setLayoutOrientation(JList.VERTICAL_WRAP);
        layerJList.setVisibleRowCount(-1);

        JScrollPane listScroller = new JScrollPane(layerJList);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel(paneLabelText);
        label.setLabelFor(layerJList);

        this.add(label);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(listScroller);

        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    }
    
    public void repaintPanel() {

        try {
            this.layerJList.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int removeLayer(int index) {

        try {

            layerJListModel.remove(index);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    public int addLayer(Layer layer) {

        try {

            layerJListModel.addElement(layer);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int addAllLayers(List<Layer> layerList) {

        if (layerList == null) {
            throw new IllegalArgumentException("layerList must not be null!");
        }

        try {

            for (Layer layer : layerList) {
                layerJListModel.addElement(layer);
            }

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int clearLayers() {

        try {

            layerJListModel.removeAllElements();
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Layer> getSelectedLayers() {

        try {

            return layerJList.getSelectedValuesList();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public int removeSelectedLayers() {

        try {
            
            int[] selectedIndexList = layerJList.getSelectedIndices();
            
            for (int i = selectedIndexList.length-1; i >= 0; i--) {
                
                int index = selectedIndexList[i]; 
                this.layerJListModel.remove(index);
            }
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    public List<Layer> getAllLayers() {

        try {

            List<Layer> layerList = new ArrayList<Layer>();
            for (int i = 0; i < this.layerJListModel.getSize(); i++) {
                
                Layer layer = this.layerJListModel.getElementAt(i);
                
                layerList.add(layer);
            }
            
            return layerList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
