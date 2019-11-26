package algorithm;

import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

import java.util.*;

public class TCPIndex {

    private int computeTrussnessLowerBound(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e) {
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        Integer v1_e0 = e.getV1();
        Integer v2_e0 = e.getV2();
        LinkedList<Integer> set01 = adjMap.get(v1_e0);
        LinkedList<Integer> set02 = adjMap.get(v2_e0);
        LinkedList<Integer> set0Common = (LinkedList<Integer>) set01.clone();
        set0Common.retainAll(set02);
        ArrayList<Integer> commonTrussList = new ArrayList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(v1_e0, w);
            Edge e2 = new Edge(v2_e0, w);
            commonTrussList.add(Math.min(trussMap.get(e1), trussMap.get(e2)));
        }

        int t_common_max = Collections.max(commonTrussList);
        HashMap<Integer, Integer> countMap = new HashMap<>();
        for (int i = 2; i < t_common_max + 2; i++) {      //countMap.put(t_common_max + 1, 0), prevent null pointer
            int count = 0;
            for (int j : commonTrussList) {
                if (j >= i) count++;
            }
            countMap.put(i, count);
        }
        int key_truss = 2;
        while (key_truss <= countMap.get(key_truss) + 2) {
            key_truss++;
        }
        return key_truss - 1;
    }

    private int computeTrussnessUpperBound(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e) {
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        Integer v1_e0 = e.getV1();
        Integer v2_e0 = e.getV2();
        LinkedList<Integer> set01 = adjMap.get(v1_e0);
        LinkedList<Integer> set02 = adjMap.get(v2_e0);
        LinkedList<Integer> set0Common = (LinkedList<Integer>) set01.clone();
        set0Common.retainAll(set02);
        ArrayList<Integer> commonTrussList = new ArrayList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(v1_e0, w);
            Edge e2 = new Edge(v2_e0, w);
            commonTrussList.add(Math.min(trussMap.get(e1), trussMap.get(e2)));
        }

        int t_common_max = Collections.max(commonTrussList);
        HashMap<Integer, Integer> countMap = new HashMap<>();
        for (int i = 2; i < t_common_max + 2; i++) {      //countMap.put(t_common_max + 1, 0), prevent null pointer
            int count = 0;
            for (int j : commonTrussList) {
                if (j >= i) count++;
            }
            countMap.put(i, count);
        }
        int key_truss = 2;
        while (key_truss <= countMap.get(key_truss-1) + 2) {
            key_truss++;
        }
        return key_truss - 1;
    }


    /**
     * compute the trussness of vertices in the given graph
     *
     * @param graph input graph
     * @return trussness of edges
     */
    public Result run(Graph graph,Edge e0) {
        System.err.println("computing TCPIndex decomposition...");

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.add(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgeToAdjMap(newAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //compute trussMap from old graph
        Hashtable<Edge, Integer> trussMap = GraphHandler.computeTrussMap(graph);

        int k1 = computeTrussnessLowerBound(newGraph, trussMap, e0);
        int k2 = computeTrussnessUpperBound(newGraph, trussMap, e0);
        trussMap.put(e0, k1);
        int k_max=k2-1;

        Hashtable<Integer, LinkedList<Edge>> Lk = new Hashtable<>();

        Integer u = e0.getV1();
        Integer v = e0.getV2();
        LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);
        for (int w : set0Common) {
            Edge e_wu = new Edge(u, w);
            Edge e_wv = new Edge(v, w);
            int t_wu = trussMap.get(e_wu);
            int t_wv = trussMap.get(e_wv);
            int t_min = Math.min(t_wu, t_wv);
            if ( t_min<= k_max) {
                if (t_wu==t_min) Lk.get(t_min).add(e_wu);
                if (t_wv==t_min) Lk.get(t_min).add(e_wv);
            }
        }

        Hashtable<Edge, Integer> s = new Hashtable<>();
        for (int k = k_max; k > 1; k--) {
            Stack<Edge> Q = new Stack<>();
            Q.addAll(Lk.get(k));
            while (!Q.isEmpty()) {
                Edge e_xy = Q.pop();
                s.put(e_xy, 0);



            }

        }

        //TODO:


        System.err.println("Truss decomposition is computed...");
        long endTime = System.currentTimeMillis();

        return new Result(trussMap, endTime - startTime, "TCPIndex");
    }

}
