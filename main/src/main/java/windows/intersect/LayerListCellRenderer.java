package windows.intersect;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.geotools.map.Layer;

public class LayerListCellRenderer extends DefaultListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -6016857903945659176L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        if (!(value instanceof Layer)) {
            throw new IllegalArgumentException(
                    "List object and value arguemnts must be of type Layer");
        }

        Component component = super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

        Layer layer = (Layer) value;
        
        if (layer.getTitle() != null && !layer.getTitle().isEmpty()) {
            
            ((JLabel) component).setText(layer.getTitle());

        } else {
            
            ((JLabel) component).setText((layer).getFeatureSource().getName()
                    .toString());
        }
        
        
        Color backgroundColor;
        Color foregroundColor;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            backgroundColor = Color.BLUE;
            foregroundColor = Color.WHITE;

            // check if this cell is selected
        } else if (isSelected) {
            backgroundColor = Color.GRAY;
            foregroundColor = Color.WHITE;

            // unselected, and not the DnD drop location
        } else {
            backgroundColor = Color.WHITE;
            foregroundColor = Color.BLACK;
        }

        component.setBackground(backgroundColor);
        component.setForeground(foregroundColor);

        return component;

    }

}
