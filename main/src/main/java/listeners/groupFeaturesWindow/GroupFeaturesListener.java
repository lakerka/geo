package listeners.groupFeaturesWindow;

import handlers.GroupFeaturesHandler;
import handlers.IntersectHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.map.Layer;

public class GroupFeaturesListener implements ActionListener {
    
    private GroupFeaturesHandler groupFeaturesHandler;

    public GroupFeaturesListener(
            GroupFeaturesHandler groupFeaturesHandler) {
        
        super();
        
        if (groupFeaturesHandler == null) {
            throw new IllegalArgumentException(
                    "intersectWindowHandler must not be null!");
        }

        this.groupFeaturesHandler = groupFeaturesHandler;
    }

    public void actionPerformed(ActionEvent e) {
        
        
        groupFeaturesHandler.groupFeatures();
    }
    
}
