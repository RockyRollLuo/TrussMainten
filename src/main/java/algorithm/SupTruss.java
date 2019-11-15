package algorithm;

import org.apache.log4j.Logger;
import util.*;

import java.util.*;

public class SupTruss {
    private static Logger LOGGER = Logger.getLogger(SupTruss.class);

    /**
     * one random edge insertion
     * @param graph the object of Graph
     * @param debug
     * @return
     */
    public Result edgeInsertion(Graph graph, int debug){
        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        Edge e0 = RandomUtils.getRandomElement(oldEdgeSet);

        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);
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
    public Result edgeInsertion(Graph graph, Edge e0, int debug){
        if (debug > 0)
            LOGGER.info("SupTruss Insertion one edge %s:" + e0.toString());

        Hashtable<Integer, LinkedList<Integer>> adjMap = graph.getAdjMap();
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        if (edgeSet.contains(e0)) {
            LOGGER.error("SupTruss edgeInsertion: Graph already contain edge:"+e0.toString());
            return null;
        }

        //compute trussMap
        TrussDecomp trussDecomp = new TrussDecomp(graph);
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussDecomp.run(debug).getOutput();

        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            Integer v1 = e.getV1();
            Integer v2 = e.getV2();
            LinkedList<Integer> set1 = adjMap.get(v1);
            LinkedList<Integer> set2 = adjMap.get(v2);
            LinkedList<Integer> setCommon = (LinkedList<Integer>) set1.clone();
            setCommon.retainAll(set2);

            int ss = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                if (trussMap.get(e) <= Math.min(trussMap.get(e1), trussMap.get(e2)))
                    ss++;
            }
            sSupMap.put(e, ss);
        }

        //compute PivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            Integer v1 = e.getV1();
            Integer v2 = e.getV2();
            LinkedList<Integer> set1 = adjMap.get(v1);
            LinkedList<Integer> set2 = adjMap.get(v2);
            LinkedList<Integer> setCommon = (LinkedList<Integer>) set1.clone();
            setCommon.retainAll(set2);

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
        }

        /**
         * main part
         * one edge insertion update
         */
        long startTime = System.currentTimeMillis();
        Integer v01 = e0.getV1();
        Integer v02 = e0.getV2();

        //v1 or v2 is new
        if ((!adjMap.keySet().contains(v01)) || (!adjMap.keySet().contains(v02))) {
            adjMap = GraphHandler.insertEdgeToAdjMap(adjMap, e0);
            edgeSet.add(e0);
            trussMap.put(e0, 2);
            return new Result(trussMap, System.currentTimeMillis() - startTime, this.getClass().toString());
        }

        //insert this edge
        edgeSet.add(e0);
        adjMap = GraphHandler.insertEdgeToAdjMap(adjMap, e0);

        //counter triangle trussness
        LinkedList<Integer> set01 = adjMap.get(v01);
        LinkedList<Integer> set02 = adjMap.get(v02);
        LinkedList<Integer> set0Common = (LinkedList<Integer>) set01.clone();
        set0Common.retainAll(set02);
        ArrayList<Integer> commonTrussList = new ArrayList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(v01, w);
            Edge e2 = new Edge(v02, w);
            commonTrussList.add(Math.min(trussMap.get(e1), trussMap.get(e2)));
        }

        //compute t_e0_LB
        int t_common_max = Collections.max(commonTrussList);
        HashMap<Integer, Integer> countMap = new HashMap<>();
        for (int i = 2; i < t_common_max + 2; i++) {      //countMap.put(t_common_max + 1, 0), prevent null pointer
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
        int t_e0_LB = key_truss - 1;
        trussMap.put(e0, t_e0_LB);

        //compute sSup
        int sSup = 0;
        for (int i : commonTrussList) {
            if (i >= t_e0_LB)
                sSup++;
        }
        sSupMap.put(e0, sSup);

        //compute pSup
        int pSup = 0;
        for (int w : set0Common) {
            Edge e1 = new Edge(w, v01);
            Edge e2 = new Edge(w, v02);

            if (trussMap.get(e1) > t_e0_LB && trussMap.get(e2) > t_e0_LB) {
                pSup++;
            } else if (trussMap.get(e1) > t_e0_LB && trussMap.get(e2) == t_e0_LB && sSupMap.get(e2) > t_e0_LB - 2) {
                pSup++;
            } else if (trussMap.get(e1) == t_e0_LB && trussMap.get(e2) > t_e0_LB && sSupMap.get(e1) > t_e0_LB - 2) {
                pSup++;
            } else if (trussMap.get(e1) == t_e0_LB && trussMap.get(e2) == t_e0_LB && sSupMap.get(e1) > t_e0_LB - 2 && sSupMap.get(e2) > t_e0_LB - 2) {
                pSup++;
            }
        }
        pSupMap.put(e0, pSup);

        //compute upper bound
        int t_e0_UB = t_e0_LB;
        if (pSup > t_e0_LB - 2) {
            t_e0_UB = t_e0_LB + 1;
        }

        //PES
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(w, v01);
            Edge e2 = new Edge(w, v02);

            if (trussMap.get(e1) < t_e0_UB) {
                promoteEdgeSet.add(e1);
            }
            if (trussMap.get(e2) < t_e0_UB) {
                promoteEdgeSet.add(e2);
            }
        }

        //initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            edgeVisitedMap.put(e, false);
            edgeElimainateMap.put(e, false);
            sMap.put(e, 0);
        }

        //traversal
        for (Edge e_root : promoteEdgeSet) {
            int t_root = trussMap.get(e_root);
            sMap.put(e_root, pSupMap.get(e_root));
            edgeVisitedMap.put(e_root, true);

            Stack<Edge> stack = new Stack<>();
            stack.push(e_root);
            while (!stack.empty()) {
                Edge e_stack = stack.pop();
                if (sMap.get(e_stack) > t_root - 2) {
                    int a = e_stack.getV1();
                    int b = e_stack.getV2();
                    LinkedList<Integer> setA = adjMap.get(a);
                    LinkedList<Integer> setB = adjMap.get(b);
                    LinkedList<Integer> setC = (LinkedList<Integer>) setA.clone();
                    setC.retainAll(setB);

                    for (int c : setC) {
                        Edge ac = new Edge(a, c);
                        Edge bc = new Edge(b, c);

                        if (trussMap.get(ac) == t_root && trussMap.get(bc) > t_root && sSupMap.get(ac) > t_root - 2 && !edgeVisitedMap.get(ac)) {
                            stack.push(ac);
                            edgeVisitedMap.put(ac, true);
                            int s_ac = sMap.get(ac);
                            sMap.put(ac, s_ac + pSupMap.get(ac));
                        }else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && sSupMap.get(bc) > t_root - 2 && !edgeElimainateMap.get(bc)) {
                            stack.push(bc);
                            edgeVisitedMap.put(bc, true);
                            int s_bc = sMap.get(bc);
                            sMap.put(bc, s_bc + pSupMap.get(bc));
                        }else if (trussMap.get(ac) == t_root && trussMap.get(bc) == t_root && sSupMap.get(ac) > t_root - 2 && sSupMap.get(bc) > t_root - 2) {
                            if (!edgeVisitedMap.get(ac)) {
                                stack.push(ac);
                                edgeVisitedMap.put(ac, true);
                                int s_ac = sMap.get(ac);
                                sMap.put(ac, s_ac + pSupMap.get(ac));
                            }
                            if (!edgeVisitedMap.get(bc)) {
                                stack.push(bc);
                                edgeVisitedMap.put(bc, true);
                                int s_bc = sMap.get(bc);
                                sMap.put(bc, s_bc + pSupMap.get(bc));
                            }
                        }
                    }
                } else {
                    if (!edgeElimainateMap.get(e_stack)) {
                        eliminate(adjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);
                    }
                }
            }
        }

