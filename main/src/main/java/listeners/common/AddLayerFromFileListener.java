package listeners.common;

import interfaces.ICommonOperations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.Support;

public class AddLayerFromFileListener implements ActionListener {

    private ICommonOperations handler;

    public AddLayerFromFileListener(ICommonOperations handler) {

        super();

        if (handler == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.handler = handler;
    }

    public void actionPerformed(ActionEvent e) {

        SimpleFeatureSource simpleFeatureSource = Support.loadShapeFile();

        if (simpleFeatureSource == null) {
            return;
        }

        handler.addLayer(Support
                .simpleFeatureSourceToLayer(simpleFeatureSource));
    }

}