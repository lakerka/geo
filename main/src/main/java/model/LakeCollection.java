package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.index.Data;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.main.Support;
import org.geotools.main.Validator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class LakeCollection {

    private Stack<Lake> lakes = new Stack<Lake>();
    private Stack<SimpleFeature> bufferedSimpleFeatures = new Stack<SimpleFeature>();
    private HashMap<SimpleFeature, Lake> lakeHashMap = new HashMap<SimpleFeature, Lake>();

    public LakeCollection(Collection<Lake> lakeList) {

        Validator.checkNullPointerPassed(lakeList);

        for (Lake lake : lakeList) {

            if (!add(lake)) {
                throw new ExceptionInInitializerError("Failed to initialize "
                        + LakeCollection.class);
            }
        }
    }

    public LakeCollection() {
    }

    public boolean add(Lake lake) {

        Validator.checkNullPointerPassed(lake);
        SimpleFeature bufferedLakeSimpleFeature = lake.getBufferedLakeSimpleFeature();
        Validator.checkNotInitialized(bufferedLakeSimpleFeature);

        if (lakes.add(lake)) {

            if (bufferedSimpleFeatures.add(bufferedLakeSimpleFeature)) {

                // returns null if there was no previous mapping
                if (lakeHashMap.put(bufferedLakeSimpleFeature, lake) == null) {

                    return true;

                } else {

                    lakes.pop();
                    bufferedSimpleFeatures.pop();
                    lakeHashMap.remove(bufferedLakeSimpleFeature);
                }

            } else {

                lakes.pop();
            }
        }

        return false;
    }

    public int getSize() {
        return lakes.size();
    }

    public Lake getLake(int index) throws IndexOutOfBoundsException {

        return this.lakes.get(index);
    }

    /**
     * Filters by argument geometry bounding box.
     */
    public LakeCollection filter(SimpleFeature simpleFeature) {

        try {

            Geometry simpleFeatureGeometry = (Geometry) simpleFeature
                    .getDefaultGeometry();

            SimpleFeatureCollection simpleFeatureCollectionToFilter = DataUtilities
                    .collection(this.bufferedSimpleFeatures);

            // filter
            SimpleFeatureCollection SimpleFeatureCollectionFiltered = Support
                    .filterByReferenceEnvelope(simpleFeatureGeometry,
                            simpleFeatureCollectionToFilter);

            LakeCollection lakeCollection = new LakeCollection();
            
            SimpleFeatureIterator iterator = SimpleFeatureCollectionFiltered
                    .features();
            try {

                while (iterator.hasNext()) {

                    SimpleFeature feature = iterator.next();
                    lakeCollection.add(this.lakeHashMap.get(feature));

                }

            } finally {
                iterator.close();
            }

            return lakeCollection;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
