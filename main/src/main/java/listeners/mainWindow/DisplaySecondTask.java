package listeners.mainWindow;

import handlers.MapHandler;
import handlers.SelectHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.main.Support;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;

import views.windows.FeatureTableWindow;
import views.windows.SecondTask;

public class DisplaySecondTask implements ActionListener {

    private SecondTask secondTaskWindow;
    private MapHandler mapHandler;
    private SelectHandler selectHandler;

    public DisplaySecondTask(SecondTask secondTaskWindow,
            MapHandler mapHandler, SelectHandler selectHandler) {

        super();

        if (secondTaskWindow == null || mapHandler == null
                || selectHandler == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.secondTaskWindow = secondTaskWindow;
        this.mapHandler = mapHandler;
        this.selectHandler = selectHandler;
    }

    public void actionPerformed(ActionEvent e) {

        try {
            // Simple
            List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
            simpleFeatureList.addAll(selectHandler.getSelectedFeatures());

            Collection<SimpleFeature> simpleFeatureCollection = simpleFeatureList;

            Layer selectedLayer = mapHandler.getSelectedOrVisibleLayer(true,
                    false).get(0);
            List<SimpleFeature> selectedSimpleFeatureList = new ArrayList<SimpleFeature>();
            selectedSimpleFeatureList.addAll(selectHandler
                    .getFeaturesBelongingToLayer(selectedLayer,
                            simpleFeatureCollection));
            
            Layer newLayer = Support.simpleFeatureListToLayer(selectedSimpleFeatureList);
            
            this.secondTaskWindow.start(newLayer);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }

}