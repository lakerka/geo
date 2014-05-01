package listeners.featureTableWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import windows.FeatureTableWindow;


public class OpenFileMenuItemListener implements ActionListener {

    private FeatureTableWindow featureTableWindow;

    public OpenFileMenuItemListener(FeatureTableWindow featureTableHandler) {

        super();

        if (featureTableHandler == null) {
            throw new IllegalArgumentException(
                    "featureTableHandler must not be null!");
        }

        this.featureTableWindow = featureTableHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            featureTableWindow.addLayer();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}
