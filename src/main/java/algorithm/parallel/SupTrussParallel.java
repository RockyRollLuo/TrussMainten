package algorithm.parallel;

import org.apache.log4j.Logger;
import util.*;

import javax.sound.sampled.Line;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.*;

public class SupTrussParallel {
    private static Logger LOGGER = Logger.getLogger(SupTrussParallel.class);

    /**
     * insert a random set of edges to graph
     *
     * @param graph_rest
     * @param dynamicEdges
     * @return
     */
    public static Result edgesInsertion(Graph graph_rest, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap_rest, int threadNum) {
        LOGGER.info("Start SupTrussnessParallel insert dynamicEdges, size=" + dynamicEdges.size());
        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussMap_rest.clone();
        Graph graph = graph_rest.clone();

        long totalTime = 0;
        int edgeNum = addEdges.size();
        Result tempResult;
        int times = 0;
        LinkedList<Integer> tdsSizeList = new LinkedList<>();

        while (!addEdges.isEmpty()) {
            LOGGER.info("SupTrussnessParallel insert edges progress: " + (edgeNum - addEdges.size()) + "/" + edgeNum);

            LinkedList<Edge> tds = GraphHandler.getInsertionTDS(graph, addEdges);
            tdsSizeList.add(tds.size());
            //compute tds
            tempResult = edgeTDSInsertionTrussParallel(graph, tds, trussMap, threadNum);

            //cumulative time
            totalTime += tempResult.getTakenTime();

            //update graph
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();

            times++;
        }

        Result result = new Result(trussMap, totalTime, "SupTrussnessParallel");
        result.setTimes(times);
        result.setTdsSizeList(tdsSizeList);

        LOGGER.info("End SupTrussnessParallel insert dynamicEdges, size=" + addEdges.size());
        return result;
    }


    /***
     * tds edges insertion
     * @param graph
     * @param tds
     * @return
     */
    public static Result edgeTDSInsertionTrussParallel(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("Start SupTrussnessParallel Insert TDS, size:" + tds.size());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        //new Graph
        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.addAll(tds);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgesToAdjMap(newAdjMap, tds);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //computing trussness of new inserting edges
        for (Edge e0 : tds) {
            //compute truss of e0
            int t_e0_LB = GraphHandler.computeTrussnessLowerBound(newAdjMap, trussMap, e0);
            trussMap.put(e0, t_e0_LB);
        }
        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);
        //compute PivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = GraphHandler.computePivotalSupportMap(newGraph, trussMap, sSupMap);

        //PES
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (Edge e0 : tds) {
            int t_e0_UB = GraphHandler.computeTrussnessUpperBound(pSupMap, e0, trussMap.get(e0));
            Integer v1_e0 = e0.getV1();
            Integer v2_e0 = e0.getV2();
            LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);
            for (int w : set0Common) {
                Edge e1 = new Edge(w, v1_e0);
                Edge e2 = new Edge(w, v2_e0);
                if (trussMap.get(e1) < t_e0_UB) promoteEdgeSet.add(e1);
                if (trussMap.get(e2) < t_e0_UB) promoteEdgeSet.add(e2);
            }
        }

        //sameTrussEdgeList Map
        Hashtable<Integer, LinkedList<Edge>> PESMap = new Hashtable<>();
        for (Edge e_root : promoteEdgeSet) {
            int t = trussMap.get(e_root);
            LinkedList<Edge> sameTrussEdgeList = (PESMap.get(t) == null ? new LinkedList<Edge>() : PESMap.get(t));
            sameTrussEdgeList.add(e_root);
        }

        long startTime = System.currentTimeMillis();
        //tread pool
        Hashtable<Edge, Boolean> changeMap = new Hashtable<>();
//        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (int k : PESMap.keySet()) {
            LinkedList<Edge> sameTrussEdgeList = PESMap.get(k);
            executorService.submit(new ThreadTrussnessInsert(graph, sameTrussEdgeList, k, trussMap, sSupMap, pSupMap, changeMap));
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) break;
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "SupTrussInsertEdgeParallel");
        result.setGraph(newGraph);

        LOGGER.info("End SupTrussnessParallel insert tds, size:" + tds.size());
        return result;
    }


    /**
     * delete a random set of edge from graph
     *
     * @param graph_full
     * @param dynamicEdges
     * @return
     */
    public static Result edgesDeletion(Graph graph_full, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap_full, int threadNum) {
        LOGGER.info("Start SupTruss delete dynamicEdges, size=" + dynamicEdges.size());
        LinkedList<Edge> removeEdges = (LinkedList<Edge>) dynamicEdges.clone();
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussMap_full.clone();
        Graph graph = graph_full.clone();

        int edgeNum = dynamicEdges.size();
        long totalTime = 0;
        Result tempResult;
        int times = 0;
        LinkedList<Integer> tdsSizeList = new LinkedList<>();

        while (!removeEdges.isEmpty()) {
            LOGGER.info("SupTruss insert edges progress: " + (edgeNum - removeEdges.size()) + "/" + edgeNum + "...");

            //compute tds
            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, removeEdges);
            tdsSizeList.add(tds.size());
            tempResult = edgeTDSDeletionTrussParallel(graph, tds, trussMap, threadNum);

            //cumulative time
            totalTime += tempResult.getTakenTime();

            //update graph
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();

            //update dynamicEdges
            times++;
        }

        Result result = new Result(trussMap, totalTime, "SupDeleteEdgesParallel");
        result.setTimes(times);
        result.setTdsSizeList(tdsSizeList);

        LOGGER.info("End SupTruss delete dynamicEdges, size=" + removeEdges.size());
        return result;
    }


    /**
     * delete a tds from a graph
     *
     * @param graph
     * @param tds
     * @return
     */
    public static Result edgeTDSDeletionTrussParallel(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("SupTruss deletion tds, size:" + tds.size());

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        //Construct new graph
        edgeSet.removeAll(tds);
        adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, tds);
        Graph newGraph = new Graph(adjMap, edgeSet);

        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);

        //lazy initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            edgeVisitedMap.put(e, false);
            edgeElimainateMap.put(e, false);
            sMap.put(e, 0);
        }
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (Edge e0 : tds) {
            int t_e0 = trussMap.get(e0); //need delete edges trussMap,
            Integer v1_e0 = e0.getV1();
            Integer v2_e0 = e0.getV2();
            LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(adjMap, e0);
            for (int w : set0Common) {
                Edge e1 = new Edge(w, v1_e0);
                Edge e2 = new Edge(w, v2_e0);
                if (trussMap.get(e1) <= t_e0) promoteEdgeSet.add(e1);
                if (trussMap.get(e2) <= t_e0) promoteEdgeSet.add(e2);
            }
        }

        //sameTrussEdgeList Map
        Hashtable<Integer, LinkedList<Edge>> PESMap = new Hashtable<>();
        for (Edge e_root : promoteEdgeSet) {
            int t = trussMap.get(e_root);
            LinkedList<Edge> sameTrussEdgeList = (PESMap.get(t) == null ? new LinkedList<Edge>() : PESMap.get(t));
            sameTrussEdgeList.add(e_root);
        }

        //tread pool
        long startTime = System.currentTimeMillis();
        Hashtable<Edge, Boolean> changeMap = new Hashtable<>();
