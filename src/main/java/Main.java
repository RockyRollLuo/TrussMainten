import algorithm.SupTruss;
import algorithm.TrussDecomp;
import algorithm.parallel.Parallel;
import algorithm.parallel.SupTrussInsertion;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class Main {
//    @Option(abbr = 'a', usage = "0:trussDecomp, 1:TCP-Index, 2:OneEdgeTrussDecomp, 3:MultipleEdges, 4:MultipleVertices")
//    public static int algorithm = 0;

    @Option(abbr = 'r', usage = "Print the result.")
    public static boolean printResult = true;

    @Option(abbr = 'p', usage = "print the progress")
    public static int debug = 1;

    @Option(abbr = 's', usage = "sperate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int order = 0;

    @Option(abbr = 'd', usage = "dynamic insertion or deletion, insert:insertion, delete:deletion")
    public static String d = "insert1";

    @Option(abbr = 'e', usage = "execution, sequ:sequential, para:parallel")
    public static String e = "sequ";


    public static void main(String[] args) throws IOException {
        //read parameters
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        //read graph
        String datasetName = args[0];
        Graph dataSet = GraphImport.load(datasetName, delim, debug);

        //dynamic edges 2^d
        LinkedList<Edge> dynamicEdges = RandomUtils.getRandomSetFromSet(dataSet.getEdgeSet(), (int) Math.pow(2, order));

        //Graph
        LinkedList<Edge> edgeSet = (LinkedList<Edge>) dataSet.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(dataSet.getAdjMap());
        edgeSet.removeAll(dynamicEdges);
        adjMap = GraphHandler.removeEdgesFromAdjMap(adjMap, dynamicEdges);
        Graph graph = new Graph(adjMap, edgeSet);






        /**
         * INSERTION: graph->dataSet
         */
        if (d.equals("insert")) {
            //trussDecomp ture
            Result result = new TrussDecomp(dataSet).run();
            result.setDatasetName(datasetName);

            //tds
            Result result1 = SupTruss.edgeTDSInsertion(graph, dynamicEdges);
            result1.setDatasetName(datasetName);


            //print result
            if (printResult) {
                Export.writeFile(result, debug);
                Export.writeFile(result1, debug);
            }

            /**
             * DELETION: dataSet->graph
             */
        } else if (d.equals("delete")) {
            //trussDecomp ture
            Result result = new TrussDecomp(graph).run();
            result.setDatasetName(datasetName);


            //SupTruss
            Result result1 = SupTruss.edgeTDSDeletion(graph, dynamicEdges);
            result1.setDatasetName(datasetName);

            //print result
            if (printResult) {
                Export.writeFile(result, debug);
                Export.writeFile(result1, debug);
            }
        }
    }

}
