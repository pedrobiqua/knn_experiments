package com.experiments_knn;

import moa.streams.ArffFileStream;
import moa.classifiers.Classifier;
import moa.classifiers.lazy.RW_kNN;
import moa.classifiers.lazy.kNN;
import weka.core.Utils;
import moa.core.InstanceExample;
import moa.core.TimingUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// import com.experiments_knn.lazy.ANN;
// import com.experiments_knn.lazy.kNNCamberra;
import com.experiments_knn.lazy.KNN;
import com.experiments_knn.lazy.kNNCamberra;
import com.experiments_knn.lazy.kNNKDTree;
import com.yahoo.labs.samoa.instances.Instance;

public class App {
    public static void main(String[] args) throws Exception {
        // Configuração dataset
        // WriteStreamToARFFFile -s (generators.AgrawalGenerator -b) -f
        // /home/pedro/projects/knn_experiments/src/main/resources/dataset/agrawal_dataset.arff
        // -m 1000000
        // WriteStreamToARFFFile -s generators.AssetNegotiationGenerator -f
        // /home/pedro/projects/knn_experiments/src/main/resources/dataset/asset_negotiation_dataset.arff
        // -m 1000000
        // WriteStreamToARFFFile -s generators.HyperplaneGenerator -f
        // /home/pedro/projects/knn_experiments/src/main/resources/dataset/hyperplane_dataset.arff
        // -m 1000000
        // WriteStreamToARFFFile -s (generators.SEAGenerator -b) -f
        // /home/pedro/projects/knn_experiments/src/main/resources/dataset/sea_dataset.arff
        // -m 1000000
        // WriteStreamToARFFFile -s (generators.STAGGERGenerator -b) -f
        // /home/pedro/projects/knn_experiments/src/main/resources/dataset/stagger_dataset.arff
        // -m 1000000

        // Lista de datasets
        String[] datasets = {
                "dataset/agrawal_dataset.arff",
                "dataset/asset_negotiation_dataset.arff",
                "dataset/hyperplane_dataset.arff",
                "dataset/sea_dataset.arff",
                "dataset/stagger_dataset.arff"
        };

        // Loop sobre cada dataset
        for (String datasetPath : datasets) {
            System.out.println("\n=== Executando dataset: " + datasetPath + " ===");
            String arffFile = App.class.getClassLoader().getResource(datasetPath).getPath();
            ArffFileStream stream = new ArffFileStream(arffFile, -1);

            // Define a classe (último atributo)
            if (stream.getHeader().classIndex() == -1) {
                stream.getHeader().setClassIndex(stream.getHeader().numAttributes() - 1);
            }

            ArrayList<Classifier> classifiers = new ArrayList<>();
            // classifiers.add(new kNNKDTree()); // Minha implementação de KDTree. // Não está bom, pq?
            // Vendo os tempos, está muito mais lento que o do MOA. Principalmente no build.
            // A busca está boa. No build verifiquei que é diferente do MOA. O MOOA faz algumas
            // otimizações.
            // Talvez seja isso.
            classifiers.add(new kNNCamberra()); // Impl do Eduardo.
            kNN knn_moa = new kNN();
            knn_moa.nearestNeighbourSearchOption.setChosenIndex(1);
            classifiers.add(knn_moa); // kNN do MOA com KDTree
            classifiers.add(new KNN()); // KNN ingenuo Brute force aqui uso o LinearSearch
            classifiers.add(new RW_kNN()); // RW_KNN -> Tambem está no MOA e é um mais
            // recente | Usa 2 KDTree PADRÃO:
            // LinearNN Revouir: 500 Window: 500
            // classifiers.add(new ANN()); // Implementar o que está no River

            ArrayList<Double> times_median = new ArrayList<>();
            ArrayList<Double> accuracies = new ArrayList<>();
            ArrayList<Double> executionTime = new ArrayList<Double>();

            for (Classifier classifier : classifiers) {
                ArrayList<Double> times = new ArrayList<>();
                System.out.println("\nClassificador: " + classifier.getClass().getName());
                classifier.prepareForUse();
                stream.prepareForUse();

                int correct = 0;
                int total = 0;
                int count = 0;

                long startTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                while (stream.hasMoreInstances()) {
                    count++;
                    InstanceExample ex = stream.nextInstance();
                    Instance inst = ex.getData();

                    long predictStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                    double[] votes = classifier.getVotesForInstance(inst);
                    long predictEndTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                    double time = TimingUtils.nanoTimeToSeconds(predictEndTime - predictStartTime);
                    System.out.println(time);

                    times.add(TimingUtils.nanoTimeToSeconds(predictEndTime - predictStartTime));

                    int predicted = Utils.maxIndex(votes);
                    int trueClass = (int) inst.classValue();

                    if (predicted == trueClass) {
                        correct++;
                    }
                    total++;

                    classifier.trainOnInstance(inst);
                }

                long endTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
                double timeExecution = TimingUtils.nanoTimeToSeconds(endTime - startTime);
                executionTime.add(timeExecution);

                double sumTimes = 0;
                for (Double time : times) {
                    sumTimes += time;
                }

                double median = sumTimes / (double) times.size();
                times_median.add(median);

                double accuracy = correct / (double) total;
                accuracies.add(accuracy);

                System.out.println("Acurácia final: " + accuracy);
                System.out.println("Média: " + median);
                System.out.println("Tempo total: " + timeExecution);
            }

            // Salvar resultados em CSV
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String outputPath = App.class.getClassLoader().getResource("dataset").getPath() + "/results_"
                    + datasetPath.replace("/", "_") + "_" + date + ".csv";

            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.append("Classifier,Accuracy,MeanTimePredict,ExecutionTime\n");
                for (int i = 0; i < classifiers.size(); i++) {
                    writer.append(classifiers.get(i).getClass().getSimpleName())
                            .append(",")
                            .append(String.valueOf(accuracies.get(i)))
                            .append(",")
                            .append(String.valueOf(times_median.get(i)))
                            .append(",")
                            .append(String.valueOf(executionTime.get(i)))
                            .append("\n");
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Resultados salvos em: " + outputPath);
        }
    }
}
