package views.windows.intersect;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import listeners.common.AddLayerFromFileListener;
import listeners.common.AddSelectedLayersFromMapListener;
import listeners.common.AddSelectedLayersToMapListener;
import listeners.common.RemoveSelectedListener;
import listeners.featureTableWindow.AddLayersFromMapToAttributeTableListener;
import listeners.featureTableWindow.FilterTableContentMenuItemListener;
import listeners.featureTableWindow.OpenFileMenuItemListener;
import listeners.featureTableWindow.SelectInMapSelectedInTableListener;
import listeners.intersectWindow.IntersectButtonListener;
import listeners.mainWindow.DisplaySelectedFeaturesMenuItemListener;
import listeners.mainWindow.ExportLayerButtonListener;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Roles;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.geotools.swing.MapLayerTable;
import org.geotools.swing.MapLayerTableCellRenderer.LayerControlItem;
import org.geotools.swing.action.SafeAction;

import views.panels.LayerJListPanel;
import views.windows.FeatureTableWindow;
import handlers.FeatureTableHandler;
import handlers.IntersectHandler;
import handlers.MapHandler;
import handlers.SelectHandler;

public class IntersectWindow extends JFrame {

    public handlers.IntersectHandler intersectWindowHandler;

    public FeatureTableWindow featureTableWindow;
    public LayerJListPanel layerJListPanel;
    private MapHandler mapHandler;

    public IntersectWindow(SelectHandler selectHandler, MapHandler mapHandler) {

        this.setTitle("Intersect");
        
        this.featureTableWindow = new FeatureTableWindow(selectHandler,
                mapHandler);

        this.mapHandler = mapHandler;

        JList<Layer> list;

        // add menu bar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        DefaultListModel<Layer> listModel = new DefaultListModel<Layer>();

        layerJListPanel = new LayerJListPanel(listModel,
                "Layers to select from");

        this.intersectWindowHandler = new IntersectHandler(
                this.featureTableWindow, this.layerJListPanel, this, mapHandler);

        JPanel buttonPane = new JPanel();

        createButtonAndAddToButtonPane("Add selected layers from map",
                new AddSelectedLayersFromMapListener(
                        this.intersectWindowHandler), buttonPane);

        createButtonAndAddToButtonPane("Add layer from file",
                new AddLayerFromFileListener(this.intersectWindowHandler),
                buttonPane);
        createButtonAndAddToButtonPane("Remove selected",
                new RemoveSelectedListener(this.intersectWindowHandler),
                buttonPane);
        createButtonAndAddToButtonPane("Intersect selected",
                new IntersectButtonListener(this.intersectWindowHandler),
                buttonPane);

        // add selected layers to map
        createButtonAndAddToButtonPane(
                "Add selected to map",
                new AddSelectedLayersToMapListener(this.intersectWindowHandler),
                buttonPane);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(layerJListPanel, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        pack();

    }

    private int createButtonAndAddToButtonPane(String JButtonText,
            ActionListener actionListener, JPanel buttonJPanel) {

        JButton jButton = new JButton(JButtonText);

        if (actionListener != null) {
            jButton.addActionListener(actionListener);
        }

        buttonJPanel
                .setLayout(new BoxLayout(buttonJPanel, BoxLayout.LINE_AXIS));
        buttonJPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonJPanel.add(Box.createHorizontalGlue());
        buttonJPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        buttonJPanel.add(jButton);

        return 1;
    }

    public String getNameOfLayerToExport() {
        String name = JOptionPane.showInputDialog("Enter a layer name");
        return name;
    }

}
