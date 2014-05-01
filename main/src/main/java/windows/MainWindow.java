package windows;

import handlers.MapHandler;
import handlers.SelectHandler;
import handlers.ZoomToSelectHandler;

import java.awt.Button;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import listeners.mainWindow.AddLayerButtonListener;
import listeners.mainWindow.DisplayAttributeTableButtonListener;
import listeners.mainWindow.DisplayIntersectWindowButtonListener;
import listeners.mainWindow.DisplaySumCharackteristicsWindowListener;
import listeners.mainWindow.ExportLayerButtonListener;
import listeners.mainWindow.SelectButtonListener;
import listeners.mainWindow.ZoomToSelectionButtonListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Roles;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.tool.CursorTool;

import windows.intersect.IntersectWindow;

public class MainWindow {

    public MapHandler mapHandler;
    public JMapFrame mapFrame;
    public SelectHandler selectHandler;
    public ZoomToSelectHandler zoomToSelectHandler;
    public FeatureTableWindow featureTableWindow;
    public SumCharacteristicsWindow sumCharacteristicsWindow;

    public MainWindow(SimpleFeatureSource featureSource, int windowLength,
            int windowHeight) {

        // Create a map context
        this.mapHandler = new MapHandler(new MapContent());

        if (featureSource != null) {
            addLayer(featureSource);
        }

        // Create a JMapFrame with custom toolbar buttons
        this.mapFrame = new JMapFrame(mapHandler.getMapContent());
        this.mapFrame.enableToolBar(true);
        this.mapFrame.enableStatusBar(true);
        this.mapFrame.enableLayerTable(true);

        JToolBar toolbar = this.mapFrame.getToolBar();
        toolbar.addSeparator();

        Button addLayerButton = new Button(Roles.AddLayer.label);
        addLayerButton.addActionListener(new AddLayerButtonListener());
        toolbar.add(addLayerButton);

        Button selectButton = new Button(Roles.Select.label);
        selectButton.addActionListener(new SelectButtonListener());
        selectButton.setFocusable(true);
        selectButton.setFocusTraversalKeysEnabled(false);

        toolbar.add(selectButton);

        Button zoomToSelectionButton = new Button(Roles.ZoomToSelection.label);
        zoomToSelectionButton
                .addActionListener(new ZoomToSelectionButtonListener());
        toolbar.add(zoomToSelectionButton);

        /*
         * initializing button functionality
         */
        this.selectHandler = new SelectHandler(this.mapFrame, this.mapHandler);
        this.zoomToSelectHandler = new ZoomToSelectHandler(this.mapFrame);

        // Display the map frame. When it is closed the application will exit
        this.mapFrame.setSize(windowLength, windowHeight);

        this.featureTableWindow = new FeatureTableWindow(this.selectHandler,
                this.mapHandler);

        this.sumCharacteristicsWindow = new SumCharacteristicsWindow(
                this.mapHandler);

        // add attribute table window button
        addButtonToToolBar(
                Roles.DisplayAttributeTable.label,
                new DisplayAttributeTableButtonListener(this.featureTableWindow),
                toolbar);

        // add intersect window button
        addButtonToToolBar(Roles.DisplayIntersectWindow.label,
                new DisplayIntersectWindowButtonListener(new IntersectWindow(
                        this.selectHandler, this.mapHandler)), toolbar);

        // add [export selected and visible] button window button
        addButtonToToolBar("Export layer", new ExportLayerButtonListener(
                this.mapHandler), toolbar);

        // add button for displaying sumCharackteristics window
        addButtonToToolBar("Sum charackteristics",
                new DisplaySumCharackteristicsWindowListener(
                        this.sumCharacteristicsWindow), toolbar);
        
        this.mapFrame.setVisible(true);
    }

    private int addButtonToToolBar(String buttonLabel,
            ActionListener actionListener, JToolBar jToolBar) {

        try {

            Button button = new Button(buttonLabel);
            button.addActionListener(actionListener);
            jToolBar.add(button);
            return 1;
        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public MapHandler getMapHandler() {
        return mapHandler;
    }

    public JMapFrame getMapFrame() {
        return mapFrame;
    }

    public SelectHandler getSelectHandler() {
        return selectHandler;
    }

    public void setLayerTableEnabled(boolean state) {

        this.mapFrame.enableLayerTable(state);
    }

    public int addLayer(SimpleFeatureSource simpleFeatureSource) {

        if (simpleFeatureSource == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureSource must not be null!");
        }

        try {

            return this.mapHandler.addLayerToMapContent(simpleFeatureSource);

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public int addLayer(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            return this.mapHandler.addLayerToMapContent(layer);

        } catch (Exception exception) {

            exception.printStackTrace();

        }
        return 0;
    }

}
