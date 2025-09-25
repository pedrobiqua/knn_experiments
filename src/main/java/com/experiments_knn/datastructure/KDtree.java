package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;

public class KDtree extends NearestNeighbourSearch {

    public class Node {
        Node left;
        Node right;
        Instance instance;
        int splitDim;

        public Node(Instance value, Node left, Node right, int splitDim) {
            this.instance = value;
            this.left = left;
            this.right = right;
            this.splitDim = splitDim;
        }
    }

    public class NodeDistPair {
        Node node;
        double dist;

        NodeDistPair(Node node, double dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    private Node root;
    private int numDim;
    private int k;
    private PriorityQueue<NodeDistPair> bests;

    public KDtree(Instances insts) {
        this.numDim = insts.get(0).numValues() - 1;
        this.root = build(insts, 0);
        this.bests = new PriorityQueue<>(Comparator.comparingDouble((NodeDistPair p) -> p.dist).reversed());
    }

    @Override
    public Instance nearestNeighbour(Instance target) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nearestNeighbour'");
    }

    @Override
    public Instances kNearestNeighbours(Instance target, int k) throws Exception {
        this.k = k;
        search(this.root, target, 0);

        InstancesHeader header = (InstancesHeader) target.dataset();
        Instances insts = new Instances(header);

        // Adiciona todas as instâncias da fila
        for (NodeDistPair pair : bests) {
            insts.add(pair.node.instance);
        }

        return insts;
    }

    public void search(Node root, Instance target, int depth) {
        if (root == null) {
            return;
        }

        Node next_branch;
        Node other_branch;

        int axis = depth % numDim;

        if (target.toDoubleArray()[axis] < root.instance.toDoubleArray()[axis]) {
            next_branch = root.left;
            other_branch = root.right;
        } else {
            next_branch = root.right;
            other_branch = root.left;
        }

        search(next_branch, target, depth + 1);
        double dist = distSquared(target, root.instance);

        // Armazena na priority queue
        if (bests.size() < this.k) {
            bests.add(new NodeDistPair(root, dist));
        } else if (dist < bests.peek().dist) {
            bests.poll();
            bests.add(new NodeDistPair(root, dist));
        }

        double diff = target.toDoubleArray()[axis] - root.instance.toDoubleArray()[axis];
        // Verifico se preciso ir para o other_branch, a outra subarvore
        if (bests.size() < k || diff * diff < bests.peek().dist) {
            search(other_branch, target, depth + 1);
        }

    }

    public double distSquared(Instance p0, Instance p1) {
        double total = 0.0;

        for (int i = 0; i < this.numDim; i++) {
            double diff = p0.value(i) - p1.value(i);
            total += diff * diff; // evita pow, mais eficiente
        }

        return total;
    }

    @Override
    public double[] getDistances() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistances'");
    }

    @Override
    public void update(Instance ins) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    Node build(Instances insts, int depth) {
        if (insts.size() == 0) {
            return null;
        }

        int axis = depth % numDim;
        ArrayList<Double> values_dim = new ArrayList<>();
        for (int i = 0; i < insts.size(); i++) {
            values_dim.add(insts.get(i).toDoubleArray()[axis]);
        }

        Collections.sort(values_dim);
        int median = values_dim.size() / 2;
        double value_median = values_dim.get(median);
        Instance node_value = null;

        Instances left = new Instances(insts, insts.size());
        Instances right = new Instances(insts, insts.size());

        for (int i = 0; i < values_dim.size(); i++) {
            double value = values_dim.get(i);
            if (value == value_median && node_value == null) {
                node_value = insts.get(i);
            } else if (value < value_median) {
                left.add(insts.get(i));
            } else {
                right.add(insts.get(i));
            }
        }

        return new Node(
                node_value,
                build(left, depth + 1),
                build(right, depth + 1),
                depth % numDim);
    }

}
