package com.experiments_knn.lazy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.experiments_knn.datastructure.Vertex;
import com.yahoo.labs.samoa.instances.Instance;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;

public class ANN extends AbstractClassifier implements MultiClassClassifier {
    // Aqui vai ser amazenado a minha lista 
    private int window_size = 500;

    public ANN() {
        
    }

    @Override
    public boolean isRandomizable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRandomizable'");
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVotesForInstance'");
    }

    @Override
    public void resetLearningImpl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetLearningImpl'");
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        // Sliding window
        

    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelMeasurementsImpl'");
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelDescription'");
    }
    
}
