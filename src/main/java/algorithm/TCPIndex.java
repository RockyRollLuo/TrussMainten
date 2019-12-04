package algorithm;

import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

import java.util.*;

public class TCPIndex {
    private static Logger LOGGER = Logger.getLogger(TCPIndex.class);

    /**
     * compute the lower bound of the new inserting edge
     *
     * @param graph
     * @param trussMap
     * @param e
     * @return
     */
    private static int computeTrussnessLowerBound(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e) {
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

        if (commonTrussList.size() == 0) {
            return 2;
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

    /**
     * compute the upper bound of the new inserting edge
     *
     * @param graph
     * @param trussMap
     * @param e
     * @return
     */
    private static int computeTrussnessUpperBound(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e) {
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

        if (commonTrussList.size() == 0 || commonTrussList == null) {
            return 2;
        }
        int t_common_max = Collections.max(commonTrussList);
        HashMap<Integer, Integer> countMap = new HashMap<>();
        for (int i = 1; i < t_common_max + 2; i++) {      //countMap.put(t_common_max + 1, 0), prevent null pointer
            int count = 0;
            for (int j : commonTrussList) {
                if (j >= i) count++;
            }
            countMap.put(i, count);
        }
        int key_truss = 2;
        while (key_truss <= countMap.get(key_truss - 1) + 2) {
            key_truss++;
        }
        return key_truss - 1;
    }


    /**
     * a list of edges insertion
     *
     * @param graph
     * @param dynamicEdges
     * @return
     */
    public static Result edgesInsertion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap) {
        LOGGER.info("Start TCPTruss insert dynamicEdges, size=" + dynamicEdges.size());

        long startTime = System.currentTimeMillis();
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();
        Result tempResult = null;

        while (!dynamicEdges.isEmpty()) {
            Edge e0 = dynamicEdges.poll();
            tempResult = edgeInsertionRun(graph, trussMap, e0);
            //update graph
            adjMap = GraphHandler.insertEdgeToAdjMap(adjMap, e0);
            edgeSet.add(e0);
            graph = new Graph(adjMap, edgeSet);
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();
        }

        LOGGER.info("End TCPTruss insert dynamicEdges, size=" + dynamicEdges.size());
        return new Result(tempResult.getOutput(), System.currentTimeMillis() - startTime, "TCPInsertEdges");
    }

    /**
     * compute the trussness of vertices in the given graph
     *
     * @param graph input graph
     * @return trussness of edges
     */
    public static Result edgeInsertionRun(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e0) {

        System.err.println("computing TCPIndex decomposition...");

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.add(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgeToAdjMap(newAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        int k1 = computeTrussnessLowerBound(newGraph, trussMap, e0);
        int k2 = computeTrussnessUpperBound(newGraph, trussMap, e0);
        trussMap.put(e0, k1);
        int k_max = k2 - 1;

        Hashtable<Integer, LinkedList<Edge>> LkMap = new Hashtable<>();

        Integer u = e0.getV1();
        Integer v = e0.getV2();
        LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);
        for (int w : set0Common) {
            Edge e_wu = new Edge(u, w);
            Edge e_wv = new Edge(v, w);
            int t_wu = trussMap.get(e_wu);
            int t_wv = trussMap.get(e_wv);
            int t_min = Math.min(t_wu, t_wv);
            LinkedList<Edge> Lk = LkMap.get(t_min)==null?new LinkedList<Edge>():LkMap.get(t_min);
            if (t_min <= k_max) {
                if (t_wu == t_min) {
                    Lk.add(e_wu);
                }
                if (t_wv == t_min) {
                    Lk.add(e_wv);
                }
            }
        }

        Hashtable<Edge, Integer> s = new Hashtable<>();
        for (int k = k_max; k > 1; k--) {
            LinkedList<Edge> Lk = LkMap.get(k);
            if (Lk == null) continue;
            Stack<Edge> Q = new Stack<>();
            Q.addAll(Lk);
            while (!Q.isEmpty()) {
                Edge e_xy = Q.pop();
                s.put(e_xy, 0);
                Integer x = e_xy.getV1();
                Integer y = e_xy.getV2();
                LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(newAdjMap, e_xy);
                for (Integer z : setZ) {
                    Edge e_zx = new Edge(z, x);
                    Edge e_zy = new Edge(z, y);
                    int t_zx = trussMap.get(e_zx);
                    int t_zy = trussMap.get(e_zy);
                    if (t_zx < k || t_zy < k) continue;
                    int s_xy = s.get(e_xy);
                    s.put(e_xy, s_xy + 1);
                    if (t_zx == k && !Lk.contains(e_zx)) Q.push(e_zx);
                    if (t_zy == k && !Lk.contains(e_zy)) Q.push(e_zy);
                }
            }

            for (Edge e_xy : Lk) {
                if (s.get(e_xy) < k - 2) {
                    Lk.remove(e_xy);
                    Integer x = e_xy.getV1();
                    Integer y = e_xy.getV2();
                    LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(newAdjMap, e_xy);
                    for (Integer z : setZ) {
                        Edge e_zx = new Edge(z, x);
                        Edge e_zy = new Edge(z, y);
                        int t_zx = trussMap.get(e_zx);
                        int t_zy = trussMap.get(e_zy);

                        if (t_zx < k || t_zy < k) continue;
                        if (t_zx == k && !Lk.contains(e_zx)) continue;
                        if (t_zy == k && !Lk.contains(e_zy)) continue;
                        if (Lk.contains(e_zx)) {
                            int s_zx = s.get(e_zx);
                            s.put(e_zx, s_zx - 1);
                        }
                        if (Lk.contains(e_zy)) {
                            int s_zy = s.get(e_zy);
                            s.put(e_zy, s_zy - 1);
                        }
                    }
                }
            }

            for (Edge e_xy : Lk) {
                int t_xy = trussMap.get(e_xy);
                trussMap.put(e_xy, t_xy + 1);
            }
        }

        System.err.println("Truss decomposition is computed...");
        long endTime = System.currentTimeMillis();
        return new Result(trussMap, endTime - startTime, "TCPIndex");
    }


    /**
     * a list of edges deletion
     *
     * @param graph
     * @param dynamicEdges
     * @return
     */
    public static Result edgesDeletion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap) {
        LOGGER.info("Start TCPTruss deletion dynamicEdges, size=" + dynamicEdges.size());

        long startTime = System.currentTimeMillis();
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();
        Result tempResult = null;

        while (!dynamicEdges.isEmpty()) {
            Edge e0 = dynamicEdges.poll();
            tempResult = edgeDeletionRun(graph, trussMap, e0);
            //update graph
            adjMap = GraphHandler.removeEdgeFromAdjMap(adjMap, e0);
            edgeSet.add(e0);
            graph = new Graph(adjMap, edgeSet);
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();
        }
        LOGGER.info("End TCPTruss insert dynamicEdges, size=" + dynamicEdges.size());
        return new Result(trussMap, System.currentTimeMillis() - startTime, "TCPDeleteEdges");
    }


    /**
     * one edge deletion
     *
     * @param graph
     * @param e0
     * @return
     */
    public static Result edgeDeletionRun(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e0) {
        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.add(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgeToAdjMap(newAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        int k1 = computeTrussnessLowerBound(newGraph, trussMap, e0);
        int k2 = computeTrussnessUpperBound(newGraph, trussMap, e0);
        trussMap.put(e0, k1);
        int k_max = k2 - 1;

        Hashtable<Integer, LinkedList<Edge>> LkMap = new Hashtable<>();

        Integer u = e0.getV1();
        Integer v = e0.getV2();
        LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);
        for (int w : set0Common) {
            Edge e_wu = new Edge(u, w);
            Edge e_wv = new Edge(v, w);
            int t_wu = trussMap.get(e_wu);
            int t_wv = trussMap.get(e_wv);
            int t_min = Math.min(t_wu, t_wv);
            LinkedList<Edge> Lk = LkMap.get(t_min);
            if (t_min <= k_max) {
                if (t_wu == t_min) Lk.add(e_wu);
                if (t_wv == t_min) Lk.add(e_wv);
            }
        }

        Hashtable<Edge, Integer> s = new Hashtable<>();
        for (int k = k_max; k > 1; k--) {
            LinkedList<Edge> Lk = LkMap.get(k);

            Stack<Edge> Q = new Stack<>();
            Q.addAll(Lk);
            while (!Q.isEmpty()) {
                Edge e_xy = Q.pop();
                s.put(e_xy, 0);
                Integer x = e_xy.getV1();
                Integer y = e_xy.getV2();
                LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(newAdjMap, e_xy);
                for (Integer z : setZ) {
                    Edge e_zx = new Edge(z, x);
                    Edge e_zy = new Edge(z, y);
                    int t_zx = trussMap.get(e_zx);
                    int t_zy = trussMap.get(e_zy);
                    if (t_zx < k || t_zy < k) continue;
                    int s_xy = s.get(e_xy);
                    s.put(e_xy, s_xy + 1);
                    if (t_zx == k && !Lk.contains(e_zx)) Q.push(e_zx);
                    if (t_zy == k && !Lk.contains(e_zy)) Q.push(e_zy);
                }
            }

            for (Edge e_xy : Lk) {
                if (s.get(e_xy) < k - 2) {
                    Lk.remove(e_xy);
                    Integer x = e_xy.getV1();
                    Integer y = e_xy.getV2();
                    LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(newAdjMap, e_xy);
                    for (Integer z : setZ) {
                        Edge e_zx = new Edge(z, x);
                        Edge e_zy = new Edge(z, y);
                        int t_zx = trussMap.get(e_zx);
                        int t_zy = trussMap.get(e_zy);

                        if (t_zx < k || t_zy < k) continue;
                        if (t_zx == k && !Lk.contains(e_zx)) continue;
                        if (t_zy == k && !Lk.contains(e_zy)) continue;
                        if (Lk.contains(e_zx)) {
                            int s_zx = s.get(e_zx);
                            s.put(e_zx, s_zx - 1);
                        }
                        if (Lk.contains(e_zy)) {
                            int s_zy = s.get(e_zy);
                            s.put(e_zy, s_zy - 1);
                        }
                    }
                }
            }

            for (Edge e_xy : Lk) {
                int t_xy = trussMap.get(e_xy);
                trussMap.put(e_xy, t_xy + 1);
            }
        }

        System.err.println("Truss decomposition is computed...");
        long endTime = System.currentTimeMillis();
        return new Result(trussMap, endTime - startTime, "TCPIndex");
    }
}
