package algorithm;

import org.apache.log4j.Logger;
import util.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

public class SupTruss {
    private static Logger LOGGER = Logger.getLogger(TrussDecomposition.class);

    /**
     * one random edge insertion
     *
     * @param graph
     * @param debug
     * @return
     */
    public Result edgeInsertion(Graph graph, int debug) {
        Hashtable<Integer, TreeSet<Integer>> oldAdjMap = graph.getAdjMap();
        TreeSet<Edge> oldEdgeSet = graph.getEdgeSet();

        Edge e0 = RandomUtils.getRandomElement(oldEdgeSet);

        TreeSet<Edge> newEdgeSet = (TreeSet<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(e0);
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        return edgeInsertion(newGraph, e0, debug);
    }


    /**
     * one edge insertion
     *
     * @param graph
     * @param e0
     * @param debug
     * @return
     */
    public Result edgeInsertion(Graph graph, Edge e0, int debug) {
        if (debug > 0)
            LOGGER.info("SupTruss Insertion one edge %s:" + e0.toString());

        Hashtable<Integer, TreeSet<Integer>> adjMap = graph.getAdjMap();
        TreeSet<Edge> edgeSet = graph.getEdgeSet();

        if (edgeSet.contains(e0)) {
            return null;
        }

        //compute truss
        Result result1 = TrussDecomposition.run(graph, debug);
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) result1.getOutput();

        //compute SustainSupport
        Hashtable<Edge, Integer> sSMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            int v1 = e.getV1();
            int v2 = e.getV2();
            TreeSet<Integer> set1 = adjMap.get(v1);
            TreeSet<Integer> set2 = adjMap.get(v2);
            TreeSet<Integer> setCommon = (TreeSet<Integer>) set1.clone();
            setCommon.retainAll(set2);

            int ss = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                if (trussMap.get(e) <= Math.min(trussMap.get(e1), trussMap.get(e2)))
                    ss++;
            }
            sSMap.put(e, ss);
        }

        //compute PivotalSupport
        Hashtable<Edge, Integer> pSMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            int v1 = e.getV1();
            int v2 = e.getV2();
            TreeSet<Integer> set1 = adjMap.get(v1);
            TreeSet<Integer> set2 = adjMap.get(v2);
            TreeSet<Integer> setCommon = (TreeSet<Integer>) set1.clone();
            setCommon.retainAll(set2);

            int ps = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                int t0 = trussMap.get(e);
                int t1 = trussMap.get(e1);
                int t2 = trussMap.get(e2);

                if (t1 > t0 && t2 > t0) ps++;
                else if (t1 == t0 && t2 > t0 && sSMap.get(e1) > t0 - 2) ps++;
                else if (t2 == t0 && t1 > t0 && sSMap.get(e2) > t0 - 2) ps++;
                else if (t1 == t0 && t2 == t0 && sSMap.get(e1) > t0 - 2 && sSMap.get(e2) > t0 - 2) ps++;
            }
            pSMap.put(e, ps);
        }

        /**
         * main part
         * one edge insertion update
         */
        long startTime = System.currentTimeMillis();
        int v1 = e0.getV1();
        int v2 = e0.getV2();

        //v1 or v2 is new
        if ((!adjMap.keySet().contains(v1)) || (!adjMap.keySet().contains(v2))) {
            trussMap.put(e0, 2);
            return new Result(trussMap, System.currentTimeMillis() - startTime, this.getClass().toString());
        }

        TreeSet<Integer> set1 = adjMap.get(v1);
        TreeSet<Integer> set2 = adjMap.get(v2);
        TreeSet<Integer> setCommon = (TreeSet<Integer>) set1.clone();
        setCommon.retainAll(set2);

        ArrayList<Integer> trussList = new ArrayList<>();
        for (int w : setCommon) {
            Edge e1 = new Edge(v1, w);
            Edge e2 = new Edge(v2, w);
            trussList.add(Math.min(trussMap.get(e1), trussMap.get(e2)));

            //TODO: ssMap[e0],psMap[e0],LB



        }




        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, this.getClass().toString());

        if (debug > 0)
            LOGGER.info("Truss decomposition is computed");
        return result;
    }

    /***
     * edges insertion
     * @param graph
     * @param changeEdges
     * @return
     */
    public Result edgesInsertion(Graph graph, TreeSet<Edge> changeEdges) {


        return null;
    }


}
