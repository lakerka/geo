package views.windows;

import handlers.GroupFeaturesHandler;
import handlers.MapHandler;
import handlers.SummarizeHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.main.FeatureCollectionTableModelExtended;
import org.geotools.main.Main;
import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import setsRelated.GeometrySet;

public class SecondTask extends JFrame {

    private final int divisionPrecision = 6;

    HashMap<Object, HashMap<Object, ArrayList<Geometry>>> plotCutHashMap = new HashMap<Object, HashMap<Object, ArrayList<Geometry>>>();
    HashMap<Object, Geometry> savHashMap = new HashMap<Object, Geometry>();
    HashMap<Object, Geometry> savCutHashMap = new HashMap<Object, Geometry>();

    /**
     * 
     */
    private static final long serialVersionUID = 1666489575899750368L;

    public SecondTask() {

    }

    private int createButtonAndAddToButtonPane(String JButtonText,
            ActionListener actionListener, JPanel buttonJPanel) {

        JButton jButton = new JButton(JButtonText);

        if (actionListener != null) {
            jButton.addActionListener(actionListener);
        }

        buttonJPanel
                .setLayout(new BoxLayout(buttonJPanel, BoxLayout.LINE_AXIS));
        buttonJPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        // buttonJPanel.add(Box.createHorizontalGlue());
        buttonJPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        buttonJPanel.add(jButton, BorderLayout.CENTER);

        return 1;
    }

    public String getAttributeName() {
        String name = JOptionPane.showInputDialog("Enter attribute name");
        return name;
    }

    public void displayPopUpBox(String message) {

        JOptionPane.showMessageDialog(this, message);

    }

    public List<SimpleFeature> filterFeatures(
            List<SimpleFeature> simpleFeatureList, String cqlPredicate)
            throws Exception {

        try {

            Layer layer = Support.simpleFeatureListToLayer(simpleFeatureList);

            SimpleFeatureSource source = Support
                    .layerToSimpleFeatureSource(layer);

            Filter filter = CQL.toFilter(cqlPredicate);
            SimpleFeatureCollection features = source.getFeatures(filter);

            return Support.simpleFeatureCollectionToSimpleFeatureList(features);

        } catch (Exception exception) {

            exception.printStackTrace();
            return null;
        }

    }

    public Layer filterFeatures(Layer layer, String cqlPredicate)
            throws Exception {

        try {

            SimpleFeatureSource source = Support
                    .layerToSimpleFeatureSource(layer);

            Filter filter = CQL.toFilter(cqlPredicate);
            SimpleFeatureCollection features = source.getFeatures(filter);

            return Support.simpleFeatureCollectionToLayer(features);

        } catch (Exception exception) {

            exception.printStackTrace();
            return null;
        }

    }

