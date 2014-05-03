package handlers;

import interfaces.ICommonOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;

import windows.Command;
import windows.SumCharacteristicsWindow;
import windows.intersect.LayerJListPanel;

public class SumCharacteristicsHandler implements ICommonOperations {

    private LayerJListPanel layerJListPanel;
    private SumCharacteristicsWindow sumCharacteristicsWindow;
    private MapHandler mapHandler;
    public Command command = null;
    private final int divisionPrecision = 6;

    public SumCharacteristicsHandler(LayerJListPanel layerJListPanel,
            SumCharacteristicsWindow sumCharacteristicsWindow,
            MapHandler mapHandler) {

        super();

        if (sumCharacteristicsWindow == null || layerJListPanel == null
                || mapHandler == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.sumCharacteristicsWindow = sumCharacteristicsWindow;
        this.layerJListPanel = layerJListPanel;
        this.mapHandler = mapHandler;
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
                }

                Layer layer1 = layerList.get(0);
                Layer layer2 = layerList.get(1);
                BigDecimal layer1DividedByLayer2 = getRatio(layer1, layer2,
                        command);

                displayTwoRatios(layer1, layer2, layer1DividedByLayer2, command);

                break;

            case GET_AREA:
            case GET_LENGTH:

                List<BigDecimal> bigDecimalList = getLayerPropertySumList(
                        layerList, command);
                displayBigDecimalList(bigDecimalList, layerList, command);

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

    private BigDecimal sumProperty(Layer layer, Command command) {

        try {

            SimpleFeatureIterator iterator = ((SimpleFeatureCollection) layer
                    .getFeatureSource().getFeatures()).features();

            BigDecimal sumValue = new BigDecimal(0);

            try {

                while (iterator.hasNext()) {

                    SimpleFeature feature = iterator.next();
                    double value = 0;

                    switch (command) {

                    case GET_AREA:
                        value = ((Geometry) feature.getDefaultGeometry())
                                .getArea();
                        break;

                    case GET_LENGTH:
                        value = ((Geometry) feature.getDefaultGeometry())
                                .getLength();
                        break;
                    }

                    sumValue = sumValue.add(BigDecimal.valueOf(value));

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

    private int displayBigDecimalList(List<BigDecimal> bigDecimalList,
            List<Layer> layerList, Command command) {

        try {

            String commandString = commandToString(command);

            for (int i = 0; i < bigDecimalList.size(); i++) {

                String value = bigDecimalList.get(i).toPlainString();
                String layerName = Support.getLayerName(layerList.get(i));

                this.sumCharacteristicsWindow.displayPopUpBox(layerName + " "
                        + commandString + " " + value);

            }

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

    // calculates ratio of layer1/layer2
    private BigDecimal getRatio(Layer layer1, Layer layer2, Command command) {

        if (layer1 == null || layer2 == null || command == null) {

            throw new IllegalArgumentException("arguments must not be null!");
        }

        Command propertyCommand = null;

        switch (command) {

        case GET_LENGTH_RATIO:
            propertyCommand = Command.GET_LENGTH;
            break;

        case GET_AREA_RATIO:
            propertyCommand = Command.GET_AREA;
            break;
        }

        BigDecimal layer1PropertySum = sumProperty(layer1, propertyCommand);
        BigDecimal layer2PropertySum = sumProperty(layer2, propertyCommand);

        return layer1PropertySum.divide(layer2PropertySum, divisionPrecision,
                BigDecimal.ROUND_HALF_UP);
    }

    private int displayTwoRatios(Layer layer1, Layer layer2,
            BigDecimal layer1DividedByLayer2, Command command) {

        try {

            displaySingleRatio(layer1, layer2, layer1DividedByLayer2, command);

            BigDecimal one = new BigDecimal(1);
            BigDecimal layer2DividedByLayer1 = one.divide(
                    layer1DividedByLayer2, divisionPrecision,
                    BigDecimal.ROUND_HALF_UP);

            displaySingleRatio(layer2, layer1, layer2DividedByLayer1, command);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    // displays layer1/layer2 ratio
    private int displaySingleRatio(Layer layer1, Layer layer2,
            BigDecimal layer1DividedByLayer2, Command command) {

        try {

            String commandString = commandToString(command);

            String layer1DividedByLayer2String = layer1DividedByLayer2
                    .toPlainString();

            String layer1Name = Support.getLayerName(layer1);
            String layer2Name = Support.getLayerName(layer2);

            this.sumCharacteristicsWindow.displayPopUpBox(layer1Name + "/"
                    + layer2Name + " " + commandString + " "
                    + layer1DividedByLayer2String);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }

    public int addSelectedLayersToMap() {

        throw new UnsupportedOperationException(
                "Method not implemented!");

    }
}
