package org.geotools.main;

import handlers.ExportShapeFileAction;







import java.io.File;
import java.io.IOException;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.FactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import setsRelated.GeometrySet;
import setsRelated.IntersectTest;

import com.vividsolutions.jts.geom.*;

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
//            Filter f = CQL.toFilter("BBOX(SHAPE, 10,20,30,40)");
            mainWindow = new MainWindow(null, 1200, 600);

//            String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
//            SimpleFeatureSource simpleFeatureSourceKeliai = Support
//                    .loadShapeFile(path + "sven_KEL_L.shp");
//            Layer keliaiLayer = Support.simpleFeatureSourceToLayer(simpleFeatureSourceKeliai);
//            
//            Main.mainWindow.addLayer(keliaiLayer);
//            Thread.sleep(1000);
//            
//            SimpleFeatureCollection s = Support.layerToSimpleFeatureCollection(keliaiLayer); 
//            
//            //minx miny maxx maxy
//            Filter filter = ECQL.toFilter("BBOX(the_geom, 576747, 6150069,  626858, 6170876)");
//
//            SimpleFeatureCollection subCollection = s.subCollection(filter);
//            Layer newLayer = Support.simpleFeatureCollectionToLayer(subCollection);
//            Main.mainWindow.addLayer(newLayer);
//            
//            GeometrySet geometrySet = new GeometrySet();
//            geometrySet.intersect(keliaiLayer, newLayer, 4);
            
           // long startTime = System.nanoTime();

            //Main.test();
//            long endTime = System.nanoTime();
//            System.out.println("Took " + (endTime - startTime) / 60000000000.0
//                    + " min");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void test() {

        String path = "C:\\Users\\as\\Desktop\\gis\\LTsventoji\\";
        SimpleFeatureSource simpleFeatureSourceKeliai = Support
                .loadShapeFile(path + "sven_KEL_L.shp");
        SimpleFeatureSource simpleFeatureSourceApskritys = Support
                .loadShapeFile(path + "sven_SAV_P.shp");

        try {

            GeometrySet geometrySet = new GeometrySet();

            SimpleFeatureSource intersectSimpleFeatureSource = geometrySet
                    .intersect(simpleFeatureSourceApskritys,simpleFeatureSourceKeliai
                            , 1);

            Layer intersectLayer = Support
                    .simpleFeatureSourceToLayer(intersectSimpleFeatureSource);

            Main.mainWindow.addLayer(intersectLayer);
            sop("Done intersecting!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
    }

    public static void sop(String string) {
        System.out.println(string);
    }
}
