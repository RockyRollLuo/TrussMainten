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

    @Option(abbr = 'r', usage = "Print the result.")
    public static boolean printResult = true;

    @Option(abbr = 'p', usage = "print the progress")
    public static int debug = 1;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int order = 1;

    @Option(abbr = 'a', usage = "algorithm type, 0:TrussDecomp, 1:MultiEdgesInsertion, 2:MultiEdgesDeletion, 3:MultiVerticesInsertion, 4:MultiVerticesDeletion")
    public static int a = 0;

    @Option(abbr = 't', usage = "max thread number")
    public static int t = 1;


    public static void main(String[] args) throws IOException {
        //read parameters
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        LOGGER.info("Basic information:");
        System.err.println("Dynamic edges:" + (int) Math.pow(2, order));
        System.err.println("Algorithm type:" + a);
        System.err.println("Thread number:" + t);

        //read graph
        String datasetName = args[0];
        Graph fullGraph = GraphImport.load(datasetName, delim);

        //dynamic edges 2^d
        int dynamicEdgesSize = (int) Math.pow(2, order);
        LinkedList<Edge> dynamicEdges = RandomUtils.getRandomSetFromSet(fullGraph.getEdgeSet(), dynamicEdgesSize);

        //Graph
        LinkedList<Edge> edgeSet = (LinkedList<Edge>) fullGraph.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(fullGraph.getAdjMap());
        edgeSet.removeAll(dynamicEdges);
        adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, dynamicEdges);
        Graph restGraph = new Graph(adjMap, edgeSet);

        //result
        Result result_full = new TrussDecomp(fullGraph).run();
        result_full.setDatasetName(datasetName + "_full");
        result_full.setDynamicEdges(0);
        result_full.setThreadNums(0);

        Result result_rest = new TrussDecomp(restGraph).run();
        result_rest.setDatasetName(datasetName + "_rest");
        result_full.setDynamicEdges(0);
        result_full.setThreadNums(0);

        //prepare for algorithms below
        Hashtable<Edge, Integer> trussMap_full = (Hashtable<Edge, Integer>) result_full.getOutput();
        Hashtable<Edge, Integer> trussMap_rest = (Hashtable<Edge, Integer>) result_full.getOutput();

        Result result1 = null;
        Result result2 = null;
        Result result3 = null;

        /**
         * Dynamic type
         */
        switch (a) {
            case 0:
                LOGGER.info("==Algorithm 0: truss decomposition========");
                Export.writeFile(result_full);
                break;
            case 1:
                /**
                 * MultiEdgesInsertion
                 */
                LOGGER.info("==Algorithm 1: MultiEdgesInsertion========");
                result1 = TCPIndex.edgesInsertion(restGraph, dynamicEdges, trussMap_full);
                result2 = SupTruss.edgesInsertion(restGraph, dynamicEdges, trussMap_full);
                result3 = Parallel.edgesInsertion(restGraph, dynamicEdges, trussMap_full, t);

                result1.setDatasetName(datasetName);
                result2.setDatasetName(datasetName);
                result3.setDatasetName(datasetName);

                result1.setDynamicEdges(dynamicEdgesSize);
                result2.setDynamicEdges(dynamicEdgesSize);
                result3.setDynamicEdges(dynamicEdgesSize);

                Export.writeFile(result_full);

                break;
            case 2:
                /**
                 * MultiEdgesInsertion
                 */
                LOGGER.info("==Algorithm 2: MultiEdgesDeletion========");
                result1 = TCPIndex.edgesDeletion(fullGraph, dynamicEdges, trussMap_rest);
                result2 = SupTruss.edgesDeletion(fullGraph, dynamicEdges, trussMap_rest);
                result3 = Parallel.edgesDeletion(fullGraph, dynamicEdges, trussMap_rest, t);

                result1.setDatasetName(datasetName);
                result2.setDatasetName(datasetName);
                result3.setDatasetName(datasetName);

                Export.writeFile(result_rest);
                break;
            case 3:
                LOGGER.info("==Algorithm 3: MultiVerticsInsertion========");
                break;
            case 4:
                LOGGER.info("==Algorithm 4: MultiVerticsDeletion========");
                break;
            default:

                break;
        }
        Export.writeFile(result1);
        Export.writeFile(result2);
        Export.writeFile(result3);
    }
}
