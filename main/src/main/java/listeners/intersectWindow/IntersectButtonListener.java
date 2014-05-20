package listeners.intersectWindow;

import handlers.IntersectHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.map.Layer;

import views.windows.intersect.IntersectWindow;

//used to intersect layers from insertesct window
public class IntersectButtonListener implements ActionListener {
    
    private IntersectHandler intersectWindowHandler;

    public IntersectButtonListener(
            IntersectHandler intersectWindowHandler) {
        
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
