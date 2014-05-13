package windows;

import handlers.FeatureTableHandler;
import handlers.MapHandler;
import handlers.SelectHandler;
import handlers.SummarizeHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import listeners.common.AddLayerFromFileListener;
import listeners.common.AddSelectedLayersFromMapListener;
import listeners.common.RemoveSelectedListener;
import listeners.featureTableWindow.AddLayersFromMapToAttributeTableListener;
import listeners.featureTableWindow.AddSelectedFeaturesAsNewLayerListener;
import listeners.featureTableWindow.FilterTableContentMenuItemListener;
import listeners.featureTableWindow.OpenFileMenuItemListener;
import listeners.featureTableWindow.SelectInMapSelectedInTableListener;
import listeners.mainWindow.DisplaySelectedFeaturesMenuItemListener;
import listeners.sumCharackteristics.SetCommandListener;
import listeners.sumCharackteristics.SumCharacteristicsListener;

import org.geotools.data.DataStore;
import org.geotools.main.Main;
import org.geotools.main.Roles;
import org.geotools.map.Layer;

import windows.intersect.LayerJListPanel;

public class SummarizeWindow extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -6803473575112496352L;

    private MapHandler mapHandler;
    private SummarizeHandler summarizeHandler;
    private LayerJListPanel layerJListPanel;
    private JTable jTable;

    public SummarizeWindow(MapHandler mapHandler) {

        this.setTitle("Summarize");

        this.mapHandler = mapHandler;
        this.layerJListPanel = new LayerJListPanel("Layers to select from");
        this.summarizeHandler = new SummarizeHandler(this.layerJListPanel,
                this, this.mapHandler);

        // add menu bar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        // JPanel for choosing operation
        JPanel chooseJPanel = initChooseJPanel(new JPanel());

        // JPanel for buttons
        JPanel buttonPane = new JPanel();

        // Lay out the buttons from left to right.
        this.jTable = new JTable();
        jTable.setSize(800, 300);
        JPanel tablePane = initPanel(jTable);

        // add layer from file
        createButtonAndAddToButtonPane("Add layer from file",
                new AddLayerFromFileListener(this.summarizeHandler), buttonPane);

        // add layers that are selected in map
        createButtonAndAddToButtonPane("Add selected layers from map",
                new AddSelectedLayersFromMapListener(this.summarizeHandler),
                buttonPane);

        // remove selected layer
        createButtonAndAddToButtonPane("Remove selected",
                new RemoveSelectedListener(this.summarizeHandler), buttonPane);

        // sum characteristics
        createButtonAndAddToButtonPane("Calculate",
                new SumCharacteristicsListener(this.summarizeHandler),
                buttonPane);

        // Put everything together, using the content pane's
        Container contentPane = getContentPane();

        contentPane.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.7;

        contentPane.add(chooseJPanel, gbc);

        gbc.weighty = 0.1;
        gbc.gridy = 1;
        contentPane.add(layerJListPanel, gbc);

        gbc.weighty = 0.1;
        gbc.gridy = 2;
        contentPane.add(buttonPane, gbc);

        gbc.weighty = 0.1;
        gbc.gridy = 3;
        contentPane.add(tablePane, gbc);

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
                group, new SetCommandListener(this.summarizeHandler,
                        Command.GET_LENGTH));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Area", jPanel, group,
                new SetCommandListener(this.summarizeHandler, Command.GET_AREA));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Length ratio",
                jPanel, group, new SetCommandListener(this.summarizeHandler,
                        Command.GET_LENGTH_RATIO));
        createJRadioButtonAndAddItToJPanelAndJRadioGroup("Area ratio", jPanel,
                group, new SetCommandListener(this.summarizeHandler,
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

    private JPanel initPanel(JTable jTable) {

        try {

            JPanel jPanel = new JPanel();
            //
            // jTable.setPreferredScrollableViewportSize(getPreferredSize());
            jTable.setFillsViewportHeight(true);

            DefaultTableModel defaultTableModel = new DefaultTableModel();
            jTable.setModel(defaultTableModel);
            jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            jTable.setPreferredScrollableViewportSize(new Dimension(500, 200));

            // Create the scroll pane and add the table to it.
            JScrollPane scrollPane = new JScrollPane(jTable);

            // Add the scroll pane to this panel.
            jPanel.add(scrollPane);

            return jPanel;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // TODO uzbaigti
    public int setTableModel(List<String> columnNames,
            List<List<Object>> dataList) {

        if (columnNames == null || dataList == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        // // TableMod
        // this.jTable.setModel(dataModel);
        // this.jTable.setModel(dataModel);

        try {

            Object[][] dataArray = new Object[dataList.size()][];
            for (int i = 0; i < dataArray.length; i++) {
                dataArray[i] = dataList.get(i).toArray();
            }
            
            TableModel tableModel = new DefaultTableModel(dataArray,
                    columnNames.toArray());
            
            this.jTable.setModel(tableModel);
            this.jTable.repaint();
            
        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public int displayYesNoWindow(String question) {
        
        int dialogResult = JOptionPane.showConfirmDialog (null, question,"Warning",JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION) {
            return 1;
        }else {
            return 0;
        }
            
    }
    
}
