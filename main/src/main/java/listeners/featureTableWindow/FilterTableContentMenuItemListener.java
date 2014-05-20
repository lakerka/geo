package listeners.featureTableWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.shapefile.ShapefileDataStoreFactory;

import views.windows.FeatureTableWindow;

public class FilterTableContentMenuItemListener implements ActionListener {

    private FeatureTableWindow featureTableWindow;

    public FilterTableContentMenuItemListener(FeatureTableWindow featureTableHandler) {

        super();

        if (featureTableHandler == null) {
            throw new IllegalArgumentException(
                    "featureTableHandler must not be null!");
        }

        this.featureTableWindow = featureTableHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.featureTableWindow.filterTableContent();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}
