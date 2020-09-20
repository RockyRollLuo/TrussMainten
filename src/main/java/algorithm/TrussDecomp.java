package algorithm;

import com.sun.javafx.image.IntPixelGetter;
import org.apache.log4j.Logger;
import util.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

public class TrussDecomp {
    private static Logger LOGGER = Logger.getLogger(TrussDecomp.class);

    private Graph graph;

    public TrussDecomp(Graph graph) {
        this.graph = graph;
    }

    /**
     * compute the trussness of vertices in the given graph
     * @return trussness of edges
     */
    public Result run() {
        LOGGER.info("Start truss decomposition...");

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        final Hashtable<Integer, Integer> coreMap = new Hashtable<>(); //output
        final Hashtable<Integer, Integer> degMap = new Hashtable<>();

        for (Integer node : adjMap.keySet()) {
            degMap.put(node, adjMap.get(node).size()); //initial trussMap as sup
        }

        Graph tempGraph = graph.clone();
        Hashtable<Integer, LinkedList<Integer>> remianAdjMap = tempGraph.getAdjMap();


        final int n = remianAdjMap.keySet().size();
        for (int t = 2; ; t++) {
            LOGGER.info("Truss Decomposition Progress:" + (n - remianAdjMap.keySet().size()) + "/" + n);

            if (remianAdjMap.isEmpty())
                break;

            LinkedList<Integer> delQueue = new LinkedList<>(); //
            for (Integer node : remianAdjMap.keySet()) {
                if (degMap.get(node) <= t - 2) {
                    delQueue.offer(node);  // add to queue
                }
            }

            while (!delQueue.isEmpty()) {
                Integer node_queue = delQueue.poll();
                LinkedList<Integer> node_queue_list = remianAdjMap.get(node_queue);
                remianAdjMap.remove(node_queue);

                for (Integer node : node_queue_list) {
                    remianAdjMap.get(node).remove(node_queue);

                    int currentDeg = degMap.get(node) == null ? 0 : degMap.get(node);
                    degMap.put(node, currentDeg-1);
                    if ((currentDeg - 1) <= t && !delQueue.contains(node)) {
                        delQueue.offer(node);
                    }
                }
                coreMap.put(node_queue, t);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(coreMap, endTime - startTime, "TrussDecomp");

        LOGGER.info("End truss decomposition");
        return result;
    }

    public static void main(String[] args) throws IOException {
        String datasetName="com-dblp.ungraph.txt";
        Graph graph= GraphImport.load(datasetName,"\t");
        Result result = new CoreDecomp(graph).run();
        result.setDatasetName(datasetName);
        Export.writeFile(result, 1);
    }

}
