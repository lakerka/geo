package handlers;

import java.lang.ref.Reference;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Binding;

import model.Forest;
import model.Lake;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.main.Validator;
import org.geotools.main.Main;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import setsRelated.GeometrySet;
import views.windows.ThirdTaskWindow;

import com.vividsolutions.jts.geom.Geometry;

public class ThirdTaskHandler {

    // distance from lake has to no larger than
    private double maxDistanceFromLake; // A

    // population of village has to be less than
    private double maxPopulation; // N

    // forest area has to be no larger than
    private double minForestArea; // S

    // road length in forest has to no less than
    private double minRoadLengthInForest; // L

    private MapHandler mapHandler;
    private SelectHandler selectHandler;

    private ThirdTaskWindow thirdTaskWindow;

    /*
     * layers we are insterested in
     */

    // Surandama sritis nutolusi nuo ežero ne toliau kaip A (sugalvokite kaip ?)
    private SimpleFeatureCollection lakesCollection;

    // Prie kiekvieno ežero surandami miškai, per kuriuos eina bent vienas
    // nemažesnio nei L ilgio kelias, priskiriant mišką tam ežerui, kuriam jis
    // artimiausias (sugalvokite kaip ?).
    private SimpleFeatureCollection forestCollection;

    // Prie kiekvieno ežero surandami tiltai, priskiriant tiltą tam ežerui,
    // kuriam is artimiausias (sugalvokite kaip ?)
    private SimpleFeatureCollection bridgesCollection;

    private SimpleFeatureCollection roadsInForestsCollection;

    // villages have to be with less than populationUpperLimit population
    private SimpleFeatureCollection villagesCollection;

    // additional variables
    private ReferencedEnvelope selectedWorldRectangleEnvelope;

    // collection containing single feature with geometry of selected
    // rectangle
    private SimpleFeatureCollection selectedRectangleSimpleFeatureCollection;

    // bounding box geometry
    private Geometry selectedReferenceEnvelopeGeometry;

    private List<Lake> lakeList;

    /**
     * Forest that have at least one rode of at least minimal required length
     */
    private List<Forest> forestList;

    public ThirdTaskHandler(ThirdTaskWindow thirdTaskWindow,
            MapHandler mapHandler, SelectHandler selectHandler) {

        this.mapHandler = mapHandler;
        this.selectHandler = selectHandler;
        this.thirdTaskWindow = thirdTaskWindow;

    }

