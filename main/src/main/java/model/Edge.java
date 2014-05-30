package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.coordinatesequence.CoordinateSequences;
import org.geotools.main.Main;
import org.geotools.main.Support;
import org.geotools.main.Validator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class Edge {

    private LineString globalLineString;
    private HashMap<Coordinate, LineString> lineStringHashMap = new HashMap<Coordinate, LineString>();

    /**
     * @param from
     *            must lie on {@code fromLineString} and must be
     *            {@code fromLineString} start or end coordinate.
     */
    public Edge(LineString fromLineString, Coordinate from, Coordinate to,
            Polygon obstacle) {

        Validator.checkNullPointerPassed(fromLineString, from, to);

        this.globalLineString = constructPathStartingWithLineString(
                fromLineString, from, to, obstacle);
    }

    public Edge(Coordinate from, Coordinate to, Polygon obstacle) {

        Validator.checkNullPointerPassed(from, to);

        this.globalLineString = constructPath(from, to, obstacle);
    }

    public LineString constructPathStartingWithLineString(
            LineString fromLineString, Coordinate from, Coordinate to,
            Polygon obstacle) {

        Validator.checkNullPointerPassed(from, to);

        try {

            Coordinate lineStringStartCoordinate = fromLineString
                    .getStartPoint().getCoordinate();
            Coordinate lineStringEndCoordinate = fromLineString.getEndPoint()
                    .getCoordinate();

            // from must be end point of {fromLineString} because we will add
            // all first n-1 points to shortest paths result
            boolean isAtEndOrStart = false;

            if (lineStringStartCoordinate.compareTo(from) == 0) {

                fromLineString = (LineString) fromLineString.reverse();
                lineStringStartCoordinate = fromLineString.getStartPoint()
                        .getCoordinate();
                lineStringEndCoordinate = fromLineString.getEndPoint()
                        .getCoordinate();
                isAtEndOrStart = true;

            } else if (lineStringEndCoordinate.compareTo(from) == 0) {

                isAtEndOrStart = true;
            }

            if (isAtEndOrStart == false) {

                throw new IllegalArgumentException(
                        "{from} must be {fromLineString} start or end point coordinate!");
            }

            List<Coordinate> fullPathCoordList = new ArrayList<Coordinate>();
            Coordinate[] fromLineStringCoords = fromLineString.getCoordinates();

            // avoid including last point because our path was constructed by
            // start at last point of {fromLineString}
            for (int i = 0; i < fromLineStringCoords.length - 1; i++) {

                Coordinate tmpCoord = fromLineStringCoords[i];
                fullPathCoordList.add(tmpCoord);
            }

            Coordinate[] partialPathCoords = getShortestPathLineString(
                    lineStringEndCoordinate, to, obstacle).getCoordinates();

            // add the rest of the path that doesn't include {fromLineString}
            for (int i = 0; i < partialPathCoords.length; i++) {

                Coordinate tmpCoord = partialPathCoords[i];
                fullPathCoordList.add(tmpCoord);
            }

            // construct full path LineString from {fullPathCoordList} list of
            // coordinates

            // only for arrays of reference types
            Coordinate[] fullPathCoordsArray = fullPathCoordList
                    .toArray(new Coordinate[fullPathCoordList.size()]);

            CoordinateArraySequence fullPathCoordArraySequence = new CoordinateArraySequence(
                    fullPathCoordsArray);

            // create factory
            GeometryFactory geometryFactory = JTSFactoryFinder
                    .getGeometryFactory(null);

            LineString fullPathLineString = new LineString(
                    fullPathCoordArraySequence, geometryFactory);

            return fullPathLineString;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public LineString constructPath(Coordinate from, Coordinate to,
            Polygon obstacle) {

        Validator.checkNullPointerPassed(from, to);

        try {

            if (obstacle == null) {

                return getDirectPath(from, to);

            }

            LineString path = getShortestPathLineString(from, to, obstacle);

            return path;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@code coordinate} must lie on {@code lineString}
     */
    public int getCoordinateIndex(LineString lineString, Coordinate coordinate) {

        Validator.checkNullPointerPassed(lineString, coordinate);

        try {

            for (int i = 0; i < lineString.getNumPoints(); i++) {

                Coordinate exteriorPoint = lineString.getCoordinateN(i);

                if (exteriorPoint.compareTo(coordinate) == 0) {

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
    public LineString getShortestPathLineString(Coordinate from, Coordinate to,
            Polygon obstacle) {

        Validator.checkNullPointerPassed(from, to);

        try {

            if (obstacle == null) {
                return getDirectPath(from, to);
            }

            LineString directPath = getDirectPath(from, to);

            if (!directPath.intersects(obstacle)
                    && !obstacle.contains(directPath)) {

                return directPath;

            }

            LineString obstacleExterior = obstacle.getExteriorRing();

            // create tmp { from } point
            GeometryBuilder builder = new GeometryBuilder();

            Point tmpFromPoint = builder.point(from.x, from.y);

            // returns points in the same order arguments provided
            Coordinate[] coords = DistanceOp.nearestPoints(tmpFromPoint,
                    obstacleExterior);

            Coordinate transitionalCoordinate = coords[1];

            int startingCoordinateIndex = getCoordinateIndex(obstacleExterior,
                    transitionalCoordinate);

            if (true) {
                return getShortestPathLineStringDijkstra(obstacleExterior,
                        from, to, obstacle, -1, startingCoordinateIndex);
            }

            // traverse right
            LineString firstPath = getShortestPathLineString(obstacleExterior,
                    from, to, obstacle, 1, startingCoordinateIndex);

            // //traverse backWards
            // LineString firstPathReverted =
            // getShortestPathLineString(obstacleExterior,
            // to, from, obstacle, 1, startingCoordinateIndex);
            //

            // traverse left
            LineString secondPath = getShortestPathLineString(obstacleExterior,
                    from, to, obstacle, -1, startingCoordinateIndex);

            // LineString secondPathReverted =
            // getShortestPathLineString(obstacleExterior,
            // to, from, obstacle, -1, startingCoordinateIndex);
            //
            // if (secondPathReverted.getLength() < secondPath.getLength()) {
            //
            // secondPath = (LineString)secondPathReverted.reverse();
            // }
            //
            // if (firstPathReverted.getLength() < secondPath.getLength()) {
            //
            // secondPath = (LineString)firstPathReverted.reverse();
            // }

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
    public LineString getShortestPathLineStringDijkstra(
            LineString obstacleExterior, Coordinate from, Coordinate to,
            Polygon obstacle, int direction, int startingCoordinateIndex) {

        Validator.checkNullPointerPassed(obstacleExterior, from, to);
        Validator.checkNegative(startingCoordinateIndex);

        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("direction must be: 1 or -1.");
        }

        try {

            // if there is no obstacle then go by direct path
            if (obstacle == null) {
                return getDirectPath(from, to);
            }
            // if direct path doesn't cross obstacle return direct path
            LineString directPathLineString = getDirectPath(from, to);
            if (!directPathLineString.crosses(obstacle)
                    || obstacle.contains(directPathLineString)) {

                return directPathLineString;
            }

            int upperBound = obstacleExterior.getNumPoints();
            // max vertex count
            int MAXN = upperBound + 2 + 20;

            HashMap<Integer, Coordinate> coordHashMap = new HashMap<Integer, Coordinate>();

            Coordinate[] obstacleExteriorCoordsArray = obstacleExterior
                    .getCoordinates();

            List<Coordinate> coordinates = new ArrayList<Coordinate>();

            for (Coordinate exteriorCoord : obstacleExteriorCoordsArray) {
                coordinates.add(exteriorCoord);
            }
            coordinates.add(to);
            coordinates.add(from);

            for (int i = 0; i < coordinates.size(); i++) {

                Coordinate coord = coordinates.get(i);
                coordHashMap.put(i, coord);
            }

            Integer p[] = new Integer[MAXN];
            double d[] = new double[MAXN];
            boolean v[] = new boolean[MAXN];

            for (int i = 0; i < MAXN; i++) {
                d[i] = Double.MAX_VALUE;
            }

            // initialize connectivity
            ArrayList<ArrayList<SimpleEdge>> con = new ArrayList<ArrayList<SimpleEdge>>(
                    MAXN);

            for (int i = 0; i >= 0 && i < MAXN; i++) {
                con.add(new ArrayList<SimpleEdge>());
            }

            for (int i = 0; i >= 0 && i < coordinates.size(); i++) {

                Coordinate c1 = coordHashMap.get(i);

                for (int j = i + 1; j >= 0 && j < coordinates.size(); j++) {

                    Coordinate c2 = coordHashMap.get(j);

                    LineString lineString = getDirectPath(c1, c2);

                    if (!lineString.crosses(obstacle)
                            && !obstacle.contains(lineString)) {

                        double len = lineString.getLength();

                        con.get(i).add(new SimpleEdge(len, j));
                        con.get(j).add(new SimpleEdge(len, i));

                    }

                }
            }

            // initialize initial variables variables
            int start = coordinates.size() - 1;
            int destination = coordinates.size() - 2;

            int current = start;
            d[current] = 0;

            while (current != -1) {

                v[current] = true;

                for (int i = 0; i < con.get(current).size(); i++) {

                    int neigh = con.get(current).get(i).connectedVertex;
                    double edgeLen = con.get(current).get(i).edgeLength;

                    double newdist = d[current] + edgeLen;

                    if (newdist < d[neigh]) {
                        d[neigh] = newdist;
                        p[neigh] = current;
                    }
                }

                current = -1;
                for (int j = 0; j < coordinates.size(); ++j) {
                    if (!v[j] && (current == -1 || d[j] < d[current]))
                        current = j;
                }
            }

            List<Coordinate> pathList = new ArrayList<Coordinate>();

            int cur = destination;

            while (p[cur] != start) {

                pathList.add(coordHashMap.get(cur));
                cur = p[cur];
            }

            pathList.add(coordHashMap.get(start));

            LineString pathLineString = getDirectPath(
                    pathList.get(pathList.size() - 1),
                    pathList.get(pathList.size() - 2));

            for (int i = pathList.size() - 3; i >= 0; i--) {

                Coordinate coord = pathList.get(i);
                pathLineString = extendLineStringWithCoordinate(pathLineString,
                        coord);
            }

            return pathLineString;

        } catch (Exception e) {

            e.printStackTrace();
        }

        // path not found or error occured
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
    public LineString getShortestPathLineString(LineString obstacleExterior,
            Coordinate from, Coordinate to, Geometry obstacle, int direction,
            int startingCoordinateIndex) {

        Validator.checkNullPointerPassed(obstacleExterior, from, to);
        Validator.checkNegative(startingCoordinateIndex);

        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("direction must be: 1 or -1.");
        }

        try {

            // if there is no obstacle then go by direct path
            if (obstacle == null) {
                return getDirectPath(from, to);
            }

            // if direct path doesn't cross obstacle return direct path
            LineString directPathLineString = getDirectPath(from, to);
            if (!directPathLineString.crosses(obstacle)) {
                return directPathLineString;
            }

            int upperBound = obstacleExterior.getNumPoints();

            LineString newLineString = null;

            Coordinate prevCoordinate = from;
            Coordinate nextCoordinate = null;

            // turetume eiti tol kol neatsiranda tiesau kelio, o ne iki nurodytu
            // ribu
            for (int i = startingCoordinateIndex; i >= 0 && i < upperBound; i += direction) {

                // final Coordinate starting = lineString.getCoordinateN(i);
                Coordinate currCoordinate = obstacleExterior.getCoordinateN(i);

                // check whether we can skip surface segments
                int j = i;

                lookForwardWhileLoop: while (j >= 0 && j < upperBound) {

                    nextCoordinate = obstacleExterior.getCoordinateN(j);

                    // check whether there is a direct path to see if we can go
                    // by shorter by
                    LineString tmpLineString = getDirectPath(prevCoordinate,
                            nextCoordinate);
                    // TODO fix
                    // if (!tmpLineString.crosses(obstacle)
                    // && !obstacle.contains(tmpLineString)) {
                    if (tmpLineString.touches(obstacle)) {
                        currCoordinate = nextCoordinate;
                        i = j;

                    }
                    // TODO fix
                    // check whether we arrived at point from which there is a
                    // direct path to target Point
                    LineString testLineString = getDirectPath(nextCoordinate,
                            to);
                    // if (!testLineString.crosses(obstacle)
                    // && !obstacle.contains(tmpLineString)) {
                    if (testLineString.touches(obstacle)) {

                        break lookForwardWhileLoop;
                    }

                    j += direction;

                }

                if (newLineString == null) {

                    newLineString = getDirectPath(from, currCoordinate);

                } else {

                    newLineString = extendLineStringWithCoordinate(
                            newLineString, currCoordinate);

                }

                LineString testLineString = getDirectPath(currCoordinate, to);
                if (!testLineString.crosses(obstacle)
                        && !obstacle.contains(testLineString)) {
                    return extendLineStringWithCoordinate(newLineString, to);
                }

                prevCoordinate = currCoordinate;
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

            // copy coordinates to list
            Coordinate[] lineStringCoords = lineString.getCoordinates();

            List<Coordinate> coordsList = new ArrayList<Coordinate>();

            for (int i = 0; i < lineStringCoords.length; i++) {

                coordsList.add(lineStringCoords[i]);
            }

            // add new coordinate
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
        return globalLineString.getLength();
    }

    public LineString getLineString() {
        return globalLineString;
    }

}
