package interfaces;

import org.geotools.map.Layer;

public interface ICommonOperations {

    //add selected layer from map
    int addSelectedLayersFromMap();

    //add layer to some content
    int addLayer(Layer layer);

    //add selected layer to map 
    int addSelectedLayersToMap();

    //remove selected
    int removeSelected();
}
