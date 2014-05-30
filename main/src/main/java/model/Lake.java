package model;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.main.Support;
import org.geotools.main.Validator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class Lake {

    private int id;
    private double bufferDistance;

    // nearest objects
    private List<SimpleFeature> bridgesList = new ArrayList<SimpleFeature>();

    private List<Forest> forestsList = new ArrayList<Forest>();

    private List<SimpleFeature> villagesList = new ArrayList<SimpleFeature>();

    public final SimpleFeature lakeSimpleFeature;

    private SimpleFeature bufferedLakeSimpleFeature;

    public Lake(int id, SimpleFeature lakeSimpleFeature, double bufferDistance) {

        Validator.checkNullPointerPassed(lakeSimpleFeature);

        this.id = id;

        this.lakeSimpleFeature = lakeSimpleFeature;
        this.bufferDistance = bufferDistance;

        Geometry bufferedLakeGeometry = ((Geometry) lakeSimpleFeature
                .getDefaultGeometry()).buffer(bufferDistance);

        this.bufferedLakeSimpleFeature = new SimpleFeatureImpl(
                lakeSimpleFeature.getAttributes(),
                lakeSimpleFeature.getFeatureType(), new FeatureIdImpl(
                        getBufferedFeatureId()));
        this.bufferedLakeSimpleFeature.setDefaultGeometry(bufferedLakeGeometry);
    }

    public int addBridge(SimpleFeature simpleFeature) {
        return bridgesList.add(simpleFeature) == true ? 1 : 0;
    }

    public int addForest(Forest forest) {
        return forestsList.add(forest) == true ? 1 : 0;
    }

    public int addVillage(SimpleFeature simpleFeature) {
        return villagesList.add(simpleFeature) == true ? 1 : 0;
    }

    public int getId() {
        return this.id;
    }

    public Geometry getBufferedLakeGeometry() {
        return (Geometry) this.bufferedLakeSimpleFeature.getDefaultGeometry();
    }

    /**
     * Sets new buffer of buffered lake by creating new buffered lake from
     * initial lake and replacing old buffered lake with new one
     */
    public int setBufferedFeatureBuffer(double bufferDistance) {

        this.bufferedLakeSimpleFeature = new SimpleFeatureImpl(
                lakeSimpleFeature.getAttributes(),
                lakeSimpleFeature.getFeatureType(), new FeatureIdImpl(
                        getBufferedFeatureId()));

        this.bufferedLakeSimpleFeature.setDefaultGeometry(getLakeGeometry()
                .buffer(bufferDistance));

        this.bufferDistance = bufferDistance;

        return 1;
    }

    private String getBufferedFeatureId() {
        return "" + 1000000 + id;
    }

    public SimpleFeatureCollection getVillagesCollection() {
        return DataUtilities.collection(this.villagesList);
    }

    public SimpleFeature getBufferedLakeSimpleFeature() {
        return this.bufferedLakeSimpleFeature;
    }

    public Geometry getLakeGeometry() {
        return (Geometry) this.lakeSimpleFeature.getDefaultGeometry();
    }

    public int getPointOfInterestCount() {
        return this.bridgesList.size() + this.bridgesList.size()
                + this.forestsList.size();
    }

    public List<SimpleFeature> getVillagesList() {
        return this.villagesList;
    }

    public List<SimpleFeature> getBridgesList() {
        return this.bridgesList;
    }
    
    public SimpleFeatureCollection getBridgesSimpleFeatureCollection() {
        return DataUtilities.collection(this.bridgesList);
    }

    public List<Forest> getForestsList() {
        return this.forestsList;
    }

    /**
     * @return list of forests simple features that are gathered from all
     *         forests.
     */
    public List<SimpleFeature> getAllForestsSimpleFeatureList() {

        List<SimpleFeature> forestSimpleFeatureList = new ArrayList<SimpleFeature>();
        for (Forest forest : this.forestsList) {
            forestSimpleFeatureList.add(forest.getForestSimpleFeature());
        }

        return forestSimpleFeatureList;
    }
    
    public SimpleFeatureCollection getAllForestsSimpleFeatureCollection() {

        return DataUtilities.collection(getAllForestsSimpleFeatureList());
    }

    
    public SimpleFeature getLakeSimpleFeature() {
        return this.lakeSimpleFeature;
    }
    
    public SimpleFeatureCollection getAllRoadsCollection() {
        
        List<SimpleFeature> roadSimpleFeatureList = new ArrayList<SimpleFeature>();
        
        for (Forest forest : forestsList) {
            roadSimpleFeatureList.addAll( forest.getRoadFeatureList() );
        }
        
        return DataUtilities.collection(roadSimpleFeatureList);
    }
    
}
