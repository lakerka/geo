package handlers;

import interfaces.ICommonOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.main.FeatureCollectionTableModelExtended;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import views.panels.LayerJListPanel;
import views.windows.Command;
import views.windows.SummarizeWindow;

//TODO uzbaigti
public class SummarizeHandler implements ICommonOperations {

    private LayerJListPanel layerJListPanel;
    private SummarizeWindow sumCharacteristicsWindow;
    private MapHandler mapHandler;
    public Command command = null;
    private final int divisionPrecision = 6;

    private List<String> columnNames;
    private List<List<Object>> cells;

    public SummarizeHandler() {
        super();
    }

    public SummarizeHandler(LayerJListPanel layerJListPanel,
            SummarizeWindow sumCharacteristicsWindow, MapHandler mapHandler) {

        super();

        if (sumCharacteristicsWindow == null || layerJListPanel == null
                || mapHandler == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.sumCharacteristicsWindow = sumCharacteristicsWindow;
        this.layerJListPanel = layerJListPanel;
        this.mapHandler = mapHandler;
        this.columnNames = new ArrayList<String>();
        this.cells = new ArrayList<List<Object>>();
    }

    public int addLayer(Layer layer) {

        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null!");
        }

        try {

            this.layerJListPanel.addLayer(layer);

            return 1;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public int removeSelected() {

        try {

            return this.layerJListPanel.removeSelectedLayers();

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    // TODO baigti
    public Layer sumCharacteristics() {
        try {

            List<Layer> layerList = this.layerJListPanel.getSelectedLayers();

            if (layerList == null || layerList.isEmpty()) {
                return null;
            }

            switch (this.command) {

            case GET_LENGTH_RATIO:
            case GET_AREA_RATIO:

                if (layerList.size() < 2) {
                    this.sumCharacteristicsWindow
                            .displayPopUpBox("Select 2 layers!");
                    return null;
                }

                Layer layer1 = layerList.get(0);
                Layer layer2 = layerList.get(1);

                String layer1Name = Support.getLayerName(layer1);
                String layer2Name = Support.getLayerName(layer2);

                int layer1ByLayer2 = sumCharacteristicsWindow
                        .displayYesNoWindow(layer1Name + "/" + layer2Name
                                + " ratio?");

                // swap if the case
                if (layer1ByLayer2 != 1) {

                    Layer tmpLayer = layer2;
                    layer2 = layer1;
                    layer1 = tmpLayer;
                }

                List<String> commandsList = new ArrayList<String>();
                commandsList.add(commandToString(command));

                List<List<BigDecimal>> eachByEachValues = new ArrayList<List<BigDecimal>>();
                List<BigDecimal> bigDecimalList = getRatioOfProperties(layer1,
                        layer2, command);

                int mod = commandsList.size();

                int j = -1;

                for (int i = 0; i < bigDecimalList.size(); i++) {

                    if (i % mod == 0) {

                        j++;
                        eachByEachValues.add(new ArrayList<BigDecimal>());

                    }

                    BigDecimal bigDecimal = bigDecimalList.get(i);

                    eachByEachValues.get(j).add(bigDecimal);

                }

                displayEachByEach(layer1, layer2, eachByEachValues,
                        commandsList);
                // command);

                break;

            case GET_AREA:
            case GET_LENGTH:

                // List<BigDecimal> bigDecimalList = getLayerPropertySumList(
                // layerList, command);
                // displayBigDecimalList(bigDecimalList, layerList, command);

                break;
            }

            return null;

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return null;
    }

    public int addSelectedLayersFromMap() {

        try {

            List<Layer> layerList = this.mapHandler.getSelectedOrVisibleLayer(
                    true, false);

            if (layerList == null || layerList.isEmpty()) {
                return 1;
            }

            return this.layerJListPanel.addAllLayers(layerList);

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        return 0;
    }

    public int setCommand(Command command) {

        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        this.command = command;
        return 1;
    }

    public BigDecimal sumProperty(Layer layer, Command command) {

        if (layer == null || command == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(layer);

            return sumProperty(simpleFeatureList, command);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal sumProperty(List<SimpleFeature> simpleFeatureList,
            Command command) {

        if (simpleFeatureList == null || command == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            SimpleFeatureIterator iterator = DataUtilities.collection(
                    simpleFeatureList).features();

            BigDecimal sumValue = new BigDecimal(0);

            Command getPropertyCommand;

            switch (command) {

            case GET_LENGTH_SUM:
                getPropertyCommand = Command.GET_LENGTH;
                break;

            case GET_AREA_SUM:
                getPropertyCommand = Command.GET_AREA;
                break;

            default:
                return null;
            }

            try {

                while (iterator.hasNext()) {

                    SimpleFeature simpleFeature = iterator.next();
                    BigDecimal value = getProperty(simpleFeature,
                            getPropertyCommand);

                    sumValue = sumValue.add(value);

                }

            } finally {
                iterator.close();
            }

            return sumValue;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    private BigDecimal getProperty(SimpleFeature simpleFeature, Command command) {

        try {

            BigDecimal value = null;

            switch (command) {

            case GET_AREA:
                value = new BigDecimal(
                        ((Geometry) simpleFeature.getDefaultGeometry())
                                .getArea());
                break;

            case GET_LENGTH:
                value = new BigDecimal(
                        ((Geometry) simpleFeature.getDefaultGeometry())
                                .getLength());
                break;

            default:
                break;
            }

            return value;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    private BigDecimal getProperty(Geometry geometry, Command command) {

        try {

            BigDecimal value = null;

            switch (command) {

            case GET_AREA:
                value = new BigDecimal(geometry.getArea());
                break;

            case GET_LENGTH:
                value = new BigDecimal(geometry.getLength());
                break;

            default:
                break;
            }

            return value;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    // sum property for each layer and return list of each layer's property sum
    private List<BigDecimal> getLayerPropertySumList(List<Layer> layerList,
            Command command) {

        try {

            List<BigDecimal> bigDecimalsList = new ArrayList<BigDecimal>();

            for (Layer layer : layerList) {

                BigDecimal value = sumProperty(layer, command);
                bigDecimalsList.add(value);

            }

            return bigDecimalsList;

        } catch (Exception exception) {

            exception.printStackTrace();

        }

        return null;
    }

    // TODO finish and test
    private int displayBigDecimalList(List<BigDecimal> bigDecimalList,
            List<Layer> layerList, Command command) {

        try {

            String commandString = commandToString(command);

            List<String> columnNames = new ArrayList<String>();

            for (Layer layer : layerList) {

                columnNames.add(Support.getLayerName(layer));
            }

            List<List<String>> data = new ArrayList<List<String>>();
            for (int i = 0; i < bigDecimalList.size(); i++) {

                data.add(new ArrayList<String>());

                String value = bigDecimalList.get(i).toPlainString();

                data.get(i).add(value);
            }

            // this.

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    private String commandToString(Command command) {

        switch (command) {
        case GET_AREA:
            return "area";

        case GET_LENGTH:
            return "length";

        case GET_AREA_RATIO:
            return "area ratio";

        case GET_LENGTH_RATIO:
            return "length ratio";

        default:
            return "unknown";
        }

    }

    // calculates ratio between layer1 property sum/ layer2 property sum
    private BigDecimal getRatioOfPropertySums(Layer layer1, Layer layer2,
            Command command) {

        if (layer1 == null || layer2 == null || command == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            Command propertyCommand = null;

            switch (command) {

            case GET_LENGTH_RATIO:
                propertyCommand = Command.GET_LENGTH;
                break;

            case GET_AREA_RATIO:
                propertyCommand = Command.GET_AREA;
                break;

            default:
                return null;
            }

            BigDecimal layer1PropertySum = sumProperty(layer1, propertyCommand);
            BigDecimal layer2PropertySum = sumProperty(layer2, propertyCommand);

            return layer1PropertySum.divide(layer2PropertySum,
                    divisionPrecision, BigDecimal.ROUND_HALF_UP);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // TODO test
    private int displayEachByEach(Layer layer1, Layer layer2,
            List<List<BigDecimal>> values, List<String> commands) {

        if (values == null || layer1 == null || layer2 == null
                || commands == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            // get column names
            List<String> columnNames = new ArrayList<String>();

            addLayerFeatureNames(layer1, columnNames);
            addLayerFeatureNames(layer2, columnNames);

            for (String command : commands) {
                columnNames.add(command);
            }

            // get cell values
            List<List<Object>> data = eachByEachToRows(layer1, layer2);

            for (int i = 0; i < data.size(); i++) {
                for (int j = 0; j < commands.size(); j++) {

                    Object value = values.get(i).get(j).toPlainString();
                    data.get(i).add(value);
                }
            }

            // display
            return sumCharacteristicsWindow.setTableModelAndRepaint(
                    columnNames, data);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public int addSelectedLayersToMap() {

        throw new UnsupportedOperationException("Method not implemented!");

    }

    // add layer feature names
    private int addLayerFeatureNames(Layer layer, List<String> columnNames) {

        if (layer == null || columnNames == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            SimpleFeatureSource simpleFeatureSource = Support
                    .layerToSimpleFeatureSource(layer);

            String layerName = Support.getLayerName(layer);

            SimpleFeatureType simpleFeatureType = simpleFeatureSource
                    .getSchema();

            int columnCount = simpleFeatureType.getAttributeCount();

            // TODO check for featureIdentifier for i=0
            for (int i = 0; i < columnCount; i++) {

                String columnName = simpleFeatureType.getDescriptor(i)
                        .getLocalName();
                String columnNameFormated = layerName + "_" + columnName;

                columnNames.add(columnNameFormated);
            }

            return 1;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // add layer features in form layer1 | layer2
    private List<List<Object>> eachByEachToRows(Layer layer1, Layer layer2) {

        if (layer1 == null || layer2 == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            List<List<Object>> data = new ArrayList<List<Object>>();

            addLayerFeaturesAsRows(layer1, data);

            data = concatenateEachRowWithEachRow(layer2, data);

            return data;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // add layer features to list of list
    private int addLayerFeaturesAsRows(Layer layer, List<List<Object>> data) {

        if (layer == null || data == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(layer);

            for (SimpleFeature simpleFeature : simpleFeatureList) {

                List<Object> row = simpleFeature.getAttributes();

                data.add(row);
            }

            return 1;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // Creates new rows from existing rows and new rows by concatenating them 1
    // by all
    // if existing row is 1 2 3 4 and layer rows are 5 6 produces: 1 + 5, 1 + 6,
    // 2 + 5, 2 + 6 ....
    private List<List<Object>> concatenateEachRowWithEachRow(Layer layer,
            List<List<Object>> data) {

        if (layer == null || data == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            List<List<Object>> modifiedData = new ArrayList<List<Object>>();

            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(layer);

            for (List<Object> row : data) {

                for (SimpleFeature simpleFeature : simpleFeatureList) {

                    List<Object> concatRow = new ArrayList<Object>();

                    concatRow.addAll(row);
                    concatRow.addAll(simpleFeature.getAttributes());

                    modifiedData.add(concatRow);
                }
            }

            return modifiedData;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // calculates ratio between layer1 property sum/ layer2 property sum
    private List<BigDecimal> getRatioOfProperties(Layer layer1, Layer layer2,
            Command command) {

        if (layer1 == null || layer2 == null || command == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        try {

            Command propertyCommand = null;

            switch (command) {

            case GET_LENGTH_RATIO:
                propertyCommand = Command.GET_LENGTH;
                break;

            case GET_AREA_RATIO:
                propertyCommand = Command.GET_AREA;
                break;

            default:
                return null;
            }

            List<BigDecimal> values = new ArrayList<BigDecimal>();

            List<SimpleFeature> simpleFeatureList1 = Support
                    .layerToSimpleFeatureList(layer1);
            List<SimpleFeature> simpleFeatureList2 = Support
                    .layerToSimpleFeatureList(layer2);

            BigDecimal zero = new BigDecimal(0);

            for (SimpleFeature simpleFeature1 : simpleFeatureList1) {

                BigDecimal bigDecimal1 = getProperty(simpleFeature1,
                        propertyCommand);

                for (SimpleFeature simpleFeature2 : simpleFeatureList2) {

                    BigDecimal bigDecimal2 = getProperty(simpleFeature2,
                            propertyCommand);

                    BigDecimal divisionResult;

                    // avoid division by zero
                    if (bigDecimal2.compareTo(zero) == 0) {

                        divisionResult = zero;

                    } else {

                        divisionResult = bigDecimal1.divide(bigDecimal2,
                                divisionPrecision, BigDecimal.ROUND_HALF_UP);

                    }

                    values.add(divisionResult);
                }
            }

            return values;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal sumProperty(Collection<Geometry> geometryCollection, Command command) {

        if (geometryCollection == null || command == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        try {

            BigDecimal sumValue = new BigDecimal(0);

            Command getPropertyCommand;

            switch (command) {

            case GET_LENGTH_SUM:
                getPropertyCommand = Command.GET_LENGTH;
                break;

            case GET_AREA_SUM:
                getPropertyCommand = Command.GET_AREA;
                break;

            default:
                return null;
            }

            for (Geometry geometry : geometryCollection) {

                BigDecimal value = getProperty(geometry, getPropertyCommand);

                sumValue = sumValue.add(value);

            }

            return sumValue;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }
}
