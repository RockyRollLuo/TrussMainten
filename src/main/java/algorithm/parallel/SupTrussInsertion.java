package algorithm.parallel;

import util.Edge;
import util.Graph;

import java.util.Hashtable;
import java.util.LinkedList;

public class SupTrussInsertion implements Runnable {

    private Graph graph;
    private Edge e0;
    private Hashtable<Edge, Integer> trussMap; //include edges in graph and e0

    /**
     * constructor
     * @param graph
     * @param e0
     */
    public SupTrussInsertion(Graph graph, Edge e0) {
        this.graph = graph;
        this.e0 = e0;
    }

    /**
     * constructor
     * @param graph
     * @param e0
     * @param trussMap
     */
    public SupTrussInsertion(Graph graph, Edge e0, Hashtable<Edge, Integer> trussMap) {
        this.graph = graph;
        this.e0 = e0;
        this.trussMap = trussMap;
    }

    @Override
    public void run() {
        //TODO:



    }
}
