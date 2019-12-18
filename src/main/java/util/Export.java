package util;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Export the result to a file
 */
public class Export {
    private static Logger LOGGER = Logger.getLogger(Export.class);

    /**
     * write the result to a file
     *
     * @param result
     * @throws IOException
     */
    public static void writeFile(Result result, int print) throws IOException {
        long takenTime = result.getTakenTime();
        Hashtable<Object, Integer> output = (Hashtable<Object, Integer>) result.getOutput();
        String algorithmName = result.getAlgorithmName();
        String datasetName = result.getDatasetName();
        int times = result.getTimes();
        int order = result.getOrder();
        int threadNums = result.getThreadNums();
        LinkedList<Integer> tdsSizeList = result.getTdsSizeList();

        //Operate System
        String pathSeparator = "\\";
        String os = System.getProperty("os.name");
        if (!os.toLowerCase().startsWith("win")) {
            pathSeparator = "/";
        }
        String fileName = "outputs" + pathSeparator + algorithmName + "_" + datasetName + "_D" + order + "_T" + threadNums;

        LOGGER.info("Exporting graph... ");


        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("#takenTime:" + takenTime + "ms");
        bw.newLine();

        bw.write("#iterTimes:" + times+"-"+tdsSizeList);
        bw.newLine();

        bw.write("#dynamic edges:" + (int) Math.pow(10, order));
        bw.newLine();

        bw.write("#threadNums:" + threadNums); //only useful for Parallel way
        bw.newLine();

        if (print > 0) {
            for (Object key : output.keySet()) {
                bw.write(key.toString() + "\t" + output.get(key));
                bw.newLine();
            }
        }
        bw.close();

        LOGGER.info("Result was exported.: " + fileName);
    }
}
