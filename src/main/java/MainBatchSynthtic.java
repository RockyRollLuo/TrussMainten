import algorithm.SupTruss;
import algorithm.TrussDecomp;
import org.apache.log4j.Logger;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * synthtic graphs vertex size from 2^15--2^20
 * dynamic edges is 10^4
 */

public class MainBatchSynthtic {
    private static Logger LOGGER = Logger.getLogger(MainBatchSynthtic.class);
    @Option(abbr = 'p', usage = "Print trussMap")
    public static int print = 0;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude")
    public static int maxOrder = 4;

    public static void main(String[] args) throws IOException {
        //read parameters
        MainBatchSynthtic main = new MainBatchSynthtic();
        args = SetOpt.setOpt(main, args);

        for (int dataseteOrder = 15; dataseteOrder <= 20; dataseteOrder=dataseteOrder+2) {
            String datasetName="hk_"+dataseteOrder+".txt";

            LOGGER.info("Basic information:");
            System.err.println("datasetName:" + datasetName);
            System.err.println("Order of dynamic edges:" + maxOrder);

            //full graph
            Graph fullGraph = GraphImport.load(datasetName, delim);

            //result_full
            Result result_full = new TrussDecomp(fullGraph).run();
            result_full.setDatasetName(datasetName + "_full");
            Hashtable<Edge, Integer> trussMap_full = (Hashtable<Edge, Integer>) result_full.getOutput();
            Export.writeFile(result_full, 0);

            //dynamic edges 10^d
            int dynamicEdgesSize = (int) Math.pow(10, maxOrder);
            LinkedList<Edge> dynamicEdges = RandomUtils.getRandomSetFromSet(fullGraph.getEdgeSet(), dynamicEdgesSize);

            //rest Graph
            LinkedList<Edge> edgeSet = (LinkedList<Edge>) fullGraph.getEdgeSet().clone();
            Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(fullGraph.getAdjMap());
            edgeSet.removeAll(dynamicEdges);
            adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, dynamicEdges);
            Graph restGraph = new Graph(adjMap, edgeSet);

            //result_rest
            Result result_rest = new TrussDecomp(restGraph).run();
            result_rest.setDatasetName(datasetName + "_rest");
            result_rest.setOrder(maxOrder);
            Hashtable<Edge, Integer> trussMap_rest = (Hashtable<Edge, Integer>) result_rest.getOutput();
            Export.writeFile(result_rest, print);

            //ONE-two algorithm2:SupInsertion
            Result result12 = SupTruss.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
            result12.setDatasetName(datasetName);
            result12.setOrder(maxOrder);
            Export.writeFile(result12, print);

            //TWO-two algorithm2:SupDeletion
            Result result22 = SupTruss.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
            result22.setDatasetName(datasetName);
            result22.setOrder(maxOrder);
            Export.writeFile(result22, print);
        }


    }
}
