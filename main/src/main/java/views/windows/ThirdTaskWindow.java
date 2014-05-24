package views.windows;

import handlers.ThirdTaskHandler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.geotools.main.Validator;

import com.vividsolutions.jts.geom.Geometry;

import setsRelated.GeometrySet;
import views.other.JTextFieldNumbersSupported;
import views.panels.VerticalPanel;
import views.panels.VerticalPanel.HorzPanel;

public class ThirdTaskWindow extends JFrame {

    protected static Color DARK_GREEN = new Color(79, 195, 31);

    // swing related
    JTextFieldNumbersSupported maxDistanceFromLakeTxtField;
    JTextFieldNumbersSupported populationUpperLimitTxtField;
    JTextFieldNumbersSupported minForestAreaTxtField;
    JTextFieldNumbersSupported minRoadLengthInForestTxtField;

    private JLabel isSelectedLabel;

    // logic related
    private ThirdTaskHandler thirdTaskHandler;

    // third task handler field

    /**
     * 
     */
    private static final long serialVersionUID = -8864344732419382635L;

    public ThirdTaskWindow() {

        initThirdTaskWindow();
    }

    public ThirdTaskWindow(ThirdTaskHandler thirdTaskHandler) {

        this();
        this.thirdTaskHandler = thirdTaskHandler;

    }

    void initThirdTaskWindow() {

        // Create and set up the window.
        setTitle("Trečia užduotis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 200));

        VerticalPanel verticalPanel = new VerticalPanel();

        /*
         * initialize text fields
         */
        int jTextFieldWidth = 13;

        maxDistanceFromLakeTxtField = new JTextFieldNumbersSupported(
                jTextFieldWidth);
        maxDistanceFromLakeTxtField.setText(1000);
        
        populationUpperLimitTxtField = new JTextFieldNumbersSupported(
                jTextFieldWidth);
        populationUpperLimitTxtField.setText(1000);
        
        minForestAreaTxtField = new JTextFieldNumbersSupported(jTextFieldWidth);
        minForestAreaTxtField.setText(60000);
        
        minRoadLengthInForestTxtField = new JTextFieldNumbersSupported(
                jTextFieldWidth);
        minRoadLengthInForestTxtField.setText(200);

        JButton getThirdTaskResultsButton = new JButton("Find routes");

        getThirdTaskResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getThirdTaskResults();

            }
        });
        
        JButton getSelectedRectangleButton = new JButton("Get selected rectangle");

        getSelectedRectangleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSelectedRectangle();

            }
        });

        verticalPanel.add("Distance from lake no larger than",
                maxDistanceFromLakeTxtField);
        verticalPanel.add("Population less than", populationUpperLimitTxtField);
        verticalPanel.add("Forest area at least", minForestAreaTxtField);
        verticalPanel.add("Road length in forest at least",
                minRoadLengthInForestTxtField);

        this.isSelectedLabel = new JLabel();
        changeRectangleIsSelectedLabelText(false);

        verticalPanel.add(isSelectedLabel, getSelectedRectangleButton);
        
        verticalPanel.add(getThirdTaskResultsButton);

        add(verticalPanel);

        pack();

    }

    private void setHandlerFieldsWithGuiValues() {

        try {

            thirdTaskHandler.setMaxDistanceFromLake(maxDistanceFromLakeTxtField
                    .getDouble());
            thirdTaskHandler
                    .setMaxForestArea(minForestAreaTxtField.getDouble());
            thirdTaskHandler
                    .setMinRoadLengthInForest(minRoadLengthInForestTxtField
                            .getDouble());
            thirdTaskHandler
                    .setPopulationUpperLimit(populationUpperLimitTxtField
                            .getDouble());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getThirdTaskResults() {
        setHandlerFieldsWithGuiValues();
        thirdTaskHandler.start();
    }

    public void getSelectedRectangle() {

        changeRectangleIsSelectedLabelText(false);
        thirdTaskHandler.selectRectangle();
    }

    
    public void changeRectangleIsSelectedLabelText(boolean isSelected) {
        
        if (isSelected) {

            isSelectedLabel.setText("Map area is selected");
            isSelectedLabel.setForeground(DARK_GREEN);

        } else {

            isSelectedLabel.setText("Map area not is selected");
            isSelectedLabel.setForeground(Color.RED);
        }
        
    }

    public void displayPopUpBox(String message) {

        JOptionPane.showMessageDialog(this, message);

    }

    public int displayYesNoPopUpBox(String question) {

        int dialogResult = JOptionPane.showConfirmDialog(null, question,
                "Warning", JOptionPane.YES_NO_OPTION);

        if (dialogResult == JOptionPane.YES_OPTION) {
            return 1;
        } else {
            return 0;
        }

    }

    public ThirdTaskHandler getThirdTaskHandler() {
        return thirdTaskHandler;
    }

    public void setThirdTaskHandler(ThirdTaskHandler thirdTaskHandler) {

        try {

            Validator.checkNullPointerPassed(thirdTaskHandler);

            this.thirdTaskHandler = thirdTaskHandler;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // // test window
    // public static void main(String[] args) {
    //
    // ThirdTaskWindow thirdTaskWindow = new ThirdTaskWindow();
    // }

}