    public void start(Layer savivaldybesLayer) {
        try {

            this.setTitle("Second task");

            // add menu bar
            JMenuBar menubar = new JMenuBar();
            setJMenuBar(menubar);

            JPanel buttonPane = new JPanel();

            String pathCut = "C:\\Users\\as\\git\\geograf\\main\\cut\\";
            
            String pathSventoji = "C:\\Users\\as\\git\\geograf\\main\\LTsventoji\\";

//            Layer testSavCutLayer = Support
//                    .loadLayer(pathCut + "sav_cut_3.shp");

            Layer testSavCutLayer = savivaldybesLayer;
            
            Layer hidLayer = Support.loadLayer(pathSventoji + "sven_HID_L.shp");

            Layer tmpKeliai = Support
                    .loadLayer(pathSventoji + "sven_KEL_L.shp");

            Layer plotLayer = Support
                    .loadLayer(pathSventoji + "sven_PLO_P.shp");

            Layer pastLayer = Support
                    .loadLayer(pathSventoji + "sven_PAS_P.shp");

            // pagalbiniai
            GeometrySet geometrySet = new GeometrySet();

            SummarizeWindow summarizeWindow = new SummarizeWindow(
                    new MapHandler(new MapContent()));

            // 1
            JLabel hidroLabel = new JLabel("HIDRO_L length");
            JTextField hidroTextField = new JTextField("Unknown");
            JPanel firstPanel = new JPanel(new BorderLayout());
            firstPanel.add(hidroLabel, BorderLayout.WEST);
            firstPanel.add(hidroTextField, BorderLayout.CENTER);
            // TODO uncomment

            Layer upesLayer = filterFeatures(hidLayer, "TIPAS = 1");

            Layer upesCutLayer = geometrySet.intersect(testSavCutLayer,
                    upesLayer, 4);

            BigDecimal upesLength = summarizeWindow.summarizeHandler
                    .sumProperty(upesCutLayer, Command.GET_LENGTH_SUM);
            hidroTextField.setText(upesLength.toPlainString());

            // 2
            // //////////////////////////
            JLabel keliaiLabel = new JLabel("KELIAI length");
            JTextField keliaiTextField = new JTextField("Unknown");
            JPanel secondPanel = new JPanel(new BorderLayout());
            secondPanel.add(keliaiLabel, BorderLayout.WEST);
            secondPanel.add(keliaiTextField, BorderLayout.CENTER);

            Layer keliaiCutLayer = geometrySet.intersect(testSavCutLayer,
                    tmpKeliai, 4);

            BigDecimal keliaiLength = summarizeWindow.summarizeHandler
                    .sumProperty(keliaiCutLayer, Command.GET_LENGTH_SUM);
            keliaiTextField.setText(keliaiLength.toPlainString());
            // //////////////////////

            // 3
            // //////////////////////////

            Layer plotLayerCut = geometrySet.intersect(testSavCutLayer,
                    plotLayer, 4);

            // //TODO test. Commnet after testing
            // SimpleFeatureSource simpleFeatureSourcePLOT = Support
            // .loadShapeFile(pathCut + "plot_cut_3.shp");
            // Layer plotLayerCut = Support
            // .simpleFeatureSourceToLayer(simpleFeatureSourcePLOT);
            // //

            // pradedame formuoti rezultatus lenteles duomenis
            String columnNames[] = { "SAVIVALDYBE", "GKODAS", "PLOT_PLOTAS",
                    "SAV_PLOTAS", "SANTYKIS" };
            Object[][] data = gatherInfoFor3(plotLayerCut, testSavCutLayer);

            JTable jTable = new JTable();
            jTable.setSize(800, 300);
            JPanel tablePane = initPanel(jTable);
            DefaultTableModel defaultTableModel = new DefaultTableModel(data,
                    columnNames);
            jTable.setModel(defaultTableModel);
            jTable.repaint();
            // //////////////////////////

            // 41
            // //////////////////////////

            Layer pastLayerCut = geometrySet.intersect(testSavCutLayer,
                    pastLayer, 4);

            String columnNames41[] = { "SAVIVALDYBE", "PAST_PLOTAS",
                    "SAV_PLOTAS" };
            Object[][] data41 = gatherInfoFor41(testSavCutLayer, pastLayerCut);

            JTable jTable41 = new JTable();
            jTable41.setAutoResizeMode(HEIGHT);
            jTable41.setAutoResizeMode(WIDTH);
            JPanel tablePane41 = initPanel(jTable41);
            DefaultTableModel defaultTableModel41 = new DefaultTableModel(
                    data41, columnNames41);
            jTable41.setModel(defaultTableModel41);
            jTable41.repaint();
            // //////////////////////////

            // 42
            // //////////////////////////
            String columnNames42[] = { "SAVIVALDYBE", "GKODAS", "PAST_PLOTAS",
                    "PLOT_PLOTAS", "SANTYKIS" };

            Layer pastIntersectWithPlot = geometrySet.intersect(plotLayerCut,
                    pastLayerCut, 4);
            Object[][] data42 = gatherInfoFor42(testSavCutLayer,
                    pastIntersectWithPlot);

            JTable jTable42 = new JTable();
            jTable42.setAutoResizeMode(HEIGHT);
            jTable42.setAutoResizeMode(WIDTH);
            JPanel tablePane42 = initPanel(jTable42);
            DefaultTableModel defaultTableModel42 = new DefaultTableModel(
                    data42, columnNames42);
            jTable42.setModel(defaultTableModel42);
            jTable42.repaint();
            // //////////////////////////

            // Put everything together, using the content pane's BorderLayout.
            Container contentPane = getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            contentPane.add(buttonPane);
            contentPane.add(firstPanel);
            contentPane.add(secondPanel);
            contentPane.add(tablePane);
            contentPane.add(tablePane41);
            contentPane.add(tablePane42);

            pack();
            this.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Object[][] gatherInfoFor42(Layer savCut, Layer pastIntersectWithPlot) {

        if (savCut == null || pastIntersectWithPlot == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        try {

            String savColumnName = "SAV";
            String gkodasColumnName = "GKODAS";

            List<SimpleFeature> savCutSimpleFeatureList = Support
                    .layerToSimpleFeatureList(savCut);

            if (!savHashMap.isEmpty() && savCutHashMap.isEmpty()) {
                savCutHashMap = savHashMap;
            }

            if (savCutHashMap.isEmpty()) {

                // gauname savivaldybiu hashmap
                for (SimpleFeature simpleFeature : savCutSimpleFeatureList) {

                    Geometry geometry = (Geometry) simpleFeature
                            .getDefaultGeometry();

                    Object attribute = simpleFeature
                            .getAttribute(savColumnName);

                    if (!savCutHashMap.containsKey(attribute)) {

                        savCutHashMap.put(attribute, geometry);

                    }
                }
            }
            // reikalingas, kad veliau galetume suskaiciuoti bendra plota
            // savivaldybeje

            HashMap<Object, HashMap<Object, ArrayList<Geometry>>> pastBySavHashMap = new HashMap<Object, HashMap<Object, ArrayList<Geometry>>>();

            List<SimpleFeature> pastSimpleFeatureList = Support
                    .layerToSimpleFeatureList(pastIntersectWithPlot);

            // suformuojame plotu pagal savivaldybes ir gkodus hashmap
            for (SimpleFeature simpleFeature : pastSimpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object savObj = simpleFeature.getAttribute(savColumnName);
                Object gkodasObj = simpleFeature.getAttribute(gkodasColumnName);

                // sujungiame hidrografijas
                String gkodas = gkodasObj.toString();
                if (gkodas.equals("hd1") || gkodas.equals("hd2")
                        || gkodas.equals("hd3") || gkodas.equals("hd4")
                        || gkodas.equals("hd5") || gkodas.equals("hd9")) {

                    gkodasObj = (Object) "hidro";
                }

                if (!pastBySavHashMap.containsKey(savObj)) {

                    pastBySavHashMap.put(savObj,
                            new HashMap<Object, ArrayList<Geometry>>());

                }

                if (!pastBySavHashMap.get(savObj).containsKey(gkodasObj)) {

                    pastBySavHashMap.get(savObj).put(gkodasObj,
                            new ArrayList<Geometry>());
                }

                pastBySavHashMap.get(savObj).get(gkodasObj).add(geometry);
            }

            // String columnNames2[] = { "SAVIVALDYBE", "GKODAS",
            // "PAST_PLOTAS", "PLOT_PLOTAS", "SANTYKIS" };

            List<List<Object>> rez = new ArrayList<List<Object>>();

            SummarizeHandler summarizeHandler = new SummarizeHandler();

            Set<Object> savObjSet = savCutHashMap.keySet(); // sav vardu setas

            for (Object savObj : savObjSet) {

                Set<Object> gkodasObjSet = pastBySavHashMap.get(savObj)
                        .keySet();

                for (Object gkodasObj : gkodasObjSet) {

                    rez.add(new ArrayList<Object>());

                    int index = rez.size() - 1;

                    List<Object> row = rez.get(index);

                    // SAVIVALDYBE
                    row.add(savObj);

                    // GKODAS
                    row.add(gkodasObj);

                    // PAST_PLOTAS
                    List<Geometry> pastGeometryList = pastBySavHashMap.get(
                            savObj).get(gkodasObj);

                    BigDecimal pastAreaSum = summarizeHandler.sumProperty(
                            pastGeometryList, Command.GET_AREA_SUM);

                    row.add(pastAreaSum.toPlainString());

                    // PLOT_PLOTAS
                    // global variable
                    List<Geometry> plotGeometryList = plotCutHashMap
                            .get(savObj).get(gkodasObj);

                    BigDecimal plotAreaSum = summarizeHandler.sumProperty(
                            plotGeometryList, Command.GET_AREA_SUM);

                    row.add(plotAreaSum.toPlainString());

                    // SANTYKIS
                    BigDecimal zero = new BigDecimal(0.0);

                    BigDecimal ratio = null;

                    if (pastAreaSum.compareTo(zero) == 0) {

                        ratio = zero;

                    } else {

                        ratio = pastAreaSum.divide(plotAreaSum,
                                divisionPrecision, BigDecimal.ROUND_HALF_UP);
                    }

                    row.add(ratio);
                }

            }

            return Support.listOfListToArrayOfArray(rez);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    public Object[][] gatherInfoFor41(Layer savCut, Layer pastCut) {

        if (savCut == null || pastCut == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        try {

            String savColumnName = "SAV";

            List<SimpleFeature> savCutSimpleFeatureList = Support
                    .layerToSimpleFeatureList(savCut);

            if (!savHashMap.isEmpty() && savCutHashMap.isEmpty()) {
                savCutHashMap = savHashMap;
            }

            if (savCutHashMap.isEmpty()) {

                // gauname savivaldybiu hashmap
                for (SimpleFeature simpleFeature : savCutSimpleFeatureList) {

                    Geometry geometry = (Geometry) simpleFeature
                            .getDefaultGeometry();

                    Object attribute = simpleFeature
                            .getAttribute(savColumnName);

                    if (!savCutHashMap.containsKey(attribute)) {

                        savCutHashMap.put(attribute, geometry);

                    }
                }
            }
            // reikalingas, kad veliau galetume suskaiciuoti bendra plota
            // savivaldybeje

            HashMap<Object, ArrayList<Geometry>> pastBySavHashMap = new HashMap<Object, ArrayList<Geometry>>();

            List<SimpleFeature> pastSimpleFeatureList = Support
                    .layerToSimpleFeatureList(pastCut);

            for (SimpleFeature simpleFeature : pastSimpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object attribute = simpleFeature.getAttribute(savColumnName);

                if (!pastBySavHashMap.containsKey(attribute)) {

                    pastBySavHashMap.put(attribute, new ArrayList<Geometry>());

                }

                pastBySavHashMap.get(attribute).add(geometry);
            }

            // String columnNames[] = { "SAVIVALDYBE", "SAV_PLOTAS",
            // "PAST_PLOTAS" };

            List<List<Object>> rez = new ArrayList<List<Object>>();

            SummarizeHandler summarizeHandler = new SummarizeHandler();

            Set<Object> savObjSet = savCutHashMap.keySet(); // sav vardu setas

            for (Object savObj : savObjSet) {

                Geometry savGeometry = savCutHashMap.get(savObj);

                rez.add(new ArrayList<Object>());

                int index = rez.size() - 1;

                List<Object> row = rez.get(index);

                // SAVIVALDYBE
                row.add(savObj);

                // PAST_PLOTAS
                List<Geometry> pastGeometryList = pastBySavHashMap.get(savObj);

                BigDecimal pastAreaSum = summarizeHandler.sumProperty(
                        pastGeometryList, Command.GET_AREA_SUM);

                row.add(pastAreaSum.toPlainString());

                // SAV_PLOTAS
                BigDecimal savArea = new BigDecimal(savGeometry.getArea());

                row.add(savArea.toPlainString());

            }

            return Support.listOfListToArrayOfArray(rez);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    public Object[][] gatherInfoFor3(Layer plotaiCutBySav, Layer savCut) {

        if (plotaiCutBySav == null || savCut == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        try {
            // < savivaldybe, <GKODAS, geometrija> >
            List<SimpleFeature> simpleFeatureList = Support
                    .layerToSimpleFeatureList(plotaiCutBySav);

            String savColumnName = "SAV";
            String gkodasColumnName = "GKODAS";

            // sudedame savivaldybes pagal ju pavadinimus
            List<SimpleFeature> savSimpleFeatureList = Support
                    .layerToSimpleFeatureList(savCut);

            for (SimpleFeature simpleFeature : savSimpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object attribute = simpleFeature.getAttribute(savColumnName);

                // 1 sav = 1 geom
                if (!savHashMap.containsKey(attribute)) {

                    savHashMap.put(attribute, geometry);

                }

            }

            // suformuojame plotu pagal savivaldybes ir gkodus hashmap
            for (SimpleFeature simpleFeature : simpleFeatureList) {

                Geometry geometry = (Geometry) simpleFeature
                        .getDefaultGeometry();

                Object savObj = simpleFeature.getAttribute(savColumnName);
                Object gkodasObj = simpleFeature.getAttribute(gkodasColumnName);

                // sujungiame hidrografijas
                String gkodas = gkodasObj.toString();
                if (gkodas.equals("hd1") || gkodas.equals("hd2")
                        || gkodas.equals("hd3") || gkodas.equals("hd4")
                        || gkodas.equals("hd5") || gkodas.equals("hd9")) {

                    gkodasObj = (Object) "hidro";
                }

                if (!plotCutHashMap.containsKey(savObj)) {

                    plotCutHashMap.put(savObj,
                            new HashMap<Object, ArrayList<Geometry>>());

                }

                if (!plotCutHashMap.get(savObj).containsKey(gkodasObj)) {

                    plotCutHashMap.get(savObj).put(gkodasObj,
                            new ArrayList<Geometry>());
                }

                plotCutHashMap.get(savObj).get(gkodasObj).add(geometry);
            }

            Set<Object> savObjSet = plotCutHashMap.keySet();

            List<List<Object>> rez = new ArrayList<List<Object>>();

            SummarizeHandler summarizeHandler = new SummarizeHandler();

            for (Object savObj : savObjSet) {

                Geometry savGeometry = savHashMap.get(savObj);

                Set<Object> gkodasObjSet = plotCutHashMap.get(savObj).keySet();

                for (Object gkodasObj : gkodasObjSet) {

                    rez.add(new ArrayList<Object>());

                    int index = rez.size() - 1;

                    List<Object> row = rez.get(index);

                    // SAVIVALDYBE
                    row.add(savObj);

                    // GKODAS
                    rez.get(index).add(gkodasObj);

                    // PLOT_PLOTAS
                    List<Geometry> plotGeometryList = plotCutHashMap
                            .get(savObj).get(gkodasObj);

                    BigDecimal plotAreaSum = summarizeHandler.sumProperty(
                            plotGeometryList, Command.GET_AREA_SUM);

                    row.add(plotAreaSum.toPlainString());

                    // SAV_PLOTAS
                    BigDecimal savArea = new BigDecimal(savGeometry.getArea());

                    row.add(savArea.toPlainString());

                    // SANTYKIS
                    BigDecimal zero = new BigDecimal(0.0);

                    BigDecimal ratio = null;

                    if (savArea.compareTo(zero) == 0) {

                        ratio = zero;

                    } else {

                        ratio = plotAreaSum.divide(savArea, divisionPrecision,
                                BigDecimal.ROUND_HALF_UP);
                    }

                    row.add(ratio);
                }

            }

            return Support.listOfListToArrayOfArray(rez);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public Layer modifiePlot(Layer plotLayer) {

        try {

            List<SimpleFeature> plotSimpleFeatureList = Support
                    .layerToSimpleFeatureList(plotLayer);
            System.out.println("1");
            String hidroCqlPredicate = "GKODAS = 'hd1' or GKODAS = 'hd2' or GKODAS = 'hd3' or GKODAS = 'hd4' or GKODAS = 'hd5' or GKODAS = 'hd9'";
            List<SimpleFeature> plotHidroSimpleFeatureList = filterFeatures(
                    plotSimpleFeatureList, hidroCqlPredicate);
            System.out.println("2");
            SimpleFeature hidroSimpleFeature = mergeToSingleFeature(plotHidroSimpleFeatureList);
            System.out.println("3");
            Support.setAttributeValue(hidroSimpleFeature, "GKODAS", "hidro");

            // get features that are not hidro
            String withOutHidroCqlPredicate = "not(" + hidroCqlPredicate + ")";
            List<SimpleFeature> simpleFeatureList = filterFeatures(
                    plotSimpleFeatureList, withOutHidroCqlPredicate);

            // add hidro to those features
            simpleFeatureList.add(hidroSimpleFeature);
            System.out.println("4");
            // group features by GKODAS
            GroupFeaturesHandler groupFeaturesHandler = new GroupFeaturesHandler();

            Layer layer = Support.simpleFeatureListToLayer(simpleFeatureList);
            // grouping takes a lot of time
            Layer groupedLayer = groupFeaturesHandler.groupFeaturesByAttribute(
                    layer, "GKODAS");
            System.out.println("5");
            Main.mainWindow.addLayer(groupedLayer);
            return groupedLayer;

        } catch (Exception exception) {

            exception.printStackTrace();
        }
        return null;
    }

    // merges by combining geometries
    public SimpleFeature mergeToSingleFeature(
            List<SimpleFeature> simpleFeatureList) {

        try {

            List<Geometry> geometryList = new ArrayList<Geometry>();

            for (SimpleFeature simpleFeature : simpleFeatureList) {

                geometryList.add((Geometry) simpleFeature.getDefaultGeometry());
            }

            geometryList = Support.validateGeometryList(geometryList);

            Collection<Geometry> collectionGeometry = geometryList;

            Geometry combinedGeometry = Support
                    .combineIntoOneGeometry(collectionGeometry);

            SimpleFeature simpleFeature = simpleFeatureList.get(0);

            simpleFeature.setDefaultGeometry(combinedGeometry);

            return simpleFeature;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    private JPanel initPanel(JTable jTable) {

        try {

            JPanel jPanel = new JPanel();
            //
            // jTable.setPreferredScrollableViewportSize(getPreferredSize());
            jTable.setFillsViewportHeight(true);

            DefaultTableModel defaultTableModel = new DefaultTableModel();
            jTable.setModel(defaultTableModel);
            // jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            jTable.setPreferredScrollableViewportSize(new Dimension(500, 200));

            // Create the scroll pane and add the table to it.
            JScrollPane scrollPane = new JScrollPane(jTable);

            // Add the scroll pane to this panel.
            jPanel.add(scrollPane);

            return jPanel;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

}
