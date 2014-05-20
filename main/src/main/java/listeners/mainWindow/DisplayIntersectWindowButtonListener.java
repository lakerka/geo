package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import views.windows.FeatureTableWindow;
import views.windows.intersect.IntersectWindow;

public class DisplayIntersectWindowButtonListener implements ActionListener {

    private IntersectWindow intersectWindow;

    public DisplayIntersectWindowButtonListener(IntersectWindow intersectWindow) {

        super();

        if (intersectWindow == null) {
            throw new IllegalArgumentException(
                    "Constructor argument must not be null!");
        }

        this.intersectWindow = intersectWindow;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            intersectWindow.setVisible(true);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}
