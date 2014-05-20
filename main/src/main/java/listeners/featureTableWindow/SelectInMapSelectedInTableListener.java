package listeners.featureTableWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import views.windows.FeatureTableWindow;

public class SelectInMapSelectedInTableListener implements ActionListener {

    private FeatureTableWindow featureTableWindow;

    public SelectInMapSelectedInTableListener(FeatureTableWindow featureTableHandler) {

        super();

        if (featureTableHandler == null) {
            throw new IllegalArgumentException(
                    "featureTableHandler must not be null!");
        }

        this.featureTableWindow = featureTableHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.featureTableWindow.selectInMapFromSelectedInTable();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}

