package listeners.sumCharackteristics;

import handlers.SumCharacteristicsHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;
import org.geotools.map.Layer;

import windows.Command;
import windows.SumCharacteristicsWindow;

public class AddLayerFromFileListener implements ActionListener {

    private SumCharacteristicsHandler sumCharackteristicsHandler;

    public AddLayerFromFileListener(
            SumCharacteristicsHandler sumCharackteristicsHandler) {

        super();

        if (sumCharackteristicsHandler == null) {
            throw new IllegalArgumentException("sumCharackteristicsHandler must not be null!");
        }

        this.sumCharackteristicsHandler = sumCharackteristicsHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            SimpleFeatureSource simpleFeatureSource = Support.loadShapeFile();
            Layer layer = Support.simpleFeatureSourceToLayer(simpleFeatureSource);
            
            this.sumCharackteristicsHandler.addLayer(layer);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}

