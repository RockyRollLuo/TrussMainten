package algorithm;

import org.apache.log4j.Logger;
import util.*;

import java.util.*;

public class SupTruss {
    private static Logger LOGGER = Logger.getLogger(SupTruss.class);

    /**
     * one random edge insertion
     *
     * @param graph the object of Graph
     * @return
     */
    public static Result edgeInsertion(Graph graph) {
        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        Edge e0 = RandomUtils.getRandomElement(oldEdgeSet);

        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        return edgeInsertion(newGraph, e0);
    }

    /**
     * one edge insertion
     *
     * @param graph
     * @param e0
     * @return
     */
    public static Result edgeInsertion(Graph graph, Edge e0) {
        LOGGER.info("Initial SupTruss Insertion one edge" + e0.toString());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        if (oldEdgeSet.contains(e0)) {
            LOGGER.error("SupTruss edgeInsertion: Graph already contain edge:" + e0.toString());
            return new Result(GraphHandler.computeTrussMap(graph), 0, "SupTrussInsertion");
        }

        //new Graph
        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.add(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgeToAdjMap(newAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //compute trussMap from old graph
        Hashtable<Edge, Integer> trussMap = GraphHandler.computeTrussMap(graph);

        //compute truss of e0
        int t_e0_LB = GraphHandler.computeTrussnessLowerBound(newGraph, trussMap, e0);
        trussMap.put(e0, t_e0_LB);

        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);

        //compute PivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = GraphHandler.computePivotalSupportMap(newGraph, trussMap, sSupMap);

        int t_e0_UB = GraphHandler.computeTrussnessUpperBound(pSupMap, e0, t_e0_LB);

        /**
         * main process
         * one edge insertion update
         */
        LOGGER.info("Start SupTruss Insertion one edge" + e0.toString());
        long startTime = System.currentTimeMillis();

        //PES
        Integer v1_e0 = e0.getV1();
        Integer v2_e0 = e0.getV2();
        LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);

        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(w, v1_e0);
            Edge e2 = new Edge(w, v2_e0);

            if (trussMap.get(e1) < t_e0_UB) {
                promoteEdgeSet.add(e1);
            }
            if (trussMap.get(e2) < t_e0_UB) {
                promoteEdgeSet.add(e2);
            }
        }

        //lazy initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : newEdgeSet) {
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
                    LinkedList<Integer> setC=GraphHandler.getCommonNeighbors(newAdjMap,e_stack);

                    for (int c : setC) {
                        Edge ac = new Edge(a, c);
                        Edge bc = new Edge(b, c);

                        if (trussMap.get(ac) == t_root && trussMap.get(bc) > t_root && sSupMap.get(ac) > t_root - 2 && !edgeVisitedMap.get(ac)) {
                            stack.push(ac);
                            edgeVisitedMap.put(ac, true);
                            int s_ac = sMap.get(ac);
                            sMap.put(ac, s_ac + pSupMap.get(ac));
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && sSupMap.get(bc) > t_root - 2 && !edgeElimainateMap.get(bc)) {
                            stack.push(bc);
                            edgeVisitedMap.put(bc, true);
                            int s_bc = sMap.get(bc);
                            sMap.put(bc, s_bc + pSupMap.get(bc));
                        } else if (trussMap.get(ac) == t_root && trussMap.get(bc) == t_root && sSupMap.get(ac) > t_root - 2 && sSupMap.get(bc) > t_root - 2) {
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
                        eliminate(newAdjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);
                    }
                }
            }
        }

