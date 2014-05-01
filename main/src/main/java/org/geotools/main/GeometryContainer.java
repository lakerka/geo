package org.geotools.main;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.Layer;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryContainer {

    private GeometryDescriptor geometryDescriptor;
    private GeometryType geometryType;
    
    public GeometryContainer(SimpleFeatureSource simpleFeatureSource) {

        if (simpleFeatureSource == null) {
            throw new IllegalArgumentException("simpleFeatureSource must not be null!");
        }
        setSimpleFeatureSourceGeometryDescriptor(simpleFeatureSource);
    }
    
    public GeometryContainer(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }
        
        setSimpleFeatureSourceGeometryDescriptor( (SimpleFeatureSource)layer.getFeatureSource() );
    }


    private void setSimpleFeatureSourceGeometryDescriptor(
            SimpleFeatureSource simpleFeatureSource) {

        this.geometryDescriptor = simpleFeatureSource.getSchema()
                .getGeometryDescriptor();

        Class<?> clazz = this.geometryDescriptor.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz)
                || MultiPolygon.class.isAssignableFrom(clazz)) {
            geometryType = GeometryType.POLYGON;

        } else if (LineString.class.isAssignableFrom(clazz)
                || MultiLineString.class.isAssignableFrom(clazz)) {

            geometryType = GeometryType.LINE;

        } else {
            geometryType = GeometryType.POINT;
        }

    }

    public GeometryType getGeometryType() {
        return geometryType;
    }

    public GeometryDescriptor getGeometryDescriptor() {
        return this.geometryDescriptor;
    }

}
