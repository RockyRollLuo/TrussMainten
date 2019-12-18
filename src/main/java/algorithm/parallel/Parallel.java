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
     * @param graph_rest
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesInsertion(Graph graph_rest, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap_rest, int threadNum) {
        LOGGER.info("Start Parallel insert dynamicEdges, size=" + dynamicEdges.size());
        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussMap_rest.clone();
        Graph graph = graph_rest.clone();

        long totalTime = 0;
        Result tempResult;
        int times = 0;
        LinkedList<Integer> tdsSizeList = new LinkedList<>();

        while (!addEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getInsertionTDS(graph, addEdges);
            tdsSizeList.add(tds.size());
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
        result.setTdsSizeList(tdsSizeList);

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

        Hashtable<Edge, Boolean> changeMap = new Hashtable<>();

        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            executorService.submit(new ThreadEdgeInsert(graph, e, trussMap,changeMap));
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
     * @param graph_full
     * @param dynamicEdges
     * @param threadNum
     * @return
     */
    public static Result edgesDeletion(Graph graph_full, LinkedList<Edge> dynamicEdges, Hashtable<Edge, Integer> trussMap_full, int threadNum) {
        LOGGER.info("Start Parallel delete dynamicEdges, size=" + dynamicEdges.size() + " threadNum:" + threadNum);
        LinkedList<Edge> addEdges = (LinkedList<Edge>) dynamicEdges.clone();
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussMap_full.clone();
        Graph graph = graph_full.clone();

        long totalTime = 0;
        Result tempResult;
        int times = 0;
        LinkedList<Integer> tdsSizeList = new LinkedList<>();

        while (!addEdges.isEmpty()) {
            //get tds
            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, addEdges);
            tdsSizeList.add(tds.size());
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
        result.setTdsSizeList(tdsSizeList);

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

        Hashtable<Edge, Boolean> changeMap = new Hashtable<>();

        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.NANOSECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (Edge e : tds) {
            executorService.submit(new ThreadEdgeDelete(graph, e, trussMap, changeMap));
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