//        Hashtable<Edge, Integer> newTrussMap = (Hashtable<Edge, Integer>) trussMap.clone();
        for (Edge e : newEdgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = trussMap.get(e);
                trussMap.put(e, t + 1);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "SupTrussInsertEdge");

        LOGGER.info("Truss decomposition is computed");
        return result;
    }


    /**
     * insert a random set of edges to graph
     * @param graph
     * @param dynamicEdges
     * @return
     */
    public static Result edgesInsertion(Graph graph, LinkedList<Edge> dynamicEdges) {
        long totalTime = 0;
        Result tempResult = null;
        int times=0;

        while (!dynamicEdges.isEmpty()) {

            LinkedList<Edge> tds = GraphHandler.getInsertionTDS(graph, dynamicEdges);

            //compute tds
            tempResult = edgeTDSInsertion(graph, tds);
            totalTime += tempResult.getTakenTime();

            //update graph
            graph = tempResult.getGraph();

            //update dynamicEdges
            dynamicEdges.removeAll(tds);

            times++;
        }

        Result result = new Result(tempResult.getOutput(), totalTime, "SupTrussEdges");
        result.setTimes(times);

        return result;
    }


    /***
     * tds edges insertion
     * @param graph
     * @param tds
     * @return
     */
    public static Result edgeTDSInsertion(Graph graph, LinkedList<Edge> tds) {
        LOGGER.info("Initial SupTruss Inserte  TDS:" + tds.toString());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        //new Graph
        LinkedList<Edge> newEdgeSet = ((LinkedList<Edge>) oldEdgeSet.clone());
        newEdgeSet.addAll(tds);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.insertEdgesToAdjMap(newAdjMap, tds);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //compute trussMap from old graph
        Hashtable<Edge, Integer> trussMap = GraphHandler.computeTrussMap(graph);

        //computing trussness of new inserting edges
        for (Edge e0 : tds) {
            //compute truss of e0
            int t_e0_LB = GraphHandler.computeTrussnessLowerBound(newGraph, trussMap, e0);
            trussMap.put(e0, t_e0_LB);
        }

        /**
         * start main processing
         */
        long startTime = System.currentTimeMillis();

        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);

        //compute PivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = GraphHandler.computePivotalSupportMap(newGraph, trussMap, sSupMap);

        //lazy initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : newEdgeSet) {
            edgeVisitedMap.put(e, false);
            edgeElimainateMap.put(e, false);
            sMap.put(e, 0);
        }
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();

        for (Edge e0 : tds) {
            int t_e0_UB = GraphHandler.computeTrussnessUpperBound(pSupMap, e0, trussMap.get(e0));

            Integer v1_e0 = e0.getV1();
            Integer v2_e0 = e0.getV2();
            LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(newAdjMap, e0);

            for (int w : set0Common) {
                Edge e1 = new Edge(w, v1_e0);
                Edge e2 = new Edge(w, v2_e0);

                if (trussMap.get(e1) < t_e0_UB) {
                    promoteEdgeSet.add(e1);
                }
                if (trussMap.get(e2) < t_e0_UB) {
                    promoteEdgeSet.add(e2);
                }
            }
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
                    LinkedList<Integer> setC=GraphHandler.getCommonNeighbors(newAdjMap,e_stack);

                    for (int c : setC) {
                        Edge ac = new Edge(a, c);
                        Edge bc = new Edge(b, c);

                        if (trussMap.get(ac) == t_root && trussMap.get(bc) > t_root && sSupMap.get(ac) > t_root - 2 && !edgeVisitedMap.get(ac)) {
                            stack.push(ac);
                            edgeVisitedMap.put(ac, true);
                            int s_ac = sMap.get(ac);
                            sMap.put(ac, s_ac + pSupMap.get(ac));
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && sSupMap.get(bc) > t_root - 2 && !edgeElimainateMap.get(bc)) {
                            stack.push(bc);
                            edgeVisitedMap.put(bc, true);
                            int s_bc = sMap.get(bc);
                            sMap.put(bc, s_bc + pSupMap.get(bc));
                        } else if (trussMap.get(ac) == t_root && trussMap.get(bc) == t_root && sSupMap.get(ac) > t_root - 2 && sSupMap.get(bc) > t_root - 2) {
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
                        eliminate(newAdjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);
                    }
                }
            }
        }

