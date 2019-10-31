package util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeSet;

/**
 * Load an input graph in memory
 */
public class GraphImport {

    private static Logger LOGGER = Logger.getLogger(GraphImport.class);

    /**
     * load an input graph in memory
     * @param datasetName dataset name
     * @param delim seperate sybolm
     * @param debug
     * @return a graph
     * @throws IOException
     */
    public static Graph load(String datasetName, String delim, int debug) throws IOException {
        String path="datasets\\"+datasetName;

        if(debug>0)
            LOGGER.info("loading graph...: "+ path);

        final Hashtable<Integer, TreeSet<Integer>> adjMap = new Hashtable<>();
        final TreeSet<Edge> edgeSet = new TreeSet<>();

        final BufferedReader br = new BufferedReader(new FileReader(path));
        while(true) {
            final String line = br.readLine();
            if(line == null) {
                break;
            }
            else if(line.startsWith("#") || line.startsWith("%") || line.startsWith("//")) { //comment

                if(debug>1) {
                    LOGGER.info("The following line was ignored during loading a graph:");
                    System.err.println(line);
                }
                continue;
            }
            else {
                String[] tokens = line.split(delim);
                int v1 = Integer.parseInt(tokens[0]); //Integer.valueof(token[0])
                int v2 = Integer.parseInt(tokens[1]);

                if(v1==v2)
                    continue;

                if(!adjMap.containsKey(v1)) {
                    adjMap.put(v1, new TreeSet());
                }
                adjMap.get(v1).add(v2);

                if (!adjMap.containsKey(v2)) {
                    adjMap.put(v2, new TreeSet());
                }
                adjMap.get(v2).add(v1);

                if (v1 < v2)
                    edgeSet.add(new Edge(v1, v2));
                else
                    edgeSet.add(new Edge(v2, v1));
            }
        }

        if(debug>0)
            LOGGER.info("graph was loaded: "+path);

        return new Graph(adjMap,edgeSet);
    }

}
