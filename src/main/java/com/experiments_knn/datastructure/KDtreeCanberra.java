package com.experiments_knn.datastructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.management.InstanceNotFoundException;

import org.apache.commons.math3.util.FastMath;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.lazy.neighboursearch.NearestNeighbourSearch;
import moa.classifiers.lazy.neighboursearch.NormalizableDistance;


public class KDtreeCanberra extends NearestNeighbourSearch {
    public class Node {
        Node left;
        Node right;
        Instance instance;
        boolean active;
        int splitDim;

        public Node(Instance inst, int dim) {
            this.instance = inst;
            this.splitDim = dim;
            this.active = true;
            this.left = null;
            this.right = null;
        }

        public Node(Instance inst, int dim, Node left, Node right) {
            this.instance = inst;
            this.splitDim = dim;
            this.active = true;
            this.left = left;
            this.right = right;
        }

        public boolean isAleaf() {
            return this.left == null && this.right == null;
        }

        public boolean isActive() {
            return this.active;
        }

        public double[] getValues() {
            return this.instance.toDoubleArray();
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        /**
         * Verifica se o Node é igual ao outro
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node other = (Node) o;

            boolean found = true;
            double[] infoInst1 = this.instance.toDoubleArray();
            double[] infoInst2 = other.instance.toDoubleArray();

            for (int i = 0; i < infoInst1.length; i++) {
                if (infoInst1[i] != infoInst2[i]) {
                    found = false;
                    break;
                }
            }
            return found;
        }
    }

    public class CanberraDistance extends NormalizableDistance {

        @Override
		public double distance(Instance first, Instance second) {
			double[] x = first.toDoubleArray();
			double[] y = second.toDoubleArray();

			int classIndex = first.classIndex();

			double sum = 0;

			for (int i = 0; i < x.length; i++) {
				if (i != classIndex) {
					double numerator = FastMath.abs(x[i]-y[i]);
					double denominator = FastMath.abs(x[i]) + FastMath.abs(y[i]);
					if (denominator != 0)
						sum += numerator / denominator;
				}
			}
			return sum;
		}


        @Override
        public String globalInfo() {
            return "Canberra Distance Function";
        }

        @Override
        protected double updateDistance(double currDist, double diff) {
            return 0;
        }

    }

    private Node root_;
    private int nDims;
    private int nNodes;
    private int numNodesDeactivated = 0;
    private int initialNumInstances = 0;
    private int numNeighbours = 0; // Uso isso dentro do kNearestNeighbours

    private NormalizableDistance distanceFunction = new CanberraDistance();
    private ArrayList<Instance> instancesList = new ArrayList<>();
    private ArrayList<Double> distancesList = new ArrayList<Double>();


    public KDtreeCanberra(Instances instances) {
        this.nDims = instances.get(0).numAttributes()-1;
        this.buildTree(instances);
    }

    public KDtreeCanberra() {}

    @Override
    public Instance nearestNeighbour(Instance target) throws Exception {
        Instances dist = kNearestNeighbours(target, 1);
		return dist.get(0);
    }

    @Override
    public Instances kNearestNeighbours(Instance target, int k) throws Exception {

        this.numNeighbours = k;
        // ArrayList<Double> distances = kNearestNeighboursRecursive(this.root_, target, k);
        ArrayList<Double> distances =  getDistancesOfBranches(this.root_, target);


        int kNeighbors;
		if (distances.size() < k)
			kNeighbors = distances.size();
		else
			kNeighbors = k;

		// Pega as instancias com as menores distancias
        InstancesHeader header = (InstancesHeader) target.dataset();
		Instances insts = new Instances(header);
		for (int i = 0; i < kNeighbors; i++) {
			insts.add(instancesList.get(i));
		}

		return insts;
    }

    /**
     * IMPLEMENTAÇÃO DO QUE EU ENTENDI NO ALGORITMO
     */
    public ArrayList<Double> kNearestNeighboursRecursive(Node node, Instance target, int k) {
        if (node == null) {
            return this.distancesList;
        }

        int splitDim = node.splitDim;
        Instance inst = node.instance;
        Node best = null;
        Node other = null;


        if (node.isActive()) {
            double distanceToNode = this.distanceFunction.distance(inst, target);
            if (this.distancesList.size() < k) {
                this.distancesList.add(distanceToNode);
                this.instancesList.add(inst);
            } else {

                // Get Maximun das distanciasList
                double maximum = distancesList.get(0);
				int maxIndex = 0;
				for (int i = 0; i < distancesList.size(); i++) {
					double toTest = distancesList.get(i);
					if (toTest > maximum) {
						maximum = toTest;
						maxIndex = i;
					}
				}


                if (distanceToNode <= distancesList.get(maxIndex)) {
					distancesList.remove(maxIndex);
					distancesList.add(distanceToNode);
					this.instancesList.remove(maxIndex);
					this.instancesList.add(inst);
				}

            }
        }

        if (node.left != null && node.right != null) {
            if (target.toDoubleArray()[splitDim] >= inst.toDoubleArray()[splitDim]) {
                best = node.right;
                other = node.left;
            } else {
                best = node.left;
                other = node.right;
            }
        } else if (node.left != null) {
            best = node.left;
        } else if (node.right != null) {
            best = node.right;
        } else {
            return this.distancesList;
        }

        ArrayList<Double> distances = kNearestNeighboursRecursive(best, target, k);
        double maximum = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < distances.size(); i++) {
			double toTest = distances.get(i);
			if (toTest > maximum)
				maximum = toTest;
		}

        if (other != null) {
            if (isToSearchNode(node, target, node.splitDim, maximum) || distances.size() < k) {
				distances = kNearestNeighboursRecursive(other, target, k);
			}
        }

        return distances;
    }

