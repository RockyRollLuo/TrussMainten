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

    @Option(abbr = 's', usage = "sperate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int order = 0;

    @Option(abbr = 'a', usage = "algorithm type, 0:TrussDecomp, 1:MultiEdgesInsertion, 2:MultiEdgesDeletion, 3:MultiVerticesInsertion, 4:MultiVerticesDeletion")
    public static int a = 0;


    public static void main(String[] args) throws IOException {
        //read parameters
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        //read graph
        String datasetName = args[0];
        Graph fullGraph = GraphImport.load(datasetName, delim, debug);

        //dynamic edges 2^d
        LinkedList<Edge> dynamicEdges = RandomUtils.getRandomSetFromSet(fullGraph.getEdgeSet(), (int) Math.pow(2, order));

        //Graph
        LinkedList<Edge> edgeSet = (LinkedList<Edge>) fullGraph.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(fullGraph.getAdjMap());
        edgeSet.removeAll(dynamicEdges);
        adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, dynamicEdges);
        Graph restGraph = new Graph(adjMap, edgeSet);

        //result
        Result result_full = new TrussDecomp(fullGraph).run();
        Result result_rest = new TrussDecomp(restGraph).run();
        result_full.setDatasetName(datasetName+"full");
        result_rest.setDatasetName(datasetName+"rest");

        Result result1 = null;
        Result result2 = null;
        Result result3 = null;

        /**
         * Dynamic type
         */
        switch (a) {
            case 0:
                Export.writeFile(result_full, debug);
                break;
            case 1:
                /**
                 * MultiEdgesInsertion
                 */
                result1 = TCPIndex.edgesInsertion(restGraph, dynamicEdges);
                result2 = SupTruss.edgesInsertion(restGraph, dynamicEdges);
                result3 = Parallel.edgesInsertion(restGraph, dynamicEdges);

                result1.setDatasetName(datasetName);
                result2.setDatasetName(datasetName);
                result3.setDatasetName(datasetName);

                Export.writeFile(result1, debug);
                Export.writeFile(result2, debug);
                Export.writeFile(result3, debug);

                break;
            case 2:
                /**
                 * MultiEdgesInsertion
                 */
                result1 = TCPIndex.edgesDeletion(fullGraph, dynamicEdges);
                result2 = SupTruss.edgesDeletion(fullGraph, dynamicEdges);
                result3 = Parallel.edgesDeletion(fullGraph, dynamicEdges);

                result1.setDatasetName(datasetName);
                result2.setDatasetName(datasetName);
                result3.setDatasetName(datasetName);

                Export.writeFile(result1, debug);
                Export.writeFile(result2, debug);
                Export.writeFile(result3, debug);

                break;
            case 3:

                break;
            case 4:

                break;
            default:

                break;
        }
    }
}
