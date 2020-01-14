import algorithm.TCPIndex;
import algorithm.TrussDecomp;
import org.apache.log4j.Logger;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

public class MainBatchTCP {
    private static Logger LOGGER = Logger.getLogger(MainBatchTCP.class);
    @Option(abbr = 'p', usage = "Print trussMap")
    public static int print = 0;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int maxOrder = 2;


    public static void main(String[] args) throws IOException {
        //read parameters
        MainBatchTCP main = new MainBatchTCP();
        args = SetOpt.setOpt(main, args);
//        String datasetName = "com-amazon.ungraph.txt";

        ArrayList<String> datasetList = new ArrayList<>();
        datasetList.add("CA-HepPh.txt");
//        datasetList.add("Brightkite_edges.txt");
//        datasetList.add("Gowalla_edges.txt");
//        datasetList.add("roadNet-PA.txt");

        for (String datasetName : datasetList) {
            LOGGER.info("Basic information:");
            System.err.println("datasetName:" + datasetName);
            System.err.println("Max order of dynamic edges:" + maxOrder);

            //full graph
            Graph fullGraph = GraphImport.load(datasetName, delim);

            //result_full
            Result result_full = new TrussDecomp(fullGraph).run();
            result_full.setDatasetName(datasetName + "_full");
            Hashtable<Edge, Integer> trussMap_full = (Hashtable<Edge, Integer>) result_full.getOutput();
            Export.writeFile(result_full, 1);

            LOGGER.error(" ======Stage Begin, order" + maxOrder);

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

            /**
             * ONE Multi Edges Insertion
             */
            //ONE-one algorithm1:TCPInsertion
            Result result11 = TCPIndex.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
            result11.setDatasetName(datasetName);
            result11.setOrder(maxOrder);
            Export.writeFile(result11, print);

            /**
             * TWO Multi Edges Deletion
             */
            //TWO-one algorithm1:TCPDeletion
            Result result21 = TCPIndex.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
            result21.setDatasetName(datasetName);
            result21.setOrder(maxOrder);
            Export.writeFile(result21, print);


        }

    }
}
