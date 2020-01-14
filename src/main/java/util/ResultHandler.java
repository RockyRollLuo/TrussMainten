package util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Load an result in memory
 */
public class ResultHandler {

    private static Logger LOGGER = Logger.getLogger(ResultHandler.class);
    /**
     * load an input graph in memory
     * @return a graph
     * @throws IOException
     */
    public static Hashtable<Integer, Integer> load(String resultName) throws IOException {
        //Operate System
        String pathSeparator = "\\";
        String os = System.getProperty("os.name");
        if (!os.toLowerCase().startsWith("win")) {
            pathSeparator = "/";
        }

        String path = "outputs" + pathSeparator + resultName;

        LOGGER.info("Start loading result: " + path);

        Hashtable<Integer, Integer> trussDistribution = new Hashtable<>();

        final BufferedReader br = new BufferedReader(new FileReader(path));
        while (true) {
            final String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) { //comment
                continue;
            }

            String[] tokens = line.split("\t");
            String edgeStr = tokens[0]; //edge
            int t = Integer.parseInt(tokens[1]); //trussness

            int num = (trussDistribution.get(t) == null ? 0 : trussDistribution.get(t));
            num = num + 1;
            trussDistribution.put(t, num);
        }

        LOGGER.info("End graph was loaded!");

        return trussDistribution;
    }


    public static void main(String[] args) throws IOException {
        String resultName = "TrussDecomp_sx-superuser.txt_full_D0_T0";

        HashMap<Integer, Float> precentMap = new HashMap<>();

        Hashtable<Integer, Integer> trussDistribution = load(resultName);
        int totalNum= 0;
        for (Integer num : trussDistribution.values()) {
            totalNum += num;
        }
        for (int k : trussDistribution.keySet()) {
            precentMap.put(k,  ((float)(trussDistribution.get(k)*100) / totalNum));
        }
        System.out.println(precentMap);

    }

}
