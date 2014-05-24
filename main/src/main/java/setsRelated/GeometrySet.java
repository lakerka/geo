package setsRelated;

import java.awt.Point;
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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.shp.MultiLineHandler;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

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
            SimpleFeatureCollection simpleFeatureCollection_1 = Support
                    .layerToSimpleFeatureCollection(layer1);
            SimpleFeatureCollection simpleFeatureCollection_2 = Support
                    .layerToSimpleFeatureCollection(layer2);

            SimpleFeatureCollection intersectionCollection = this.intersect(
                    simpleFeatureCollection_1, simpleFeatureCollection_2,
                    threadCount);

            // /TODO orgiginal delete
            // List<SimpleFeature> simpleFeatureCollection_1 = Support
            // .layerToSimpleFeatureList(layer1);
            // List<SimpleFeature> simpleFeatureCollection_2 = Support
            // .layerToSimpleFeatureList(layer2);
            //
            // List<SimpleFeature> intersectSimpleFeatures = intersect(
            // simpleFeatureCollection_1, simpleFeatureCollection_2,
            // threadCount);
            //
            // SimpleFeatureCollection intersectionCollection = DataUtilities
            // .collection(intersectSimpleFeatures);
            // /

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
        List<SimpleFeature> firstFeatureList;
        List<SimpleFeature> secondFeatureList;

        if (simpleFeatureList1.size() > simpleFeatureList2.size()) {

            firstFeatureList = simpleFeatureList1;
            secondFeatureList = simpleFeatureList2;

        } else {

            firstFeatureList = simpleFeatureList2;
            secondFeatureList = simpleFeatureList1;

        }

        List<List<SimpleFeature>> simpleFeaturePartition = partition(
                firstFeatureList, threadCount);
        /*
         * this list will be appended by many threads so we need to keep add
         * operation safe
         */
        List<SimpleFeature> intersectSimpleFeatureList = Collections
                .synchronizedList(new ArrayList<SimpleFeature>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int id = 0;

        InverseSemaphore inverseSemaphore = new InverseSemaphore(threadCount);

        for (int i = 0; i < threadCount; i++) {

            String threadName = "Thread_" + String.valueOf(i);

            inverseSemaphore.beforeSubmit();

            executor.execute(new IntersectSimpleFeaturesThread(threadName,
                    DataUtilities.collection(simpleFeaturePartition.get(i)),
                    DataUtilities.collection(secondFeatureList),
                    intersectSimpleFeatureList, id, inverseSemaphore));

            id += (simpleFeaturePartition.get(i).size()
                    * secondFeatureList.size() + 1);
        }

        executor.shutdown();
        /*
         * wait until all threads completed intersect
         */
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {

        }

        return intersectSimpleFeatureList;

    }

    public SimpleFeatureCollection intersect(
            SimpleFeatureCollection simpleFeatureCollection_1,
            SimpleFeatureCollection simpleFeatureCollection_2, int threadCount) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException("Arguments must be positive!");
        }

        if (simpleFeatureCollection_1 == null
                || simpleFeatureCollection_2 == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        /*
         * if any collection is empty their intersection is empty collection
         */
        if (simpleFeatureCollection_1.isEmpty()) {

            return simpleFeatureCollection_1;
        }

        if (simpleFeatureCollection_2.isEmpty()) {

            return simpleFeatureCollection_2;
        }

        try {

            // TODO remove printing
            long startTime = System.nanoTime();

            Class geometryType_1 = simpleFeatureCollection_1.getSchema()
                    .getGeometryDescriptor().getType().getBinding();
            Class geometryType_2 = simpleFeatureCollection_2.getSchema()
                    .getGeometryDescriptor().getType().getBinding();

            boolean geometryType_1_IsPolygonOrLine = isPolygonOrLine(geometryType_1);
            boolean geometryType_2_IsPolygonOrLine = isPolygonOrLine(geometryType_2);

            if (!geometryType_1_IsPolygonOrLine
                    && !geometryType_2_IsPolygonOrLine) {

                throw new IllegalArgumentException(
                        "simpleFeatureCollection_1 and simpleFeatureCollection_2 cant have point/multipoint geometry!");
            }

            SimpleFeatureCollection firstSimpleFeatureCollection = null;
            SimpleFeatureCollection secondSimpleFeatureCollection = null;

            /*
             * we want first collection to be of type polygons and have least
             * elements
             */

            boolean geometry_1_IsPolygon = isPolygon(geometryType_1);
            boolean geometry_2_IsPolygon = isPolygon(geometryType_2);

            // 1 - polygon, 2 - polygon
            if (geometry_1_IsPolygon && geometry_2_IsPolygon) {

                int collection_1_size = simpleFeatureCollection_1.size();
                int collection_2_size = simpleFeatureCollection_2.size();

                // 1 - size < 2 - size
                if (collection_1_size < collection_2_size) {

                    firstSimpleFeatureCollection = simpleFeatureCollection_1;
                    secondSimpleFeatureCollection = simpleFeatureCollection_2;

                    // 1 - size >= 2 - size
                } else {

                    firstSimpleFeatureCollection = simpleFeatureCollection_2;
                    secondSimpleFeatureCollection = simpleFeatureCollection_1;
                }
                // 1 - polygon, 2 - not polygon
            } else if (geometry_1_IsPolygon && !geometry_2_IsPolygon) {

                firstSimpleFeatureCollection = simpleFeatureCollection_1;
                secondSimpleFeatureCollection = simpleFeatureCollection_2;

                // 1 - not polygon, 2 - polygon
            } else if (!geometry_1_IsPolygon && geometry_2_IsPolygon) {

                firstSimpleFeatureCollection = simpleFeatureCollection_2;
                secondSimpleFeatureCollection = simpleFeatureCollection_1;

                // 1 - line or point , 2 - line or point
            } else {

                boolean geometry_1_IsLine = isLine(geometryType_1);
                boolean geometry_2_IsLine = isLine(geometryType_2);

                // 1 - is line, 2 - line or point
                if (geometry_1_IsLine) {

                    firstSimpleFeatureCollection = simpleFeatureCollection_1;
                    secondSimpleFeatureCollection = simpleFeatureCollection_2;

                    // 1 - point, 2 - line
                } else {

                    firstSimpleFeatureCollection = simpleFeatureCollection_2;
                    secondSimpleFeatureCollection = simpleFeatureCollection_1;
                }
            }

            int firtPartitionsCount = Math.min(firstSimpleFeatureCollection.size(), 10);
            int secondPartitionsCount = Math.min(secondSimpleFeatureCollection.size(), 10);

            List<SimpleFeatureCollection> simpleFeaturePartition_1 = partition(
                    firstSimpleFeatureCollection, firtPartitionsCount);

            List<SimpleFeatureCollection> simpleFeaturePartition_2 = partition(
                    secondSimpleFeatureCollection, secondPartitionsCount);

            /*
             * this list will be appended by many threads so we need to keep add
             * operation safe
             */
            List<SimpleFeature> intersectSimpleFeatureList = Collections
                    .synchronizedList(new ArrayList<SimpleFeature>());

            ExecutorService executor = Executors
                    .newFixedThreadPool(threadCount);

            long idToStartFrom = 0;
            InverseSemaphore inverseSemaphore = new InverseSemaphore(threadCount);

            for (int i = 0; i < simpleFeaturePartition_1.size(); i++) {

                for (int j = 0; j < simpleFeaturePartition_2.size(); j++) {

                    String threadName = "Runnable_" + String.valueOf(i) + "_"
                            + String.valueOf(j);

                    inverseSemaphore.beforeSubmit();

                    executor.execute(new IntersectSimpleFeaturesThread(
                            threadName, simpleFeaturePartition_1.get(i),
                            simpleFeaturePartition_2.get(j),
                            intersectSimpleFeatureList, idToStartFrom,
                            inverseSemaphore));
                    

                    idToStartFrom += (simpleFeaturePartition_1.get(i).size()
                            * simpleFeaturePartition_2.get(j).size() + 1);

                    inverseSemaphore.awaitFreeOfTaskThread();
                }

            }
            
            inverseSemaphore.awaitCompletion();
            executor.shutdownNow();
            /*
             * wait until all threads completed intersect
             */

            // TODO remove printing
            long endTime = System.nanoTime();
            System.out.println("Took " + (endTime - startTime) / 60000000000.0 * 60.0
                    + " s");

            return DataUtilities.collection(intersectSimpleFeatureList);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    /*
     * function used to partition list into several other list
     */
    private List<List<SimpleFeature>> partition(
            List<SimpleFeature> simpleFeatureList, int numberOfPartitions) {

        if (numberOfPartitions <= 0) {
            throw new IllegalArgumentException(
                    "numberOfPartitions must be positive!");
        }

        if (simpleFeatureList == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureList must not be null!");
        }

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

    private List<SimpleFeatureCollection> partition(
            SimpleFeatureCollection simpleFeatureCollection,
            int numberOfPartitions) {

        if (numberOfPartitions <= 0) {
            throw new IllegalArgumentException(
                    "numberOfPartitions must be positive!");
        }

        if (simpleFeatureCollection == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureCollection must not be null!");
        }

        try {
            List<SimpleFeatureCollection> partitionsList = new ArrayList<SimpleFeatureCollection>();
            List<List<SimpleFeature>> tmpPartitionsList = new ArrayList<List<SimpleFeature>>();

            for (int i = 0; i < numberOfPartitions; i++) {
                tmpPartitionsList.add(new ArrayList<SimpleFeature>());
            }

            SimpleFeatureIterator simpleFeatureIterator = simpleFeatureCollection
                    .features();

            try {
                int k = 0;

                while (simpleFeatureIterator.hasNext()) {

                    SimpleFeature simpleFeature = simpleFeatureIterator.next();

                    tmpPartitionsList.get(k % numberOfPartitions).add(
                            simpleFeature);

                    k++;
                }

                for (int i = 0; i < numberOfPartitions; i++) {

                    partitionsList.add(DataUtilities
                            .collection(tmpPartitionsList.get(i)));
                }

                return partitionsList;

                // we must close iterator in any case
            } finally {
                simpleFeatureIterator.close();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    boolean isGeometryTypeIn(Class test, Class... targets) {

        if (targets == null) {
            throw new IllegalArgumentException("targets must not be null!");
        }

        for (Class target : targets) {

            if (target.isAssignableFrom(test)) {
                return true;
            }
        }
        return false;
    }

    boolean isPolygonOrLine(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiPolygon.class,
                Polygon.class, MultiLineString.class, LineString.class);

    }

    boolean isPolygon(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiPolygon.class, Polygon.class);

    }

    boolean isLine(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiLineString.class,
                LineString.class);

    }
}
