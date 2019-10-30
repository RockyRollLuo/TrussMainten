import algorithm.TrussDecomposition;
import util.*;
import util.SetOpt.Option;

import java.io.IOException;

public class Main {
    @Option(abbr = 'a', usage = "0:trussDecomp, 1:TCP-Index, 2:OneEdgeTrussDecomp, 3:MultipleEdges, 4:MultipleVertices")
    public static int algorithm = 0;

    @Option(abbr = 'p', usage = "Print the result.")
    public static boolean printResult = true;

    @Option(abbr = 'd', usage = "print the progress")
    public static int debug = 1;

    @Option(abbr = 's', usage = "sperate delimiter,0:tab,1:space,2:comma")
    public static String delim = "\t";

    public Result run(String datasetName) throws IOException {
        final Graph graph = GraphImport.load(datasetName, delim, debug);

        Result result = TrussDecomposition.run(graph, debug);
        result.setAlgorithmName(ConstValue.ALG_TYPE.get(algorithm));
        result.setDatasetName(datasetName);
        return result;
    }


    public static void main(String[] args) throws IOException {
        Main main = new Main();
        args = SetOpt.setOpt(main, args);

        String datasetName = args[0];
        Result result = main.run(datasetName);

        if (printResult) {
            Export.writeFile(result, debug);
        }

    }

}
