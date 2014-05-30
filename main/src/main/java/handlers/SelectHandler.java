package handlers;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.DESedeKeySpec;
import javax.swing.RepaintManager;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.main.GeometryContainer;
import org.geotools.main.Main;
import org.geotools.main.MousePressAndReleasePoints;
import org.geotools.main.Support;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.geotools.map.Layer;

public class SelectHandler {

    private static final boolean MUST_BE_VISIBLE = true;
    private static final boolean MUST_BE_SELECTED = true;

    public static final int MIN_SELECTED_RECT_SIZE = 2;

    private JMapFrame mapFrame;
    private MapHandler mapHandler;

    // all selected features covering rectangle
    // used by zoom
    //is covering all features that are intersecting
    // with selected rectangle
    private ReferencedEnvelope selectedFeaturesReferencedEnvelope;
    
    //selected reference envelope covering only by selected rectangle
    private ReferencedEnvelope selectedRectangleReferencedEnvelope;

    private Set<SimpleFeature> selectedFeatures;

    public SelectHandler(JMapFrame mapFrame, MapHandler mapHandler) {

        this.mapFrame = mapFrame;
        this.mapHandler = mapHandler;
        this.selectedFeaturesReferencedEnvelope = new ReferencedEnvelope();
        this.selectedRectangleReferencedEnvelope = new ReferencedEnvelope();
        this.selectedFeatures = new HashSet<SimpleFeature>();
    }

    public void selectFeatures(
            List<MousePressAndReleasePoints> mousePressedRealWordlPointList,
            boolean keepOldSelected) {

        try {
            // prevent repainting multiple times after layers change
            this.mapFrame.getMapPane().setIgnoreRepaint(true);

            List<Rectangle> selectedRealWorldRectanglesList = getSelectedScreenRectanglesList(mousePressedRealWordlPointList);

            // get layers that we are interested in (selected)
            List<Layer> layersList = this.mapHandler.getSelectedOrVisibleLayer(
                    MUST_BE_SELECTED, MUST_BE_VISIBLE);

            if (!keepOldSelected) {

                // clear all previously selected selected features covering
                // rectangle
                setToNullSelectedReferencedEnvelope();
                selectedRectangleReferencedEnvelope.setToNull();

                // clear all previously selected selected features if needed
                selectedFeatures.clear();
            }

            FilterFactory2 filterFactory2 = CommonFactoryFinder
                    .getFilterFactory2();

            for (int layerIndex = 0; layerIndex < layersList.size(); layerIndex++) {

                if (layerIndex == layersList.size() - 1) {
                    this.mapFrame.getMapPane().setIgnoreRepaint(false);
                }

                Layer layer = layersList.get(layerIndex);

                GeometryContainer geometryContainer = new GeometryContainer(
                        layer);

                SimpleFeatureSource simpleFeatureSource = (SimpleFeatureSource) layer
                        .getFeatureSource();

                Set<FeatureId> IDs = new HashSet<FeatureId>();

                appendSimpleFeatureIDs(
                        IDs,
                        new ArrayList<SimpleFeature>(
                                getFeaturesBelongingToLayer(layer,
                                        this.selectedFeatures)));

                for (Rectangle selectedRealWorldRectangle : selectedRealWorldRectanglesList) {

                    ReferencedEnvelope realWorldBoundingRectangle = rectangleToReferenceEnvelope(selectedRealWorldRectangle);

                    selectedRectangleReferencedEnvelope.expandToInclude(realWorldBoundingRectangle);
                    
                    Filter filter = filterFactory2.bbox(filterFactory2
                            .property(geometryContainer.getGeometryDescriptor()
                                    .getLocalName()),
                            realWorldBoundingRectangle);

                    SimpleFeatureCollection simpleFeatureCollection = simpleFeatureSource
                            .getFeatures(filter);

                    //if nothing was chosen then deselect all
                    if (DataUtilities.list(simpleFeatureCollection).isEmpty()) {
                        
                        deselectAll();
                        return;
                    }

                    expandSelectedReferenceEnvelope(simpleFeatureCollection);

                    addToSelectedFeatures(simpleFeatureCollection);

                    appendSimpleFeatureIDs(IDs, simpleFeatureCollection);

                }

                if (IDs.isEmpty()) {

                    System.out.println("   no feature selected");

                }
                for (FeatureId id : IDs) {
                    System.out.println("Selected: " + id.toString());

                }

                changeStyle(IDs, geometryContainer, layer);
            }

        } catch (Exception exception) {

            exception.printStackTrace();

        }
    }

