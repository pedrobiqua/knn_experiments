package com.experiments_knn;

import moa.streams.ArffFileStream;
import moa.classifiers.Classifier;
import moa.classifiers.lazy.RW_kNN;
import moa.classifiers.lazy.kNN;
import weka.core.Utils;
import moa.core.InstanceExample;
import moa.core.TimingUtils;

import java.util.ArrayList;

// import com.experiments_knn.lazy.ANN;
// import com.experiments_knn.lazy.kNNCamberra;
import com.experiments_knn.lazy.KNN;
import com.yahoo.labs.samoa.instances.Instance;

public class App {
    public static void main(String[] args) throws Exception {
        String arffFile = App.class.getClassLoader().getResource("dataset/teste.arff").getPath();
        ArffFileStream stream = new ArffFileStream(arffFile, -1);

        // Define a classe (último atributo)
        if (stream.getHeader().classIndex() == -1) {
            stream.getHeader().setClassIndex(stream.getHeader().numAttributes() - 1);
        }

        ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
        classifiers.add(new KNN()); // KNN ingenuo Brute force
        classifiers.add(new RW_kNN()); // RW_KNN -> Tambem está no MOA e é um mais recente | Usa KDTree
        classifiers.add(new kNN()); // kNN do MOA, usa KDTree
        // classifiers.add(new ANN()); // Implementar o que está no River 
        // classifiers.add(new kNNCanberra()) // kNN do eduardo, vou ter que adaptar

        ArrayList<Double> times_median = new ArrayList<Double>();
        for (Classifier classifier : classifiers) {
            ArrayList<Double> times = new ArrayList<Double>();
            System.out.println(classifier.getClass().getName());
            classifier.prepareForUse();
            stream.prepareForUse();

            int correct = 0;
            int total = 0;

            while (stream.hasMoreInstances()) {
                InstanceExample ex = stream.nextInstance();
                Instance inst = ex.getData();

                long predictStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                double[] votes = classifier.getVotesForInstance(inst);
                long predictEndTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                times.add(TimingUtils.nanoTimeToSeconds(predictEndTime - predictStartTime));
                
                int predicted = Utils.maxIndex(votes);
                int trueClass = (int) inst.classValue();

                if (predicted == trueClass) {
                    correct++;
                }
                total++;

                classifier.trainOnInstance(inst);
            }

            double sumTimes = 0;
            for (Double time : times) {
                sumTimes += time;
            }

            double median = sumTimes / (double)times.size();
            times_median.add(median);

            System.out.println("Acurácia final: " + (correct / (double) total));
            System.out.println("Média: " + median);
        }
    }
}
