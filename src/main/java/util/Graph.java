package util;

import java.util.Hashtable;
import java.util.TreeSet;

public class Graph {
    private Hashtable<Integer, TreeSet<Integer>> adjMap;
    private TreeSet<Edge> edgeSet;

    public Graph(Hashtable<Integer, TreeSet<Integer>> adjMap, TreeSet<Edge> edgeSet) {
        this.adjMap = adjMap;
        this.edgeSet = edgeSet;
    }

    @Override
    public String toString() {
        return "Graph{"  + adjMap.toString() + '}';
    }

    /**
     * Getter() and Setter()
     */
    public Hashtable<Integer, TreeSet<Integer>> getAdjMap() {
        return adjMap;
    }

    public void setAdjMap(Hashtable<Integer, TreeSet<Integer>> adjMap) {
        this.adjMap = adjMap;
    }

    public TreeSet<Edge> getEdgeSet() {
        return edgeSet;
    }

    public void setEdgeSet(TreeSet<Edge> edgeSet) {
        this.edgeSet = edgeSet;
    }
}
