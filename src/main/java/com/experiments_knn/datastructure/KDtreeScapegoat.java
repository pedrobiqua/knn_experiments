package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.Collections;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

public class KDtreeScapegoat {
    public class Node {
        Instance values;
        Node left;
        Node right;
        int split_dim;

        public Node(Instance inst, int split_dim) {
            values = inst;
            this.split_dim = split_dim;
        }

        public Node(Node left, Node right, Instance values, int split_dim) {
            this.left = left;
            this.right = right;

            this.values = values;
            this.split_dim = split_dim;
        }
    }

    private Node root;
    private int numDim;
    private int numNodes;
    private int maxNodes;

    double alpha = 2.0 / 3.0;

    public KDtreeScapegoat(Instances insts) {
        this.numDim = insts.get(0).numValues() - 1;
        build_tree(insts);
    }

    public void insert_node(Instance ins) {
        int depth = 0;
        Node p = this.root;
        Node prev = null;

        while (p != null) {
            prev = p;
            int axis = depth % numDim;
            if (ins.toDoubleArray()[axis] < p.values.toDoubleArray()[axis]) {
                p = p.left;
            } else {
                p = p.right;
            }

            depth++;
        }

        this.numNodes++;
        if (numNodes > maxNodes) {
            maxNodes = numNodes;
        }

        if (root == null) {
            root = new Node(ins, 0);
            return;
        }

        // Profundidade de prev
        int axis = (depth - 1) % numDim;

        if (ins.toDoubleArray()[axis] < prev.values.toDoubleArray()[axis]) {
            prev.left = new Node(ins, (depth % numDim));
        } else {
            prev.right = new Node(ins, (depth % numDim));
        }
    }

    public int calculate_a_height() {
        int n = numNodes;
        alpha = 2.0 / 3.0;
        double logBase = Math.log(n) / Math.log(1.0 / alpha);
        int ha = (int) Math.floor(logBase);
        return ha;
    }

    public int height(Node node) {
        if (node == null)
            return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public int height() {
        return height(root);
    }

    public boolean isBalanced(Node root) {
        if (root == null) {
            return true;
        }

        int leftHeight = height(root.left);
        int rightHeight = height(root.right);

        if (Math.abs((leftHeight - rightHeight)) > 1) {
            return false;
        }

        return isBalanced(root.left) && isBalanced(root.right);
    }

    public boolean isTooDeep() {
        return height() > calculate_a_height();
    }

    public void build_tree(Instances insts) {
        this.root = build_tree_balanced(insts, 0);
    }

    private Node build_tree_balanced(Instances insts, int depth) {
        if (insts.size() == 0) {
            return null;
        }

        ArrayList<Double> values_insts = new ArrayList<Double>();
        for (int i = 0; i < insts.size(); i++) {
            values_insts.add(insts.get(i).toDoubleArray()[depth]);
        }

        Collections.sort(values_insts);
        double median = values_insts.get((int) (values_insts.size() + 1) / 2 - 1);
        Instance medianInstance = null;

        Instances instsToTheLeft = new Instances(insts, insts.size());
        Instances instsToTheRight = new Instances(insts, insts.size());

        for (int i = 0; i < insts.size(); i++) {
            if (insts.get(i).toDoubleArray()[depth] == median && medianInstance == null) {
                medianInstance = insts.get(i);
            } else if (insts.get(i).toDoubleArray()[depth] < median)
                instsToTheLeft.add(insts.get(i));
            else
                instsToTheRight.add(insts.get(i));
        }

        // Monta a árvore em cima dessa recursão
        return new Node(
                build_tree_balanced(instsToTheLeft, (depth + 1) % this.numDim),
                build_tree_balanced(instsToTheRight, (depth + 1) % this.numDim),
                medianInstance,
                depth);
    }

}
