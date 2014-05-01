package org.geotools.main;

import org.geotools.geometry.DirectPosition2D;


public class MousePressAndReleasePoints {

    private DirectPosition2D mousePressedScreenPoint;
    private DirectPosition2D mouseReleasedScreenPoint;
    
    public MousePressAndReleasePoints() { 
        
    }
    
    public MousePressAndReleasePoints(DirectPosition2D mousePressedScreenPoint,
            DirectPosition2D mouseReleasedScreenPoint) {
        
        this.mousePressedScreenPoint = mousePressedScreenPoint;
        this.mouseReleasedScreenPoint = mouseReleasedScreenPoint;
    }
    
    public DirectPosition2D getMousePressedScreenPoint() {
        return mousePressedScreenPoint;
    }
    public void setMousePressedScreenPoint(DirectPosition2D mousePressedScreenPoint) {
        this.mousePressedScreenPoint = mousePressedScreenPoint;
    }
    public DirectPosition2D getMouseReleasedScreenPoint() {
        return mouseReleasedScreenPoint;
    }
    public void setMouseReleasedScreenPoint(DirectPosition2D mouseReleasedScreenPoint) {
        this.mouseReleasedScreenPoint = mouseReleasedScreenPoint;
    }
    
}
