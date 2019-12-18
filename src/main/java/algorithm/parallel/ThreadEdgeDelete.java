package algorithm.parallel;

import util.Edge;
import util.Graph;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class ThreadEdgeDelete implements Runnable {

    private Graph graph;
    private Edge e0;
    private Hashtable<Edge, Integer> trussMap; //include edges in graph and e0

    private Hashtable<Edge, Boolean> changeMap;//record an edge change

    /**
     * constructor
     */
    public ThreadEdgeDelete(Graph graph, Edge e0, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Boolean> changeMap) {
        this.graph = graph;
        this.e0 = e0;
        this.trussMap = trussMap;
        this.changeMap = changeMap;
    }

    @Override
    public void run() {
        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();
        Integer v1_e0 = e0.getV1();
        Integer v2_e0 = e0.getV2();
        LinkedList<Integer> set1_e0 = adjMap.get(v1_e0);
        LinkedList<Integer> set2_e0 = adjMap.get(v2_e0);
        LinkedList<Integer> set3_e0 = (LinkedList<Integer>) set1_e0.clone();
        set3_e0.retainAll(set2_e0);

        int t_e0 = trussMap.get(e0);

        //PES
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (int w : set3_e0) {
            Edge e1 = new Edge(w, v1_e0);
            Edge e2 = new Edge(w, v2_e0);

            if (trussMap.get(e1) <= t_e0) {
                promoteEdgeSet.add(e1);
            }
            if (trussMap.get(e2) <= t_e0) {
                promoteEdgeSet.add(e2);
            }
        }

        //update graph
        edgeSet.remove(e0);
        set1_e0.remove(v2_e0);
        set2_e0.remove(v1_e0);
        trussMap.remove(e0);

        //compute local sSupMap
        Hashtable<Edge, Integer> sSupMap = new Hashtable<>();
        for (int i = 0; i < edgeSet.size(); i++) {
            Edge e = edgeSet.get(i);
            Integer v1_e = e.getV1();
            Integer v2_e = e.getV2();
            LinkedList<Integer> set1_e = adjMap.get(v1_e);
            LinkedList<Integer> set2_e = adjMap.get(v2_e);
            LinkedList<Integer> set3_e = (LinkedList<Integer>) set1_e.clone();
            set3_e.retainAll(set2_e);

            int ss = 0;
            for (int v3 : set3_e) {
                Edge e1 = new Edge(v1_e, v3);
                Edge e2 = new Edge(v2_e, v3);
                if (trussMap.get(e) <= Math.min(trussMap.get(e1), trussMap.get(e2)))
                    ss++;
            }
            sSupMap.put(e, ss);
        }


        //Lazy initial
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (int i = 0; i < edgeSet.size(); i++) {
            Edge e = edgeSet.get(i);
            edgeElimainateMap.put(e, false);
            edgeVisitedMap.put(e, false);
            sMap.put(e, 0);
        }

        //Traversal
        for (Edge e_root : promoteEdgeSet) {
            int t_root = trussMap.get(e_root);
            sMap.put(e_root, sSupMap.get(e_root));
            edgeVisitedMap.put(e_root, true);

            Stack<Edge> stack = new Stack<>();
            stack.push(e_root);
            while (!stack.empty()) {
                Edge e_stack = stack.pop();
                if (sMap.get(e_stack) < t_root - 2) { //cannot support
                    eliminate(adjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);

                    Integer a = e_stack.getV1();
                    Integer b = e_stack.getV2();
                    LinkedList<Integer> setA = adjMap.get(a);
                    LinkedList<Integer> setB = adjMap.get(b);
                    LinkedList<Integer> setC = (LinkedList<Integer>) setA.clone();
                    setC.retainAll(setB);

                    for (int c : setC) {
                        Edge ac = new Edge(a, c);
                        Edge bc = new Edge(b, c);

                        if (trussMap.get(ac) == t_root && trussMap.get(bc) > t_root && !edgeVisitedMap.get(ac)) {
                            if (changeMap.get(ac) == null ? false : changeMap.get(ac)) continue;
                            stack.push(ac);
                            edgeVisitedMap.put(ac, true);
                            int s_ac = sMap.get(ac);
                            sMap.put(ac, s_ac + sSupMap.get(ac));
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && !edgeVisitedMap.get(bc)) {
                            if (changeMap.get(ac) == null ? false : changeMap.get(ac)) continue;
                            stack.push(bc);
                            edgeVisitedMap.put(bc, true);
                            int s_bc = sMap.get(bc);
                            sMap.put(bc, s_bc + sSupMap.get(bc));
                        } else if (trussMap.get(ac) == t_root && trussMap.get(bc) == t_root) {
                            if (!edgeVisitedMap.get(ac)) {
                                stack.push(ac);
                                edgeVisitedMap.put(ac, true);
                                int s_ac = sMap.get(ac);
                                sMap.put(ac, s_ac + sSupMap.get(ac));
                            }
                            if (!edgeVisitedMap.get(bc)) {
                                stack.push(bc);
                                edgeVisitedMap.put(bc, true);
                                int s_bc = sMap.get(bc);
                                sMap.put(bc, s_bc + sSupMap.get(bc));
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < edgeSet.size(); i++) {
            Edge e = edgeSet.get(i);
            if (edgeVisitedMap.get(e) != null) {
                if (edgeVisitedMap.get(e) && edgeElimainateMap.get(e)) {
                    int t = trussMap.get(e);
                    trussMap.put(e, t - 1);
                    changeMap.put(e, true);
                }
            }

        }
    }

    /***
     *  eliminate
     *  for edge insertion and deletion
     * @param adjMap
     * @param trussMap
     * @param sMap
     * @param edgeElimanateMap
     * @param t_root
     * @param edge
     */
    private static void eliminate(Hashtable<Integer, LinkedList<Integer>> adjMap, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sMap, Hashtable<Edge, Boolean> edgeElimanateMap, int t_root, Edge edge) {
        edgeElimanateMap.put(edge, Boolean.TRUE);
        Integer a = edge.getV1();
        Integer b = edge.getV2();
        LinkedList<Integer> setA = adjMap.get(a);
        LinkedList<Integer> setB = adjMap.get(b);
        LinkedList<Integer> setC = (LinkedList<Integer>) setA.clone();
        setC.retainAll(setB);
        for (int c : setC) {
            Edge ac = new Edge(a, c);
            Edge bc = new Edge(b, c);
            if (Math.min(trussMap.get(ac), trussMap.get(bc)) >= t_root) {
                if (trussMap.get(ac) == t_root) {
                    int s_ac = sMap.get(ac) - 1;
                    sMap.put(ac, s_ac);
                    if (s_ac == t_root - 3 && !edgeElimanateMap.get(ac)) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, ac);
                    }
                }
                if (trussMap.get(bc) == t_root) {
                    int s_bc = sMap.get(bc) - 1;
                    sMap.put(ac, s_bc);
                    if (s_bc == t_root - 3 && !edgeElimanateMap.get(bc)) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, bc);
                    }
                }
            }
        }
    }
}
