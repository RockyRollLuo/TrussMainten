package util;

import algorithm.TrussDecomp;
import org.apache.log4j.Logger;
import sun.security.provider.certpath.Vertex;

import java.util.*;

public class GraphHandler {
//    private static Logger LOGGER = Logger.getLogger(GraphHandler.class);

    /**
     * whether two lists have common elements
     * true:have common elements
     * false: don't have common elements
     *
     * @param list1
     * @param list2
     * @return
     */
    public static boolean haveCommonElement(LinkedList<Edge> list1, LinkedList<Edge> list2) {
        for (Edge e1 : list1) {
            for (Edge e2 : list2) {
                if (e1.equals(e2)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * get a edge set that all edges can form triangle with given edge
     *
     * @param edge
     * @return
     */
    public static LinkedList<Edge> getTriangleEdges(Hashtable<Integer, LinkedList<Integer>> adjMap, Edge edge) {
        LinkedList<Edge> triangleEdgeSet = new LinkedList<>();

        Integer a = edge.getV1();
        Integer b = edge.getV2();
        LinkedList<Integer> cSet = getCommonNeighbors(adjMap, edge);
        for (Integer c : cSet) {
            triangleEdgeSet.add(new Edge(a, c));
            triangleEdgeSet.add(new Edge(b, c));
        }
        return triangleEdgeSet;
    }

    public static LinkedList<Edge> getTriangleEdges(Hashtable<Integer, LinkedList<Integer>> adjMap, Integer v) {
        LinkedList<Edge> triangleEdgeSet = new LinkedList<>();


        return triangleEdgeSet;
    }

    /**
     * get edge(u,v) the common neighbors of u and v
     *
     * @param adjMap
     * @param edge   the edge do not need to be an edge of adjmap
     * @return
     */
    public static LinkedList<Integer> getCommonNeighbors(Hashtable<Integer, LinkedList<Integer>> adjMap, Edge edge) {
        Integer v1 = edge.getV1();
        Integer v2 = edge.getV2();
        LinkedList<Integer> set1 = adjMap.get(v1);
        LinkedList<Integer> set2 = adjMap.get(v2);
        LinkedList<Integer> setCommon = (LinkedList<Integer>) set1.clone();
        setCommon.retainAll(set2);
        return setCommon;
    }

    /**
     * clone a adjMap
     *
     * @param adjMap
     * @return
     */
    public static Hashtable<Integer, LinkedList<Integer>> deepCloneAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = new Hashtable<>();
        for (Integer i : adjMap.keySet()) {
            LinkedList<Integer> adjList = adjMap.get(i);
            newAdjMap.put(i, (LinkedList<Integer>) adjList.clone());
        }
        return newAdjMap;
    }

    /**
     * insert one edge to adjMap
     *
     * @param adjMap original adjMap
     * @param e      inserted edge
     * @return new adjMap
     */
    public static Hashtable<Integer, LinkedList<Integer>> insertEdgeToAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, Edge e) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();
        Integer v1 = e.getV1();
        Integer v2 = e.getV2();

        Set<Integer> keySet = newAdjMap.keySet();
        if (keySet.contains(v1)) {
            newAdjMap.get(v1).add(v2);
        } else {
            LinkedList<Integer> v1AdjList = new LinkedList<>();
            v1AdjList.add(v2);
            newAdjMap.put(v1, v1AdjList);
        }
        if (keySet.contains(v2)) {
            newAdjMap.get(v2).add(v1);
        } else {
            LinkedList<Integer> v2AdjList = new LinkedList<>();
            v2AdjList.add(v1);
            newAdjMap.put(v2, v2AdjList);
        }
        return newAdjMap;
    }

    /**
     * inserted a set of edges to adjMap
     *
     * @param adjMap
     * @param edges
     * @return
     */
    public static Hashtable<Integer, LinkedList<Integer>> insertEdgesToAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, LinkedList<Edge> edges) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();
        for (Edge e : edges) {
            newAdjMap = insertEdgeToAdjMap(newAdjMap, e);
        }
        return newAdjMap;
    }

    /**
     * remove one edge from adjMap
     * @param adjMap original adjMap
     * @param e      removed edge
     * @return new adjMap
     */
    public static Hashtable<Integer, LinkedList<Integer>> removeEdgeFromAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, Edge e) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();
        Integer v1 = e.getV1();
        Integer v2 = e.getV2();

        Set<Integer> keySet = newAdjMap.keySet();
        if (keySet.contains(v1)) {
            newAdjMap.get(v1).remove(v2);
            if (newAdjMap.get(v1).size() == 0) {
                newAdjMap.remove(v1);
            }
        }
        if (keySet.contains(v2)) {
            newAdjMap.get(v2).remove(v1);
            if (newAdjMap.get(v2).size() == 0) {
                newAdjMap.remove(v2);
            }
        }
        return newAdjMap;
    }

    /**
     * remove a set of edges from adjMap
     *
     * @param adjMap
     * @param changeEdges
     * @return
     */
    public static Hashtable<Integer, LinkedList<Integer>> removeEdgesFromAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, LinkedList<Edge> changeEdges) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();

        for (Edge e : changeEdges) {
            removeEdgeFromAdjMap(newAdjMap, e);
        }
        return newAdjMap;
    }

    /**
     * remove a set of edges from a graph
     *
     * @param graph
     * @param changedEdges
     * @return new graph
     */
    public static Graph removeEdgesFromGraph(Graph graph, LinkedList<Edge> changedEdges) {
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>)graph.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) graph.getAdjMap().clone();

        newEdgeSet.removeAll(changedEdges);
        newAdjMap = removeEdgesFromAdjMap(newAdjMap, changedEdges);

        return new Graph(newAdjMap, newEdgeSet);
    }

    /**
     *  remove one vertex from adjmap
     * @param adjMap
     * @param v
     * @return
     */
    public static Hashtable<Integer, LinkedList<Integer>> removeVertexFromAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, Integer v) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();
        Set<Integer> keySet = newAdjMap.keySet();
        if (keySet.contains(v)) {
            LinkedList<Integer> adjList=newAdjMap.get(v);
            for(Integer u:adjList){
                newAdjMap.get(u).remove(v);
            }
            newAdjMap.remove(v);
        }
        return newAdjMap;
    }

    /**
     *  remove a set of vertex from adjmap
     * @param adjMap
     * @param vList
     * @return
     */
    public static Hashtable<Integer, LinkedList<Integer>> removeVerticesFromAdjMap(Hashtable<Integer, LinkedList<Integer>> adjMap, LinkedList<Integer> vList) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) adjMap.clone();
        for (Integer v : vList) {
            if (newAdjMap.keySet().contains(v)) {
                LinkedList<Integer> adjList=newAdjMap.get(v);
                for(Integer u:adjList){
                    newAdjMap.get(u).remove(v);
                }
                newAdjMap.remove(v);
            }
        }
        return newAdjMap;
    }

    /**
     * remove one vertex from graph
     * @param v
     * @return
     */
    public static Graph removeVertexFromGraph(Graph graph, Integer v) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) graph.getAdjMap().clone();
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) graph.getEdgeSet().clone();

        if (newAdjMap.keySet().contains(v)) {
            LinkedList<Integer> adjList=newAdjMap.get(v);
            for(Integer u:adjList){
                newAdjMap.get(u).remove(v);
                newEdgeSet.remove(new Edge(u,v));
            }
            newAdjMap.remove(v);
        }
        return new Graph(newAdjMap, newEdgeSet);
    }

    /**
     * remove a set of vertex from graph
     * @param vList
     * @return
     */
    public static Graph removeVerticesFromGraph(Graph graph, LinkedList<Integer> vList) {
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = (Hashtable<Integer, LinkedList<Integer>>) graph.getAdjMap().clone();
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) graph.getEdgeSet().clone();

        for (Integer v : vList) {
            if (newAdjMap.keySet().contains(v)) {
                LinkedList<Integer> adjList=newAdjMap.get(v);
                for(Integer u:adjList){
                    newAdjMap.get(u).remove(v);
                    newEdgeSet.remove(new Edge(u,v));
                }
                newAdjMap.remove(v);
            }
        }
        return new Graph(newAdjMap, newEdgeSet);
    }

    /**
     * compute the trussness of e based on the locality property
     *
     * @param trussMap trussness of old Graph
     * @param e        new inserted edge
     * @return the trussness of e
     */
    public static int computeTrussnessLowerBound(Hashtable<Integer, LinkedList<Integer>> adjMap, Hashtable<Edge, Integer> trussMap, Edge e) {

        Integer v1_e = e.getV1();
        Integer v2_e = e.getV2();
        LinkedList<Integer> set0Common = getCommonNeighbors(adjMap, e);

        ArrayList<Integer> commonTrussList = new ArrayList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(v1_e, w);
            Edge e2 = new Edge(v2_e, w);
            int t1 = trussMap.get(e1) == null ? 0 : trussMap.get(e1);
            int t2 = trussMap.get(e2) == null ? 0 : trussMap.get(e2);
            commonTrussList.add(Math.min(t1, t2));
        }

        if (commonTrussList.size() == 0) {
            return 2;
        }
        int t_common_max = Collections.max(commonTrussList);
        HashMap<Integer, Integer> countMap = new HashMap<>();
        for (int i = 0; i < t_common_max + 2; i++) {      //countMap.put(t_common_max + 1, 0), prevent null pointer
            int count = 0;
            for (int j : commonTrussList) {
                if (j >= i) count++;
            }
            countMap.put(i, count);
        }
        int key_truss = 2;
        while (key_truss <= countMap.get(key_truss) + 2) {
            key_truss++;
        }
        return key_truss - 1;
    }

    /**
     * compute the Upper bound of new inserted edge
     *
     * @param pSupMap
     * @param e
     * @param t_LB
     * @return
     */
    public static int computeTrussnessUpperBound(Hashtable<Edge, Integer> pSupMap, Edge e, int t_LB) {
        int t_UB = t_LB;
        if (pSupMap.get(e) > t_LB - 2) {
            t_UB = t_LB + 1;
        }
        return t_UB;
    }

    /**
     * compute trussMap
     *
     * @param graph
     * @return
     */
    public static Hashtable<Edge, Integer> computeTrussMap(Graph graph) {
        TrussDecomp trussDecomp = new TrussDecomp(graph);
        return (Hashtable<Edge, Integer>) trussDecomp.run().getOutput();
    }

    /**
     * compute sustainSupportMap
     *
     * @param graph
     * @param trussMap
     * @return
     */
    public static Hashtable<Edge, Integer> computeSustainSupportMap(Graph graph, Hashtable<Edge, Integer> trussMap) {
        LinkedList<Edge> edgeSet = graph.getEdgeSet();
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        Hashtable<Edge, Integer> sSupMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            Integer v1 = e.getV1();
            Integer v2 = e.getV2();
            LinkedList<Integer> setCommon = getCommonNeighbors(adjMap, e);

            int ss = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                int t_e1 = trussMap.get(e1) == null ? 0 : trussMap.get(e1);
                int t_e2 = trussMap.get(e2) == null ? 0 : trussMap.get(e2);

                if (trussMap.get(e) <= Math.min(t_e1, t_e2)) {
                    ss++;
                }
            }
            sSupMap.put(e, ss);
        }
        return sSupMap;
    }


    /**
     * compute pivotalSupportMap
     *
     * @param graph
     * @param trussMap
     * @param sSupMap
     * @return
     */
    public static Hashtable<Edge, Integer> computePivotalSupportMap(Graph graph, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sSupMap) {
        LinkedList<Edge> edgeSet = graph.getEdgeSet();
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        Hashtable<Edge, Integer> pSupMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            Integer v1 = e.getV1();
            Integer v2 = e.getV2();
            LinkedList<Integer> setCommon = getCommonNeighbors(adjMap, e);

            int ps = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                int t0 = trussMap.get(e);
                int t1 = trussMap.get(e1) == null ? 0 : trussMap.get(e1);
                int t2 = trussMap.get(e2) == null ? 0 : trussMap.get(e2);
                int sSup1 = sSupMap.get(e1) == null ? 0 : sSupMap.get(e1);
                int sSup2 = sSupMap.get(e2) == null ? 0 : sSupMap.get(e2);

                if (t1 > t0 && t2 > t0) ps++;
                else if (t1 == t0 && t2 > t0 && sSup1 > t0 - 2) ps++;
                else if (t2 == t0 && t1 > t0 && sSup2 > t0 - 2) ps++;
                else if (t1 == t0 && t2 == t0 && sSup1 > t0 - 2 && sSup2 > t0 - 2) ps++;
            }
            pSupMap.put(e, ps);
        }
        return pSupMap;
    }

    /**
     * update sSupMap after trussness changed
     *
     * @param graph
     * @param trussMap
     * @param sSupMap
     * @return
     */
    public static void updateSustainSupportMap(Graph graph, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sSupMap, LinkedList<Edge> trussChangedEdges) {
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        LinkedList<Edge> updateList = new LinkedList<>();
        for (Edge e_orignal : trussChangedEdges) {
            LinkedList<Edge> list = new LinkedList<>();
            list.add(e_orignal);
            Integer v1_e_org = e_orignal.getV1();
            Integer v2_e_org = e_orignal.getV2();
            LinkedList<Integer> v3Set_e_org = getCommonNeighbors(adjMap, e_orignal);
            for (Integer v3_e_org : v3Set_e_org) {
                list.add(new Edge(v1_e_org, v3_e_org));
                list.add(new Edge(v2_e_org, v3_e_org));
            }

            for (Edge e : list) {
                Integer v1_1hop = e_orignal.getV1();
                Integer v2_1hop = e_orignal.getV2();
                LinkedList<Integer> v3Set_1hop = getCommonNeighbors(adjMap, e);
                for (Integer v3_1hop : v3Set_1hop) {
                    list.add(new Edge(v1_1hop, v3_1hop));
                    list.add(new Edge(v2_1hop, v3_1hop));
                }
            }

            while (!list.isEmpty()) {
                Edge e = list.poll();
                if (updateList.contains(e)) continue;
                Integer v1 = e.getV1();
                Integer v2 = e.getV2();
                LinkedList<Integer> setCommon = getCommonNeighbors(adjMap, e);

                int ss = 0;
                for (int w : setCommon) {
                    Edge e1 = new Edge(v1, w);
                    Edge e2 = new Edge(v2, w);
                    int t_e1 = trussMap.get(e1) == null ? 0 : trussMap.get(e1);
                    int t_e2 = trussMap.get(e2) == null ? 0 : trussMap.get(e2);

                    if (trussMap.get(e) <= Math.min(t_e1, t_e2)) {
                        ss++;
                    }
                }
                sSupMap.put(e, ss);
                updateList.add(e);
            }
        }
    }

    /**
     * update sSupMap after trussness changed
     *
     * @param graph
     * @param trussMap
     * @param sSupMap
     * @return
     */
    public static void updatePivotalSupportMap(Graph graph, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sSupMap, Hashtable<Edge, Integer> pSupMap, LinkedList<Edge> trussChangedEdges) {
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();

        LinkedList<Edge> updateList = new LinkedList<>();
        for (Edge e_orignal : trussChangedEdges) {
            LinkedList<Edge> list = new LinkedList<>();
            Integer v1_e_org = e_orignal.getV1();
            Integer v2_e_org = e_orignal.getV2();
            LinkedList<Integer> v3Set_e_org = getCommonNeighbors(adjMap, e_orignal);
            for (Integer v3_e_org : v3Set_e_org) {
                Edge v1v3 = new Edge(v1_e_org, v3_e_org);
                list.add(v1v3);

                Edge v2v3 = new Edge(v2_e_org, v3_e_org);
                list.add(v2v3);
            }

            while (!list.isEmpty()) {
                Edge e = list.poll();
                if (updateList.contains(e)) continue;
                Integer v1 = e.getV1();
                Integer v2 = e.getV2();
                LinkedList<Integer> setCommon = getCommonNeighbors(adjMap, e);

                int ps = 0;
                for (int w : setCommon) {
                    Edge e1 = new Edge(v1, w);
                    Edge e2 = new Edge(v2, w);
                    int t0 = trussMap.get(e);
                    int t1 = trussMap.get(e1);
                    int t2 = trussMap.get(e2);
                    int sSup1 = sSupMap.get(e1);
                    int sSup2 = sSupMap.get(e2);

                    if (t1 > t0 && t2 > t0) ps++;
                    else if (t1 == t0 && t2 > t0 && sSup1 > t0 - 2) ps++;
                    else if (t2 == t0 && t1 > t0 && sSup2 > t0 - 2) ps++;
                    else if (t1 == t0 && t2 == t0 && sSup1 > t0 - 2 && sSup2 > t0 - 2) ps++;
                }
                pSupMap.put(e, ps);
                updateList.add(e);
            }
        }
    }

    /**
     * given a graph, and a set of edges not in graph, compute a tds
     *
     * @param graph
     * @param addEdges
     * @return
     */
    public static LinkedList<Edge> getInsertionTDS(Graph graph, LinkedList<Edge> addEdges) {
        LinkedList<Edge> tds = new LinkedList<>();

        Hashtable<Integer, LinkedList<Integer>> tempAdjMap = GraphHandler.deepCloneAdjMap(graph.getAdjMap());
        tempAdjMap = GraphHandler.insertEdgesToAdjMap(tempAdjMap, addEdges);

        tds.add(addEdges.poll());
        if (addEdges.isEmpty()) return tds;

        for (Edge e_new : addEdges) {
            LinkedList<Edge> e_new_triangleEdgeSet = getTriangleEdges(tempAdjMap, e_new); //new edges

            boolean tdsFlag = true;
            for (int i = 0; i < tds.size(); i++) {
                Edge e_tds = tds.get(i);
                LinkedList<Edge> e_tds_triangleEdgeSet = getTriangleEdges(tempAdjMap, e_tds);
                if (haveCommonElement(e_new_triangleEdgeSet, e_tds_triangleEdgeSet)) {
                    tdsFlag = false;
                    break;
                }
            }
            if (tdsFlag) {
                tds.add(e_new);
            }
        }
        addEdges.removeAll(tds);
        return tds;
    }


    /**
     * given a graph, and a set of edges in graph, compute a tds
     *
     * @param graph
     * @param removeEdges
     * @return
     */
    public static LinkedList<Edge> getDeletionTDS(Graph graph, LinkedList<Edge> removeEdges) {
        LinkedList<Edge> tds = new LinkedList<>();

        Hashtable<Integer, LinkedList<Integer>> tempAdjMap = GraphHandler.deepCloneAdjMap(graph.getAdjMap());

        Edge firstEdge = removeEdges.poll();
        tds.add(firstEdge);

        for (Edge e_new : removeEdges) {
            LinkedList<Edge> e_new_triangleEdgeSet = getTriangleEdges(tempAdjMap, e_new);

            boolean tdsFlag = true;
            for (int i = 0; i < tds.size(); i++) {
                Edge e_tds = tds.get(i);

                LinkedList<Edge> e_tds_triangleEdgeSet = getTriangleEdges(tempAdjMap, e_tds);

                if (haveCommonElement(e_new_triangleEdgeSet, e_tds_triangleEdgeSet)) {
                    tdsFlag = false;
                    break;
                }
            }
            if (tdsFlag) tds.add(e_new);
        }

        tempAdjMap = GraphHandler.removeEdgesFromAdjMap(tempAdjMap, tds);
        removeEdges.removeAll(tds);
        return tds;
    }





}
