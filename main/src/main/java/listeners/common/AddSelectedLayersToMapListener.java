package listeners.common;

import interfaces.ICommonOperations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddSelectedLayersToMapListener implements ActionListener {
    
    private ICommonOperations handler;

    public AddSelectedLayersToMapListener(
            ICommonOperations handler) {
        
        super();
        
        if (handler == null) {
            throw new IllegalArgumentException(
                    "Arguments must not be null!");
        }

        this.handler = handler;
    }

    public void actionPerformed(ActionEvent e) {
        
        handler.addSelectedLayersToMap();
    }

}