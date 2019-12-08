package algorithm.parallel;

import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

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

        long totalTime = 0;
        Result tempResult;
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
         * thread pool
         */
//        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            executorService.submit(new ThreadEdgeInsert(graph, e, trussMap));
        }
        executorService.shutdown();

        /**
         * wait for all thread terminated
         */
        while (true) {
            if (executorService.isTerminated()) break;
        }
        long endTime = System.currentTimeMillis();

        LOGGER.info("End parallel insert tds, size=" + tds.size());

        Result result = new Result(trussMap, endTime - startTime);
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
        LOGGER.info("Start Parallel delete dynamicEdges, size=" + dynamicEdges.size());

        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();

        long totalTime = 0;
        Result tempResult;
        int times = 0;

        while (!addEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, addEdges);

            //compute tds
            tempResult = edgesTDSDeletion(graph, tds, trussMap, threadNum);
            totalTime += tempResult.getTakenTime();

            //update dynamicEdges
            graph = tempResult.getGraph();
            trussMap = (Hashtable<Edge, Integer>) tempResult.getOutput();

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
        LOGGER.info("Start Parallel delete tds, size=" + tds.size());

        long startTime = System.currentTimeMillis();

//        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 10, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            executorService.submit(new ThreadEdgeDelete(graph, e, trussMap));
        }
        executorService.shutdown();

        /**
         * wait for all thread terminated
         */
        while (true) {
            if (executorService.isTerminated()) break;
        }
        long endTime = System.currentTimeMillis();

        LOGGER.info("End Parallel delete tds, size=" + tds.size());

        Result result = new Result(trussMap, endTime - startTime);
        result.setGraph(graph);

        return result;
    }
}
