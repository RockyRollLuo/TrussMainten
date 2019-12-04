package util;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Export the result to a file
 */
public class Export {
    private static Logger LOGGER = Logger.getLogger(Export.class);

    /**
     * write the result to a file
     * @param result
     * @throws IOException
     */
    public static void writeFile(Result result) throws IOException {
        long takenTime = result.getTakenTime();
        Hashtable<Object, Integer> output = (Hashtable<Object, Integer>) result.getOutput();
        String algorithmName = result.getAlgorithmName();
        String datasetName = result.getDatasetName();
        int times = result.getTimes();
        int dynamicEdges = result.getDynamicEdges();
        int threadNums = result.getThreadNums();

        String fileName="outputs\\"+algorithmName+"_"+datasetName;

        LOGGER.info("Exporting graph... ");


        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("#takenTime:"+takenTime+"ms");
        bw.newLine();

        bw.write("#times:"+times);
        bw.newLine();

        bw.write("#dynamic edges:" + dynamicEdges);
        bw.newLine();

        bw.write("#threadNums:" + threadNums); //only useful for Parallel way
        bw.newLine();

        for (Object key : output.keySet()) {
            bw.write(key.toString() + "\t" + output.get(key));
            bw.newLine();
        }
        bw.close();

        LOGGER.info("Graph was exported.: " + fileName);

    }
}
