package algorithm;

import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;
import util.Result;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;

public class TrussDecomp {
    private static Logger LOGGER = Logger.getLogger(TrussDecomp.class);

    private Graph graph;

    public TrussDecomp(Graph graph) {
        this.graph = graph;
    }

    /**
     * compute the trussness of vertices in the given graph
     *
     * @param debug debug level
     * @return trussness of edges
     */
    public Result run(int debug){
        if (debug > 0) {
            LOGGER.info("computing truss decomposition...");
        }

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, TreeSet<Integer>> adjMap = graph.getAdjMap();
        TreeSet<Edge> edgeSet = graph.getEdgeSet();

        final Hashtable<Edge, Integer> trussMap = new Hashtable<>(); //output
        final Hashtable<Edge, Integer> supMap = new Hashtable<>();

        for (Edge e : edgeSet) {
            int v1 = e.getV1();
            int v2 = e.getV2();

            TreeSet<Integer> set1 = adjMap.get(v1);
            TreeSet<Integer> set2 = adjMap.get(v2);

            TreeSet<Integer> set = (TreeSet<Integer>) set1.clone();
            set.retainAll(set2);

            supMap.put(e, set.size()); //initial trussMap as sup
        }

        //
        Graph tempGraph = graph.clone();
        TreeSet<Edge> remainEdges = tempGraph.getEdgeSet();
        Hashtable<Integer, TreeSet<Integer>> remianAdjMap = tempGraph.getAdjMap();


        final int m = remainEdges.size();
        for (int t = 2; ; t++) {

            if (debug > 0)
                LOGGER.info("Progress:" + (m - remainEdges.size()) + "/" + m);

            if (remainEdges.isEmpty())
                break;

            LinkedList<Edge> delQueue = new LinkedList<>(); //
            for (Edge edge : remainEdges) {
                if (supMap.get(edge) <= t - 2) {
                    delQueue.offer(edge);  // add to queue
                }
            }

            while (!delQueue.isEmpty()) {
                Edge e_queue = delQueue.poll();

                int v1 = e_queue.getV1();
                int v2 = e_queue.getV2();
                TreeSet<Integer> set1 = remianAdjMap.get(v1);
                TreeSet<Integer> set2 = remianAdjMap.get(v2);
//                if (set1 == null || set2 == null) {
//                    remainEdges.remove(e_queue);
//                    remianAdjMap = GraphHandler.romveEdgeFromAdjMap(remianAdjMap, e_queue);
//                    continue;
//                }
                TreeSet<Integer> setCommon = (TreeSet<Integer>) set1.clone();
                setCommon.retainAll(set2);

                for (Integer w : setCommon) {
                    Edge e1 = new Edge(v1, w);
                    Edge e2 = new Edge(v2, w);

                    int currentSup1 = supMap.get(e1);
                    supMap.put(e1, currentSup1-1);
                    if ((currentSup1-1) <= t - 2 && !delQueue.contains(e1)) {
                        delQueue.offer(e1);
                    }
                    int currentSup2 = supMap.get(e2);
                    supMap.put(e2, currentSup2-1);
                    if ((currentSup2-1) <= t - 2 && !delQueue.contains(e2)) {
                        delQueue.offer(e2);
                    }

                }

                trussMap.put(e_queue, t);
                boolean ret=false;
                //TODO: why cannot remove
                while (ret) {
                    ret=remainEdges.remove(e_queue);
                }
                remianAdjMap = GraphHandler.romveEdgeFromAdjMap(remianAdjMap, e_queue);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, TrussDecomp.class.toString());

        if (debug > 0)
            LOGGER.info("Truss decomp is computed...");
        return result;
    }

}