//        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (int k : PESMap.keySet()) {
            LinkedList<Edge> sameTrussEdgeList = PESMap.get(k);
            executorService.submit(new ThreadTrussnessDelete(graph, sameTrussEdgeList, k, trussMap, sSupMap, changeMap));
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) break;
        }
        long endTime = System.currentTimeMillis();

        Result result = new Result(trussMap, endTime - startTime, "SupDeleteEdge");
        result.setGraph(newGraph);

        LOGGER.info("End SupTruss deletion tds, size:" + tds.size());
        return result;
    }

    /***
     *  eliminate
     *  for edge insertion
     * @param adjMap
     * @param trussMap
     * @param sMap
     * @param edgeElimanateMap
     * @param t_root
     * @param edge
     */
    private static void eliminateInsertion(Hashtable<Integer, LinkedList<Integer>> adjMap, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sMap, Hashtable<Edge, Boolean> edgeElimanateMap, int t_root, Edge edge) {
        edgeElimanateMap.put(edge, true);
        Integer a = edge.getV1();
        Integer b = edge.getV2();
        LinkedList<Integer> setA = adjMap.get(a);
        LinkedList<Integer> setB = adjMap.get(b);
        LinkedList<Integer> setC = (LinkedList<Integer>) setA.clone();
        setC.retainAll(setB);
        for (int c : setC) {
            Edge ac = new Edge(a, c);
            Edge bc = new Edge(b, c);
            if (Math.min(trussMap.get(ac), trussMap.get(bc)) >= t_root) {
                if (trussMap.get(ac) == t_root) {
                    int s_ac = sMap.get(ac);
                    sMap.put(ac, s_ac - 1);
                    if (s_ac == t_root - 2 && !edgeElimanateMap.get(ac)) {
                        eliminateInsertion(adjMap, trussMap, sMap, edgeElimanateMap, t_root, ac);
                    }
                }
                if (trussMap.get(bc) == t_root) {
                    int s_bc = sMap.get(bc);
                    sMap.put(ac, s_bc - 1);

                    if (s_bc == t_root - 2 && !edgeElimanateMap.get(bc)) {
                        eliminateInsertion(adjMap, trussMap, sMap, edgeElimanateMap, t_root, bc);
                    }
                }
            }
        }
    }


    /***
     *  eliminate
     *  for edge deletion
     * @param adjMap
     * @param trussMap
     * @param sMap
     * @param edgeElimanateMap
     * @param t_root
     * @param edge
     */
    private static void eliminateDeletion(Hashtable<Integer, LinkedList<Integer>> adjMap, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sMap, Hashtable<Edge, Boolean> edgeElimanateMap, int t_root, Edge edge) {
        edgeElimanateMap.put(edge, true);
        Integer a = edge.getV1();
        Integer b = edge.getV2();
        LinkedList<Integer> setA = adjMap.get(a);
        LinkedList<Integer> setB = adjMap.get(b);
        LinkedList<Integer> setC = (LinkedList<Integer>) setA.clone();
        setC.retainAll(setB);
        for (int c : setC) {
            Edge ac = new Edge(a, c);
            Edge bc = new Edge(b, c);
            if (Math.min(trussMap.get(ac), trussMap.get(bc)) >= t_root) {
                if (trussMap.get(ac) == t_root) {
                    int s_ac = sMap.get(ac);
                    sMap.put(ac, s_ac - 1);
                    if (s_ac == t_root - 3 && !edgeElimanateMap.get(ac)) {
                        eliminateDeletion(adjMap, trussMap, sMap, edgeElimanateMap, t_root, ac);
                    }
                }
                if (trussMap.get(bc) == t_root) {
                    int s_bc = sMap.get(bc);
                    sMap.put(ac, s_bc - 1);

                    if (s_bc == t_root - 3 && !edgeElimanateMap.get(bc)) {
                        eliminateDeletion(adjMap, trussMap, sMap, edgeElimanateMap, t_root, bc);
                    }
                }
            }
        }
    }

}