    /**
     * IMPLEMENTAÇÃO DO EDUARDO
     */
    protected ArrayList<Double> getDistancesOfBranches(Node node, Instance target) {
		// Isso é o core da aplicação
		// Achei interessante pois é bem diferente do algoritmo original do kNearestNeighbours
		// A politica de poda é algo bastante interessante. Normalmente é feito algo assim:
		// double diff = target.at(axis) - root->point_.at(axis); // É calculado a diferença
		// diff * diff < this->bests_.top().first // valido fazendo essa diferença ao quadrado e verifico se é menor com a top da HEAP
		// (A mais distante das mais proximas)
		ArrayList<Double> distances = new ArrayList<Double>();

		// Ele salva isso como atributo da classe, pq?
		// Fazer recursão mantendo a lista de instancias, não sei como eu faria isso
		this.instancesList.clear();

		double[] targetInfo = target.toDoubleArray();

		if (node.isActive()) {
			// Calcula a distancia do node com o target
			double distanceToNode = this.distanceFunction.distance(node.instance, target);
				distances.add(distanceToNode);
				this.instancesList.add(node.instance);
		}

		Node best = null;
		Node other = null;

		if (node.right != null && node.left != null) {

			if (targetInfo[node.splitDim] >= node.instance.toDoubleArray()[node.splitDim]) {
				best = node.right;
				other = node.left;
			} else {
				best = node.left;
				other = node.right;
			}
		} else if (node.right != null) {
			best = node.right;
		} else if (node.left != null) {
			best = node.left;
		// Aqui eu sei que eu cheguei no No folha da arvore binária
		} else return distances;


		// Aqui da uma enganada, aqui está sendo chamado a função sobrecarregada
		// Eu achei bastante dificil de entender o porque ele faz isso, depois
		// de uma análise vi que é por conta da estrutura distances e inicialmente
		// não tempos ela montada
		// E nessa recursão ele analisa apenas a melhor sub-arvore, de acordo com o artigo
		distances = getDistancesOfBranches(best, target, distances);

		// Pego o maximo
		double maximum = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < distances.size(); i++) {
			double toTest = distances.get(i);
			if (toTest > maximum)
				maximum = toTest;
		}

		// Verifico com o criterio seg se vou para a outra subarvore
		if (other != null) {
			if (isToSearchNode(node, target, node.splitDim, maximum) || distances.size() < this.numNeighbours) {
				distances = getDistancesOfBranches(other, target, distances);
			}
		}