    public int expandSelectedReferenceEnvelope(
            SimpleFeatureCollection simpleFeatureeCollection) {

        if (simpleFeatureeCollection == null) {

            throw new IllegalArgumentException(
                    "simpleFeatureeCollection must not be null!");
        }

        try {

            ReferencedEnvelope referencedEnvelope = simpleFeatureeCollection
                    .getBounds();

            if (this.selectedFeaturesReferencedEnvelope == null) {

                setSelectedReferencedEnvelope(referencedEnvelope);

            } else {

                this.selectedFeaturesReferencedEnvelope
                        .expandToInclude(referencedEnvelope);

            }
            return 1;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return 0;
    }

    public ReferencedEnvelope getSelectedFeaturesReferencedEnvelope() {
        return selectedFeaturesReferencedEnvelope;
    }
    
    public ReferencedEnvelope getSelectedRectangleReferenceEnvelope() {
        return selectedRectangleReferencedEnvelope;
    }

    public int setToNullSelectedReferencedEnvelope() {

        try {

            this.selectedFeaturesReferencedEnvelope.setToNull();
            return 1;

        } catch (NullPointerException nullPointerException) {

            return 0;
        }
    }

    public void setSelectedReferencedEnvelope(
            ReferencedEnvelope selectedReferencedEnvelope) {

        if (selectedReferencedEnvelope == null) {

            throw new IllegalArgumentException(
                    "simpleFeatureeCollection must not be null!");
        }

        this.selectedFeaturesReferencedEnvelope = selectedReferencedEnvelope;
    }

    // returns list of selected screen rectangles
    private List<Rectangle> getSelectedScreenRectanglesList(
            List<MousePressAndReleasePoints> mousePressedAndReleasePointsList) {

        List<Rectangle> selectedScreenRectangles = new ArrayList<Rectangle>();

        for (MousePressAndReleasePoints mousePressePressAndReleasePoints : mousePressedAndReleasePointsList) {

            Rectangle selectedScreenRectangle = formRectangleFromTwoPoints(
                    mousePressePressAndReleasePoints
                            .getMousePressedScreenPoint(),
                    mousePressePressAndReleasePoints
                            .getMouseReleasedScreenPoint());

            selectedScreenRectangle.width = decideSelectedRectangleWidth(selectedScreenRectangle.width);
            selectedScreenRectangle.height = decideSelectedRectangleHeight(selectedScreenRectangle.height);

            selectedScreenRectangles.add(selectedScreenRectangle);
        }
        return selectedScreenRectangles;
    }

    public BoundingBox getSelectedBoundingBox() {
        return this.selectedFeaturesReferencedEnvelope;
    }

    /**
     * Sets the display to paint selected features with some style and
     * unselected features in the default style.
     * 
     * @param IDs
     *            identifiers of currently selected features
     */
    public void changeStyle(Set<FeatureId> selectedFeaturesIDs,
            GeometryContainer geometryContainer, Layer layer) {

        Style style;

        if (selectedFeaturesIDs.isEmpty()) {

            style = StyleHandler.createDefaultStyle(geometryContainer);

        } else {
            style = StyleHandler.createSelectedStyle(selectedFeaturesIDs,
                    geometryContainer);
        }

        ((FeatureLayer) layer).setStyle(style);
    }

    public ReferencedEnvelope getRealWorldRectangle(
            Rectangle selectedScreenRectangle,
            CoordinateReferenceSystem coordinateReferenceSystem) {

        if (selectedScreenRectangle == null) {
            throw new IllegalArgumentException(
                    "selectedScreenRectangle must not be null!");
        }

        AffineTransform screenToWorld = this.mapFrame.getMapPane()
                .getScreenToWorldTransform();

        Rectangle2D selectedWorldRectangle = screenToWorld
                .createTransformedShape(selectedScreenRectangle).getBounds2D();

        ReferencedEnvelope referenceEnvelope = new ReferencedEnvelope(
                selectedWorldRectangle, mapFrame.getMapContent()
                        .getCoordinateReferenceSystem());

        ReferencedEnvelope realWorldRectangle = new ReferencedEnvelope(
                referenceEnvelope.getMinX(), referenceEnvelope.getMaxX(),
                referenceEnvelope.getMinY(), referenceEnvelope.getMaxY(),
                coordinateReferenceSystem);

        return realWorldRectangle;
    }

    public ReferencedEnvelope rectangleToReferenceEnvelope(Rectangle rectangle) {

        if (rectangle == null) {
            throw new IllegalArgumentException(
                    "selectedScreenRectangle must not be null!");
        }

        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(
                rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(),
                rectangle.getMaxY(), null);

        return referencedEnvelope;
    }

    public static Rectangle formRectangleFromTwoPoints(
            DirectPosition2D mousePressedScreenPoint,
            DirectPosition2D mouseReleasedScreenPoint) {

        if (mousePressedScreenPoint == null) {
            throw new IllegalArgumentException(
                    "mousePressedScreenPoint must not be null!");
        }
        if (mouseReleasedScreenPoint == null) {
            throw new IllegalArgumentException(
                    "mouseReleasedScreenPoint must not be null!");
        }

        DirectPosition2D upperLeftRectanglePoint = Support.getUpperLeftPoint(
                mousePressedScreenPoint, mouseReleasedScreenPoint);

        Double rectangleWidth = Support.getRectangleWidth(
                upperLeftRectanglePoint, mousePressedScreenPoint,
                mouseReleasedScreenPoint);

        Double rectangleHeight = Support.getRectangleHeight(
                upperLeftRectanglePoint, mousePressedScreenPoint,
                mouseReleasedScreenPoint);

        // rounding used to get int after Double.intValues is used
        rectangleWidth += 0.5;
        rectangleHeight += 0.5;

        Rectangle selectedScreenRectangle = new Rectangle(
                (int) upperLeftRectanglePoint.x,
                (int) upperLeftRectanglePoint.y, rectangleWidth.intValue(),
                rectangleHeight.intValue());

        return selectedScreenRectangle;
    }

    public static int decideSelectedRectangleWidth(int rectangleWidth) {

        return Math.max(rectangleWidth, MIN_SELECTED_RECT_SIZE);
    }

    public static int decideSelectedRectangleHeight(int rectangleHeight) {

        return Math.max(rectangleHeight, MIN_SELECTED_RECT_SIZE);
    }

    private int addToSelectedFeatures(
            SimpleFeatureCollection simpleFeatureCollection) {

        try {

            this.selectedFeatures.addAll(DataUtilities
                    .list(simpleFeatureCollection));
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int setSelectedFeatures(Collection<SimpleFeature> simpleFeaturesList) {

        try {

            this.selectedFeatures.clear();
            this.selectedFeatures.addAll(simpleFeaturesList);

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Set<SimpleFeature> getSelectedFeatures() {

        return this.selectedFeatures;
    }

    private Set<FeatureId> getSImpleFeatureIDs(
            SimpleFeatureCollection simpleFeatureCollection) {

        List<SimpleFeature> simpleFeatureList = DataUtilities
                .list(simpleFeatureCollection);

        return getSImpleFeatureIDs(simpleFeatureList);
    }

    private Set<FeatureId> getSImpleFeatureIDs(
            List<SimpleFeature> simpleFeatureList) {

        Set<FeatureId> IDs = new HashSet<FeatureId>();

        for (SimpleFeature simpleFeature : simpleFeatureList) {

            IDs.add(simpleFeature.getIdentifier());

        }

        return IDs;
    }

    private void appendSimpleFeatureIDs(Set<FeatureId> IDs,
            List<SimpleFeature> simpleFeatureList) {

        for (SimpleFeature simpleFeature : simpleFeatureList) {

            IDs.add(simpleFeature.getIdentifier());

        }
    }

    private void appendSimpleFeatureIDs(Set<FeatureId> IDs,
            SimpleFeatureCollection simpleFeatureCollection) {

        List<SimpleFeature> simpleFeatureList = DataUtilities
                .list(simpleFeatureCollection);

        appendSimpleFeatureIDs(IDs, simpleFeatureList);
    }

    public int selectByFeatures(List<SimpleFeature> simpleFeaturesList) {

        try {

            // get layers that we are interested in (selected)
            List<Layer> layersList = getLayersOpenForSelect();

            Collection<SimpleFeature> collectionOfSimpleFeatures = (Collection<SimpleFeature>) simpleFeaturesList;
            SimpleFeatureCollection simpleFeaturesCollection = DataUtilities
                    .collection(simpleFeaturesList);

            for (int layerIndex = 0; layerIndex < layersList.size(); layerIndex++) {

                Layer layer = layersList.get(layerIndex);

                SimpleFeatureCollection layerFeatureCollection = ((SimpleFeatureSource) layer
                        .getFeatureSource()).getFeatures();

                if (layerFeatureCollection
                        .containsAll(collectionOfSimpleFeatures)) {

                    GeometryContainer geometryContainer = new GeometryContainer(
                            layer);

                    Set<FeatureId> IDs = new HashSet<FeatureId>();

                    appendSimpleFeatureIDs(
                            IDs,
                            new ArrayList<SimpleFeature>(
                                    getFeaturesBelongingToLayer(layer,
                                            this.selectedFeatures)));

                    appendSimpleFeatureIDs(IDs, simpleFeaturesList);

                    changeStyle(IDs, geometryContainer, layer);

                    expandSelectedReferenceEnvelope(simpleFeaturesCollection);

                    addToSelectedFeatures(simpleFeaturesCollection);

                    this.mapFrame.repaint();

                    return 1;
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public Set<SimpleFeature> getFeaturesBelongingToLayer(Layer layer,
            Collection<SimpleFeature> simpleFeatureCollection) {

        try {

            Set<SimpleFeature> simpleFeaturesSet = new HashSet<SimpleFeature>();

            SimpleFeatureCollection layerFeaturesCollection = ((SimpleFeatureSource) layer
                    .getFeatureSource()).getFeatures();

            for (SimpleFeature simpleFeature : simpleFeatureCollection) {

                if (layerFeaturesCollection.contains(simpleFeature)) {

                    simpleFeaturesSet.add(simpleFeature);
                }
            }

            return simpleFeaturesSet;

        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return null;
    }

    public List<Layer> getLayersOpenForSelect() {

        try {
            return this.mapHandler.getSelectedOrVisibleLayer(MUST_BE_SELECTED,
                    MUST_BE_VISIBLE);

        } catch (Exception exception) {

            // something went wrong
            exception.printStackTrace();

        }
        return null;
    }

    public int deselectAll() {

        try {
            
            this.selectedFeatures.clear();
            this.selectedFeaturesReferencedEnvelope = new ReferencedEnvelope();

            // get layers that we are interested in (selected)
            List<Layer> layersList = this.mapHandler.getSelectedOrVisibleLayer(
                    MUST_BE_SELECTED, MUST_BE_VISIBLE);
            
         // prevent repainting multiple times after layers change
            this.mapFrame.getMapPane().setIgnoreRepaint(true);
            
            for (int layerIndex = 0; layerIndex < layersList.size(); layerIndex++) {
                
                //if this is last layer allow repaint, since it will be done only once
                if (layerIndex == layersList.size() - 1) {
                    this.mapFrame.getMapPane().setIgnoreRepaint(false);
                }
                
                Layer layer = layersList.get(layerIndex);

                GeometryContainer geometryContainer = new GeometryContainer(
                        layer);

                SimpleFeatureSource simpleFeatureSource = (SimpleFeatureSource) layer
                        .getFeatureSource();

                Set<FeatureId> IDs = new HashSet<FeatureId>();
                
                changeStyle(IDs, geometryContainer, layer);
            }
            return 1;
            
        } catch (NullPointerException e) {

            e.printStackTrace();
        }
        
        return 0;
    }
}
