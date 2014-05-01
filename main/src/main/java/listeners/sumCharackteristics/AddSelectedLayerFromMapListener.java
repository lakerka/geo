package listeners.sumCharackteristics;

import handlers.SumCharacteristicsHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddSelectedLayerFromMapListener implements ActionListener {

    private SumCharacteristicsHandler sumCharackteristicsHandler;

    public AddSelectedLayerFromMapListener(
            SumCharacteristicsHandler sumCharackteristicsHandler) {

        super();

        if (sumCharackteristicsHandler == null) {
            throw new IllegalArgumentException("sumCharackteristicsHandler must not be null!");
        }

        this.sumCharackteristicsHandler = sumCharackteristicsHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.sumCharackteristicsHandler.addSelectedLayersFromMap();

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}

