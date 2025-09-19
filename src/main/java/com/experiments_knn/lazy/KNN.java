package com.experiments_knn.lazy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.lazy.neighboursearch.LinearNNSearch;
import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import moa.core.Measurement;

/*
 * AQUI, PODERIA USAR O DO MOA, POREM AQUI DEIXO APENAS O FORCE BRUTE E USO O SLIDING WINDOW usando o LinkedList
 */
public class KNN extends AbstractClassifier implements MultiClassClassifier {

    LinkedList<Instance> window;
    int window_size = 1000;
    int num_class = 0;

    public class AttributesUtils {
        public static List<Attribute> copyAtributes(Instances originalDataset){
            List<Attribute> atributos = new ArrayList<Attribute>(originalDataset.numAttributes());
            for(int i =0; i < originalDataset.numAttributes(); i++){
                Attribute atribOriginal = originalDataset.attribute(i);
                atributos.add(atribOriginal);
            }
            return atributos;
        }
        
        public static ArrayList<Attribute> copyAtributes(Instance instance){
            ArrayList<Attribute> atributes = new ArrayList<Attribute>(instance.numAttributes());
            for(int i =0; i < instance.numAttributes(); i++){
                    Attribute atribOriginal = instance.attribute(i);
                    atributes.add(atribOriginal);
            }
            return atributes;
        }
    }

    public class InstancesUtils {
	
        public static Instances gerarDataset(List<Instance> instancias, String nomeDataset) throws Exception{
            ArrayList<Attribute> atributos = AttributesUtils.copyAtributes(instancias.get(0));
            
            Instances retorno = new Instances(nomeDataset, atributos,
                    instancias.size());
            for(Instance inst : instancias){
                retorno.add(inst);
            }
            
            retorno.setClassIndex(retorno.numAttributes() - 1);
            
            return retorno;
        }
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double[] v = new double[num_class + 1];
        try {
            NearestNeighbourSearch search;
            Instances window_instances = InstancesUtils.gerarDataset(window, "Validation Instances");
            search = new LinearNNSearch(window_instances);
            Instances neighbours = search.kNearestNeighbours(inst, 3);
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
        // Sliding window
        if (num_class > 0) {
            num_class = (int) inst.classValue();
        }
        if (window == null) {
            window = new LinkedList<Instance>();
        }

        if (window.size() < window_size) {
            window.add(inst);
        } else {
            window.remove(0);
            window.add(inst);
        }

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
