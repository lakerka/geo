package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Roles;
import org.geotools.main.Main;
import org.geotools.main.Support;

public class AddLayerButtonListener extends JFrame implements ActionListener {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = 2764680601088989230L;

    public void actionPerformed(ActionEvent e) {
        
        String command = e.getActionCommand();
        
        if (command.equals(Roles.AddLayer.label)) {
            addLayer();
        }
    }

    public void addLayer() {

        SimpleFeatureSource simpleFeatureSource = Support.loadShapeFile();
        
        if (simpleFeatureSource == null) {
            return;
        }
        
        Main.mainWindow.addLayer(simpleFeatureSource);
    }
       
}
