package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import windows.FeatureTableWindow;

public class DisplayAttributeTableButtonListener implements ActionListener {

    private FeatureTableWindow featureTableWindow;

    public DisplayAttributeTableButtonListener(
            FeatureTableWindow featureTableWindow) {

        super();

        if (featureTableWindow == null) {
            throw new IllegalArgumentException(
                    "featureTableWindow must not be null!");
        }

        this.featureTableWindow = featureTableWindow;
    }

    public void actionPerformed(ActionEvent e) {

        try {
            
                featureTableWindow.setVisible(true);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}
