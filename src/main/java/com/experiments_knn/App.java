package com.experiments_knn;

import moa.streams.ArffFileStream;
import moa.core.InstanceExample;
import com.yahoo.labs.samoa.instances.Instances;
import com.experiments_knn.lazy.KNN;
import com.yahoo.labs.samoa.instances.Instance;

public class App {
    public static void main(String[] args) throws Exception {
        String arffFile = "/home/pedro/projects/knn/src/main/java/com/experiments_knn/dataset/teste.arff";
        ArffFileStream stream = new ArffFileStream(arffFile, -1);
        stream.prepareForUse();

        Instances header = stream.getHeader();
        Instances dataset = new Instances(header);

        while (stream.hasMoreInstances()) {
            InstanceExample ex = stream.nextInstance();
            Instance inst = ex.getData();
            dataset.add(inst);
        }

        if (dataset.classIndex() == -1) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }

        // passa pro seu KNN (que espera um com.yahoo.labs.samoa.instances.Instances)
        KNN knn = new KNN(dataset);
        System.out.println("KNN criado com " + knn.getInstances().numInstances() + " instâncias.");
    }
}
