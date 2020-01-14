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

public class MainTrussDecomp {
    private static Logger LOGGER = Logger.getLogger(MainTrussDecomp.class);
    @Option(abbr = 'p', usage = "Print trussMap")
    public static int print = 1;

    @Option(abbr = 's', usage = "Separate delimiter,0:tab,1:space,2:comma")
    public static String delim = "    ";

    @Option(abbr = 'd', usage = "dynamic type, 0:static TrussDecomp, 1:MultiEdgesInsertion, 2:MultiEdgesDeletion, 3:SupTrussnessParallel")
    public static int dynamicType = 0;


    public static void main(String[] args) throws IOException {
        //read parameters
        MainTrussDecomp main = new MainTrussDecomp();
        args = SetOpt.setOpt(main, args);

        LOGGER.info("Basic information:");
        System.err.println("Dynamic type:" + dynamicType);


        //full graph
        String datasetName = "sx-superuser.txt";
        Graph fullGraph = GraphImport.load(datasetName, delim);

        //result_full
        Result result_full = new TrussDecomp(fullGraph).run();
        result_full.setDatasetName(datasetName + "_full");

        Export.writeFile(result_full, print);
    }
}
