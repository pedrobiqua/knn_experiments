package com.experiments_knn.lazy;

import java.util.LinkedList;

import com.experiments_knn.datastructure.KDtreeCanberra;
import com.experiments_knn.lazy.KNN.InstancesUtils;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
// import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import moa.core.Measurement;

public class kNNCamberra extends AbstractClassifier implements MultiClassClassifier {

    private LinkedList<Instance> window;
    private int window_size = 1000;
    private KDtreeCanberra search = new KDtreeCanberra();
    private boolean updateNNSearch = true;
    private boolean knnWasSet = false;

    @Override
    public boolean isRandomizable() {
        return false;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double[] v = new double[inst.numClasses()];
        try {
            if (updateNNSearch){
				this.rebuildTree();
			}
            Instances neighbours = this.search.kNearestNeighbours(inst, 3);
            for (int i = 0; i < neighbours.numInstances(); i++) {
                v[(int) neighbours.instance(i).classValue()]++;
            }
        } catch (Exception e) {
            return new double[inst.numClasses()];
        }
        return v;
    }

    @Override
    public void resetLearningImpl() {
        window = new LinkedList<Instance>();
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        try {
            // Sliding window
            if (window == null) {
                window = new LinkedList<Instance>();
            }

            if (knnWasSet) {
                search.update(inst);
            }

            if (search.isToRebuild()) {
                knnWasSet = false;
                rebuildTree();
            }

            if (window.size() < window_size) {
                window.add(inst);
            } else {
                Instance remove = window.getFirst();
                window.remove(0);
                window.add(inst);
                if (knnWasSet) {
                    search.removeInstance(remove);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelMeasurementsImpl'");
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {

    }

    ///////////////////////////////////
    // RECRIA A ÁRVORE
    private void rebuildTree() {
        try {
            Instances window_instances = InstancesUtils.gerarDataset(this.window, "Validation Instances");
            this.search = new KDtreeCanberra(window_instances);
            updateNNSearch = false;
            knnWasSet = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
