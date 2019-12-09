package algorithm;

import org.apache.commons.logging.impl.LogKitLogger;
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
        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();

        long startTime = System.currentTimeMillis();
        Result tempResult;
        while (!addEdges.isEmpty()) {
            Edge e0 = addEdges.poll();
            tempResult = edgeInsertionRun(graph, trussMap, e0);

            //update graph
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();
        }

        LOGGER.info("End TCPTruss insert dynamicEdges, size=" + addEdges.size());
        return new Result(trussMap, System.currentTimeMillis() - startTime, "TCPInsertEdges");
    }

    /**
     * compute the trussness of vertices in the given graph
     *
     * @param graph input graph
     * @return trussness of edges
     */
    public static Result edgeInsertionRun(Graph graph, Hashtable<Edge, Integer> trussMap, Edge e0) {
        LOGGER.info("Start run TCPInsertion e0:" + e0.toString());

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        //update graph
        int v1 = e0.getV1();
        int v2 = e0.getV2();
        LinkedList<Integer> set1 = adjMap.get(v1);
        LinkedList<Integer> set2 = adjMap.get(v2);
        edgeSet.add(e0);
        set1.add(v2);
        set2.add(v1);

        int k1 = computeTrussnessLowerBound(graph, trussMap, e0);
        int k2 = computeTrussnessUpperBound(graph, trussMap, e0);
        trussMap.put(e0, k1);
        int k_max = k2 - 1;

        Hashtable<Integer, LinkedList<Edge>> LkMap = new Hashtable<>();

        Integer u = e0.getV1();
        Integer v = e0.getV2();
        LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(adjMap, e0);
        for (int w : set0Common) {
            Edge e_wu = new Edge(u, w);
            Edge e_wv = new Edge(v, w);
            int t_wu = trussMap.get(e_wu);
            int t_wv = trussMap.get(e_wv);
            int t_min = Math.min(t_wu, t_wv);
            LinkedList<Edge> Lk = LkMap.get(t_min) == null ? new LinkedList<Edge>() : LkMap.get(t_min);
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
                LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(adjMap, e_xy);
                for (Integer z : setZ) {
                    Edge e_zx = new Edge(z, x);
                    Edge e_zy = new Edge(z, y);
                    int t_zx = trussMap.get(e_zx);
                    int t_zy = trussMap.get(e_zy);
                    if (t_zx < k || t_zy < k) continue;
                    int s_xy = s.get(e_xy);
                    s.put(e_xy, s_xy + 1);
                    if (t_zx == k && !Lk.contains(e_zx)) {
                        Lk.add(e_zx);
                        Q.push(e_zx);
                    }
                    if (t_zy == k && !Lk.contains(e_zy)) {
                        Lk.add(e_zy);
                        Q.push(e_zy);
                    }
                }
            }

            LinkedList<Edge> delQueue = new LinkedList<>();
            for (Edge e_xy : Lk) {
                if (s.get(e_xy) < k - 2) {
                    delQueue.offer(e_xy);
                }
            }
            while (!delQueue.isEmpty()) {
                Edge e_xy = delQueue.poll();
                Lk.remove(e_xy);
                Integer x = e_xy.getV1();
                Integer y = e_xy.getV2();
                LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(adjMap, e_xy);
                for (Integer z : setZ) {
                    Edge e_zx = new Edge(z, x);
                    Edge e_zy = new Edge(z, y);
                    int t_zx = trussMap.get(e_zx);
                    int t_zy = trussMap.get(e_zy);

                    if (t_zx < k || t_zy < k) continue;
                    if (t_zx == k && !Lk.contains(e_zx)) continue;
                    if (t_zy == k && !Lk.contains(e_zy)) continue;
                    if (Lk.contains(e_zx)) {
                        int s_zx = s.get(e_zx) - 1;
                        s.put(e_zx, s_zx);
                        if (s_zx == k - 3) delQueue.offer(e_zx);
                    }
                    if (Lk.contains(e_zy)) {
                        int s_zy = s.get(e_zy) - 1;
                        s.put(e_zy, s_zy);
                        if (s_zy == k - 3) delQueue.offer(e_zy);
                    }
                }
            }

            for (Edge e_xy : Lk) {
                int t_xy = trussMap.get(e_xy);
                trussMap.put(e_xy, t_xy + 1);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "TCPInsertEdge");
        result.setGraph(graph);
        return result;
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
        LinkedList<Edge> removeEdge = (LinkedList<Edge>) dynamicEdges.clone();

        long startTime = System.currentTimeMillis();
        Result tempResult;
        while (!removeEdge.isEmpty()) {
            Edge e0 = removeEdge.poll();
            tempResult = edgeDeletionRun(graph, trussMap, e0);

            //update graph
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();
        }
        LOGGER.info("End TCPTruss insert dynamicEdges, size=" + removeEdge.size());
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
        LOGGER.info("Start TCP edge deletion, e:" + e0.toString());

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        //update graph
        edgeSet.remove(e0);
        adjMap = GraphHandler.removeEdgeFromAdjMap(adjMap, e0);

        int k_root = trussMap.get(e0);
        if (k_root > 2) {
            Hashtable<Integer, LinkedList<Edge>> LkMap = new Hashtable<>();

            Integer u = e0.getV1();
            Integer v = e0.getV2();
            LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(adjMap, e0);

            //initialize Lk
            for (int w : set0Common) {
                Edge e_wu = new Edge(u, w);
                Edge e_wv = new Edge(v, w);
                int t_wu = trussMap.get(e_wu);
                int t_wv = trussMap.get(e_wv);
                int t_min = Math.min(t_wu, t_wv);
                LinkedList<Edge> Lk = LkMap.get(t_min) == null ? new LinkedList<Edge>() : LkMap.get(t_min);
                if (t_min <= k_root) {
                    if (t_wu == t_min) Lk.add(e_wu);
                    if (t_wv == t_min) Lk.add(e_wv);
                }
                LkMap.put(t_min, Lk);
            }

            for (int k = k_root; k > 1; k--) {
                LinkedList<Edge> Lk = LkMap.get(k);
                if (Lk == null) continue;
                Stack<Edge> Q = new Stack<>();
                Q.addAll(Lk);
                Hashtable<Edge, Integer> s = new Hashtable<>();
                //traversal
                while (!Q.isEmpty()) {
                    Edge e_xy = Q.pop();
                    s.put(e_xy, 0);
                    Integer x = e_xy.getV1();
                    Integer y = e_xy.getV2();
                    LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(adjMap, e_xy);
                    for (Integer z : setZ) {
                        Edge e_zx = new Edge(z, x);
                        Edge e_zy = new Edge(z, y);
                        int t_zx = trussMap.get(e_zx);
                        int t_zy = trussMap.get(e_zy);
                        if (t_zx < k || t_zy < k) continue;
                        int s_xy = s.get(e_xy);
                        s.put(e_xy, s_xy + 1);
                        if (t_zx == k && !Lk.contains(e_zx)) {
                            Lk.add(e_zx);
                            Q.push(e_zx);
                        }
                        if (t_zy == k && !Lk.contains(e_zy)) {
                            Lk.add(e_zy);
                            Q.push(e_zy);
                        }

                    }
                }

                LinkedList<Edge> queue = new LinkedList<>(Lk);
                while (!queue.isEmpty()) {
                    Edge e_xy = queue.poll();

                    if (s.get(e_xy) < k - 2) {
                        //update truss
                        int t_xy = trussMap.get(e_xy);
                        trussMap.put(e_xy, t_xy - 1);

                        Lk.remove(e_xy);
                        Integer x = e_xy.getV1();
                        Integer y = e_xy.getV2();
                        LinkedList<Integer> setZ = GraphHandler.getCommonNeighbors(adjMap, e_xy);
                        for (Integer z : setZ) {
                            Edge e_zx = new Edge(z, x);
                            Edge e_zy = new Edge(z, y);
                            int t_zx = trussMap.get(e_zx);
                            int t_zy = trussMap.get(e_zy);

                            if (t_zx < k || t_zy < k) continue;
                            if (t_zx == k && !Lk.contains(e_zx)) continue;
                            if (t_zy == k && !Lk.contains(e_zy)) continue;
                            if (Lk.contains(e_zx)) {
                                int s_zx = s.get(e_zx) - 1;
                                s.put(e_zx, s_zx);
                                if (s_zx == k - 1) queue.offer(e_zx); //just reduce 1
                            }
                            if (Lk.contains(e_zy)) {
                                int s_zy = s.get(e_zy) - 1;
                                s.put(e_zy, s_zy);
                                if (s_zy == k - 1) queue.offer(e_zy);
                            }
                        }
                    }
                }

            }
        }
        trussMap.remove(e0);

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "TCPDeleteEdge");
        result.setGraph(graph);
        return result;
    }
}
