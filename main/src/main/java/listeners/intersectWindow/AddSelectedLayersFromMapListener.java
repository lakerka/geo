package listeners.intersectWindow;

import handlers.IntersectWindowHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;

//used for intersect window for adding layer from file
public class AddSelectedLayersFromMapListener implements ActionListener {

    private IntersectWindowHandler intersectWindowHandler;

    public AddSelectedLayersFromMapListener(
            IntersectWindowHandler intersectWindowHandler) {

        super();

        if (intersectWindowHandler == null) {
            throw new IllegalArgumentException(
                    "intersectWindowHandler must not be null!");
        }

        this.intersectWindowHandler = intersectWindowHandler;
    }

    public void actionPerformed(ActionEvent e) {

        this.intersectWindowHandler.addSelectedLayersFromMap();
    }

}
