package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import moa.core.TimingUtils;

public class KDtree extends NearestNeighbourSearch implements StreamNeighborSearch {

    public class Node {
        Node left, right, parent;
        Instance instance;
        int splitDim;

        public Node(Instance inst, int splitDim) {
            this.instance = inst;
            this.splitDim = splitDim;
            this.left = null;
            this.right = null;
        }

        public Node(Instance value, Node left, Node right, int splitDim) {
            this.instance = value;
            this.left = left;
            this.right = right;
            this.splitDim = splitDim;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        // NÃO SEI SE VOU USAR, POREM APRENDI COMO VERIFICA SE UMA ARVORE ESTÁ
        // BALANCEADA, O RUIM DISSO É SER O(n), NÃO SEI SE VAI SER VIAVEL PRA PESQUISA
        public static boolean isBalanced(Node root) {
            // Verificar a altura da esquerda e da direita
            // ver se a diferença é muito grande
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

        private static int height(Node root) {

            if (root == null) {
                return 0;
            }

            return 1 + (Math.max(height(root.left), height(root.right)));
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
    private int numNodes;

    public KDtree(int numDim) {
        this.numDim = numDim;
        this.numNodes = 0;
        this.bests = new PriorityQueue<>(Comparator.comparingDouble((NodeDistPair p) -> p.dist).reversed());
    }

    public KDtree(Instances insts) {
        this.numDim = insts.get(0).numValues() - 1;
        long startTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        this.root = build(insts, 0);
        long endTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        System.out.println("Build: " + TimingUtils.nanoTimeToSeconds(endTime - startTime));
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
        long startTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        search(this.root, target, 0);
        long endTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        System.out.println("Search: " + TimingUtils.nanoTimeToSeconds(endTime - startTime));

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

        if (target.value(axis) < root.instance.value(axis)) {
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

        double diff = target.value(axis) - root.instance.value(axis);
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

    private Node findNode(Node p, Instance inst) {
        if (p == null) {
            return p;
        }

        if (p.instance.equals(inst)) {
            return p;
        }

        int dim = p.splitDim;
        if (inst.toDoubleArray()[dim] < p.instance.toDoubleArray()[dim]) {
            return findNode(p.left, inst);
        } else {
            return findNode(p.right, inst);
        }
    }

    @Override
    public double[] getDistances() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistances'");
    }

    Node build(Instances insts, int depth) {
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
        return new Node(medianInstance,
                build(instsToTheLeft, (depth + 1) % this.numDim),
                build(instsToTheRight, (depth + 1) % this.numDim),
                depth);
    }

    // Funções da Interface que vao ser usadas para o KDTree dinamico em Streams de
    // dados
    @Override
    public void update(Instance ins) throws Exception {
        int depth = 0;
        Node p = this.root;
        Node prev = null;

        while (p != null) {
            prev = p;
            int axis = depth % numDim;
            if (ins.toDoubleArray()[axis] < p.instance.toDoubleArray()[axis]) {
                p = p.left;
            } else {
                p = p.right;
            }

            depth++;
        }

        this.numNodes++;

        // Se for nulo o meu root
        if (root == null) {
            root = new Node(ins, 0);
            return;
        }

        // Profundidade de prev
        int axis = (depth - 1) % numDim;

        if (ins.toDoubleArray()[axis] < prev.instance.toDoubleArray()[axis]) {
            prev.left = new Node(ins, (depth % numDim));
        } else {
            prev.right = new Node(ins, (depth % numDim));
        }
    }

    @Override
    public void removeInstance(Instance inst) throws Exception {
        Node p = findNode(this.root, inst); // No que eu quero remover
        // AVISO !!!
        // ISSO ESTÁ ERRADO, POIS SE EU APAGAR O ROOT, DEVO MEXER EM TODOS OS SPLIT
        // DIMENSION
        // FAZENDO COM QUE FIQUE ERRADO AS OPERAÇÕES SEGUINTES
        // ALTERAR O FIND NODE PARA BUSCAR A PARTIR DO SPLIIT DIM
        // E RETORNAR ESSE VALOR E O NODE
        delete(p, p.splitDim);
    }

    private void delete(Node p, int discriminator) {
        if (p.isLeaf()) {
            p = null;
            return;
        }

        Node q = null;
        if (p.right != null) {
            q = smallest(p.right, discriminator, ((discriminator + 1) % numDim));
        } else {
            q = smallest(p.left, discriminator, ((discriminator + 1) % numDim));
            p.right = p.left;
            p.left = null;
        }

        p.instance = q.instance;
        delete(q, discriminator);
    }

    private Node smallest(Node q, int i, int j) {
        Node qq = q;
        if (i == j) {
            if (q.left != null) {
                qq = q = q.left;
            } else {
                return q;
            }
        }

        if (q.left != null) {
            Node left = smallest(qq.left, i, ((j + 1) % numDim));
            if (qq.instance.toDoubleArray()[i] >= left.instance.toDoubleArray()[i]) {
                qq = left;
            }
        }

        if (q.right != null) {
            Node right = smallest(qq.right, i, ((j + 1) % numDim));
            if (qq.instance.toDoubleArray()[i] >= right.instance.toDoubleArray()[i]) {
                qq = right;
            }
        }

        return qq;
    }

    @Override
    public void setInstances(Instances insts) throws Exception {
        // TODO Auto-generated method stub
        super.setInstances(insts);
    }

    @Override
    public boolean isToRebuild() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isToRebuild'");
    }

}
