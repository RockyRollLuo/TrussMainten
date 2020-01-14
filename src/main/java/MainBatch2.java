import algorithm.SupTruss;
import algorithm.TCPIndex;
import algorithm.TrussDecomp;
import algorithm.parallel.Parallel;
import algorithm.parallel.SupTrussParallel;
import org.apache.log4j.Logger;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

public class MainBatch2 {
    private static Logger LOGGER = Logger.getLogger(MainBatch2.class);
    @Option(abbr = 'p', usage = "Print trussMap")
    public static int print = 0;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    @Option(abbr = 'o', usage = "orders of magnitude,number=2^o,o=0,1,2,3,4,5,6")
    public static int maxOrder = 0;

    @Option(abbr = 't', usage = "max thread number")
    public static int maxThreadNum = Runtime.getRuntime().availableProcessors();

    @Option(abbr = 'r', usage = "thread nums range from 1 to maxThreadNum")
    public static int threadRange = 0;

    public static void main(String[] args) throws IOException {
        //read parameters
        MainBatch2 main = new MainBatch2();
        args = SetOpt.setOpt(main, args);
        String datasetName = args[0];

        LOGGER.info("Basic information:");
        System.err.println("datasetName:" + datasetName);
        System.err.println("Max order of dynamic edges:" + maxOrder);
        System.err.println("Max thread number:" + maxThreadNum);

        //full graph
        Graph fullGraph = GraphImport.load(datasetName, delim);

        //result_full
        Result result_full = new TrussDecomp(fullGraph).run();
        result_full.setDatasetName(datasetName + "_full");
        Hashtable<Edge, Integer> trussMap_full = (Hashtable<Edge, Integer>) result_full.getOutput();
        Export.writeFile(result_full, 1);

        for (int order = 1; order <= maxOrder; order++) {
            LOGGER.error(" ======Stage Begin, order" + order);

            //dynamic edges 10^d
            int dynamicEdgesSize = (int) Math.pow(10, order);
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
            result_rest.setOrder(order);
            Hashtable<Edge, Integer> trussMap_rest = (Hashtable<Edge, Integer>) result_rest.getOutput();
            Export.writeFile(result_rest, print);

            /**
             * ONE== Multi Edges Insertion
             */
            //ONE-one algorithm1:TCPInsertion
            Result result11 = TCPIndex.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
            result11.setDatasetName(datasetName);
            result11.setOrder(order);
            Export.writeFile(result11, print);

            //ONE-two algorithm2:SupInsertion
            Result result12 = SupTruss.edgesInsertion(restGraph, dynamicEdges, trussMap_rest);
            result12.setDatasetName(datasetName);
            result12.setOrder(order);
            Export.writeFile(result12, print);

            //ONE-three algorithm3:ParaTrussInsertion
            if (threadRange > 0) {
                for (int threadNum = 2; threadNum <= maxThreadNum*2; threadNum = threadNum*2) {
                    Result result13 = SupTrussParallel.edgesInsertion(restGraph, dynamicEdges, trussMap_rest, threadNum);
                    result13.setDatasetName(datasetName);
                    result13.setOrder(order);
                    result13.setThreadNums(threadNum);
                    Export.writeFile(result13, print);
                }
            } else {
                Result result13 = SupTrussParallel.edgesInsertion(restGraph, dynamicEdges, trussMap_rest, maxThreadNum);
                result13.setDatasetName(datasetName);
                result13.setOrder(order);
                result13.setThreadNums(maxThreadNum);
                Export.writeFile(result13, print);
            }

            /**
             * TWO ==Multi Edges Deletion
             */
            //TWO-one algorithm1:TCPDeletion
            Result result21 = TCPIndex.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
            result21.setDatasetName(datasetName);
            result21.setOrder(order);
            Export.writeFile(result21, print);

            //TWO-two algorithm2:SupDeletion
            Result result22 = SupTruss.edgesDeletion(fullGraph, dynamicEdges, trussMap_full);
            result22.setDatasetName(datasetName);
            result22.setOrder(order);
            Export.writeFile(result22, print);

            //TWO-three algorithm3:ParaDeletion
            if (threadRange > 0) {
                for (int threadNum = 2; threadNum <= maxThreadNum*2; threadNum = threadNum * 2) {
                    Result result23 = SupTrussParallel.edgesDeletion(fullGraph, dynamicEdges, trussMap_full, threadNum);
                    result23.setDatasetName(datasetName);
                    result23.setOrder(order);
                    result23.setThreadNums(threadNum);
                    Export.writeFile(result23, print);
                }
            } else {
                Result result23 = SupTrussParallel.edgesDeletion(fullGraph, dynamicEdges, trussMap_full, maxThreadNum);
                result23.setDatasetName(datasetName);
                result23.setOrder(order);
                result23.setThreadNums(maxThreadNum);
                Export.writeFile(result23, print);
            }

        }
    }
}
