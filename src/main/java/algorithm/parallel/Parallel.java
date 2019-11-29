package algorithm.parallel;

import util.Edge;
import util.Graph;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel {

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
            threadPool.submit(new SupTrussInsertion(graph, e));
            System.out.println("inserting edge:e"+e.toString());
        }
        System.out.println(graph.getEdgeSet().toString());
        threadPool.shutdown();
    }

}
