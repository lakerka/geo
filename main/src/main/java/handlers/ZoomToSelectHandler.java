package handlers;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.swing.JMapFrame;
import org.opengis.geometry.BoundingBox;

public class ZoomToSelectHandler {
    
    private JMapFrame mapFrame;

    public ZoomToSelectHandler(JMapFrame mapFrame) {

        if (mapFrame == null) {
            throw new IllegalArgumentException("mapHandler must not be null!");
        }
        this.mapFrame = mapFrame;
    }

    public int zoomToSelect(
            ReferencedEnvelope selectedReferencedEnvelope) {

        if (selectedReferencedEnvelope == null) {

            throw new IllegalArgumentException(
                    "selectedReferencedEnvelope must not be null!");
        }

        try {
            
            this.mapFrame.getMapPane().setDisplayArea(selectedReferencedEnvelope);
            return 1;

        } catch (Exception exception) {
            
            exception.printStackTrace();
            return 0;
        }

    }
}
