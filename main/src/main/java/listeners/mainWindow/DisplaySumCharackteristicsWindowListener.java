package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import windows.FeatureTableWindow;
import windows.SumCharacteristicsWindow;

public class DisplaySumCharackteristicsWindowListener implements ActionListener {

    private SumCharacteristicsWindow sumCharacteristicsWindow;

    public DisplaySumCharackteristicsWindowListener(
            SumCharacteristicsWindow sumCharacteristicsWindow) {

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