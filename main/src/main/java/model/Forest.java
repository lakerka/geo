package model;

import java.util.ArrayList;
import java.util.List;

import org.geotools.main.Validator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class Forest {

    private List<SimpleFeature> roadFeatureList = new ArrayList<SimpleFeature>();

    private final SimpleFeature forestSimpleFeature;

    private double minRoadLength;
    private double maxRoadInForestLength = 0;

    public Forest(SimpleFeature forestSimpleFeature, double minRoadLength) {

        this.forestSimpleFeature = forestSimpleFeature;
        this.minRoadLength = minRoadLength;
    }

    public List<SimpleFeature> getRoadFeatureList() {
        return roadFeatureList;
    }

    public Geometry getForestGeometry() {
        return (Geometry) this.forestSimpleFeature.getDefaultGeometry();
    }


    public SimpleFeature getForestSimpleFeature() {
        return forestSimpleFeature;
    }

    public int addRoad(SimpleFeature roadSimpleFeature) {

        Validator.checkNullPointerPassed(roadSimpleFeature);

        Geometry forestGeometry = (Geometry) forestSimpleFeature
                .getDefaultGeometry();
        Geometry roadGeometry = (Geometry) roadSimpleFeature
                .getDefaultGeometry();

        // check roads length
        if (roadGeometry.getLength() < this.minRoadLength) {
            return -1;
            // throw new IllegalArgumentException(
            // "Road length must be at least: " + this.minRoadLength);
        }

        if (forestGeometry.intersects(roadGeometry)) {

            maxRoadInForestLength = Math.max(maxRoadInForestLength,
                    roadGeometry.getLength());
            this.roadFeatureList.add(roadSimpleFeature);
            return 1;

        } else {
            return -2;
            // throw new IllegalArgumentException(
            // "Road must intersect with forest!");
        }
    }
    
    public boolean hasRoads() {
        return !this.roadFeatureList.isEmpty(); 
    }

//    public void intersect(Geometry g) {
//        throw new Uns
//    }

}
