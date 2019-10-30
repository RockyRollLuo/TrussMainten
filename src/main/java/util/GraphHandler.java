package util;

import java.util.Hashtable;
import java.util.TreeSet;

public class GraphHandler {

    /**
     * remove one edge from adjMap
     * @param adjMap
     * @param e
     * @return
     */
    public static Hashtable<Integer, TreeSet<Integer>> romveEdgeFromAdjMap(Hashtable<Integer, TreeSet<Integer>> adjMap, Edge e) {
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = (Hashtable<Integer, TreeSet<Integer>>) adjMap.clone();

        int v1 = e.getV1();
        int v2 = e.getV2();
        newAdjMap.get(v1).remove(v2);
        newAdjMap.get(v2).remove(v1);

        return newAdjMap;
    }


    /**
     * remove a set of edges from adjMap
     * @param adjMap
     * @param changeEdges
     * @return
     */
    public static Hashtable<Integer, TreeSet<Integer>> removeEdgesFromAdjMap(Hashtable<Integer, TreeSet<Integer>> adjMap, TreeSet<Edge> changeEdges) {
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = (Hashtable<Integer, TreeSet<Integer>>) adjMap.clone();

        for (Edge e : changeEdges) {
            romveEdgeFromAdjMap(newAdjMap, e);
        }
        return newAdjMap;
    }



}
