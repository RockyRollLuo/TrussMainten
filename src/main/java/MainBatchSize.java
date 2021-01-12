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

public class MainBatchSize {
    private static final Logger LOGGER = Logger.getLogger(MainBatchSize.class);

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";


    public static void main(String[] args) throws IOException {
        //read parameters
        MainBatchSize main = new MainBatchSize();
        args = SetOpt.setOpt(main, args);

        //full graph
//        String datasetName = args[0];
        //"EmailEnron.txt","Gowalla.txt","EmailEuAll.txt","amazon.txt","Youtube.txt","WikiTalk.txt",
        String datasetName = "Youtube.txt";
        Graph graph = GraphImport.load(datasetName, delim);

        //Graph
        LinkedList<Edge> edgeSet = (LinkedList<Edge>) graph.getEdgeSet().clone();
        Hashtable<Integer, LinkedList<Integer>> adjMap = GraphHandler.deepCloneAdjMap(graph.getAdjMap());


        int dynamicVerticesSize = 11350;
        LinkedList<Integer> dynamicVertices = RandomUtils.getRandomSetFromSet(graph.getVerticesSet(), dynamicVerticesSize);

        LinkedList<Edge> dynamicEdges = new LinkedList<>();
        for (Integer v : dynamicVertices) {
            LinkedList<Integer> vNei = adjMap.get(v);
            for (Integer u : vNei) {
                dynamicEdges.add(new Edge(v, u));
            }
        }

        System.out.println("==========================================");
        System.out.println("dynamic vertices:" + dynamicVertices.size());
        System.out.println("dynamic edges:" + dynamicEdges.size());


        LinkedList<Edge> removeEdges = new LinkedList<>(dynamicEdges);
        LinkedList<Integer> tdsSizeList = new LinkedList<>();
        int edgeNum = dynamicEdges.size();

        while (!removeEdges.isEmpty()) {
            LOGGER.info("SupTruss insert edges progress: " + (edgeNum - removeEdges.size()) + "/" + edgeNum + "...");

            //compute tds
            LinkedList<Edge> tds = GraphHandler.getDeletionTDS(graph, removeEdges);
            tdsSizeList.add(tds.size());

            //update graph
            adjMap=GraphHandler.removeEdgesFromAdjMap(adjMap, tds);
            edgeSet.removeAll(tds);
            graph = new Graph(adjMap, edgeSet);

            removeEdges.removeAll(tds);
        }

        System.out.println("==========================================");
        System.out.println("tdsSize:" + tdsSizeList.size());
    }
}
