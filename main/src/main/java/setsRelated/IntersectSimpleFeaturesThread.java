package setsRelated;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class IntersectSimpleFeaturesThread implements Runnable {

    // private Thread thread;
    private String runnableName;
    private SimpleFeatureCollection simpleFeatureList1;

    private SimpleFeatureCollection simpleFeatureList2;
    private List<SimpleFeature> localSimpleFeatureList;
    private List<SimpleFeature> globalIntersectSimpleFeatureList;
    private SimpleFeatureType simpleFeatureType;
    private long id;
    private InverseSemaphore inverseSemaphore;

    public IntersectSimpleFeaturesThread(String name,
            SimpleFeatureCollection simpleFeatureList1,
            SimpleFeatureCollection simpleFeatureList2,
            List<SimpleFeature> intersectSimpleFeatureList, long id,
            InverseSemaphore inverseSemaphore) {

        this.runnableName = name;
        this.simpleFeatureList1 = simpleFeatureList1;
        this.simpleFeatureList2 = simpleFeatureList2;
        this.globalIntersectSimpleFeatureList = intersectSimpleFeatureList;
        this.localSimpleFeatureList = new ArrayList<SimpleFeature>();
        this.id = id;
        this.inverseSemaphore = inverseSemaphore;
        // System.out.println("Creating " + this.runnableName);
    }

    public String getThreadName() {
        return this.runnableName;
    }

    private long getId() {
        return this.id++;
    }

    // public void run() {
    //
    // try {
    // // System.out.println("Starting " + this.runnableName);
    // // intersection of empty set and something will not produce any
    // // results
    // if (simpleFeatureList1.isEmpty() || simpleFeatureList2.isEmpty()) {
    // return;
    // }
    //
    // SimpleFeatureCollection simpleCollectionFromSingleFeature = null;
    //
    // SimpleFeatureIterator iterator = simpleFeatureList1.features();
    // try {
    // while (iterator.hasNext()) {
    //
    // SimpleFeature simpleFeatureFromCollection1 = iterator
    // .next();
    //
    // // filter second collection
    // // SimpleFeatureCollection filteredSmpleFeatureCollection2 =
    // filterSimpleFeatureCollection(
    // // simpleFeatureFromCollection1, simpleFeatureList2);
    // SimpleFeatureCollection filteredSmpleFeatureCollection2 =
    // simpleFeatureList2;
    //
    // // add single feature from first collection
    // simpleCollectionFromSingleFeature = DataUtilities
    // .collection(simpleFeatureFromCollection1);
    //
    // IntersectionFeatureCollection ifc = new IntersectionFeatureCollection();
    // SimpleFeatureIterator iter;
    //
    // SimpleFeatureCollection kk = ifc.execute(
    // simpleCollectionFromSingleFeature,
    // filteredSmpleFeatureCollection2, null, null, null,
    // null, null);
    // iter = kk.features();
    //
    // for (; iter.hasNext();) {
    //
    // SimpleFeature sf = iter.next();
    //
    // SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
    // sf.getFeatureType());
    //
    // builder.addAll(sf.getAttributes());
    // localSimpleFeatureList.add(builder.buildFeature(""
    // + getId()));
    // }
    // }
    // } finally {
    // iterator.close();
    // }
    // this.globalIntersectSimpleFeatureList
    // .addAll(localSimpleFeatureList);
    // } catch (Exception exception) {
    //
    // exception.printStackTrace();
    //
    // } finally {
    //
    // this.inverseSemaphore.taskCompleted();
    //
    // // System.out.println("Ending " + this.runnableName);
    // }
    // }

    public void run() {
        
        boolean PRINT_START_AND_END = false;

        try {
            
            if (PRINT_START_AND_END) {
                System.out.println("Starting: " + this.runnableName);
            }
            
            if (simpleFeatureList1.isEmpty() || simpleFeatureList2.isEmpty()) {
                return;
            }

            IntersectionFeatureCollection ifc = new IntersectionFeatureCollection();
            SimpleFeatureIterator iter;

            SimpleFeatureCollection kk = ifc.execute(simpleFeatureList1,
                    simpleFeatureList2, null, null, null, null, null);
            iter = kk.features();

            for (; iter.hasNext();) {

                SimpleFeature sf = iter.next();

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
                        sf.getFeatureType());

                builder.addAll(sf.getAttributes());
                localSimpleFeatureList.add(builder.buildFeature("" + getId()));
            }
            this.globalIntersectSimpleFeatureList
                    .addAll(localSimpleFeatureList);
        } catch (Exception exception) {

            exception.printStackTrace();

        } finally {

            if (PRINT_START_AND_END) {
                System.out.println("Starting: " + this.runnableName);
            }
            this.inverseSemaphore.taskCompleted();

        }
    }

    public void setThreadName(String threadName) {
        this.runnableName = threadName;
    }

    public SimpleFeatureCollection filterSimpleFeatureCollection(
            SimpleFeature simpleFeature,
            SimpleFeatureCollection collectionToFilter) {

        try {

            // ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(
            // simpleFeatureCollection.getBounds(),
            // simpleFeatureCollection.getSchema()
            // .getCoordinateReferenceSystem());
            //
            // FilterFactory2 filterFactory2 = CommonFactoryFinder
            // .getFilterFactory2();
            //
            // String geometryDescriptorLocalName = simpleFeatureCollection
            // .getSchema().getGeometryDescriptor().getLocalName();
            //
            // Filter filter = filterFactory2.bbox(
            // filterFactory2.property(geometryDescriptorLocalName),
            // referencedEnvelope);
            //
            // SimpleFeatureCollection filteredCollection = collectionToFilter
            // .subCollection(filter);
            try {

                ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(
                        new ReferencedEnvelope(simpleFeature.getBounds()),
                        simpleFeature.getFeatureType()
                                .getCoordinateReferenceSystem());

                FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
                SimpleFeatureType schema = simpleFeature.getFeatureType();

                // usually "THE_GEOM" for shapefiles
                String geometryPropertyName = schema.getGeometryDescriptor()
                        .getLocalName();
                CoordinateReferenceSystem targetCRS = schema
                        .getGeometryDescriptor().getCoordinateReferenceSystem();

                ReferencedEnvelope bbox = new ReferencedEnvelope(
                        referencedEnvelope.getMinX(),
                        referencedEnvelope.getMaxX(),
                        referencedEnvelope.getMinY(),
                        referencedEnvelope.getMaxY(), targetCRS);

                // Filter filter = ff
                // .bbox(ff.property(geometryPropertyName), bbox);

                // //minx miny maxx maxy
                // "BBOX(the_geom, 576747, 6150069,  626858, 6170876)"
                String ecqlPredicate = "BBOX(" + geometryPropertyName + ","
                        + referencedEnvelope.getMinX() + ","
                        + referencedEnvelope.getMinY() + ","
                        + referencedEnvelope.getMaxX() + ","
                        + referencedEnvelope.getMaxY() + ")";

                Filter filter = ECQL.toFilter(ecqlPredicate);

                SimpleFeatureCollection filteredCollection = collectionToFilter
                        .subCollection(filter);

                return filteredCollection;

            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;

        } catch (Exception exception) {

            return collectionToFilter;
        }
    }
}