		return distances;
	}

    /**
     * IMPLEMENTAÇÃO DO EDUARDO
     */
	protected ArrayList<Double> getDistancesOfBranches(Node node, Instance target, ArrayList<Double> distances) {

		// Porque essa sobrecarga é ligeramente diferente do algoritmo mostrado

		// Crieterio de parada
		if (node == null)
			return distances;


		// this.nodesSearched++;


		if (node.isActive()) {
			// Calcula a distancia do Node atual com o target
			double distanceToNode = this.distanceFunction.distance(node.instance, target);
			if (distances.size() < this.numNeighbours) {
				distances.add(distanceToNode);
				this.instancesList.add(node.instance);
			}
			else {
				// Aqui é a parte onde é verificado se vai entrar para a nossa HEAP
				// dos k vizinhos mais proximos, porque não foi usado uma estrutura como
				// priority_queue?
				double maximum = distances.get(0);
				int maxIndex = 0;
				for (int i = 0; i < distances.size(); i++) {
					double toTest = distances.get(i);
					if (toTest > maximum) {
						maximum = toTest;
						maxIndex = i;
					}
				}
				if (distanceToNode <= distances.get(maxIndex)) {
					distances.remove(maxIndex);
					distances.add(distanceToNode);
					this.instancesList.remove(maxIndex);
					this.instancesList.add(node.instance);
				}
			}
		}

		double[] targetInfo = target.toDoubleArray();

		// SE NÃO FOR UM NO FOLHA
		if (node.right != null && node.left != null) {

			Node best = null;
			Node other = null;

			if (targetInfo[node.splitDim] >= node.instance.toDoubleArray()[node.splitDim]) {
				best = node.right;
				other = node.left;
			} else {
				best = node.left;
				other = node.right;
			}

			double maximum = Double.NEGATIVE_INFINITY;

			// Obtem o maximo, a maior distancia das k distancias
			for (int i = 1; i < distances.size(); i++) {
				double toTest = distances.get(i);
				if (toTest > maximum)
					maximum = toTest;
			}

			// Verifico a subarvore do melhor lado
			if (isToSearchNode(node, target, node.splitDim, maximum) || distances.size() < this.numNeighbours) {
				distances = getDistancesOfBranches(best, target, distances);
			}

			maximum = Double.NEGATIVE_INFINITY;

			for (int i = 1; i < distances.size(); i++) {
				double toTest = distances.get(i);
				if (toTest > maximum)
					maximum = toTest;
			}

			// Aqui verifico se é necessário ir para o outra sub arvore
			if (other != null) {
				if (isToSearchNode(node, target, node.splitDim, maximum) || distances.size() < this.numNeighbours) {
					distances = getDistancesOfBranches(other, target, distances);
				}
			}
		} else if (node.right != null) {
			// Aqui não entendi essa parte
			distances = getDistancesOfBranches(node.right, target, distances);
		} else if (node.left != null) {
			// Aqui não entendi essa parte
			distances = getDistancesOfBranches(node.left, target, distances);
		}

		return distances;
	}

    private boolean isToSearchNode(Node root, Instance target, int splitDim, double maximum) {

		// É nesse trecho que aplicamos a formula do seg do artigo Eq(3).
		// E verificamos com o maximo
		double minimumDistance = target.toDoubleArray()[splitDim] - root.instance.toDoubleArray()[splitDim];
		double denominator = FastMath.abs(target.toDoubleArray()[splitDim]) + FastMath.abs(root.instance.toDoubleArray()[splitDim]);
		double modDist = 0;

		if (denominator != 0) {
			modDist = FastMath.abs(minimumDistance) / denominator;
		}

		// double compare = a*modDist;
		double compare = 1*modDist;

		if (compare <= maximum)
			return true;

		return false;
	}

