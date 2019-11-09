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
     * @param debug
     * @throws IOException
     */
    public static void writeFile(Result result, int debug) throws IOException {
        long takenTime = result.getTakenTime();
        Hashtable<Object, Integer> output = (Hashtable<Object, Integer>) result.getOutput();
        String algorithmName = result.getAlgorithmName();
        String datasetName = result.getDatasetName();

        String fileName="outputs\\"+algorithmName+"_"+datasetName;

        if (debug>0)
            LOGGER.info("Exporting graph... ");


        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("takenTime:"+takenTime+"ms");
        bw.newLine();
        for (Object key : output.keySet()) {
            bw.write(key.toString() + "\t" + output.get(key));
            bw.newLine();
        }
        bw.close();

        if (debug>0)
            LOGGER.info("Graph was exported.: " + fileName);

    }
}
