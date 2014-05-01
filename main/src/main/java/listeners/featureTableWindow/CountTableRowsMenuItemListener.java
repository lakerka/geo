package listeners.featureTableWindow;

import handlers.FeatureTableHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.shapefile.ShapefileDataStoreFactory;

public class CountTableRowsMenuItemListener implements ActionListener {

    private FeatureTableHandler featureTableHandler;

    public CountTableRowsMenuItemListener(FeatureTableHandler featureTableHandler) {

        super();

        if (featureTableHandler == null) {
            throw new IllegalArgumentException(
                    "featureTableHandler must not be null!");
        }

        this.featureTableHandler = featureTableHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

//            featureTableHandler.countFeatures();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}
