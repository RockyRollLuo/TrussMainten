package util;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;

public class Graph implements Cloneable{
    private Hashtable<Integer, LinkedList<Integer>> adjMap;
    private LinkedList<Edge> edgeSet;

    public Graph(Hashtable<Integer, LinkedList<Integer>> adjMap, LinkedList<Edge> edgeSet) {
        this.adjMap = adjMap;
        this.edgeSet = edgeSet;
    }

    @Override
    public String toString() {
        return "Graph{"  + adjMap.toString() + '}';
    }

    @Override
    public Graph clone(){
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) edgeSet.clone();
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = new Hashtable<>();
        for (int i : adjMap.keySet()) {
            LinkedList<Integer> adjList = (LinkedList<Integer>) adjMap.get(i).clone();
            newAdjMap.put(i, adjList);
        }
        return new Graph(newAdjMap, newEdgeSet);
    }

    public LinkedList<Integer> getVerticesSet(){
        return new LinkedList<>(this.adjMap.keySet());
    }

    public int getVerticesSize(){
        return this.adjMap.keySet().size();
    }

    /**
     * Getter() and Setter()
     */
    public Hashtable<Integer, LinkedList<Integer>> getAdjMap() {
        return adjMap;
    }

    public void setAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap) {
        this.adjMap = adjMap;
    }

    public LinkedList<Edge> getEdgeSet() {
        return edgeSet;
    }

    public void setEdgeSet(LinkedList<Edge> edgeSet) {
        this.edgeSet = edgeSet;
    }





}
