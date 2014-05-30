package org.geotools.main;

import java.text.SimpleDateFormat;
import java.util.List;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes.Name;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.FactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.process.vector.TransformProcess;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

public class Support {

    public static SimpleFeatureSource loadShapeFile() {

        try {

            File file = JFileDataStoreChooser.showOpenFile(".shp",
                    new java.io.File("."), null);
            return loadShapeFile(file);

        } catch (Exception exception) {

            if (!(exception instanceof IOException)
                    && !(exception instanceof NullPointerException)) {

                exception.printStackTrace();

            }

            return null;
        }
    }

    public static SimpleFeatureSource loadShapeFile(String filePath) {

        try {

            File file = new File(filePath);
            return loadShapeFile(file);

        } catch (Exception exception) {

            if (!(exception instanceof IOException)
                    && !(exception instanceof NullPointerException)) {

                exception.printStackTrace();

            }

            return null;
        }
    }

    public static Layer loadLayer(String filePath) {

        try {

            File file = new File(filePath);
            return Support.simpleFeatureSourceToLayer(loadShapeFile(file));

        } catch (Exception exception) {

            if (!(exception instanceof IOException)
                    && !(exception instanceof NullPointerException)) {

                exception.printStackTrace();

            }

            return null;
        }
    }

    public static SimpleFeatureSource loadShapeFile(File file) {

        FileDataStore store = null;
        try {

            store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();

            return featureSource;

        } catch (Exception exception) {

            if (!(exception instanceof IOException)
                    && !(exception instanceof NullPointerException)) {

                exception.printStackTrace();

            }

            return null;
        }
    }

    public static Point getUpperLeftPoint(Point a, Point b) {

        DirectPosition2D aDirectPosition2D = awtPointToDirectPosition2D(a);
        DirectPosition2D bDirectPosition2D = awtPointToDirectPosition2D(b);

        DirectPosition2D upperLeftPointDirectPosition2D = getUpperLeftPoint(
                aDirectPosition2D, bDirectPosition2D);

        Point upperLeftPoint = new Point(
                (int) upperLeftPointDirectPosition2D.getX(),
                (int) upperLeftPointDirectPosition2D.getY());

        return upperLeftPoint;
    }

    public static DirectPosition2D getUpperLeftPoint(DirectPosition2D a,
            DirectPosition2D b) {

        DirectPosition2D upperLeftPoint = new DirectPosition2D(Math.min(a.x,
                b.x), Math.min(a.y, b.y));
        return upperLeftPoint;
    }

    public static Double getRectangleWidth(Point upperLeftPoint, Point a,
            Point b) {

        DirectPosition2D upperLeftPointDirectPosition2D = awtPointToDirectPosition2D(upperLeftPoint);
        DirectPosition2D aDirectPosition2D = awtPointToDirectPosition2D(a);
        DirectPosition2D bDirectPosition2D = awtPointToDirectPosition2D(b);

        Double length = getRectangleWidth(upperLeftPointDirectPosition2D,
                aDirectPosition2D, bDirectPosition2D);

        return length;
    }

    public static Double getRectangleWidth(DirectPosition2D upperLeftPoint,
            DirectPosition2D a, DirectPosition2D b) {

        Double length = (double) Math.max(Math.abs(upperLeftPoint.x - a.x),
                Math.abs(upperLeftPoint.x - b.x));
        return Math.abs(length);
    }

    public static Double getRectangleHeight(Point upperLeftPoint, Point a,
            Point b) {

        DirectPosition2D upperLeftPointDirectPosition2D = awtPointToDirectPosition2D(upperLeftPoint);
        DirectPosition2D aDirectPosition2D = awtPointToDirectPosition2D(a);
        DirectPosition2D bDirectPosition2D = awtPointToDirectPosition2D(b);

        Double height = getRectangleWidth(upperLeftPointDirectPosition2D,
                aDirectPosition2D, bDirectPosition2D);

        return height;
    }

    public static Double getRectangleHeight(DirectPosition2D upperLeftPoint,
            DirectPosition2D a, DirectPosition2D b) {

        Double height = (double) Math.max(Math.abs(upperLeftPoint.y - a.y),
                Math.abs(upperLeftPoint.y - b.y));
        return Math.abs(height);
    }

    public static DirectPosition2D awtPointToDirectPosition2D(Point point) {

        if (point == null) {
            throw new IllegalArgumentException("point must not be null!");
        }

        return new DirectPosition2D(point.getX(), point.getY());

    }