    private Node search(Instance inst, Node node) throws Exception {
		double[] instInfo = inst.toDoubleArray();

		if (node == null)
			return null;

		if (isInstanceEqual(inst, node.instance) && node.isActive())
			return node;

		Node nodeToReturn = null;

		if (instInfo[node.splitDim] < node.instance.toDoubleArray()[node.splitDim]) {
			nodeToReturn = search(inst, node.left);
		}
		else {
			nodeToReturn = search(inst, node.right);
		}

		return nodeToReturn;

	}

    private boolean isInstanceEqual(Instance inst1, Instance inst2) {

		boolean found = true;

		double[] infoInst1 = inst1.toDoubleArray();
		double[] infoInst2 = inst2.toDoubleArray();

		for (int i = 0; i < infoInst1.length; i++) {
			if (infoInst1[i] != infoInst2[i]) {
				found = false;
				break;
			}
		}
		return found;
	}

    public boolean isToRebuild() {
        // AQUI VAI SER MONTADO A POLITICA DE RECRIAÇÃO DA ARVORE
        boolean isRebuild = false;
		if (((double) this.numNodesDeactivated / (double) this.nNodes >= 0.3)) {
			isRebuild = true;
		}

		if (this.nNodes > this.initialNumInstances*2) {
			isRebuild = true;
		}
		return isRebuild;
    }

    public void removeInstance(Instance inst) {
        try {
            Node nodeToRemove = this.search(inst, root_);

            if (nodeToRemove == null)
                throw new InstanceNotFoundException("Instance not found on KDTree. Is there any missing data on the dataset?");

            delete(nodeToRemove);
            this.nNodes--;
        } catch (Exception e) {
        }
    }

    private void delete(Node p) throws Exception {
		if (p.isAleaf()) {
			p = null;
			return;
		}
        p.setActive(false);
        this.numNodesDeactivated++;
	}


    @Override
    public double[] getDistances() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDistances'");
    }

    @Override
    public void update(Instance ins) throws Exception {
        this.insertInstance(ins);
    }

    private void buildTree(Instances insts) {
        this.root_ = this.buildTreeBalanced(insts, 0);
        this.initialNumInstances = this.nNodes;
    }

    private Node buildTreeBalanced(Instances insts, int depth) {
        if (insts.size() == 0) {
            return null;
        }

        ArrayList<Double> values_insts = new ArrayList<Double>();
        for (int i = 0; i < insts.size(); i++) {
            values_insts.add(insts.get(i).toDoubleArray()[depth]);
        }

        Collections.sort(values_insts);
        double median = values_insts.get( (int) (values_insts.size() + 1) / 2 - 1);
        Instance medianInstance = null;

        Instances instsToTheLeft = new Instances(insts, insts.size());
        Instances instsToTheRight = new Instances(insts, insts.size());


        for (int i = 0; i < insts.size(); i++) {
			if(insts.get(i).toDoubleArray()[depth] == median && medianInstance == null) {
				medianInstance = insts.get(i);
			}
			else if (insts.get(i).toDoubleArray()[depth] < median)
				instsToTheLeft.add(insts.get(i));
			else
				instsToTheRight.add(insts.get(i));
		}

        this.nNodes++;

        // Monta a árvore em cima dessa recursão
        return new Node(medianInstance, depth,
            buildTreeBalanced(instsToTheLeft, (depth+1)%this.nDims),
            buildTreeBalanced(instsToTheRight, (depth+1)%this.nDims)
        );
    }

    private void insertInstance(Instance inst) {
        Node p = this.root_;
        Node prev = null;
        int depth = 0;

        while (p != null) {
            prev = p;
            int axis = depth % this.nDims;
            if (inst.toDoubleArray()[axis] < p.instance.toDoubleArray()[axis]) {
                p = p.left;
            } else {
                p = p.right;
            }

            depth++;
        }

        int axis = (depth - 1 + this.nDims) % this.nDims;

        // Caso a árovre seja nula
        if (this.root_ == null) {
            this.root_ = new Node(inst, axis);
        }
        else if (inst.toDoubleArray()[axis] < prev.instance.toDoubleArray()[axis]) {
            this.nNodes++;
            prev.left = new Node(inst, axis);
        } else {
            this.nNodes++;
            prev.right = new Node(inst, axis);
        }

    }

}
