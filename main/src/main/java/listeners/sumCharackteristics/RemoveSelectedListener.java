package listeners.sumCharackteristics;

import handlers.SumCharacteristicsHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoveSelectedListener implements ActionListener {

    private SumCharacteristicsHandler sumCharackteristicsHandler;

    public RemoveSelectedListener(
            SumCharacteristicsHandler sumCharackteristicsHandler) {

        super();

        if (sumCharackteristicsHandler == null) {
            throw new IllegalArgumentException("sumCharackteristicsHandler must not be null!");
        }

        this.sumCharackteristicsHandler = sumCharackteristicsHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.sumCharackteristicsHandler.removeSelectedFromJpanel();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}

