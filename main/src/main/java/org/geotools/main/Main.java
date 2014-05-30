package org.geotools.main;

import handlers.ExportShapeFileAction;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.index.Data;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.FactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.metadata.iso.extent.BoundingPolygonImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import setsRelated.GeometrySet;
import setsRelated.IntersectTest;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geomgraph.Edge;

import views.windows.MainWindow;

/*
 * + add/remove layer
 * + show/hidden layer
 * + zoom in/zoom out
 * + pan (postumis)
 * + full extent (grizimas i pradini vaizda)
 * + select / multiselect
 * + zoom to select
 * + show attribute data referencing selected map content / show map content referencing selected attribute data
 * + display layer attribute data
 * + object search relied on supplied attribute values
 */

public class Main {

    public static MainWindow mainWindow;

    public static void main(String[] args) throws Exception {

        try {
            // System.out.println((new
            // BigDecimal(Double.MAX_VALUE)).toPlainString());
            // Filter f = CQL.toFilter("BBOX(SHAPE, 10,20,30,40)");

            mainWindow = new MainWindow(null, 1200, 600);
//            displaySav();
//            displayHidro();
             test4();
            // test2();
            // long endTime = System.nanoTime();
            // System.out.println("Took " + (endTime - startTime) /
            // 60000000000.0
            // + " min");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void displaySav() {

        Main.mainWindow.mapHandler.addLayerToMapContent(getSavLayer(),
                "savivaldybes");
    }
    
    public static void displayHidro() {

        Main.mainWindow.mapHandler.addLayerToMapContent(getUpesLayer(),
                "hidro");
    }

    public static Layer getSavLayer() {

        String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";

        SimpleFeatureSource sfsSav = Support.loadShapeFile(path
                + "sven_SAV_P.shp");

        Layer savLayer = Support.simpleFeatureSourceToLayer(sfsSav);
        return savLayer;
    }

    public static Layer getUpesLayer() {
        
        String pathTrecia = "C:\\Users\\as\\git\\geograf\\main\\LTsventoji\\";
        
        SimpleFeatureSource sfsUpes = Support.loadShapeFile(pathTrecia
                + "sven_HID_L.shp");

        Layer upesLayer = Support.simpleFeatureSourceToLayer(sfsUpes);
        return upesLayer;
    }

    // TODO
    // used for testing how edge finds path around obstacle including some given
    // LineString
    public static void test4() {

        String pathTrecia = "C:\\Users\\as\\git\\geograf\\main\\trecia\\";
        SimpleFeatureSource sfsLakes = Support.loadShapeFile(pathTrecia
                + "sven_LAKES_P.shp");

        SimpleFeatureSource sfsRoads = Support.loadShapeFile(pathTrecia
                + "sven_ROADS_IN_FORESTS_L.shp");

        try {

            Main.mainWindow.mapHandler.addLayerToMapContent(sfsLakes, "lakes");
            Main.mainWindow.mapHandler.addLayerToMapContent(sfsRoads,
                    "all roads");

            // create bbox and get road we are interested in
            // /
            // bbox upper left
            // 623940.451 6142317.958 Meters

            // bbox lower right
            // 624115.797 6142150.236 Meters

            // /

            ReferencedEnvelope refEnv = new ReferencedEnvelope();

            Coordinate upperLeft = new Coordinate(623940.451, 6142317.958);
            Coordinate lowerRight = new Coordinate(624115.797, 6142150.236);

            refEnv.expandToInclude(upperLeft);
            refEnv.expandToInclude(lowerRight);

            List<SimpleFeature> simpleList = DataUtilities.list(Support
                    .filterByReferenceEnvelope(sfsRoads.getFeatures(), refEnv));
            SimpleFeature roadSimpleFeature = simpleList.get(1);
            System.out.println(simpleList.size());

            MultiLineString roadMultiLineString = (MultiLineString) roadSimpleFeature
                    .getDefaultGeometry();
            LineString roadLineString = (LineString) roadMultiLineString
                    .getGeometryN(0);

            SimpleFeatureType sft = createFeatureType();
            SimpleFeature singleRoadSimpleFeature = buildSimpleFeature(
                    roadLineString, sft);

            Layer singleRoadLayer = Support
                    .simpleFeatureToLayer(singleRoadSimpleFeature);

            Main.mainWindow.mapHandler.addLayerToMapContent(singleRoadLayer,
                    "test road");

            // //

            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
                    .getDefaultHints());

            Set<FeatureId> fids = new HashSet<FeatureId>();
            fids.add(ff.featureId("sven_LAKES_P.1118"));
            Filter filter = ff.id(fids);

            List<SimpleFeature> rez = Support.getFilteredFeaturesList(
                    sfsLakes.getFeatures(), filter);

            // System.out.println(rez.size());

            SimpleFeature lakefeature = rez.get(0);

            MultiPolygon obstacles = (MultiPolygon) lakefeature
                    .getDefaultGeometry();
            // System.out.println(obstacles.getNumGeometries());
            Polygon obstacle = (Polygon) obstacles.getGeometryN(0);

            // from lineString

            // from
            Coordinate from = roadLineString.getStartPoint().getCoordinate();

            // to
            // 623,581.687 6,139,886.232 Meters surrounded by poligon area
            Coordinate to = new Coordinate(623581.687, 6139886.232);
//              x=624013.7445 y=6140133.1083 slightly intersects polygon
//             Coordinate to = new Coordinate(624013.7445 , 6140133.1083);

            model.Edge edge = new model.Edge(roadLineString, from, to, obstacle);

            SimpleFeature path = buildSimpleFeature(edge.getLineString(),
                    createFeatureType());

            List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
            featureList.add(path);

            Layer pathLayer = Support.simpleFeatureListToLayer(featureList);

            Layer testLakeLayer = Support.simpleFeatureToLayer(lakefeature);

            Main.mainWindow.mapHandler.addLayerToMapContent(testLakeLayer,
                    "lake to avoid");
            Main.mainWindow.mapHandler.addLayerToMapContent(pathLayer, "path");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // used for testing how edge finds path around obstacle
    public static void test3() {

        String pathTrecia = "C:\\Users\\as\\git\\geograf\\main\\trecia\\";
        SimpleFeatureSource sfs = Support.loadShapeFile(pathTrecia
                + "sven_LAKES_P.shp");

        Main.mainWindow.mapHandler.addLayerToMapContent(sfs);

        try {

            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
                    .getDefaultHints());

            Set<FeatureId> fids = new HashSet<FeatureId>();
            fids.add(ff.featureId("sven_LAKES_P.1115"));
            Filter filter = ff.id(fids);

            List<SimpleFeature> rez = Support.getFilteredFeaturesList(
                    sfs.getFeatures(), filter);

            // System.out.println(rez.size());

            SimpleFeature feature = rez.get(0);

            MultiPolygon obstacles = (MultiPolygon) feature
                    .getDefaultGeometry();
            // System.out.println(obstacles.getNumGeometries());
            Polygon obstacle = (Polygon) obstacles.getGeometryN(0);

            Coordinate from = new Coordinate(621773.7010, 6146292.9196);
            Coordinate to = new Coordinate(620868.089, 6145153.985);
            // Coordinate extension = new Coordinate();

            model.Edge edge = new model.Edge(from, to, obstacle);

            SimpleFeature path = buildSimpleFeature(edge.getLineString(),
                    createFeatureType());

            List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
            featureList.add(path);

            Layer pathLayer = Support.simpleFeatureListToLayer(featureList);

            Main.mainWindow.mapHandler.addLayerToMapContent(pathLayer);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void test2() {
        String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
        SimpleFeatureSource simpleFeatureSourceKeliai = Support
                .loadShapeFile(path + "sven_KEL_L.shp");
        SimpleFeatureSource simpleFeatureSourceApskritys = Support
                .loadShapeFile(path + "sven_SAV_P.shp");
        SimpleFeatureSource simpleFeatureSourcePlotai = Support
                .loadShapeFile(path + "sven_PLO_P.shp");

        Layer keliaiLayer = Support
                .simpleFeatureSourceToLayer(simpleFeatureSourceKeliai);
        Layer apskritysLayer = Support
                .simpleFeatureSourceToLayer(simpleFeatureSourceApskritys);
        Layer plotaiLayer = Support
                .simpleFeatureSourceToLayer(simpleFeatureSourcePlotai);

        Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer);

        String pathTest = "C:\\Users\\as\\git\\geograf\\main\\test\\";
        // Support.exportToShapeFile(layer, schemaTypeName,
        // directory.getParentFile(), directory.getName())
        DataStore dataStore = Support.exportToShapeFile(apskritysLayer, null,
                new File(pathTest), "testFailasSuIlguVardu");

        if (dataStore == null) {
            System.out.println("IS NULL");
        } else {
            System.out.println("Exported!");
        }
        // dataStore.

        // try {
        //
        // Thread.sleep(1000);
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
        // Main.mainWindow.mapHandler.addLayerToMapContent(plotaiLayer);
        // Main.mainWindow.mapHandler.addLayerToMapContent(apskritysLayer);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer);

        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer, false);
        // Main.mainWindow.mapHandler.addLayerToMapContent(apskritysLayer,
        // true);
    }

    public static void test() {

        String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
        SimpleFeatureSource simpleFeatureSourceKeliai = Support
                .loadShapeFile(path + "sven_KEL_L.shp");
        SimpleFeatureSource simpleFeatureSourceApskritys = Support
                .loadShapeFile(path + "sven_SAV_P.shp");

        Layer keliaiLayer = Support
                .simpleFeatureSourceToLayer(simpleFeatureSourceKeliai);
        Layer apskritysLayer = Support
                .simpleFeatureSourceToLayer(simpleFeatureSourceApskritys);

        try {

            Main.mainWindow.mapHandler.addLayerToMapContent(apskritysLayer);
            // Thread.sleep(1000);
            // Main.mainWindow.mapHandler.addLayerToMapContent(keliaiLayer);

            // Layer keliaiSameLayer =
            // Main.mainWindow.mapHandler.getLayers().get(1);
            // Thread.sleep(1000);
            // keliaiSameLayer.setSelected(false);
            // keliaiSameLayer.setVisible(false);

            // GeometrySet geometrySet = new GeometrySet();
            //
            // SimpleFeatureSource intersectSimpleFeatureSource = geometrySet
            // .intersect(simpleFeatureSourceApskritys,simpleFeatureSourceKeliai
            // , 1);
            //
            // Layer intersectLayer = Support
            // .simpleFeatureSourceToLayer(intersectSimpleFeatureSource);
            //
            // Main.mainWindow.addLayer(intersectLayer);
            // sop("Done intersecting!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println();
    }

    // creates simpleFeatureType with only geometry
    private static SimpleFeatureType createFeatureType() {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Path");
        builder.setCRS(null); // <- Coordinate reference
                              // system

        // add attributes in order
        builder.add("the_geom", LineString.class);

        // build the type
        final SimpleFeatureType LOCATION = builder.buildFeatureType();

        return LOCATION;
    }

    private static SimpleFeature buildSimpleFeature(Geometry geometry,
            SimpleFeatureType simpleFeatureType) {

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
                simpleFeatureType);

        featureBuilder.add(geometry);
        SimpleFeature feature = featureBuilder.buildFeature(null);

        return feature;

    }

    public static void sop(String string) {
        System.out.println(string);
    }
}
