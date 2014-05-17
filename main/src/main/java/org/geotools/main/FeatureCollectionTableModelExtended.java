package org.geotools.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public  class FeatureCollectionTableModelExtended extends FeatureCollectionTableModel {

    private static final long serialVersionUID = -1301090739881878263L;

    private List<SimpleFeature> simpleFeatureList;

    public FeatureCollectionTableModelExtended(
            SimpleFeatureCollection simpleFeatureCollection) {
        
        super(simpleFeatureCollection);
        this.simpleFeatureList = DataUtilities.list(simpleFeatureCollection);
    }

    public List<SimpleFeature> getSimpleFeatures(int indexes[]) {

        List<SimpleFeature> simpleFeatureList = null;

        try {

            simpleFeatureList = new ArrayList<SimpleFeature>();

            for (int i = 0; i < indexes.length; i++) {

                int index = indexes[i];
                simpleFeatureList.add(this.simpleFeatureList.get(index));
            }
            return simpleFeatureList;

        } catch (Exception e) {

            // something went wrong
            e.printStackTrace();

            return null;
        }

    }
    
    public List<SimpleFeature>  getSimpleFeatureList() {
        return this.simpleFeatureList;
    }

}