//        Hashtable<Edge, Integer> newTrussMap = (Hashtable<Edge, Integer>) trussMap.clone();
        for (Edge e : edgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = trussMap.get(e);
                trussMap.put(e, t + 1);
            }
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
     * @param TDS
     * @return
     */
    public Result edgesInsertion(Graph graph, TreeSet<Edge> TDS) {
        //TODO



        return null;
    }


    /**
     * TODO:remain
     * one random edge deletion
     *
     * @param graph the object of Graph
     * @param debug
     * @return
     */
    public Result edgeDeletion(Graph graph, int debug)  {
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        Edge e0 = RandomUtils.getRandomElement(edgeSet);

        return edgeDeletion(graph, e0, debug);
    }

    /**
     * one edge deletion
     *
     * @param graph
     * @param e0
     * @param debug
     * @return
     */
    public Result edgeDeletion(Graph graph, Edge e0, int debug){
        if (debug > 0)
            LOGGER.info("SupTruss deletion one edge:" + e0.toString());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        if (!oldEdgeSet.contains(e0)) {
            LOGGER.error("SupTruss edgeDeletion: Graph does not contain edge:"+e0.toString());
            return null;
        }

        //compute old graph trussness
        TrussDecomp trussDecomp= new TrussDecomp(graph);
        Hashtable<Edge, Integer> trussMap = (Hashtable<Edge, Integer>) trussDecomp.run(debug).getOutput();

        //Construct new graph
        TreeSet<Edge> edgeSet = (TreeSet<Edge>) oldEdgeSet.clone();
        edgeSet.remove(e0);
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);

        //compute SustainSupport
        Hashtable<Edge, Integer> sSupMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            Integer v1 = e.getV1();
            Integer v2 = e.getV2();
            LinkedList<Integer> set1 = adjMap.get(v1);
            LinkedList<Integer> set2 = adjMap.get(v2);
            LinkedList<Integer> setCommon = (LinkedList<Integer>) set1.clone();
            setCommon.retainAll(set2);

            int ss = 0;
            for (int w : setCommon) {
                Edge e1 = new Edge(v1, w);
                Edge e2 = new Edge(v2, w);
                if (trussMap.get(e) <= Math.min(trussMap.get(e1), trussMap.get(e2)))
                    ss++;
            }
            sSupMap.put(e, ss);
        }

        /**
         * main part
         * one edge insertion update
         */
        long startTime = System.currentTimeMillis();

        int t_e0 = trussMap.get(e0);
        Integer v1 = e0.getV1();
        Integer v2 = e0.getV2();
        LinkedList<Integer> set1 = oldAdjMap.get(v1);
        LinkedList<Integer> set2 = oldAdjMap.get(v2);
        LinkedList<Integer> setCommon = (LinkedList<Integer>) set1.clone();
        setCommon.retainAll(set2);

        ArrayList<Integer> commonTrussList = new ArrayList<>();
        for (int w : setCommon) {
            Edge e1 = new Edge(v1, w);
            Edge e2 = new Edge(v2, w);
            commonTrussList.add(Math.min(trussMap.get(e1), trussMap.get(e2)));
        }

        //PES
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (int w : setCommon) {
            Edge e1 = new Edge(w, v1);
            Edge e2 = new Edge(w, v2);

            if (trussMap.get(e1) <= t_e0) {
                promoteEdgeSet.add(e1);
            }
            if (trussMap.get(e2) <= t_e0) {
                promoteEdgeSet.add(e2);
            }
        }

        //initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : edgeSet) {
            edgeVisitedMap.put(e, false);
            edgeElimainateMap.put(e, false);
            sMap.put(e, 0);
        }

        //traversal
        for (Edge e_root : promoteEdgeSet) {
            int t_root = trussMap.get(e_root);
            sMap.put(e_root, sSupMap.get(e_root)); //just sSup for edge deletion
            edgeVisitedMap.put(e_root, true);

            Stack<Edge> stack = new Stack<>();
            stack.push(e_root);
            while (!stack.empty()) {
                Edge e_stack = stack.pop();
                if (sMap.get(e_stack) < t_root - 2) {
                    eliminate(adjMap,trussMap,sMap,edgeElimainateMap,t_root,e_stack);

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
                            stack.push(ac);
                            edgeVisitedMap.put(ac, true);
                            int s_ac = sMap.get(ac);
                            sMap.put(ac, s_ac + sSupMap.get(ac));
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) >t_root && !edgeVisitedMap.get(bc)) {
                            stack.push(bc);
                            edgeVisitedMap.put(bc, true);
                            int s_bc = sMap.get(bc);
                            sMap.put(bc, s_bc + sSupMap.get(bc));
                        } else if (trussMap.get(ac) == t_root && trussMap.get(bc) == t_root ) {
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

        Hashtable<Edge, Integer> newTrussMap = (Hashtable<Edge, Integer>) trussMap.clone();
        newTrussMap.remove(e0);
        for (Edge e : edgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = newTrussMap.get(e);
                newTrussMap.put(e, t - 1);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(newTrussMap, endTime - startTime, this.getClass().toString());

        if (debug > 0)
            LOGGER.info("SupTruss edgeDeleteion is computed");
        return result;
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
    private void eliminate(Hashtable<Integer, LinkedList<Integer>> adjMap, Hashtable<Edge, Integer> trussMap, Hashtable<Edge, Integer> sMap, Hashtable<Edge, Boolean> edgeElimanateMap, int t_root, Edge edge) {
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
                if (trussMap.get(ac) == t_root - 2) {
                    int s_ac = sMap.get(ac) - 1;
                    sMap.put(ac, s_ac);
                    if (s_ac == t_root - 2 && edgeElimanateMap.get(ac) == false) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, ac);
                    }
                }
                if (trussMap.get(bc) == t_root - 2) {
                    int s_bc = sMap.get(bc) - 1;
                    sMap.put(ac, s_bc);

                    if (s_bc == t_root - 2 && edgeElimanateMap.get(bc) == false) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, bc);
                    }
                }
            }
        }
    }

}
