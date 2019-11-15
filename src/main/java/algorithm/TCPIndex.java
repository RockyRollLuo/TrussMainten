package algorithm;

import util.Edge;
import util.Graph;
import util.Result;

import java.util.Hashtable;
import java.util.LinkedList;

public class TCPIndex {

    /**
     * compute the trussness of vertices in the given graph
     *
     * @param graph input graph
     * @param debug debug level
     * @return trussness of edges
     */
    public Result run(Graph graph, int debug) {
        if (debug > 0) {
            System.err.println("computing TCPIndex decomposition...");
        }

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        final Hashtable<Edge, Integer> trussMap = new Hashtable<>();

        //TODO:
        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, this.getClass().toString());

        if (debug > 0)
            System.err.println("Truss decomposition is computed...");
        return result;
    }

}
