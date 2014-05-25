package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import views.windows.GroupFeaturesWindow;

public class DisplayGroupFeaturesWindow implements ActionListener {

    private GroupFeaturesWindow groupFeaturesWindow;

    public DisplayGroupFeaturesWindow(GroupFeaturesWindow groupFeaturesWindow) {

        super();

        if (groupFeaturesWindow == null) {
            throw new IllegalArgumentException(
                    "Constructor argument must not be null!");
        }

        this.groupFeaturesWindow = groupFeaturesWindow;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            groupFeaturesWindow.setVisible(true);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}
