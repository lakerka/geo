package listeners.intersectWindow;

import handlers.IntersectWindowHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddSelectedToMapButtonListener implements ActionListener {

    
    private IntersectWindowHandler intersectWindowHandler;

    public AddSelectedToMapButtonListener(
            IntersectWindowHandler intersectWindowHandler) {
        
        super();
        
        if (intersectWindowHandler == null) {
            throw new IllegalArgumentException(
                    "intersectWindowHandler must not be null!");
        }

        this.intersectWindowHandler = intersectWindowHandler;
    }

    public void actionPerformed(ActionEvent e) {
        
        intersectWindowHandler.addSelectedLayersToMap();
    }

}
