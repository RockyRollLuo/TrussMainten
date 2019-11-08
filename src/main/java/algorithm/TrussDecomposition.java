package algorithm;

import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.Result;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.TreeSet;

public class TrussDecomposition {
    private static Logger LOGGER = Logger.getLogger(TrussDecomposition.class);

    private Graph graph;

    public TrussDecomposition(Graph graph) {
        this.graph = graph;
    }

    /**
     * compute the trussness of vertices in the given graph
     * @param debug debug level
     * @return trussness of edges
     */
    public Result run( int debug) {
        if (debug>0) {
            LOGGER.info("computing truss decomposition...");
        }

        long startTime = System.currentTimeMillis();

        Hashtable<Integer, TreeSet<Integer>> adjMap = graph.getAdjMap();
        TreeSet<Edge> edgeSet = graph.getEdgeSet();

        final Hashtable<Edge, Integer> trussMap = new Hashtable<>();

        for (Edge e : edgeSet) {
            int v1 = e.getV1();
            int v2 = e.getV2();

            TreeSet<Integer> set1 = adjMap.get(v1);
            TreeSet<Integer> set2 = adjMap.get(v2);

            TreeSet<Integer> set = (TreeSet<Integer>) set1.clone();
            set.retainAll(set2);

            trussMap.put(e, set.size());
        }

        final TreeSet<Edge> remainEdges = new TreeSet<>(edgeSet);

        final int m = remainEdges.size();
        for (int t = 2; ; t++) {

            if (debug>0)
                LOGGER.info("Progress:" + (m - remainEdges.size()) + "/" + m);

            if (remainEdges.isEmpty())
                break;

            HashSet<Edge> SDel = new HashSet<>();
            for (Edge edge : remainEdges) {
                if (trussMap.get(edge) <= t - 2) {
                    SDel.add(edge);
                }
            }

            while (!SDel.isEmpty()) {
                final HashSet<Edge> SDelNew = new HashSet<>();

                for (Edge edge : SDel) {
                    final int v1 = edge.getV1();
                    final int v2 = edge.getV2();

                    TreeSet<Integer> set1 = adjMap.get(v1);
                    TreeSet<Integer> set2 = adjMap.get(v2);
                    TreeSet<Integer> setCommon = (TreeSet<Integer>) set1.clone();
                    setCommon.retainAll(set2);

                    for (Integer w : setCommon) {
                        Edge e1 = new Edge(v1, w);
                        Edge e2 = new Edge(v2, w);


                        if (remainEdges.contains(e1)) {
                            int currentSup1 = trussMap.get(e1);
                            trussMap.put(e1, --currentSup1);
                            if (currentSup1 == t - 2) {
                                SDelNew.add(e1);
                                remainEdges.remove(e1);
                            }
                        }
                        if (remainEdges.contains(e2)) {
                            int currentSup2 = trussMap.get(e2);
                            trussMap.put(e2, --currentSup2);
                            if (currentSup2 == t - 2) {
                                SDelNew.add(e2);
                                remainEdges.remove(e2);
                            }
                        }
                    }

                    remainEdges.remove(edge);
                    trussMap.put(edge, t);
                }
                SDel=SDelNew;
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, TrussDecomposition.class.toString());

        if (debug>0)
            LOGGER.info("Truss decomposition is computed...");
        return result;
    }

}
