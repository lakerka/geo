package listeners.intersectWindow;

import handlers.IntersectWindowHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.map.Layer;

import windows.intersect.IntersectWindow;

//used to intersect layers from insertesct window
public class IntersectButtonListener implements ActionListener {
    
    private IntersectWindowHandler intersectWindowHandler;

    public IntersectButtonListener(
            IntersectWindowHandler intersectWindowHandler) {
        
        super();
        
        if (intersectWindowHandler == null) {
            throw new IllegalArgumentException(
                    "intersectWindowHandler must not be null!");
        }

        this.intersectWindowHandler = intersectWindowHandler;
    }

    public void actionPerformed(ActionEvent e) {
        
        /*
         * intersect layers and add to jpanel
         */        
        Layer layer = this.intersectWindowHandler.intersectSelected(4);
        this.intersectWindowHandler.addLayer(layer);
    }
    
}
