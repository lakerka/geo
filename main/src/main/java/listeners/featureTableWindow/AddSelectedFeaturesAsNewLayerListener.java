package listeners.featureTableWindow;

import handlers.FeatureTableHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import views.windows.FeatureTableWindow;

public class AddSelectedFeaturesAsNewLayerListener implements ActionListener {

    private FeatureTableHandler featureTableHandler;

    public AddSelectedFeaturesAsNewLayerListener(
            FeatureTableHandler featureTableHandler) {

        super();

        if (featureTableHandler == null) {
            throw new IllegalArgumentException(
                    "featureTableHandler must not be null!");
        }

        this.featureTableHandler = featureTableHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.featureTableHandler.addSelectedFeaturesAsNewLayerToMap();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}
