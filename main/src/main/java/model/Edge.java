package model;

import java.util.Arrays;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.main.Validator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Edge {

    private LineString lineString;

    public Edge(Coordinate from, Coordinate to, Polygon obstacle) {

        this.lineString = constructPath(from, to, obstacle);
    }

    public LineString constructPath(Coordinate from, Coordinate to,
            Polygon obstacle) {

        Validator.checkNullPointerPassed(from, to);

        try {

            if (obstacle == null) {

                return getDirectPath(from, to);

            }

            LineString directPath = getDirectPath(from, to);

            if (directPath.intersects(obstacle)) {

                LineString obstacleExterior = obstacle.getExteriorRing();

                LineString path = getShortestPathLineString(obstacleExterior,
                        from, to, obstacle);

                return path;

            } else {

                return directPath;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public int getCoordinateIndex(LineString lineString, Coordinate coordinate) {

        Validator.checkNullPointerPassed(lineString, coordinate);

        try {

            for (int i = 0; i < lineString.getNumPoints(); i++) {

                Coordinate exteriorPoint = lineString.getCoordinateN(i);

                if (lineString.compareTo(exteriorPoint) == 0) {

                    return i;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        throw new IllegalArgumentException(
                "Coordinate doesn't lie on LineString");
    }

    /**
     * @param from
     *            must lie on {@code lineString}
     */
    public LineString getShortestPathLineString(LineString lineString,
            Coordinate from, Coordinate to, Geometry obstacle) {

        Validator.checkNullPointerPassed(lineString, from, to);

        try {

            int startingCoordinateIndex = getCoordinateIndex(lineString, to);

            // traverse right
            LineString firstPath = getShortestPathLineString(lineString, from,
                    to, obstacle, 1, startingCoordinateIndex);

            // traverse left
            LineString secondPath = getShortestPathLineString(lineString, from,
                    to, obstacle, -1, startingCoordinateIndex);

            if (firstPath == null) {
                return secondPath;
            }

            if (secondPath == null) {
                return firstPath;
            }

            if (firstPath.getLength() < secondPath.getLength()) {

                return firstPath;
            } else {

                return secondPath;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Finds direct path from {@code from} coordinate to coordinate with
     * {@code startingCoordinateIndex} index then extends that path by
     * {@code obstacle} points then extends current path by direct path to
     * {@code to} coordinate . Traversing order is dependent on
     * {@code direction}
     * 
     * @param from
     *            must lie on {@code lineString}
     */
    public LineString getShortestPathLineString(LineString lineString,
            Coordinate from, Coordinate to, Geometry obstacle, int direction,
            int startingCoordinateIndex) {

        Validator.checkNullPointerPassed(lineString, from, to);
        Validator.checkNegative(startingCoordinateIndex);

        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("direction must be: 1 or -1.");
        }

        try {

            // if there is no obstacle then go by direct path
            if (obstacle == null) {
                return getDirectPath(from, to);
            }

            int upperBound = lineString.getNumPoints();

            LineString newLineString = null;

            for (int i = startingCoordinateIndex; i >= 0 && i < upperBound; i += direction) {

                Coordinate coordinate = lineString.getCoordinateN(i);

                // extend our LineString by adding coordinate lying on
                // LineString

                if (newLineString == null) {

                    newLineString = getDirectPath(from, coordinate);

                } else {

                    newLineString = extendLineStringWithCoordinate(
                            newLineString, coordinate);
                }

                // check whether there is a direct path to target Point
                LineString testLineString = getDirectPath(coordinate, to);
                if (!testLineString.crosses(obstacle)) {

                    return extendLineStringWithCoordinate(newLineString, to);
                }

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        // path not found or error occured
        return null;
    }

    // Validator.checkNullPointerPassed(lineString, direction);
    //
    // try {
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // return null;

    public LineString extendLineStringWithCoordinate(LineString lineString,
            Coordinate coordinate) {

        Validator.checkNullPointerPassed(lineString, coordinate);

        try {

            GeometryFactory geometryFactory = JTSFactoryFinder
                    .getGeometryFactory(null);

            List<Coordinate> coordsList = Arrays.asList(lineString
                    .getCoordinates());
            ;

            coordsList.add(coordinate);

            // only for arrays of reference types
            Coordinate[] coordsArray = coordsList
                    .toArray(new Coordinate[coordsList.size()]);

            LineString extendedLineString = geometryFactory
                    .createLineString(coordsArray);

            return extendedLineString;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public LineString getDirectPath(Point from, Point to) {

        Validator.checkNullPointerPassed(from, to);

        try {

            return getDirectPath(from.getCoordinate(), to.getCoordinate());

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public LineString getDirectPath(Coordinate from, Coordinate to) {

        Validator.checkNullPointerPassed(from, to);

        try {

            GeometryFactory geometryFactory = JTSFactoryFinder
                    .getGeometryFactory(null);

            Coordinate[] coords = new Coordinate[] { from, to };

            LineString lineString = geometryFactory.createLineString(coords);

            return lineString;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public double getLineStringLength() {
        return lineString.getLength();
    }

    public LineString getLineString() {
        return lineString;
    }

}
