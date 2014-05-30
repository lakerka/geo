package model;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.coordinatesequence.CoordinateSequences;
import org.geotools.main.Support;
import org.geotools.main.Validator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;

public class Graph {

    Lake lake;
    
    
    public Graph(Lake lake) {
        
        Validator.checkNullPointerPassed(lake);
        this.lake = lake;
    }
    
    
    
    private double distance(Geometry fromGeometry, Geometry toGeometry) {
        
        
//        if (Support.isPolygonOrMultiPolygon(fromGeometry)) {
//            
//        }
        
        return 0;
    }
    
    //TODO complete
    private void distanceBetweenPoints(Geometry firstGeometry, Geometry secondGeometry) {
        
        
    }
    
    private LineString createLineFromPoints(Point firstPoint, Point secondPoint) {
        
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
        
        List<Coordinate> cordList = new ArrayList<Coordinate>();
        cordList.add(firstPoint.getCoordinate());
        cordList.add(secondPoint.getCoordinate());
        
        Coordinate[] cordArray = cordList.toArray(new Coordinate[cordList.size()]);
        
        LineString lineString = geometryFactory.createLineString(cordArray);  
        return lineString;
    }

}
