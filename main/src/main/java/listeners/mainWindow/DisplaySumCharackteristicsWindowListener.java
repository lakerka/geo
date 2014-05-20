package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import views.windows.FeatureTableWindow;
import views.windows.SummarizeWindow;

public class DisplaySumCharackteristicsWindowListener implements ActionListener {

    private SummarizeWindow sumCharacteristicsWindow;

    public DisplaySumCharackteristicsWindowListener(
            SummarizeWindow sumCharacteristicsWindow) {

        super();

        if (sumCharacteristicsWindow == null) {
            throw new IllegalArgumentException(
                    "sumCharacteristicsWindow must not be null!");
        }

        this.sumCharacteristicsWindow = sumCharacteristicsWindow;
    }

    public void actionPerformed(ActionEvent e) {

        try {
            if (sumCharacteristicsWindow.isVisible()) {
                sumCharacteristicsWindow.setVisible(false);
            } else {
                sumCharacteristicsWindow.setVisible(true);
            }

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}
