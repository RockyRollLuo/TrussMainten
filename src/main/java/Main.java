import algorithm.SupTruss;
import algorithm.TCPIndex;
import algorithm.TrussDecomp;
import algorithm.parallel.Parallel;
import org.apache.log4j.Logger;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

public class Main {
    private static Logger LOGGER = Logger.getLogger(Main.class);
    @Option(abbr = 'p', usage = "Print trussMap")
    public static int print = 1;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'd', usage = "dynamic type, 0:static TrussDecomp, 1:MultiEdgesInsertion, 2:MultiEdgesDeletion")
    public static int dynamicType = 0;

    @Option(abbr = 'a', usage = "algorithm type, 0:TCPIndex, 1:SupTruss, 2:ParaTruss")
    public static int algorithmType = 0;

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int order = 1;

    @Option(abbr = 't', usage = "max thread number")
    public static int threadNum = 1;


    public static void main(String[] args) throws IOException {
        //read parameters
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        LOGGER.info("Basic information:");
        System.err.println("Dynamic edges:" + (int) Math.pow(10, order));
        System.err.println("Dynamic type:" + dynamicType);
        System.err.println("Algorithm type:" + algorithmType);
        System.err.println("Thread number:" + threadNum);

        //read graph
        String datasetName = args[0];
        Graph fullGraph = GraphImport.load(datasetName, delim);

        //dynamic edges 2^d
        int dynamicEdgesSize = (int) Math.pow(10, order);
        LinkedList<Edge> dynamicEdges = RandomUtils.getRandomSetFromSet(fullGraph.getEdgeSet(), dynamicEdgesSize);

        //Graph
        LinkedList<Edge> edgeSet = (LinkedList<Edge>) fullGraph.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(fullGraph.getAdjMap());
        edgeSet.removeAll(dynamicEdges);
        adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, dynamicEdges);
        Graph restGraph = new Graph(adjMap, edgeSet);

        //result_full
        Result result_full = new TrussDecomp(fullGraph).run();
        result_full.setDatasetName(datasetName + "_full");
        Hashtable<Edge, Integer> trussMap_full = (Hashtable<Edge, Integer>) result_full.getOutput();

        //result_rest
        Result result_rest = new TrussDecomp(restGraph).run();
        result_rest.setDatasetName(datasetName + "_rest");
        result_rest.setOrder(order);
        Hashtable<Edge, Integer> trussMap_rest = (Hashtable<Edge, Integer>) result_rest.getOutput();

        //result for below
        Result result1;
        Result result2;
        Result result3;

        /**
         * Dynamic type
         */
        switch (dynamicType) {
            case 0:
                LOGGER.info("==dynamicType 0: Static Truss Decomposition========");
                Export.writeFile(result_full, print);
                break;
            case 1:
                /**
                 * MultiEdgesInsertion
                 */
                LOGGER.info("==dynamicType 1: MultiEdgesInsertion========");
                Export.writeFile(result_rest, print);
                Export.writeFile(result_full, print);

                switch (algorithmType) {
                    case 0:
                        result1 = TCPIndex.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
                        result1.setDatasetName(datasetName);
                        result1.setOrder(order);
                        Export.writeFile(result1, print);
                        break;
                    case 1:
                        result2 = SupTruss.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
                        result2.setDatasetName(datasetName);
                        result2.setOrder(order);
                        Export.writeFile(result2, print);
                        break;
                    case 2:
                        result3 = Parallel.edgesInsertion(restGraph, dynamicEdges, trussMap_rest, threadNum);
                        result3.setDatasetName(datasetName);
                        result3.setOrder(order);
                        result3.setThreadNums(threadNum);
                        Export.writeFile(result3, print);
                        break;
                }
                break;
            case 2:
                /**
                 * MultiEdgesDeletion
                 */
                LOGGER.info("==dynamicType 2: MultiEdgesDeletion========");
                Export.writeFile(result_rest, print);
                Export.writeFile(result_full, print);

                switch (algorithmType) {
                    case 0:
                        result1 = TCPIndex.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
                        result1.setDatasetName(datasetName);
                        result1.setOrder(order);
                        Export.writeFile(result1, print);
                        break;

                    case 1:
                        result2 = SupTruss.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
                        result2.setDatasetName(datasetName);
                        result2.setOrder(order);
                        Export.writeFile(result2, print);
                        break;

                    case 2:
                        result3 = Parallel.edgesDeletion(fullGraph, dynamicEdges, trussMap_full, threadNum);
                        result3.setDatasetName(datasetName);
                        result3.setOrder(order);
                        result3.setThreadNums(threadNum);
                        Export.writeFile(result3, print);
                        break;
                }
                break;
            default:
                break;
        }
    }
}
