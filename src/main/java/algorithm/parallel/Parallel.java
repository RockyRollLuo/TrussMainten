package algorithm.parallel;

import util.Edge;
import util.Graph;
import util.Result;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel {

    public static Result edgesInsertion(Graph graph, LinkedList<Edge> dynamicEdges) {
        //todo


        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        for (Edge e : graph.getEdgeSet()) {
            edgeVisitedMap.put(e, false);
        }

        return null;
    }

    public static Result edgesDeletion(Graph graph, LinkedList<Edge> dynamicEdges) {
        //todo

        return null;
    }


    public static Result edgesTDSInsertion(Graph graph, LinkedList<Edge> tds) {
        //todo



        return null;
    }

    public static Result edgesTDSDeletion(Graph graph, LinkedList<Edge> tds) {
        //todo

        return null;
    }





    public static ExecutorService getThreadPool() {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        // max thread number equal cpu number
        ExecutorService threadPool = Executors.newFixedThreadPool(cpuNum);
        return threadPool;
    }


    public static void main(String[] args) {
        Hashtable<Integer, LinkedList<Integer>> adjMap = new Hashtable<>();
        LinkedList<Edge> edgeSet = new LinkedList<>();
        Graph graph = new Graph(adjMap, edgeSet);

        LinkedList<Edge> insertEdges = new LinkedList<>();
        insertEdges.add(new Edge(1, 2));
        insertEdges.add(new Edge(1, 3));
        insertEdges.add(new Edge(1, 4));
        insertEdges.add(new Edge(1, 5));
        insertEdges.add(new Edge(1, 6));
        insertEdges.add(new Edge(1, 7));
        insertEdges.add(new Edge(1, 8));
        insertEdges.add(new Edge(1, 9));
        ExecutorService threadPool = Parallel.getThreadPool();

        for (Edge e : insertEdges) {
            threadPool.submit(new ParaEdgesInsertion(graph, e));
            System.out.println("inserting edge:e"+e.toString());
        }
        System.out.println(graph.getEdgeSet().toString());
        threadPool.shutdown();
    }

}
