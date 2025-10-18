package com.experiments_knn.lazy;

import com.yahoo.labs.samoa.instances.Instance;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;

/**
 * USAR ESSE KNN PARA APENAS TESTAR A QUESTÃO DO DESBALANCEAMENTO, VOU USAR
 * AQUELA FUNÇÃO QUE CALCULA ISSO
 * ISSO APLICADO PARA DIVERSOS STREAMS, USO INSERT E DELETE? -> PEGAR DO LIVRO
 */
public class KNNTest extends AbstractClassifier implements MultiClassClassifier {

    @Override
    public boolean isRandomizable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isRandomizable'");
    }

    @Override
    public void getModelDescription(StringBuilder arg0, int arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelDescription'");
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelMeasurementsImpl'");
    }

    /**
     * recriar a árvore sempre?
     */
    @Override
    public double[] getVotesForInstance(Instance arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVotesForInstance'");
    }

    @Override
    public void resetLearningImpl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetLearningImpl'");
    }

    @Override
    public void trainOnInstanceImpl(Instance arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'trainOnInstanceImpl'");
    }

}