//        Hashtable<Edge, Integer> newTrussMap = (Hashtable<Edge, Integer>) trussMap.clone();
        for (Edge e : newEdgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = trussMap.get(e);
                trussMap.put(e, t + 1);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "SupTrussInsertEdge");

        LOGGER.info("Truss decomposition is computed");
        return result;
    }

    /**
     * one random edge deletion
     *
     * @param graph the object of Graph
     * @return
     */
    public static Result edgeDeletion(Graph graph) {
        LinkedList<Edge> edgeSet = graph.getEdgeSet();

        Edge e0 = RandomUtils.getRandomElement(edgeSet);

        return edgeDeletion(graph, e0);
    }

    /**
     * one edge deletion
     *
     * @param graph
     * @param e0
     * @return
     */
    public static Result edgeDeletion(Graph graph, Edge e0) {
        LOGGER.info("SupTruss deletion one edge:" + e0.toString());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        if (!oldEdgeSet.contains(e0)) {
            LOGGER.error("SupTruss edgeDeletion: Graph does not contain edge:" + e0.toString());
            return null;
        }

        //Construct new graph
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(e0);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.romveEdgeFromAdjMap(oldAdjMap, e0);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //compute old graph trussMap
        Hashtable<Edge, Integer> trussMap = GraphHandler.computeTrussMap(graph);

        //compute sustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);

        //compute pivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = GraphHandler.computePivotalSupportMap(newGraph, trussMap, sSupMap);

        /**
         * main part
         * one edge insertion update
         */
        long startTime = System.currentTimeMillis();
        int t_e0 = trussMap.get(e0);
        trussMap.remove(e0);

        //PES
        Integer v1_e0 = e0.getV1();
        Integer v2_e0 = e0.getV2();
        LinkedList<Integer> neiList_v1_e0 = newAdjMap.get(v1_e0);
        LinkedList<Integer> neiList_v2_e0 = newAdjMap.get(v2_e0);
        LinkedList<Integer> set0Common = (LinkedList<Integer>) neiList_v1_e0.clone();
        set0Common.retainAll(neiList_v2_e0);
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();
        for (int w : set0Common) {
            Edge e1 = new Edge(w, v1_e0);
            Edge e2 = new Edge(w, v2_e0);
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
        for (Edge e : newEdgeSet) {
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
                if (sMap.get(e_stack) < t_root - 2) { //cannot support
                    eliminate(newAdjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);

                    Integer a = e_stack.getV1();
                    Integer b = e_stack.getV2();
                    LinkedList<Integer> setA = newAdjMap.get(a);
                    LinkedList<Integer> setB = newAdjMap.get(b);
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
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && !edgeVisitedMap.get(bc)) {
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

        for (Edge e : newEdgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = trussMap.get(e);
                trussMap.put(e, t - 1);
            }
        }

        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "SupTrussDeleteEdge");

        LOGGER.info("SupTruss edgeDeleteion is computed");
        return result;
    }


    /**
     * delete a random set of edge from graph
     * @param graph
     * @param dynamicEdges
     * @return
     */
    public static Result edgesDeletion(Graph graph, LinkedList<Edge> dynamicEdges) {
        long totalTime = 0;
        Result tempResult = null;
        int times=0;

        while (!dynamicEdges.isEmpty()) {

            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, dynamicEdges);

            //compute tds
            tempResult = edgeTDSInsertion(graph, tds);
            totalTime += tempResult.getTakenTime();

            //update graph
            graph = tempResult.getGraph();

            //update dynamicEdges
            dynamicEdges.removeAll(tds);

            times++;
        }

        Result result = new Result(tempResult.getOutput(), totalTime, "SupTrussEdges");
        result.setTimes(times);

        return result;
    }


    /**
     * delete a tds from a graph
     * @param graph
     * @param tds
     * @return
     */
    public static Result edgeTDSDeletion(Graph graph, LinkedList<Edge> tds) {
        LOGGER.info("SupTruss deletion tds:" + tds.toString());

        Hashtable<Integer, LinkedList<Integer>> oldAdjMap = graph.getAdjMap();
        LinkedList<Edge> oldEdgeSet = graph.getEdgeSet();

        //Construct new graph
        LinkedList<Edge> newEdgeSet = (LinkedList<Edge>) oldEdgeSet.clone();
        newEdgeSet.remove(tds);
        Hashtable<Integer, LinkedList<Integer>> newAdjMap = GraphHandler.deepCloneAdjMap(oldAdjMap);
        newAdjMap = GraphHandler.removeEdgesFromAdjMap(oldAdjMap,tds);
        Graph newGraph = new Graph(newAdjMap, newEdgeSet);

        //compute trussMap from old graph
        Hashtable<Edge, Integer> trussMap = GraphHandler.computeTrussMap(graph);


        /**
         * start main processing
         */
        long startTime = System.currentTimeMillis();

        //compute SustainSupportMap
        Hashtable<Edge, Integer> sSupMap = GraphHandler.computeSustainSupportMap(newGraph, trussMap);

        //compute PivotalSupportMap
        Hashtable<Edge, Integer> pSupMap = GraphHandler.computePivotalSupportMap(newGraph, trussMap, sSupMap);

        //lazy initial
        Hashtable<Edge, Boolean> edgeVisitedMap = new Hashtable<>();
        Hashtable<Edge, Boolean> edgeElimainateMap = new Hashtable<>();
        Hashtable<Edge, Integer> sMap = new Hashtable<>();
        for (Edge e : newEdgeSet) {
            edgeVisitedMap.put(e, false);
            edgeElimainateMap.put(e, false);
            sMap.put(e, 0);
        }
        LinkedList<Edge> promoteEdgeSet = new LinkedList<>();

        for (Edge e0 : tds) {

            int t_e0 = trussMap.get(e0);
            Integer v1_e0 = e0.getV1();
            Integer v2_e0 = e0.getV2();
            LinkedList<Integer> set0Common = GraphHandler.getCommonNeighbors(oldAdjMap, e0);

            for (int w : set0Common) {
                Edge e1 = new Edge(w, v1_e0);
                Edge e2 = new Edge(w, v2_e0);

                if (trussMap.get(e1) <= t_e0) {
                    promoteEdgeSet.add(e1);
                }
                if (trussMap.get(e2) <= t_e0) {
                    promoteEdgeSet.add(e2);
                }
            }
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
                if (sMap.get(e_stack) < t_root - 2) { //cannot support
                    eliminate(newAdjMap, trussMap, sMap, edgeElimainateMap, t_root, e_stack);

                    Integer a = e_stack.getV1();
                    Integer b = e_stack.getV2();
                    LinkedList<Integer> setA = newAdjMap.get(a);
                    LinkedList<Integer> setB = newAdjMap.get(b);
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
                        } else if (trussMap.get(bc) == t_root && trussMap.get(ac) > t_root && !edgeVisitedMap.get(bc)) {
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

//        Hashtable<Edge, Integer> newTrussMap = (Hashtable<Edge, Integer>) trussMap.clone();
        for (Edge e : newEdgeSet) {
            if (edgeVisitedMap.get(e) && !edgeElimainateMap.get(e)) {
                int t = trussMap.get(e);
                trussMap.put(e, t - 1);
            }
        }

        trussMap.remove(tds);
        long endTime = System.currentTimeMillis();
        Result result = new Result(trussMap, endTime - startTime, "SupTrussInsertEdge");

        LOGGER.info("Truss decomposition is computed");
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
                if (trussMap.get(ac) == t_root - 2) {
                    int s_ac = sMap.get(ac) - 1;
                    sMap.put(ac, s_ac);
                    if (s_ac == t_root - 2 && !edgeElimanateMap.get(ac)) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, ac);
                    }
                }
                if (trussMap.get(bc) == t_root - 2) {
                    int s_bc = sMap.get(bc) - 1;
                    sMap.put(ac, s_bc);

                    if (s_bc == t_root - 2 && !edgeElimanateMap.get(bc)) {
                        eliminate(adjMap, trussMap, sMap, edgeElimanateMap, t_root, bc);
                    }
                }
            }
        }
    }

}
