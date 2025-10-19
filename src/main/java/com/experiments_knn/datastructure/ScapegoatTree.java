package com.experiments_knn.datastructure;

import java.util.ArrayList;

class Node {
    int key;
    Node left, right, parent;
    int size; // tamanho da subárvore

    Node(int key) {
        this.key = key;
        left = right = parent = null;
        size = 1;
    }
}

class ScapegoatTree {
    Node root;
    int size; // número total de nós
    int maxSize; // maior tamanho histórico
    double alpha; // parâmetro de balanceamento

    public ScapegoatTree(double alpha) {
        this.alpha = alpha;
        root = null;
        size = 0;
        maxSize = 0;
    }

    // Inserção principal
    public void insert(int key) {
        Node newNode = new Node(key);
        root = bstInsert(root, newNode, null);

        // Atualiza tamanho total e maxSize
        size++;
        maxSize = Math.max(size, maxSize);

        // Atualiza tamanho das subárvores subindo da nova folha
        Node node = newNode;
        while (node != null) {
            node.size = 1 + getSize(node.left) + getSize(node.right);
            node = node.parent;
        }

        // Calcula h_a
        int ha = (int) Math.floor(Math.log(size) / Math.log(1.0 / alpha));

        // Verifica profundidade da nova folha
        if (depth(newNode) > ha) {
            Node scapegoat = findScapegoat(newNode);
            if (scapegoat != null) {
                rebuildSubtree(scapegoat);
            }
        }
    }

    // Inserção normal BST
    private Node bstInsert(Node root, Node node, Node parent) {
        if (root == null) {
            node.parent = parent;
            return node;
        }
        if (node.key < root.key) {
            root.left = bstInsert(root.left, node, root);
        } else {
            root.right = bstInsert(root.right, node, root);
        }
        return root;
    }

    // Retorna tamanho da subárvore
    private int getSize(Node node) {
        return node == null ? 0 : node.size;
    }

    // Calcula profundidade de um nó
    private int depth(Node node) {
        int d = 0;
        while (node != root) {
            node = node.parent;
            d++;
        }
        return d;
    }

    // Procura o scapegoat subindo pelos ancestrais
    private Node findScapegoat(Node node) {
        Node parent = node.parent;
        while (parent != null) {
            int leftSize = getSize(parent.left);
            int rightSize = getSize(parent.right);
            int total = leftSize + rightSize + 1;
            if (Math.max(leftSize, rightSize) > alpha * total) {
                return parent;
            }
            parent = parent.parent;
        }
        return null;
    }

    // Reconstrói a subárvore do scapegoat
    private void rebuildSubtree(Node scapegoat) {
        // Para simplificação, pega todos os nós da subárvore
        // e reconstrói como árvore perfeitamente balanceada
        Node[] nodes = flatten(scapegoat);
        Node parent = scapegoat.parent;

        Node newSubtree = buildBalanced(nodes, 0, nodes.length - 1, parent);

        if (parent == null) {
            root = newSubtree;
        } else if (parent.left == scapegoat) {
            parent.left = newSubtree;
        } else {
            parent.right = newSubtree;
        }
    }

    // Converte subárvore em array ordenado
    private Node[] flatten(Node root) {
        ArrayList<Node> list = new ArrayList<>();
        inorder(root, list);
        return list.toArray(new Node[0]);
    }

    private void inorder(Node node, ArrayList<Node> list) {
        if (node == null)
            return;
        inorder(node.left, list);
        node.left = node.right = node.parent = null; // limpar ligações antigas
        list.add(node);
        inorder(node.right, list);
    }

    // Constroi árvore balanceada a partir do array
    private Node buildBalanced(Node[] nodes, int start, int end, Node parent) {
        if (start > end)
            return null;
        int mid = (start + end) / 2;
        Node root = nodes[mid];
        root.parent = parent;
        root.left = buildBalanced(nodes, start, mid - 1, root);
        root.right = buildBalanced(nodes, mid + 1, end, root);
        root.size = 1 + getSize(root.left) + getSize(root.right);
        return root;
    }
}
