package util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Load an input graph in memory
 */
public class GraphImport {

    private static Logger LOGGER = Logger.getLogger(GraphImport.class);

    /**
     * load an input graph in memory
     *
     * @param datasetName dataset name
     * @param delim       seperate sybolm
     * @return a graph
     * @throws IOException
     */
    public static Graph load(String datasetName, String delim) throws IOException {
        String path = "datasets\\" + datasetName;

        LOGGER.info("Start loading graph: " + path);

        final Hashtable<Integer, LinkedList<Integer>> adjMap = new Hashtable<>();
//        final HashSet<Edge> tempEdgeSet = new HashSet<>();
        final LinkedList<Edge> edgeSet = new LinkedList<>();

        final BufferedReader br = new BufferedReader(new FileReader(path));
        while (true) {
            final String line = br.readLine();
            if (line == null) {
                break;
            } else if (line.startsWith("#") || line.startsWith("%") || line.startsWith("//")) { //comment
//                LOGGER.info("The following line was ignored during loading a graph:");
//                System.err.println(line);
                continue;
            } else {
                String[] tokens = line.split(delim);
                int v1 = Integer.parseInt(tokens[0]); //Integer.valueof(token[0])
                int v2 = Integer.parseInt(tokens[1]);

                if (v1 == v2) continue;

                if (v1 < v2) edgeSet.add(new Edge(v1, v2));
                else edgeSet.add(new Edge(v2, v1));

                if (!adjMap.containsKey(v1)) adjMap.put(v1, new LinkedList());
                adjMap.get(v1).add(v2);

                if (!adjMap.containsKey(v2)) adjMap.put(v2, new LinkedList());
                adjMap.get(v2).add(v1);
            }
        }

        LOGGER.info("End graph was loaded!");

        return new Graph(adjMap, edgeSet);
    }

}
