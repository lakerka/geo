package org.geotools.main;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;

public class SelectCursorTool extends CursorTool {

    private static MousePressAndReleasePoints mouseRealWorldPoints = new MousePressAndReleasePoints();
    private static List<MousePressAndReleasePoints> mouseRealWorldPointsList = new ArrayList<MousePressAndReleasePoints>();
    private static boolean multiselectKeyIsPressed = false;

    @Override
    public void onMousePressed(MapMouseEvent mapMouseEvent) {

        SelectCursorTool.mouseRealWorldPoints
                .setMousePressedScreenPoint(mapMouseEvent.getWorldPos());

    }

    @Override
    public void onMouseReleased(MapMouseEvent mapMouseEvent) {

        // flag that user wants to use multiselect
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {

                    public boolean dispatchKeyEvent(KeyEvent keyEvent) {

                        if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {

                            if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL) {

                                SelectCursorTool.multiselectKeyIsPressed = true;
                            }
                        }else {
                            SelectCursorTool.multiselectKeyIsPressed = false;
                        }

//                        if (keyEvent.getID() == KeyEvent.KEY_RELEASED) {
//
//                            if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL) {
//
//                                SelectCursorTool.multiselectKeyIsPressed = false;
//                            }
//                        }
                        return false;
                    }
                });

        SelectCursorTool.mouseRealWorldPoints
                .setMouseReleasedScreenPoint(mapMouseEvent.getWorldPos());

        // if (SelectCursorTool.multiselectKeyIsPressed == false) {
        //
        // SelectCursorTool.mouseRealWorldPointsList.clear();
        // }

        SelectCursorTool.mouseRealWorldPointsList.clear();

        SelectCursorTool.mouseRealWorldPointsList
                .add(new MousePressAndReleasePoints(
                        SelectCursorTool.mouseRealWorldPoints
                                .getMousePressedScreenPoint(),
                        SelectCursorTool.mouseRealWorldPoints
                                .getMouseReleasedScreenPoint()));

        // System.out.println(SelectCursorTool.mouseRealWorldPointsList.size());

        Main.mainWindow.getSelectHandler().selectFeatures(
                SelectCursorTool.mouseRealWorldPointsList,
                SelectCursorTool.multiselectKeyIsPressed);

    }

    @Override
    public boolean drawDragBox() {
        return true;
    }

    public static int clearBufferedMouseScreenPoint() {

        SelectCursorTool.mouseRealWorldPointsList.clear();
        return 1;
    }

}
