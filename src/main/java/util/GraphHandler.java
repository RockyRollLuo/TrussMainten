package util;

import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

public class GraphHandler {

    /**
     * remove one edge from adjMap
     * @param adjMap original adjMap
     * @param e removed edge
     * @return new adjMap
     */
    public static Hashtable<Integer, TreeSet<Integer>> romveEdgeFromAdjMap(Hashtable<Integer, TreeSet<Integer>> adjMap, Edge e) {
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = (Hashtable<Integer, TreeSet<Integer>>) adjMap.clone();
        int v1 = e.getV1();
        int v2 = e.getV2();

        Set<Integer> keySet = newAdjMap.keySet();
        if (keySet.contains(v1)) {
            newAdjMap.get(v1).remove(v2);
//            if (newAdjMap.get(v1).size() == 0) {
//                newAdjMap.remove(v1);
//            }
        }
        if (keySet.contains(v2)) {
            newAdjMap.get(v2).remove(v1);
//            if (newAdjMap.get(v2).size() == 0) {
//                newAdjMap.remove(v2);
//            }
        }
        return newAdjMap;
    }

    /**
     * insert one edge to adjMap
     * @param adjMap original adjMap
     * @param e inserted edge
     * @return new adjMap
     */
    public static Hashtable<Integer, TreeSet<Integer>> insertEdgeToAdjMap(Hashtable<Integer, TreeSet<Integer>> adjMap, Edge e) {
        Hashtable<Integer, TreeSet<Integer>> newAdjMap = (Hashtable<Integer, TreeSet<Integer>>) adjMap.clone();
        int v1 = e.getV1();
        int v2 = e.getV2();

        Set<Integer> keySet = newAdjMap.keySet();
        if (keySet.contains(v1)) {
            newAdjMap.get(v1).add(v2);
        }else {
            TreeSet<Integer> v1AdjList = new TreeSet<>();
            v1AdjList.add(v2);
            newAdjMap.put(v1, v1AdjList);
        }
        if (keySet.contains(v2)) {
            newAdjMap.get(v2).add(v1);
        } else {
            TreeSet<Integer> v2AdjList = new TreeSet<>();
            v2AdjList.add(v1);
            newAdjMap.put(v2, v2AdjList);
        }
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
