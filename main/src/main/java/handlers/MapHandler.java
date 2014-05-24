package handlers;

import java.io.File;
import java.util.List;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.main.Validator;
import org.geotools.main.Support;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.MapPane;
import org.geotools.swing.event.MapMouseEventDispatcher;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.event.MapPaneListener;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.Envelope;

public class MapHandler implements MapPane {

    private MapContent mapContent;
    private JMapFrame mapFrame;

    public MapHandler(MapContent mapContent, JMapFrame jMapFrame) {

        this(mapContent);
        Validator.checkNullPointerPassed(jMapFrame);
        this.mapFrame = jMapFrame;
    }

    public MapHandler(MapContent mapContent) {

        Validator.checkNullPointerPassed(mapContent);
        this.mapContent = mapContent;
    }

    public List<Layer> getSelectedOrVisibleLayer(boolean layerMustBeSelected,
            boolean layerMustBeVisible) {

        List<Layer> layers = new ArrayList<Layer>();

        for (Layer layer : this.mapContent.layers()) {

            boolean canBeAddedToList = true;

            if (layerMustBeSelected) {
                canBeAddedToList = canBeAddedToList && layer.isSelected();
            }

            if (layerMustBeVisible) {
                canBeAddedToList = canBeAddedToList && layer.isVisible();
            }

            if (canBeAddedToList) {
                layers.add(layer);
            }
        }

        return layers;
    }

    public int exportSelectedAndVisible() {

        try {

            List<Layer> layerList = this.mapContent.layers();

            for (Layer layer : layerList) {

                if (layer.isSelected() && layer.isVisible()) {

                    File directory = Support.getFileDirectory();
                    String typeName = layer.getFeatureSource().getName()
                            .toString();

                    Support.exportToShapeFile(layer, typeName,
                            directory.getParentFile(), directory.getName());
                    JOptionPane.showMessageDialog(null, "Export successful",
                            "Export", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return 0;
    }

    public String getNameOfLayerToExport() {
        String name = JOptionPane.showInputDialog("Enter a layer name");
        return name;
    }

    public int getLayerCount() {

        if (mapContent != null) {

            return this.mapContent.layers().size();
        }
        return 0;
    }

    public int addLayerToMapContent(Layer layer) {

        Validator.checkNullPointerPassed(layer);
        Validator.checkNotInitialized(mapContent);

        try {

            return addLayerToMapContent((SimpleFeatureSource) layer
                    .getFeatureSource());

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;

    }

    public int addLayerToMapContent(Layer layer, boolean ignoreRepaint) {

        if (layer == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }
        if (mapContent == null) {
            throw new IllegalStateException("mapFrame must be initialized!");
        }
        if (mapFrame == null) {
            throw new IllegalStateException("mapFrame must be initialized!");
        }

        try {

            return addLayerToMapContent(
                    (SimpleFeatureSource) layer.getFeatureSource(),
                    ignoreRepaint);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;

    }

    public int addLayerToMapContent(SimpleFeatureSource simpleFeatureSource,
            boolean ignoreRepaint) {

        if (simpleFeatureSource == null) {
            throw new IllegalArgumentException("arguments must not be null!");
        }
        if (mapContent == null) {
            throw new IllegalStateException("mapFrame must be initialized!");
        }
        if (mapFrame == null) {
            throw new IllegalStateException("mapFrame must be initialized!");
        }

        try {

            Style style = SLD
                    .createSimpleStyle(simpleFeatureSource.getSchema());
            FeatureLayer layer = new FeatureLayer(simpleFeatureSource, style);
            layer.setVisible(ignoreRepaint);

            this.mapFrame.setIgnoreRepaint(ignoreRepaint);

            int result = (this.mapContent.addLayer(layer) ? 1 : 0);

            this.mapFrame.setIgnoreRepaint(!ignoreRepaint);

            return result;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public int addLayerToMapContent(SimpleFeatureSource simpleFeatureSource) {

        if (simpleFeatureSource == null || mapContent == null) {
            throw new IllegalArgumentException(
                    "simpleFeatureSource must not be null!");
        }

        try {

            Style style = SLD
                    .createSimpleStyle(simpleFeatureSource.getSchema());
            FeatureLayer layer = new FeatureLayer(simpleFeatureSource, style);
            layer.setVisible(true);
            // DataUtilities.simple(layer.getFeatureSource()).getBounds();
            // new layer
            // CRS.equalsIgnoreMetadata(layer.getFeatureSource().getSchema().getCoordinateReferenceSystem(),
            // layer.getBounds().getCoordinateReferenceSystem())
            // layer.getFeatureSource().getSchema().getCoordinateReferenceSystem();
            // layer.getBounds().getCoordinateReferenceSystem();
            // layer.getFeatureSource().getBounds();
            return (this.mapContent.addLayer(layer) ? 1 : 0);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public int removeLayerFromMapContent(int index) {

        try {

            this.mapContent.layers().remove(index);
            return 1;

        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            return 0;

        }
    }

    public int setLayerVisibility(int index, boolean isVisible) {

        try {

            this.mapContent.layers().get(index).setVisible(isVisible);
            return 1;

        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            return 0;

        }

    }

    public List<Layer> getLayers() {

        try {

            return this.mapContent.layers();

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return null;
    }

    public int removeAllLayersFromMapContent() {

        try {

            for (Layer layer : this.mapContent.layers()) {
                this.mapContent.removeLayer(layer);
            }

            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return 0;
    }

    public void addMapPaneListener(MapPaneListener arg0) {
        // TODO Auto-generated method stub

    }

    public void addMouseListener(MapMouseListener arg0) {
        // TODO Auto-generated method stub

    }

    public CursorTool getCursorTool() {
        // TODO Auto-generated method stub
        return null;
    }

    public ReferencedEnvelope getDisplayArea() {
        // TODO Auto-generated method stub
        return null;
    }

    public MapContent getMapContent() {

        return this.mapContent;
    }

    public MapMouseEventDispatcher getMouseEventDispatcher() {
        // TODO Auto-generated method stub
        return null;
    }

    public AffineTransform getScreenToWorldTransform() {
        // TODO Auto-generated method stub
        return null;
    }

    public AffineTransform getWorldToScreenTransform() {
        // TODO Auto-generated method stub
        return null;
    }

    public void moveImage(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void removeMapPaneListener(MapPaneListener arg0) {
        // TODO Auto-generated method stub

    }

    public void removeMouseListener(MapMouseListener arg0) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void setCursorTool(CursorTool arg0) {
        // TODO Auto-generated method stub

    }

    public void setDisplayArea(Envelope arg0) {
        // TODO Auto-generated method stub

    }

    public void setMapContent(MapContent arg0) {
        // TODO Auto-generated method stub

    }

    public void setMouseEventDispatcher(MapMouseEventDispatcher arg0) {
        // TODO Auto-generated method stub

    }

}
