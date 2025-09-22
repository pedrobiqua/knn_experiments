package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.classifiers.lazy.neighboursearch.LinearNNSearch;
import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;

public class SWINN extends NearestNeighbourSearch {

    public class CyclicUUID {

        private final int max;
        private int current;

        public CyclicUUID(int max) {
            this.max = max;
            this.current = 0;
        }

        public int next() {
            int uuid = current;
            current = (current + 1) % max; // ciclo automático
            return uuid;
        }

        // Opcional: pega o valor atual sem avançar
        public int getCurrent() {
            return current;
        }
    }

    private Vertex[] data;
    private CyclicUUID uuidGenerator;
    private boolean index = false;
    private int warmUp;
    private int window_size;

    public SWINN() {
        this.window_size = 1000;
        this.warmUp = 500;
        this.data = new Vertex[window_size];
        this.uuidGenerator = new CyclicUUID(window_size);
        this.index = false;
    }

    public SWINN(int window_size) {
        this.window_size = window_size;
        this.data = new Vertex[window_size];
        this.warmUp = 500;
        this.uuidGenerator = new CyclicUUID(window_size);
    }

    @Override
    public Instance nearestNeighbour(Instance target) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nearestNeighbour'");
    }

    @Override
    public Instances kNearestNeighbours(Instance target, int k) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'kNearestNeighbours'");
    }

    @Override
    public double[] getDistances() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistances'");
    }

    @Override
    public void update(Instance ins) throws Exception {
        // AQUI VAI SER EQUIVALENTE AO APPEND DO RIVER
        Vertex node = new Vertex(ins, uuidGenerator.next());
        // Aqui espera a janela encher para moder montarum grafo inicial
        if (!this.index) {
            this.data[uuidGenerator.next()] = node;
            if (this.data.length >= this.warmUp) {
                // Inicia o grafo
                // Roda uma função de refino?!
                this.index = true;
            }
            return;
        }

        if (this.data.length == this.window_size) {
            // REMOVER OS NODE MAIS ANTIGO
        }

        // Faz as demais operações


    }


    private Instances search(Instance target, int k) {
        NearestNeighbourSearch search;
        if (this.data.length <= this.warmUp) {
            // Se ainda não tiver instancias suficientes
            // Precisamos fazer a abordagem normal, isso de acordo com o river

            // search = new LinearNNSearch();
            // return search.kNearestNeighbours(target, k);
        }

        return search(target, k, 0.1);
    }

    private Instances search(Instance target, int k, double episolon) {
        // MONTAR AQUI A BUSCAS DAS INSTANCIAS MAIS PROXIMAS
        return null;
    }
    
}
