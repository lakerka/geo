package listeners.mainWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geotools.main.Main;
import org.geotools.main.SelectCursorTool;
import org.geotools.swing.tool.CursorTool;

/**
 * When select button is activated cursor behavior changes
 */

public class SelectButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {

        CursorTool cursorTool = new SelectCursorTool();
        
        Main.mainWindow.mapFrame.getMapPane().setCursorTool(cursorTool);
        
    }
    
}