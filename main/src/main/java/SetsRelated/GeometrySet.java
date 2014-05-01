package SetsRelated;

import java.io.IOException;
import java.lang.Thread.State;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.shp.MultiLineHandler;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.main.GeometryType;
import org.geotools.main.Main;
import org.geotools.main.Support;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.renderer.lite.SynchronizedLabelCache;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
//import org.geotools.process.vector.IntersectionFeatureCollection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

public class GeometrySet {

    public SimpleFeatureSource intersect(
            SimpleFeatureSource simpleFeatureSource1,
            SimpleFeatureSource simpleFeatureSource2, int threadCount) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive!");
        }

        if (simpleFeatureSource1 == null || simpleFeatureSource2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {
            List<SimpleFeature> simpleFeatureList1 = DataUtilities
                    .list(simpleFeatureSource1.getFeatures());
            List<SimpleFeature> simpleFeatureList2 = DataUtilities
                    .list(simpleFeatureSource2.getFeatures());

            List<SimpleFeature> intersectionList = this.intersect(
                    simpleFeatureList1, simpleFeatureList2, threadCount);

            // if intersection result is null then return empty result
            if (intersectionList == null) {
                return null;
            }

            SimpleFeatureCollection intersectCollection = DataUtilities
                    .collection(intersectionList);

            SimpleFeatureSource intersectSimpleFeatureSource = DataUtilities
                    .source(intersectCollection);

            return intersectSimpleFeatureSource;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }

    public Layer intersect(Layer layer1, Layer layer2, int threadCount) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive!");
        }

        if (layer1 == null || layer2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {
            List<SimpleFeature> simpleFeatureList1 = Support
                    .layerToSimpleFeatureList(layer1);
            List<SimpleFeature> simpleFeatureList2 = Support
                    .layerToSimpleFeatureList(layer2);

            List<SimpleFeature> intersectionList = this.intersect(
                    simpleFeatureList1, simpleFeatureList2, threadCount);

            // if intersection result is null then return empty result
            if (intersectionList == null) {
                return null;
            }

            SimpleFeatureCollection intersectionCollection = DataUtilities
                    .collection(intersectionList);
            
            Layer intersectionLayer = new FeatureLayer(intersectionCollection,
                    SLD.createSimpleStyle(intersectionCollection.getSchema()));

            
            
            return intersectionLayer;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }

    public List<SimpleFeature> intersect(
            List<SimpleFeature> simpleFeatureList1,
            List<SimpleFeature> simpleFeatureList2, int threadCount) {

        // TODO remove printing
        long startTime = System.nanoTime();

        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive!");
        }

        if (simpleFeatureList1 == null || simpleFeatureList2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        /*
         * We need to partition list with more elements so that performance
         * would increase, as thread count can be large
         */
        List<SimpleFeature> listWithMoreElements;
        List<SimpleFeature> listWithLessElements;

        if (simpleFeatureList1.size() > simpleFeatureList2.size()) {

            listWithMoreElements = simpleFeatureList1;
            listWithLessElements = simpleFeatureList2;

        } else {

            listWithMoreElements = simpleFeatureList2;
            listWithLessElements = simpleFeatureList1;

        }

        List<List<SimpleFeature>> simpleFeaturePartition = partition(
                listWithMoreElements, threadCount);
        /*
         * this list will be appended by many threads so we need to keep add
         * operation safe
         */
        List<SimpleFeature> intersectSimpleFeatureList = Collections
                .synchronizedList(new ArrayList<SimpleFeature>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int id = 0;
        
        for (int i = 0; i < threadCount; i++) {
            
            String threadName = "Thread_" + String.valueOf(i);
            
            executor.execute(new IntersectSimpleFeaturesThread(threadName,
                    DataUtilities.collection(simpleFeaturePartition.get(i)),
                    DataUtilities.collection(listWithLessElements),
                    intersectSimpleFeatureList, id));
            
            id += (simpleFeaturePartition.get(i).size()*listWithLessElements.size() + 1);

        }

        executor.shutdown();
        /*
         * wait until all threads completed intersect
         */
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {

        }

        // TODO remove printing
        long endTime = System.nanoTime();
        System.out.println("Took " + (endTime - startTime) / 60000000000.0
                + " min");

        return intersectSimpleFeatureList;

    }

    /*
     * function used to partition list into several other list
     */
    private List<List<SimpleFeature>> partition(
            List<SimpleFeature> simpleFeatureList, int numberOfPartitions) {

        List<List<SimpleFeature>> partitionsList = new ArrayList<List<SimpleFeature>>();

        for (int i = 0; i < numberOfPartitions; i++) {
            partitionsList.add(new ArrayList<SimpleFeature>());
        }

        for (int i = 0; i < simpleFeatureList.size(); i++) {

            SimpleFeature simpleFeature = simpleFeatureList.get(i);

            partitionsList.get(i % numberOfPartitions).add(simpleFeature);
        }

        return partitionsList;
    }
}
