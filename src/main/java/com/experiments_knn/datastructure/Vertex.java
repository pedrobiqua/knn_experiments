package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;

public class Vertex {

        private Instance item;
        private int uuid;
        private List<Vertex> edges;
        private List<Vertex> rEdges;

        public Vertex(Instance item, int uuid) {
            this.item = item;
            this.uuid = uuid;
            this.edges = new ArrayList<>();
            this.rEdges = new ArrayList<>();
        }

        public Instance getItem() { return item; }
        public int getUuid() { return uuid; }

        public List<Vertex> getedges() { return edges; }
        public List<Vertex> getrEdges() { return rEdges; }

        public void addNeighbor(Vertex v) {
            if (!edges.contains(v)) {
                edges.add(v);
            }
        }

        public void addRNeighbor(Vertex v) {
            if (!rEdges.contains(v)) {
                rEdges.add(v);
            }
        }

        public void removeNeighbor(Vertex v) {
            edges.remove(v);
        }

        public void removeRNeighbor(Vertex v) {
            rEdges.remove(v);
        }

        public boolean hasedges() {
            return !edges.isEmpty();
        }

        public boolean hasrEdges() {
            return !rEdges.isEmpty();
        }

        public void farewell() {
            edges.clear();
            rEdges.clear();
        }
    }