    public static List<SimpleFeature> simpleFeatureCollectionToSimpleFeatureList(
            SimpleFeatureCollection simpleFeatureeCollection) {

        if (simpleFeatureeCollection == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureeCollection must not be null");
        }

        List<SimpleFeature> simpleFeatureList;
        SimpleFeatureIterator iterator = null;

        try {

            simpleFeatureList = new ArrayList<SimpleFeature>();

            iterator = simpleFeatureeCollection.features();

            while (iterator.hasNext()) {

                SimpleFeature simpleFeature = iterator.next();

                simpleFeatureList.add(simpleFeature);
            }

        } catch (Exception e) {

            simpleFeatureList = null;
            e.printStackTrace();

        } finally {

            if (iterator != null) {

                iterator.close();
            }
        }

        return simpleFeatureList;
    }

    public static List<SimpleFeature> layerToSimpleFeatureList(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }

        try {

            SimpleFeatureCollection simpleFeatureCollection = (SimpleFeatureCollection) layer
                    .getFeatureSource().getFeatures();

            return Support
                    .simpleFeatureCollectionToSimpleFeatureList(simpleFeatureCollection);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    public static SimpleFeatureCollection layerToSimpleFeatureCollection(
            Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }

        try {

            SimpleFeatureCollection simpleFeatureCollection = (SimpleFeatureCollection) layer
                    .getFeatureSource().getFeatures();

            return simpleFeatureCollection;

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    public static List<Geometry> simpleFeatureListToGeometryList(
            List<SimpleFeature> simpleFeatureList) {

        if (simpleFeatureList == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureList must not be null!");
        }

        try {
            List<Geometry> geometryList = new ArrayList<Geometry>();

            for (SimpleFeature simpleFeature : simpleFeatureList) {
                geometryList.add((Geometry) simpleFeature.getDefaultGeometry());
            }
            return geometryList;

        } catch (Exception e) {

            e.printStackTrace();
        }
        // something went wrong
        return null;
    }

    public static SimpleFeatureSource layerToSimpleFeatureSource(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            return (SimpleFeatureSource) layer.getFeatureSource();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static Layer simpleFeatureSourceToLayer(
            SimpleFeatureSource simpleFeatureSource) {

        if (simpleFeatureSource == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureSource must not be null!");
        }

        try {

            Style style = SLD
                    .createSimpleStyle(simpleFeatureSource.getSchema());
            Layer layer = new FeatureLayer(simpleFeatureSource, style);
            layer.setVisible(false);

            return layer;

        } catch (Exception e) {

            e.printStackTrace();
        }

        // something went wrongs
        return null;

    }

    public static ShapefileDataStore simpleFeatureSourceToShapefileDataStore(
            SimpleFeatureSource simpleFeatureSource) {

        if (simpleFeatureSource == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureSource must not be null!");
        }

        try {

            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) simpleFeatureSource
                    .getDataStore();

            return shapefileDataStore;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static ShapefileDataStore layerToShapeFileDataStore(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            SimpleFeatureSource simpleFeatureSource = Support
                    .layerToSimpleFeatureSource(layer);

            return Support
                    .simpleFeatureSourceToShapefileDataStore(simpleFeatureSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataStore layerToDataStore(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            SimpleFeatureSource simpleFeatureSource = Support
                    .layerToSimpleFeatureSource(layer);

            return (DataStore) simpleFeatureSource.getDataStore();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataStore exportToShapeFile(
            SimpleFeatureCollection simpleFeatureCollection, String typeName,
            File directory, String fileName) {

        try {

            SimpleFeatureSource simpleFeatureSource = DataUtilities
                    .source(simpleFeatureCollection);
            return Support.exportToShapefile(simpleFeatureSource, typeName,
                    directory, fileName);

        } catch (IOException ioException) {

            ioException.printStackTrace();
        }
        return null;

    }

    public static DataStore exportToShapeFile(Layer layer, String typeName,
            File directory, String fileName) {

        try {

            SimpleFeatureSource simpleFeatureSource = Support
                    .layerToSimpleFeatureSource(layer);
            return Support.exportToShapefile(simpleFeatureSource, typeName,
                    directory, fileName);

        } catch (IOException ioException) {

            ioException.printStackTrace();
        }
        return null;

    }

    // public static DataStore exportToShapeFile(List<SimpleFeature>
    // simpleFeatureList, String typeName,
    // File directory) {
    //
    // try {
    // SimpleFeatureCollection simpleFeatureCollection =
    // DataUtilities.collection(simpleFeatureList);
    // MemoryDataStore memoryDataStore = new MemoryDataStore(
    // simpleFeatureCollection);
    // return Support.exportToShapefile(memoryDataStore, typeName,
    // directory);
    //
    // } catch (IOException ioException) {
    //
    // ioException.printStackTrace();
    // }
    // return null;
    //
    // }

    public static DataStore exportToShapefile(
            SimpleFeatureSource featureSource, String schemaTypeName,
            File directory, String fileName) throws IOException {

        SimpleFeatureType ft = featureSource.getSchema();

        if (!(ft instanceof SimpleFeatureTypeImpl)) {
            return null;
        }

        SimpleFeature simpleFeature = featureSource.getFeatures().features()
                .next();
        SimpleFeatureType type = simpleFeature.getFeatureType();

        SimpleFeatureTypeImpl simpleFeatureTypeImpl = new SimpleFeatureTypeImpl(
                (new NameImpl(fileName)), type.getAttributeDescriptors(),
                featureSource.getSchema().getGeometryDescriptor(),
                type.isAbstract(), type.getRestrictions(), type.getSuper(),
                type.getDescription());

        File file = new File(directory, fileName + ".shp");

        Map<String, java.io.Serializable> creationParams = new HashMap<String, java.io.Serializable>();
        creationParams.put("url", DataUtilities.fileToURL(file));

        FileDataStoreFactorySpi factory = FileDataStoreFinder
                .getDataStoreFactory("shp");
        DataStore dataStore = factory.createNewDataStore(creationParams);

        dataStore.createSchema(simpleFeatureTypeImpl);

        // The following workaround to write out the prj is no longer needed
        // ((ShapefileDataStore)dataStore).forceSchemaCRS(ft.getCoordinateReferenceSystem());

        SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore
                .getFeatureSource(fileName);

        Transaction t = new DefaultTransaction();
        try {
            SimpleFeatureCollection collection = featureSource.getFeatures(); // grab
                                                                              // all
                                                                              // features
            featureStore.addFeatures(collection);
            t.commit(); // write it out
        } catch (IOException eek) {
            eek.printStackTrace();
            try {
                t.rollback();
            } catch (IOException doubleEeek) {
                // rollback failed?
            }
        } finally {
            t.close();
        }
        return dataStore;
    }

    public static File getFileDirectory() {

        try {

            File file = JFileDataStoreChooser.showOpenFile(".shp",
                    new java.io.File("."), null);
            return file;

        } catch (Exception exception) {

            if (!(exception instanceof IOException)
                    && !(exception instanceof NullPointerException)) {

                exception.printStackTrace();

            }

            return null;
        }
    }

    public static Layer simpleFeatureToLayer(SimpleFeature simpleFeature) {

        if (simpleFeature == null) {
            throw new IllegalArgumentException("simpleFeature must not null");
        }

        try {

            List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
            simpleFeatureList.add(simpleFeature);

            SimpleFeatureCollection simpleFeatureCollection = DataUtilities
                    .collection(simpleFeatureList);

            return Support
                    .simpleFeatureCollectionToLayer(simpleFeatureCollection);

        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return null;
    }

    public static Layer simpleFeatureListToLayer(
            List<SimpleFeature> simpleFeatureList) {

        if (simpleFeatureList == null || simpleFeatureList.isEmpty()) {
            throw new IllegalArgumentException(
                    "simpleFeatureList must not be null or empty");
        }

        try {

            SimpleFeatureCollection simpleFeatureCollection = DataUtilities
                    .collection(simpleFeatureList);

            return Support
                    .simpleFeatureCollectionToLayer(simpleFeatureCollection);

        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return null;
    }

    public static Layer simpleFeatureCollectionToLayer(
            SimpleFeatureCollection simpleFeatureCollection) {

        if (simpleFeatureCollection == null
                || simpleFeatureCollection.isEmpty()) {
            throw new IllegalArgumentException(
                    "simpleFeatureCollection must not be null or empty");
        }

        try {

            SimpleFeatureSource simpleFeatureSource = DataUtilities
                    .source(simpleFeatureCollection);

            Layer layer = Support
                    .simpleFeatureSourceToLayer(simpleFeatureSource);

            return layer;

        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return null;
    }

    public static String getLayerName(Layer layer) {

        try {

            String layerName;

            if (layer.getTitle() != null && !layer.getTitle().isEmpty()) {

                layerName = layer.getTitle();

            } else {

                layerName = layer.getFeatureSource().getName().toString();
            }

            return layerName;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return null;
    }

    public static boolean isPolygonOrMultiPolygonOrLineOrMultiLine(
            Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiPolygon.class,
                Polygon.class, MultiLineString.class, LineString.class);

    }

    public static boolean isPolygonOrMultiPolygon(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiPolygon.class, Polygon.class);

    }

    public static boolean isPolygon(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, Polygon.class);

    }

    public static boolean isLineOrMultiLine(Class geometryType) {

        if (geometryType == null) {
            throw new IllegalArgumentException("geometryType must not be null!");
        }

        return isGeometryTypeIn(geometryType, MultiLineString.class,
                LineString.class);

    }

    @SuppressWarnings("unchecked")
    public static boolean isGeometryTypeIn(
            @SuppressWarnings("rawtypes") Class test,
            @SuppressWarnings("rawtypes") Class... targets) {

        if (targets == null) {
            throw new IllegalArgumentException("targets must not be null!");
        }

        for (@SuppressWarnings("rawtypes")
        Class target : targets) {

            if (target.isAssignableFrom(test)) {
                return true;
            }
        }
        return false;
    }

    // validate and combine geometries
    public static Geometry combineIntoOneGeometry(
            Collection<Geometry> collectionGeometry) {

        try {

            collectionGeometry = validateGeometryCollection(collectionGeometry);
            // List<SimpleFeature> combinedGeometryList =
            // Support.recursiveCombine(collectionGeometry);
            // collectionGeometry =

            // old code
            GeometryFactory factory = new GeometryFactory();

            // note the following geometry collection may be invalid (say
            // with
            // overlapping polygons)
            GeometryCollection geometryCollection = (GeometryCollection) factory
                    .buildGeometry(collectionGeometry);

            Geometry combinedGeometry = geometryCollection.union();

            // new code
            // Geometry combinedGeometry = null;
            //
            // for (Iterator<Geometry> iterator = collectionGeometry.iterator();
            // iterator
            // .hasNext();) {
            //
            // Geometry geometry = iterator.next();
            //
            // if (geometry == null) {
            // continue;
            // }
            //
            // if (combinedGeometry == null) {
            //
            // combinedGeometry = (Geometry) geometry.clone();
            //
            // } else {
            //
            // Geometry tmpCombinedGeometry = GeometryCombiner.combine(
            // combinedGeometry, geometry);
            //
            //
            // if (tmpCombinedGeometry.isValid()) {
            //
            // combinedGeometry = tmpCombinedGeometry;
            //
            // }
            // }
            // }

            // // new code 2
            // GeometryCombiner g = new GeometryCombiner(collectionGeometry);
            // Geometry combinedGeometry = g.combine();

            // new code 3
            // GeometryFactory factory = new GeometryFactory();
            //
            // GeometryCollection geometryCollection = (GeometryCollection)
            // factory
            // .buildGeometry(collectionGeometry);
            //
            // Geometry combinedgeometry = geometryCollection.buffer(0);

            return combinedGeometry;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // validates and filters list of geometries
    public static List<Geometry> validateGeometryList(
            List<Geometry> geometryList) {

        if (geometryList == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            Collection<Geometry> collectionGeometry = new ArrayList<Geometry>(
                    geometryList);

            Collection<Geometry> validCollectionGeometry = Support
                    .validateGeometryCollection(collectionGeometry);

            List<Geometry> validGeometryList = new ArrayList<Geometry>(
                    validCollectionGeometry);

            return validGeometryList;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // validates and filters collection of geometries
    public static Collection<Geometry> validateGeometryCollection(
            Collection<Geometry> collectionGeometry) {

        if (collectionGeometry == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            List<Geometry> geometryList = new ArrayList<Geometry>();

            for (Iterator<Geometry> iterator = collectionGeometry.iterator(); iterator
                    .hasNext();) {

                Geometry geometry = iterator.next();

                geometry = Support.validateGeometry(geometry);

                if (geometry == null) {

                    continue;

                } else {

                    geometryList.add(geometry);

                }

            }

            Collection<Geometry> validCollectionGeometry = new ArrayList<Geometry>(
                    geometryList);

            return validCollectionGeometry;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // validates geometry, return null on invalid geometry
    public static Geometry validateGeometry(Geometry geometry) {

        try {

            if (!geometry.isValid()) {

                geometry.buffer(0);

                // ignore invalid geometries
                if (!geometry.isValid()) {
                    return null;
                }

            }

            return geometry;

        } catch (Exception e) {

            return null;
        }

    }

    public static SimpleFeatureCollection addAreaColumn(
            SimpleFeatureCollection simpleFeatureCollection, String columnName) {

        if (simpleFeatureCollection == null || columnName.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments!");
        }

        try {

            String transform = Support
                    .getSimpleFeatureTypeString(simpleFeatureCollection
                            .getSchema());

            String geometryLocalName = simpleFeatureCollection.getSchema()
                    .getGeometryDescriptor().getLocalName();

            transform = transform + "\n" + columnName + "=" + "area("
                    + geometryLocalName + " )";

            TransformProcess process = new TransformProcess();

            SimpleFeatureCollection resultFeatureCollection = process.execute(
                    simpleFeatureCollection, transform);

            return resultFeatureCollection;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;

    }

    public static String getSimpleFeatureTypeString(
            SimpleFeatureType simpleFeatureType) {

        if (simpleFeatureType == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureType must not be null!");
        }

        try {

            List<AttributeDescriptor> attributeDescriptorList = simpleFeatureType
                    .getAttributeDescriptors();

            String simpleFeatureTypeString = new String("");

            for (int i = 0; i < attributeDescriptorList.size(); i++) {

                AttributeDescriptor attributeDescriptor = attributeDescriptorList
                        .get(i);

                String localName = attributeDescriptor.getLocalName();

                if (i != 0) {
                    simpleFeatureTypeString += "\n";
                }

                simpleFeatureTypeString = simpleFeatureTypeString + localName
                        + "=" + localName;
            }

            return simpleFeatureTypeString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int setAttributeValue(SimpleFeature simpeFeature,
            String attributeName, Object attributeValue) {

        if (simpeFeature == null || attributeName == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            simpeFeature.setAttribute(attributeName, attributeValue);

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Object[][] listOfListToArrayOfArray(List<List<Object>> data) {

        if (data == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }

        try {

            Object[][] dataArray = new Object[data.size()][];
            for (int i = 0; i < dataArray.length; i++) {
                dataArray[i] = data.get(i).toArray();
            }

            return dataArray;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static SimpleFeatureCollection filterByReferenceEnvelope(
            Geometry geometry, SimpleFeatureCollection simpleFeatureCollection) {

        Validator.checkNullPointerPassed(geometry, simpleFeatureCollection);

        try {

            ReferencedEnvelope referencedEnvelope = JTS.toEnvelope(geometry);

            // filter villages by bounding box
            SimpleFeatureCollection filteredCollection = filterByReferenceEnvelope(
                    simpleFeatureCollection, referencedEnvelope);

            return filteredCollection;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SimpleFeatureCollection filterByReferenceEnvelope(
            SimpleFeatureCollection simpleFeatureCollection,
            ReferencedEnvelope referencedEnvelope) {

        Validator.checkNullPointerPassed(simpleFeatureCollection,
                referencedEnvelope);
        try {

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

    public static List<SimpleFeature> getFilteredFeaturesList(
            SimpleFeatureCollection simpleFeatureCollection, String cqlPredicate) {

        Validator.checkNullPointerPassed(simpleFeatureCollection, cqlPredicate);
        if (cqlPredicate.isEmpty()) {
            throw new IllegalArgumentException("Empty cql!");
        }

        try {

            return DataUtilities
                    .list(getFilteredFeaturesSimpleFeatureCollection(
                            simpleFeatureCollection, cqlPredicate));

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return null;

    }

    public static SimpleFeatureCollection getFilteredFeaturesSimpleFeatureCollection(
            SimpleFeatureCollection simpleFeatureCollection, String cqlPredicate) {

        Validator.checkNullPointerPassed(simpleFeatureCollection, cqlPredicate);
        if (cqlPredicate.isEmpty()) {
            throw new IllegalArgumentException("Empty cql!");
        }

        try {

            Filter filter = CQL.toFilter(cqlPredicate);
            SimpleFeatureCollection features = simpleFeatureCollection
                    .subCollection(filter);
            return features;

        } catch (Exception exception) {

            exception.printStackTrace();

        }
        return null;
    }

    public static List<SimpleFeature> getFilteredFeaturesList(
            SimpleFeatureCollection simpleFeatureCollection, Filter filter) {

        Validator.checkNullPointerPassed(simpleFeatureCollection, filter);

        try {

            return DataUtilities
                    .list(getFilteredFeaturesSimpleFeatureCollection(
                            simpleFeatureCollection, filter));

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return null;

    }

    public static SimpleFeatureCollection getFilteredFeaturesSimpleFeatureCollection(
            SimpleFeatureCollection simpleFeatureCollection, Filter filter) {

        Validator.checkNullPointerPassed(simpleFeatureCollection, filter);

        try {

            SimpleFeatureCollection features = simpleFeatureCollection
                    .subCollection(filter);
            return features;

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return null;

    }

}
