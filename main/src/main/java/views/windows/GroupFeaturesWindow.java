package views.windows;

import handlers.GroupFeaturesHandler;
import handlers.IntersectHandler;
import handlers.MapHandler;
import handlers.SelectHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import listeners.common.AddLayerFromFileListener;
import listeners.common.AddSelectedLayersFromMapListener;
import listeners.common.AddSelectedLayersToMapListener;
import listeners.common.RemoveSelectedListener;
import listeners.groupFeaturesWindow.GroupFeaturesListener;

import org.geotools.map.Layer;
import org.geotools.map.MapContent;

import views.panels.LayerJListPanel;

public class GroupFeaturesWindow extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1666489575899750368L;

    public GroupFeaturesHandler groupFeaturesHandler;

    public LayerJListPanel layerJListPanel;
    private MapHandler mapHandler;
    
    public GroupFeaturesWindow(MapHandler mapHandler) {


        this.setTitle("Group");
        
        this.mapHandler = mapHandler;

        // add menu bar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        DefaultListModel<Layer> listModel = new DefaultListModel<Layer>();


        layerJListPanel = new LayerJListPanel(listModel,
                "Layers to select from");

        this.groupFeaturesHandler = new GroupFeaturesHandler(this, this.layerJListPanel, mapHandler);

        JPanel buttonPane = new JPanel();

        createButtonAndAddToButtonPane("Add selected layers from map",
                new AddSelectedLayersFromMapListener(this.groupFeaturesHandler),
                buttonPane);
        
        createButtonAndAddToButtonPane("Add layer from file",
                new AddLayerFromFileListener(this.groupFeaturesHandler), buttonPane);
        
        createButtonAndAddToButtonPane("Remove selected",
                new RemoveSelectedListener(this.groupFeaturesHandler), buttonPane);
        
        createButtonAndAddToButtonPane("Group",
                new GroupFeaturesListener(this.groupFeaturesHandler),
                buttonPane);
        
        //add selected layers to map
        createButtonAndAddToButtonPane("Add selected to map",
                new AddSelectedLayersToMapListener(this.groupFeaturesHandler),
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
    
    public String getAttributeName() {
        String name = JOptionPane.showInputDialog("Enter attribute name");
        return name;
    }

    public void displayPopUpBox(String message) {

        JOptionPane.showMessageDialog(this, message);

    }
    
}
