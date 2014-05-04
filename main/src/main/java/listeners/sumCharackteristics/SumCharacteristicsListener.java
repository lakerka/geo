package listeners.sumCharackteristics;

import handlers.SummarizeHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SumCharacteristicsListener implements ActionListener {

    private SummarizeHandler sumCharackteristicsHandler;

    public SumCharacteristicsListener(
            SummarizeHandler sumCharackteristicsHandler) {

        super();

        if (sumCharackteristicsHandler == null) {
            throw new IllegalArgumentException("sumCharackteristicsHandler must not be null!");
        }

        this.sumCharackteristicsHandler = sumCharackteristicsHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.sumCharackteristicsHandler.sumCharacteristics();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}

