package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.main.Main;

public class ZoomToSelectionButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {

        Main.mainWindow.zoomToSelectHandler
                .zoomToSelect(Main.mainWindow.selectHandler
                        .getSelectedFeaturesReferencedEnvelope());
    }

}
