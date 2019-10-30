package algorithm;

import util.*;

import java.util.Hashtable;
import java.util.TreeSet;

public class SupTruss {

    public Result run(Graph graph, int debug) {
        Hashtable<Integer, TreeSet<Integer>> oldAdjMap = graph.getAdjMap();
        TreeSet<Edge> oldEdgeSet = graph.getEdgeSet();

        Edge e0= RandomUtils.getRandomElement(oldEdgeSet);

        TreeSet<Edge> newEdgeSet = (TreeSet<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(e0);
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        return run(newGraph, e0, debug);
    }


    /**
     *  TODO:change to a thread
     * @param graph
     * @param e0
     * @param debug
     * @return
     */
    public Result run(Graph graph, Edge e0, int debug) {
        if (debug > 0) {
            System.err.println("computing truss decomposition...");
        }

        Hashtable<Integer, TreeSet<Integer>> adjMap = graph.getAdjMap();
        TreeSet<Edge> edgeSet = graph.getEdgeSet();

        if (edgeSet.contains(e0)) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        final Hashtable<Edge, Integer> trussMap = new Hashtable<>();

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, this.getClass().toString());

        if (debug > 0)
            System.err.println("Truss decomposition is computed...");
        return result;
    }

}
