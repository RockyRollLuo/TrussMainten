package algorithm.parallel;

import org.apache.log4j.Logger;
import util.Edge;
import util.Graph;
import util.GraphHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

public class ThreadTrussnessDelete implements Runnable {
    private static Logger LOGGER = Logger.getLogger(ThreadTrussnessDelete.class);

    private Graph graph;
    private LinkedList<Edge> sameTrussEdgeList;
    private int k;
    private Hashtable<Edge, Integer> trussMap; //include edges in graph and e0
    private Hashtable<Edge, Integer> sSupMap; //include edges in graph and e0
    private Hashtable<Edge, Boolean> changeMap;//record an edge change

    /**
     * constructor
     */
    public ThreadTrussnessDelete(Graph graph, LinkedList<Edge> sameTrussEdgeList, int k, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sSupMap, Hashtable<Edge, Boolean> changeMap) {
        this.graph = graph;
        this.sameTrussEdgeList = sameTrussEdgeList;
        this.k = k;
        this.trussMap = trussMap;
        this.sSupMap = sSupMap;
        this.changeMap = changeMap;
    }


    @Override
    public void run() {
        LOGGER.info("Start run thread ThreadTrussnessInsert k:" + k);

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

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
        for (Edge e_root : sameTrussEdgeList) {
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
                                if (changeMap.get(ac) == null || (!changeMap.get(ac))) {
                                    stack.push(ac);
                                    edgeVisitedMap.put(ac, true);
                                    int s_ac = sMap.get(ac);
                                    sMap.put(ac, s_ac + sSupMap.get(ac));
                                }
                            }
                            if (!edgeVisitedMap.get(bc)) {
                                if (changeMap.get(bc) == null || (!changeMap.get(bc))) {
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
        }

        LinkedList<Edge> changeTrussEdges = new LinkedList<>();
        for (int i = 0; i < edgeSet.size(); i++) {
            Edge e = edgeSet.get(i);
            if (edgeVisitedMap.get(e) != null) {
                if (edgeVisitedMap.get(e) && edgeElimainateMap.get(e)) {
                    int t = trussMap.get(e);
                    trussMap.put(e, t - 1);
                    changeMap.put(e, true);
                    changeTrussEdges.add(e);
                }
            }
        }

        //TODO:update sSupMap
        GraphHandler.updateSustainSupportMap(graph, trussMap, sSupMap, changeTrussEdges);

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
