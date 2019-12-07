package algorithm.parallel;

import algorithm.TrussDecomp;
import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

import javax.sound.sampled.Line;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Parallel {
    private static Logger LOGGER = Logger.getLogger(Parallel.class);

    /**
     * insert a set of edges, tds parallel
     *
     * @param graph
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesInsertion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("Start Parallel insert dynamicEdges, size=" + dynamicEdges.size());
        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        long totalTime = 0;
        Result tempResult = null;
        int times = 0;

        while (!addEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getInsertionTDS(graph, addEdges);

            //insert tds
            tempResult = edgesTDSInsertion(graph, tds, trussMap, threadNum);
            totalTime += tempResult.getTakenTime();

            //update graph
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();

            times++;
        }

        Result result = new Result(trussMap, totalTime, "ParaInsertEdges");
        result.setTimes(times);

        LOGGER.info("End Parallel insert dynamicEdges, size=" + addEdges.size());
        return result;
    }


    /**
     * insert a tds to a graph
     *
     * @param graph
     * @param tds
     * @param trussMap
     * @param threadNum
     * @return
     */
    public static Result edgesTDSInsertion(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        LOGGER.info("Start parallel insert tds, size=" + tds.size());



        long startTime = System.currentTimeMillis();

        /**
         * method 1:thread pool
         */
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(512);
        RejectedExecutionHandler policy = new ThreadPoolExecutor.AbortPolicy();
        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, queue, policy);
        for (Edge e : tds) {
            executorService.submit(new ThreadEdgeInsert(graph, e, trussMap));
        }
        executorService.shutdown();

        /**
         * iterate thread run, no thread pool
         */
//        for (Edge e : tds) {
//            new ThreadEdgeInsert(graph, e, trussMap).run();
//        }

        long endTime = System.currentTimeMillis();

        LOGGER.info("End parallel insert tds, size=" + tds.size());

        Result result=new Result(trussMap, endTime - startTime);
        result.setGraph(graph);
        return result;
    }


    /**
     * delete a set of edges, tds parallel
     *
     * @param graph
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesDeletion(Graph graph, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap, int threadNum) {
        //todo:change as insertion
        LOGGER.info("Start Parallel delete dynamicEdges, size=" + dynamicEdges.size());
        long totalTime = 0;
        Result tempResult;
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

        Result result = new Result(trussMap, totalTime, "ParaDeleteEdges");
        result.setTimes(times);

        LOGGER.info("End Parallel insert dynamicEdges, size=" + dynamicEdges.size());
        return result;
    }


    /**
     * delete a tds to from graph
     *
     * @param graph
     * @param tds
     * @param trussMap
     * @param threadNum
     * @return
     */
    public static Result edgesTDSDeletion(Graph graph, LinkedList<Edge> tds, Hashtable<Edge, Integer> trussMap, int threadNum) {
        //todo: change as inseriton
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