    public int selectRectangle() {

        try {

            boolean collectionInitialized = initSelectedRectangleCollection() == 1 ? true
                    : false;
            thirdTaskWindow
                    .changeRectangleIsSelectedLabelText(collectionInitialized);
            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public void start() {

        try {
            // initVillages();
            // initBridgesLayer();

            // this.mapHandler.addLayerToMapContent(Support.simpleFeatureCollectionToLayer(forestCollection));
            initCollections();
            initForestList();
            initLakesList();
            test();

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }

    private void test() {

        System.out.println("STARTED testing");

        Validator.checkNotInitialized(this.lakeList);

        if (lakeList.isEmpty()) {
            System.out.println("lakeList is empty! Nothing to test");
        }

        for (Lake lake : lakeList) {

            if (lake.getPointOfInterestCount() >= 20) {

                this.mapHandler.addLayerToMapContent(Support
                        .simpleFeatureListToLayer(lake.getBridgesList()));
                this.mapHandler.addLayerToMapContent(Support
                        .simpleFeatureListToLayer(lake.getVillagesList()));
                this.mapHandler.addLayerToMapContent(Support
                        .simpleFeatureListToLayer(lake
                                .getAllForestsSimpleFeatureList()));

                this.mapHandler
                        .addLayerToMapContent(Support
                                .simpleFeatureCollectionToLayer(this.villagesCollection));
                this.mapHandler.addLayerToMapContent(Support
                        .simpleFeatureCollectionToLayer(this.forestCollection));
                // this.mapHandler.addLayerToMapContent(Support
                // .simpleFeatureCollectionToLayer(this.));

                break;
            }

        }
        System.out.println("DONE testing");

    }

    /**
     * Initialized lakeList by adding: villages, bridges and forest.
     */
    int initLakesList() {

        System.out.println("STARTED initializing lakes");
        try {

            Validator.checkNotInitialized(villagesCollection,
                    bridgesCollection, lakesCollection, this.forestList);

            // convert to list for comfortable iterating

            List<SimpleFeature> bridgesFeatureList = DataUtilities
                    .list(bridgesCollection);
            List<SimpleFeature> villagesFeatureList = DataUtilities
                    .list(villagesCollection);
            List<SimpleFeature> lakesFeatureList = DataUtilities
                    .list(lakesCollection);

            this.lakeList = new ArrayList<Lake>();

            for (int i = 0; i < lakesFeatureList.size(); i++) {

                SimpleFeature lakeFeature = lakesFeatureList.get(i);
                lakeList.add(new Lake(i, lakeFeature, this.maxDistanceFromLake));
            }

            System.out.println("Lakes count: " + lakeList.size());

            // add to villages, tests ok
            addToNearestLake(lakeList, villagesFeatureList, 0);
            System.out.println("ADDED villages to lakes");
            // add to bridges
            addToNearestLake(lakeList, bridgesFeatureList, 1);
            System.out.println("ADDED bridges to lakes");
            // add forest
            addForestsToNearestLakes();
            System.out.println("ADDED forests to lakes");
            /*--------------TEST villages-----------*/
            // int d = 3;
            // for (int i = 0; i < lakeList.size() && d > 0; i++) {
            // List<SimpleFeature> vList = this.lakeList.get(i)
            // .getVillagesList();
            //
            // if (vList.size() > 0) {
            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureListToLayer(vList));
            // System.out.println("ADDED");
            // d--;
            // }
            // }
            /*--------------END TEST -----------*/
            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureListToLayer(lakesFeatureList));
            // this.mapHandler.addLayerToMapContent(bufferedLakeListToLayer());
            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureListToLayer(villagesFeatureList));

            System.out.println("DONE initializing lakes");

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DONE initializing lakes WITH ERRORS");

        return 0;
    }

    private int addForestsToNearestLakes() {

        Validator.checkNotInitialized(this.lakeList, this.forestList);

        try {

            for (Forest forest : forestList) {

                Lake bestLake = getNearestLake(forest.getForestSimpleFeature());

                if (bestLake != null) {

                    bestLake.addForest(forest);
                }

            }

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int addToNearestLake(List<Lake> lakeList,
            List<SimpleFeature> simpleFeaturesList, int collectionToAddTo) {

        try {

            for (SimpleFeature simpleFeature : simpleFeaturesList) {

                Lake bestLake = getNearestLake(simpleFeature);

                if (bestLake == null) {
                    continue;
                }

                switch (collectionToAddTo) {

                case 0:
                    bestLake.addVillage(simpleFeature);
                    break;

                case 1:
                    bestLake.addBridge(simpleFeature);
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Cant add to unknown collection!");
                }

            }

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get nereast lake. Lakes buffered geometry must intersect with provided
     * geometry. return null, when no lake found.
     */
    public Lake getNearestLake(SimpleFeature simpleFeature) {

        Validator.checkNullPointerPassed(simpleFeature);

        double minDistance = this.maxDistanceFromLake;
        Lake bestLake = null;

        try {

            Geometry simpleFeatureGeometry = (Geometry) simpleFeature
                    .getDefaultGeometry();

            ReferencedEnvelope refEnvelope = JTS
                    .toEnvelope(simpleFeatureGeometry);

            Geometry refEnvelopeGeometry = JTS.toGeometry(refEnvelope);

            for (Lake lake : this.lakeList) {

//                Geometry bufferedLakeGeometry = lake.getBufferedLakeGeometry();
                Geometry lakeGeometry = lake.getLakeGeometry();

                if (!lakeGeometry.isWithinDistance(refEnvelopeGeometry,
                        minDistance)) {
                    continue;
                }

                // if buffered lake doesn't intersect that mean feature is
                // too far away from lake
                if (!lakeGeometry.isWithinDistance(simpleFeatureGeometry,
                        minDistance)) {
                    continue;
                }

                double distance = lakeGeometry.distance(simpleFeatureGeometry);

                if (distance < minDistance) {

                    minDistance = distance;
                    bestLake = lake;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestLake;
    }

    /**
     * Reads from file: villages, bridges, lakes, forests, roads in forests. <br>
     * Filters villages by population and rectangle. <br>
     * Filters bridges by rectangle. <br>
     * Filters lakes by rectangle. Then filters again by: if lake completely
     * coveredBy rectangle then keep that lake. <br>
     * Filters forests by rectangle. Then intersect forests with rectangle.
     * br>Filters roads in forests by rectangle. Then intersect result roads
     * with rectangle.
     */
    int initCollections() {

        try {

            // String pathSventoji =
            // "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
            String pathTrecia = "C:\\Users\\as\\git\\geograf\\main\\trecia\\";

            GeometrySet geometrySet = new GeometrySet();

            // get lakes
            SimpleFeatureSource lakesSimpleFeatureSource = Support
                    .loadShapeFile(pathTrecia + "sven_LAKES_P.shp");

            // get forests
            SimpleFeatureSource forestsSimpleFeatureSource = Support
                    .loadShapeFile(pathTrecia + "sven_FORESTS_P.shp");

            // get briges
            SimpleFeatureSource bridgesSimpleFeatureSource = Support
                    .loadShapeFile(pathTrecia + "sven_TILTAI.shp");

            // get roads in forests
            SimpleFeatureSource roadsInForestsSimpleFeatureSource = Support
                    .loadShapeFile(pathTrecia + "sven_ROADS_IN_FORESTS_2_L.shp");

            // get villages
            SimpleFeatureSource villagesSimpleFeatureSource = Support
                    .loadShapeFile(pathTrecia + "sven_VILLAGES.shp");

            System.out.println("DONE reading from files");

            // start filtering

            // ezerai: "GKODAS='hd3'"
            // miskai: "GKODAS='ms0'"

            // get villages
            SimpleFeatureCollection villagesCollectionLocal = villagesSimpleFeatureSource
                    .getFeatures();
            Filter villageFilterByPopulation = CQL.toFilter("GYVSK <= "
                    + this.maxPopulation);
            villagesCollectionLocal = villagesCollectionLocal
                    .subCollection(villageFilterByPopulation);

            // filter villages by bounding box
            villagesCollectionLocal = filterByReferenceEnvelope(
                    villagesCollectionLocal, selectedWorldRectangleEnvelope);
            // villages completed for further processing
            this.villagesCollection = villagesCollectionLocal;

            /* ----------------------------------------------------- */

            // filter bridges by bounding box
            SimpleFeatureCollection bridgesCollectionLocal = bridgesSimpleFeatureSource
                    .getFeatures();
            bridgesCollectionLocal = filterByReferenceEnvelope(
                    bridgesCollectionLocal, selectedWorldRectangleEnvelope);
            // bridges completed for further processing
            this.bridgesCollection = bridgesCollectionLocal;

            /* ----------------------------------------------------- */

            // filter forest by bounding box
            SimpleFeatureCollection forestsCollectionLocal = forestsSimpleFeatureSource
                    .getFeatures();
            forestsCollectionLocal = filterByReferenceEnvelope(
                    forestsCollectionLocal, selectedWorldRectangleEnvelope);
            // filter by minimum forest area
            String geometryLocalName = forestsCollectionLocal.getSchema()
                    .getGeometryDescriptor().getLocalName();
            Filter forestAreaFilter = ECQL.toFilter("area(" + geometryLocalName
                    + ") >= " + this.minForestArea);
            forestsCollectionLocal = forestsCollectionLocal
                    .subCollection(forestAreaFilter);
            /* ----------------------------------------------------- */

            // filter roads by bounding box
            SimpleFeatureCollection roadsInForestsCollectionLocal = roadsInForestsSimpleFeatureSource
                    .getFeatures();

            roadsInForestsCollectionLocal = filterByReferenceEnvelope(
                    roadsInForestsCollectionLocal,
                    selectedWorldRectangleEnvelope);

            /* ----------------------------------------------------- */

            // filter lakes by bounding box
            SimpleFeatureCollection lakesCollectionLocal = lakesSimpleFeatureSource
                    .getFeatures();
            lakesCollectionLocal = filterByReferenceEnvelope(
                    lakesCollectionLocal, selectedWorldRectangleEnvelope);
            // filters lakes so that they would be completely in bounding box
            List<SimpleFeature> lakeSimpleFeatureList = new ArrayList<SimpleFeature>();
            SimpleFeatureIterator lakesIterator = lakesCollectionLocal
                    .features();
            try {
                while (lakesIterator.hasNext()) {

                    SimpleFeature lakeSimpleFeature = lakesIterator.next();
                    Geometry lakeGeometry = (Geometry) lakeSimpleFeature
                            .getDefaultGeometry();

                    if (lakeGeometry
                            .coveredBy(selectedReferenceEnvelopeGeometry)) {
                        lakeSimpleFeatureList.add(lakeSimpleFeature);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lakesIterator.close();
            }
            lakesCollectionLocal = DataUtilities
                    .collection(lakeSimpleFeatureList);
            /* ----------------------------------------------------- */

            // done filtering
            System.out.println("DONE filtering");

            // intersect forest and roads with bbox
            roadsInForestsCollectionLocal = geometrySet.intersect(
                    roadsInForestsCollectionLocal,
                    selectedRectangleSimpleFeatureCollection, 4);
            /* ----------------------------------------------------- */

            forestsCollectionLocal = geometrySet.intersect(
                    forestsCollectionLocal,
                    selectedRectangleSimpleFeatureCollection, 4);

            // done intersecting
            System.out.println("DONE intersecting");

            // test
            // this.mapHandler.removeAllLayersFromMapContent();
            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureCollectionToLayer(forestsCollection));

            // tiltu +
            // keliu miskuose +
            // misku +
            // kaimu su nurodytu gyventoju skaiciumi +
            // ezeru, kurie pilnai patektu +

            // save results that are in bounding box

            this.bridgesCollection = bridgesCollectionLocal;
            this.roadsInForestsCollection = roadsInForestsCollectionLocal;
            this.forestCollection = forestsCollectionLocal;
            this.villagesCollection = villagesCollectionLocal;
            this.lakesCollection = lakesCollectionLocal;

            return 1;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;

    }

    private SimpleFeatureCollection filterByReferenceEnvelope(
            SimpleFeatureCollection simpleFeatureCollection,
            ReferencedEnvelope referencedEnvelope) {

        try {

            Validator.checkNullPointerPassed(simpleFeatureCollection,
                    referencedEnvelope);

            FilterFactory2 filterFactory2 = CommonFactoryFinder
                    .getFilterFactory2();

            String geometryDescriptorLocalName = null;

            Filter refEnvelopeFilter;

            if (!simpleFeatureCollection.isEmpty()) {

                geometryDescriptorLocalName = simpleFeatureCollection
                        .getSchema().getGeometryDescriptor().getLocalName();

                refEnvelopeFilter = filterFactory2.bbox(

                filterFactory2.property(geometryDescriptorLocalName),
                        referencedEnvelope);

                return simpleFeatureCollection.subCollection(refEnvelopeFilter);

            } else {
                return simpleFeatureCollection;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    int initSelectedRectangleCollection() {

        try {

            // form simpleFeatureCollection from selected in map rectangle
            ReferencedEnvelope referencedEnvelope = selectHandler
                    .getSelectedRectangleReferenceEnvelope();

            this.selectedWorldRectangleEnvelope = referencedEnvelope;

            Geometry geometry = JTS.toGeometry(referencedEnvelope);

            this.selectedReferenceEnvelopeGeometry = geometry;

            SimpleFeatureTypeImpl simpleFeatureTypeImpl = buildSimpleFeatureTypeImpl(
                    geometry, referencedEnvelope);

            SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(
                    simpleFeatureTypeImpl);

            simpleFeatureBuilder.add(geometry);

            SimpleFeature newSimpleFeature = simpleFeatureBuilder
                    .buildFeature(String.valueOf(1));

            List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
            simpleFeatureList.add(newSimpleFeature);

            this.selectedRectangleSimpleFeatureCollection = DataUtilities
                    .collection(simpleFeatureList);

            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;

    }

    private SimpleFeatureTypeImpl buildSimpleFeatureTypeImpl(Geometry geometry,
            ReferencedEnvelope referencedEnvelope) {

        Validator.checkNullPointerPassed(geometry);

        // start building geometry
        AttributeTypeBuilder build = new AttributeTypeBuilder();
        build.setNillable(true);
        build.setCRS(referencedEnvelope.getCoordinateReferenceSystem());
        build.setBinding(geometry.getClass());

        GeometryType geometryType = build.buildGeometryType();
        GeometryDescriptor geometryDescriptor = build.buildDescriptor(
                "the_geom", geometryType);

        List<AttributeDescriptor> attributeDescriptorList = new ArrayList<AttributeDescriptor>();
        attributeDescriptorList.add(geometryDescriptor);

        NameImpl newSimpleFeatureTypeName = new NameImpl(new String(
                "SELECTED_RECTANGLE"));

        SimpleFeatureTypeImpl simpleFeatureTypeImpl = new SimpleFeatureTypeImpl(
                newSimpleFeatureTypeName, attributeDescriptorList,
                geometryDescriptor, false, null, null, null);

        return simpleFeatureTypeImpl;
    }

    private boolean isNegative(double number) {

        if (number < 0) {
            return true;
        }

        return false;
    }

    public int setMaxDistanceFromLake(double maxDistanceFromLake) {

        if (isNegative(maxDistanceFromLake)) {
            return 0;
        }

        this.maxDistanceFromLake = maxDistanceFromLake;

        return 1;
    }

    public int setPopulationUpperLimit(double populationUpperLimit) {

        if (isNegative(populationUpperLimit)) {
            return 0;
        }

        this.maxPopulation = populationUpperLimit;

        return 1;
    }

    public int setMaxForestArea(double minForestArea) {

        if (isNegative(minForestArea)) {
            return 0;
        }

        this.minForestArea = minForestArea;

        return 1;
    }

    public int setMinRoadLengthInForest(double minRoadLengthInForest) {

        if (isNegative(minRoadLengthInForest)) {
            return 0;
        }

        this.minRoadLengthInForest = minRoadLengthInForest;

        return 1;
    }

    public double getMaxDistanceFromLake() {
        return maxDistanceFromLake;
    }

    public double getPopulationUpperLimit() {
        return maxPopulation;
    }

    public double getMaxForestArea() {
        return minForestArea;
    }

    public double getMinRoadLengthInForest() {
        return minRoadLengthInForest;
    }

    // test collection initialization
    // public void start() {
    //
    //
    //
    // String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
    // SimpleFeatureSource simpleFeatureSourceKeliai = Support
    // .loadShapeFile(path + "sven_KEL_L.shp");
    // SimpleFeatureSource simpleFeatureSourceApskritys = Support
    // .loadShapeFile(path + "sven_SAV_P.shp");
    //
    // Layer keliaiLayer = Support
    // .simpleFeatureSourceToLayer(simpleFeatureSourceKeliai);
    //
    // //apskritys layer must be already added
    // Layer apskritysLayer = mapHandler.getSelectedOrVisibleLayer(true,
    // true).get(0);
    //
    // boolean collectionInitialized = initSelectedRectangleCollection() == 1 ?
    // true
    // : false;
    // thirdTaskWindow
    // .changeRectangleIsSelectedLabelText(collectionInitialized);
    //
    // SimpleFeatureCollection keliaiSimpleFeatureCollection = Support
    // .layerToSimpleFeatureCollection(keliaiLayer);
    //
    // GeometrySet geometrySet = new GeometrySet();
    //
    // SimpleFeatureCollection keliaiIntersect = geometrySet.intersect(
    // this.selectedRectangleSimpleFeatureCollection,
    // keliaiSimpleFeatureCollection, 4);
    //
    // Layer keliaiIntersectLayer =
    // Support.simpleFeatureCollectionToLayer(keliaiIntersect);
    //
    // this.mapHandler.addLayerToMapContent(keliaiIntersectLayer);
    //
    // }

    /**
     * gets bridges
     * 
     */
    public int initBridgesLayer() {

        try {
            String pathSventoji = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
            String pathTrecia = "C:\\Users\\as\\git\\geograf\\main\\trecia\\";
            // sven_KEL_L_HID_L_INTERSECT

            SimpleFeatureSource roadsSimpleFeatureSource = Support
                    .loadShapeFile(pathSventoji + "sven_KEL_L.shp");

            SimpleFeatureSource roadsIntersecRiverSource = Support
                    .loadShapeFile(pathTrecia
                            + "sven_UPES_KELIAI_INTERSECT.shp");

            System.out.println("DONE reading from files");

            //
            // SimpleFeatureCollection hidroCollection = hidSimpleFeatureSource
            // .getFeatures();
            //
            // //filter hidro to keep only rivers
            // Filter hidroFilterToKeepRiversFilter = CQL
            // .toFilter("TIPAS=1");
            // SimpleFeatureCollection riversCollection = hidroCollection
            // .subCollection((hidroFilterToKeepRiversFilter);

            SimpleFeatureCollection roadsCollection = roadsSimpleFeatureSource
                    .getFeatures();

            SimpleFeatureCollection roadsIntersectRiversCollection = roadsIntersecRiverSource
                    .getFeatures();

            SimpleFeatureIterator intersectPointsIterator = roadsIntersectRiversCollection
                    .features();
            SimpleFeatureIterator roadsIterator;
            List<SimpleFeature> bridgesList = new ArrayList<SimpleFeature>();

            int i = 0;
            System.out.println(roadsIntersectRiversCollection.size());
            try {
                while (intersectPointsIterator.hasNext()) {
                    i++;
                    if (i % 1000 == 0)
                        System.out.println(i);

                    SimpleFeature intersectPointFeature = intersectPointsIterator
                            .next();
                    Geometry pointGeometry = (Geometry) intersectPointFeature
                            .getDefaultGeometry();

                    double bufferSize = 5;

                    Geometry pointBufferedGeometry = pointGeometry
                            .buffer(bufferSize);

                    ReferencedEnvelope pointEnvelope = JTS
                            .toEnvelope(pointGeometry);

                    pointEnvelope.expandBy(200);

                    FilterFactory2 filterFactory2 = CommonFactoryFinder
                            .getFilterFactory2();

                    String geometryDescriptorLocalName = roadsIntersectRiversCollection
                            .getSchema().getGeometryDescriptor().getLocalName();

                    Filter filter = filterFactory2.bbox(filterFactory2
                            .property(geometryDescriptorLocalName),
                            pointEnvelope);

                    SimpleFeatureCollection filtereCollection = roadsCollection
                            .subCollection(filter);

                    roadsIterator = filtereCollection.features();

                    int intersectCount = 0;

                    try {

                        while (roadsIterator.hasNext()) {

                            SimpleFeature road = roadsIterator.next();
                            Geometry roadGeometry = (Geometry) road
                                    .getDefaultGeometry();
                            // if (roadGeometry.crosses(pointGeometry)) {
                            // System.out.println(pointGeometry
                            // .getCoordinate().x
                            // + " "
                            // + pointGeometry.getCoordinate().y);
                            // }
                            // if touches roads twice it mean that there is
                            // bridge
                            if (roadGeometry.intersects(pointBufferedGeometry)) {

                                intersectCount++;
                                // paklaida
                                double epsilonError = 0.035;
                                // 0.03 - 4602
                                // 0.025 - 3925
                                // 0.015 - 2700
                                // 0.01 - 2145
                                // 0.005 - 1632
                                // 0.008 - 1923
                                // with points that intersect with only rivers
                                // and not all other hidro
                                // 0.025 - 1432 - visual test fail
                                // 0.04 - 2348 - visual test ok TILTAI18
                                // 0.03 - 1709 visual test fail
                                // 0.035 - 2005 visual test fail
                                if (intersectCount == 2) {

                                    bridgesList.add(intersectPointFeature);
                                    break;

                                } else {

                                    Geometry roadIntersectGeometry = pointBufferedGeometry
                                            .intersection(roadGeometry);
                                    double roadIntersectLen = roadIntersectGeometry
                                            .getLength();

                                    // jeigu kelias kerta taska tai jis turi
                                    // ieiti ir iseiti is buferio, kirsdamas
                                    // centra
                                    // reiskias toks kelias turi buti bent
                                    // 2*buferio dydis.
                                    // System.out.println(roadIntersectLen);
                                    if (roadIntersectLen + epsilonError >= 2.0 * bufferSize) {

                                        bridgesList.add(intersectPointFeature);
                                        break;
                                    }
                                }
                            }

                        }
                    } finally {
                        roadsIterator.close();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                intersectPointsIterator.close();
            }
            System.out.println("Total: " + bridgesList.size());
            Layer brigesLayer = Support.simpleFeatureListToLayer(bridgesList);
            this.mapHandler.addLayerToMapContent(brigesLayer);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        return 1;
    }

    /**
     * Get points from VIE_T and filter them by GKODAS, what's left: villages
     */
    public void initVillages() {
        try {
            String pathSventoji = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";

            // get villages
            SimpleFeatureSource villagesSimpleFeatureSource = Support
                    .loadShapeFile(pathSventoji + "sven_VIE_T.shp");

            System.out.println("DONE reading from file");

            // get only villages
            Filter villageFilterByType = CQL
                    .toFilter("(GKODAS <> 'uhd6') and (GKODAS <> 'ums0') and (GKODAS <> 'unk0')");

            SimpleFeatureCollection villageCollection = villagesSimpleFeatureSource
                    .getFeatures(villageFilterByType);

            this.mapHandler.addLayerToMapContent(Support
                    .simpleFeatureCollectionToLayer(villageCollection));

            System.out.println("DONE filtering");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * initializes Forests lists by adding roads that are in forests and has
     * length of at least required minimum length. Roads that we are adding must
     * be intersected with forests beforehand.
     */
    public int initForestList() {

        Validator.checkNotPositive(this.minRoadLengthInForest);
        Validator.checkNotInitialized(forestCollection,
                roadsInForestsCollection);
        long startTime = System.currentTimeMillis();

        try {
            // convert to list for comfortable iterating
            List<SimpleFeature> foresFeaturetList = DataUtilities
                    .list(forestCollection);
            List<SimpleFeature> roadsInFeatureForestsList = DataUtilities
                    .list(roadsInForestsCollection);

            this.forestList = new ArrayList<Forest>();

            HashMap<SimpleFeature, Forest> forestHashMap = new HashMap<SimpleFeature, Forest>();

            // create new Forests
            for (int i = 0; i < foresFeaturetList.size(); i++) {

                SimpleFeature forestSimpleFeature = foresFeaturetList.get(i);

                Forest forest = new Forest(forestSimpleFeature,
                        this.minRoadLengthInForest);
                this.forestList.add(forest);

                forestHashMap.put(forestSimpleFeature, this.forestList.get(i));
            }

            System.out.println("Forests size: " + foresFeaturetList.size());
            System.out.println("Roads size: "
                    + roadsInFeatureForestsList.size());

            for (Forest forest : this.forestList) {

                Geometry forestGeometry = forest.getForestGeometry();

                SimpleFeatureCollection roadsCollectionFiltered = Support
                        .filterByReferenceEnvelope(forestGeometry,
                                roadsInForestsCollection);

                List<SimpleFeature> roadFilteredList = DataUtilities
                        .list(roadsCollectionFiltered);

                for (SimpleFeature roadSimpleFeature : roadFilteredList) {

                    Geometry roadGeometry = (Geometry) roadSimpleFeature
                            .getDefaultGeometry();

                    // ignore roads with less length than required
                    if (roadGeometry.getLength() < this.minRoadLengthInForest) {
                        continue;
                    }

                    int addResult = forest.addRoad(roadSimpleFeature);

                    boolean addSucc = (addResult == 1 ? true : false);

                    // single road can belong to single forest
                    if (addSucc) {
                        // System.out.println("road added to forest");
                        break;
                    }

                }
            }

            // remove forest that doesn't have any roads
            List<Forest> forestThatHasRoadsList = new ArrayList<Forest>();
            for (Forest forest : this.forestList) {

                if (forest.hasRoads()) {

                    forestThatHasRoadsList.add(forest);
                }
            }
            this.forestList = forestThatHasRoadsList;
            System.out.println("Forest count that have at least one road: "
                    + this.forestList.size());

            /*----------------TEST-----------------*/
            this.mapHandler.addLayerToMapContent(Support
                    .simpleFeatureCollectionToLayer(forestCollection));
            this.mapHandler.addLayerToMapContent(Support
                    .simpleFeatureCollectionToLayer(roadsInForestsCollection));

            int d = 0;
            for (int j = 0; j < forestList.size() && d < 3; j++) {
                if (!forestList.get(j).getRoadFeatureList().isEmpty()) {
                    this.mapHandler.addLayerToMapContent(Support
                            .simpleFeatureListToLayer(forestList.get(j)
                                    .getRoadFeatureList()));
                    d++;
                }
            }
            /*----------------END TEST-----------------*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(totalTime / 1000.0);
        System.out.println("DONE initializing forests");
        return 0;
    }

    public int initForestList2() {

        Validator.checkNotPositive(this.minRoadLengthInForest);
        Validator.checkNotInitialized(forestCollection,
                roadsInForestsCollection);

        // TEST
        long startTime = System.currentTimeMillis();

        try {

            System.out.println("STARTED initializing forests");

            // convert to list for comfortable iterating
            List<SimpleFeature> foresFeaturetList = DataUtilities
                    .list(forestCollection);
            List<SimpleFeature> roadsInFeatureForestsList = DataUtilities
                    .list(roadsInForestsCollection);

            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureCollectionToLayer(forestCollection));
            // this.mapHandler.addLayerToMapContent(Support
            // .simpleFeatureCollectionToLayer(roadsInForestsCollection));

            // if (roadsInForestsCollection != null) {
            // return 0;
            // }

            this.forestList = new ArrayList<Forest>();

            HashMap<Object, ArrayList<Forest>> forestHashMap = new HashMap<Object, ArrayList<Forest>>();

            // initialize attribute id
            int forestIdAttributeIndex = 0;
            SimpleFeature tmpRoadSimpleFeature = roadsInFeatureForestsList
                    .get(0);
            Object tmpforestId = tmpRoadSimpleFeature
                    .getAttribute("sven_ROADS_IN_FORESTS_L_FID_sven_B");
            for (int i = 0; i < tmpRoadSimpleFeature.getAttributeCount(); i++) {
                Object attribute = tmpRoadSimpleFeature.getAttribute(i);
                if (tmpforestId == attribute) {
                    System.out.println("Attribute index found");
                    forestIdAttributeIndex = i;
                    break;
                }
            }

            // create new Forests
            for (int i = 0; i < foresFeaturetList.size(); i++) {

                SimpleFeature forestSimpleFeature = foresFeaturetList.get(i);
                Forest forest = new Forest(forestSimpleFeature,
                        this.minRoadLengthInForest);
                this.forestList.add(forest);

                // get original forest id
                Object forestId = forestSimpleFeature
                        .getAttribute("sven_FORESTS_P_OBJECTID");

                if (!forestHashMap.containsKey(forestId)) {

                    forestHashMap.put(forestId, new ArrayList<Forest>());
                }

                forestHashMap.get(forestId).add(forest);

            }

            System.out.println("Forests size: " + foresFeaturetList.size());
            System.out.println("Roads size: "
                    + roadsInFeatureForestsList.size());

            for (SimpleFeature roadSimpleFeature : roadsInFeatureForestsList) {

                Geometry roadGeometry = (Geometry) roadSimpleFeature
                        .getDefaultGeometry();

                // filter roads by their length
                if (roadGeometry.getLength() < this.minRoadLengthInForest) {
                    continue;
                }

                // Object forestId = roadSimpleFeature
                // .getAttribute("sven_ROADS_IN_FORESTS_L_FID_sven_B");
                Object forestId = roadSimpleFeature
                        .getAttribute(forestIdAttributeIndex);

                List<Forest> foretList = forestHashMap.get(forestId);

                for (Forest forest : foretList) {

                    int addResult = forest.addRoad(roadSimpleFeature);

                    boolean addSucc = (addResult == 1 ? true : false);

                    // single road can belong to single forest
                    if (addSucc) {

                        break;
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(totalTime / 1000.0);
        System.out.println("DONE initializing forests");
        return 0;
    }

    private Layer bufferedLakeListToLayer() {

        Validator.checkNotInitialized(this.lakeList);

        try {

            List<SimpleFeature> bufferedLakeFeatureList = new ArrayList<SimpleFeature>();

            for (Lake lake : this.lakeList) {

                SimpleFeature bufferedSimpleFeature = lake
                        .getBufferedLakeSimpleFeature();
                bufferedLakeFeatureList.add(bufferedSimpleFeature);
            }

            return Support.simpleFeatureListToLayer(bufferedLakeFeatureList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
