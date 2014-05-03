package listeners.mainWindow;

import handlers.IntersectHandler;
import handlers.MapHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;

public class ExportLayerButtonListener implements ActionListener {

    private MapHandler mapHandler;

    public ExportLayerButtonListener(
            MapHandler mapHandler) {

        super();

        if (mapHandler == null) {
            throw new IllegalArgumentException(
                    "mapHandler must not be null!");
        }

        this.mapHandler = mapHandler;
    }

    public void actionPerformed(ActionEvent e) {

        this.mapHandler.exportSelectedAndVisible();
    }
}
