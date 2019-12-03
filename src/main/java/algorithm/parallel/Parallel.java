package algorithm.parallel;

import algorithm.TrussDecomp;
import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel {
    private static Logger LOGGER = Logger.getLogger(Parallel.class);

    /**
     * insert a set of edges, tds parallel
     * @param graph
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesInsertion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("Starting Parallel insert edges, threadNum:" + threadNum);


        long totalTime = 0;
        Result tempResult = null;
        int times = 0;

        while (!dynamicEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getInsertionTDS(graph, dynamicEdges);

            //compute tds
            tempResult = edgesTDSInsertion(graph, tds, trussMap, threadNum);
            totalTime += tempResult.getTakenTime();

            //update dynamicEdges
            dynamicEdges.removeAll(tds);
            times++;
        }

        Result result = new Result(trussMap, totalTime, "ParaMultiEdgesInsertion");
        result.setTimes(times);
        return result;
    }


    /**
     * insert a tds to a graph
     * @param graph
     * @param tds
     * @param trussMap
     * @param threadNum
     * @return
     */
    public static Result edgesTDSInsertion(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("Start parallel insert tds:" + tds.toString());

        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        for (Edge e : graph.getEdgeSet()) {
            edgeVisitedMap.put(e, false);
        }

        long startTime = System.currentTimeMillis();
        ExecutorService threadPool = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            threadPool.submit(new ParaEdgeInsertion(graph, e, trussMap, edgeVisitedMap));
        }
        threadPool.shutdown();
        LOGGER.info("End parallel insert tds");
        return new Result(trussMap, startTime - System.currentTimeMillis());
    }


    /**
     * delete a set of edges, tds parallel
     * @param graph
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesDeletion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap, int threadNum) {

        long totalTime = 0;
        Result tempResult = null;
        int times = 0;

        while (!dynamicEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, dynamicEdges);

            //compute tds
            tempResult = edgesTDSDeletion(graph, tds, trussMap, threadNum);
            totalTime += tempResult.getTakenTime();

            //update dynamicEdges
            dynamicEdges.removeAll(tds);
            times++;
        }

        Result result = new Result(trussMap, totalTime, "ParaMultiEdgesInsertion");
        result.setTimes(times);
        return result;
    }


    /**
     * delete a tds to from graph
     * @param graph
     * @param tds
     * @param trussMap
     * @param threadNum
     * @return
     */
    public static Result edgesTDSDeletion(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        for (Edge e : graph.getEdgeSet()) {
            edgeVisitedMap.put(e, false);
        }

        long startTime = System.currentTimeMillis();
        ExecutorService threadPool = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            threadPool.submit(new ParaEdgeDeletion(graph, e, trussMap, edgeVisitedMap));
        }
        threadPool.shutdown();
        return new Result(trussMap, startTime - System.currentTimeMillis());
    }


}
