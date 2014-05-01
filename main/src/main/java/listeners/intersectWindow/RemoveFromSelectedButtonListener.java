package listeners.intersectWindow;

import handlers.IntersectWindowHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//used to remove layer from selected in intersect window
public class RemoveFromSelectedButtonListener implements ActionListener{
    
    private IntersectWindowHandler intersectWindowHandler;

    public RemoveFromSelectedButtonListener(
            IntersectWindowHandler intersectWindowHandler) {
        
        super();
        
        if (intersectWindowHandler == null) {
            throw new IllegalArgumentException(
                    "intersectWindowHandler must not be null!");
        }

        this.intersectWindowHandler = intersectWindowHandler;
    }

    public void actionPerformed(ActionEvent e) {
        
        intersectWindowHandler.removeSelectedFromJpanel();
    }

}
