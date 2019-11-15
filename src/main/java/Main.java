import algorithm.SupTruss;
import algorithm.TrussDecomp;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;

public class Main {
//    @Option(abbr = 'a', usage = "0:trussDecomp, 1:TCP-Index, 2:OneEdgeTrussDecomp, 3:MultipleEdges, 4:MultipleVertices")
//    public static int algorithm = 0;

    @Option(abbr = 'p', usage = "Print the result.")
    public static boolean printResult = true;

    @Option(abbr = 'd', usage = "print the progress")
    public static int debug = 1;

    @Option(abbr = 's', usage = "sperate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";


    public static void main(String[] args) throws IOException {
        //read parameters
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        //read graph
        String datasetName = args[0];
        Graph graph = GraphImport.load(datasetName, delim, debug);

//        supTruss
        SupTruss supTruss = new SupTruss();
        Result result = supTruss.edgeInsertion(graph, debug);
        result.setAlgorithmName("SupTruss");
        result.setDatasetName(datasetName);

        //trussDecomp
//        TrussDecomp trussDecomp = new TrussDecomp(graph);
//        Result result = trussDecomp.run(debug);
//        result.setAlgorithmName("TrussDecomp");
//        result.setDatasetName(datasetName);

        //print result
        if (printResult) {
            Export.writeFile(result, debug);
        }

    }

}
