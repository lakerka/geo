package windows;

import handlers.IntersectWindowHandler;
import handlers.MapHandler;
import handlers.SelectHandler;
import handlers.SumCharacteristicsHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import listeners.intersectWindow.AddFromFileToSelectFromButtonListener;
import listeners.intersectWindow.AddSelectedToMapButtonListener;
import listeners.intersectWindow.IntersectButtonListener;
import listeners.intersectWindow.RemoveFromSelectedButtonListener;
import listeners.sumCharackteristics.AddLayerFromFileListener;
import listeners.sumCharackteristics.AddSelectedLayerFromMapListener;
import listeners.sumCharackteristics.RemoveSelectedListener;
import listeners.sumCharackteristics.SetCommandListener;
import listeners.sumCharackteristics.SumCharacteristicsListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;
import org.geotools.map.Layer;

import windows.intersect.LayerJListPanel;

public class SumCharacteristicsWindow extends JFrame {

    private MapHandler mapHandler;
    private SumCharacteristicsHandler sumCharackteristicsHandler;
    private LayerJListPanel layerJListPanel;

    // TODO uzbaigti inicializacija
    public SumCharacteristicsWindow(MapHandler mapHandler) {

        this.mapHandler = mapHandler;
        this.layerJListPanel = new LayerJListPanel("Layers to select from");
        this.sumCharackteristicsHandler = new SumCharacteristicsHandler(
                this.layerJListPanel, this, this.mapHandler);

        JList<Layer> list;

        // add menu bar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        // JPanel for choosing operation
        JPanel chooseJPanel = initChooseJPanel(new JPanel());

        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();

        // add layer from file
        createButtonAndAddToButtonPane("Add layer from file",
                new AddLayerFromFileListener(this.sumCharackteristicsHandler),
                buttonPane);

        // add layers that are selected in map
        createButtonAndAddToButtonPane("Add selected layers from map",
                new AddSelectedLayerFromMapListener(
                        this.sumCharackteristicsHandler), buttonPane);

        // remove selected layer
        createButtonAndAddToButtonPane("Remove selected",
                new RemoveSelectedListener(this.sumCharackteristicsHandler),
                buttonPane);

        // sum characteristics
        createButtonAndAddToButtonPane(
                "Calculate",
                new SumCharacteristicsListener(this.sumCharackteristicsHandler),
                buttonPane);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(layerJListPanel, BorderLayout.CENTER);
        contentPane.add(chooseJPanel, BorderLayout.BEFORE_FIRST_LINE);
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

    private JPanel initChooseJPanel(JPanel jPanel) {

        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Length", jPanel,
                group, new SetCommandListener(this.sumCharackteristicsHandler,
                        Command.GET_LENGTH));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Area", jPanel, group,
                new SetCommandListener(this.sumCharackteristicsHandler,
                        Command.GET_AREA));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Length ratio", jPanel,
                group, new SetCommandListener(this.sumCharackteristicsHandler,
                        Command.GET_LENGTH_RATIO));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Area ratio", jPanel,
                group, new SetCommandListener(this.sumCharackteristicsHandler,
                        Command.GET_AREA_RATIO));

        return jPanel;
    }

    private int createJRadioButtonAndAddItToJPanelAndJRadioGroup(String label,
            JPanel jPanel, ButtonGroup buttonGroup,
            ActionListener actionListener) {

        if (label == null || actionListener == null || jPanel == null
                || buttonGroup == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        JRadioButton jRadioButton = new JRadioButton(label);
        buttonGroup.add(jRadioButton);
        jPanel.add(jRadioButton);
        jRadioButton.addActionListener(actionListener);

        return 1;
    }

    public void displayPopUpBox(String message) {

        JOptionPane.showMessageDialog(this, message);

    }

}
