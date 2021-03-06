package util;

import java.util.LinkedList;

public class Result {
    private Graph graph;
    private Object output;
    private long takenTime;
    private int times;  //tds iteration times
    private String algorithmName;
    private String datasetName;
    private int order;
    private int threadNums;
    private LinkedList<Integer> tdsSizeList;


    /**
     * constructor
     */

    public Result(Object output, long takenTime) {
        this.output = output;
        this.takenTime = takenTime;
        this.times = 1;
    }

    public Result(Object output, long takenTime, String algorithmName) {
        this.output = output;
        this.takenTime = takenTime;
        this.algorithmName = algorithmName;
        this.times = 1;
    }

    public Result(Object output, long takenTime, String algorithmName, String datasetName) {
        this.output = output;
        this.takenTime = takenTime;
        this.algorithmName = algorithmName;
        this.datasetName = datasetName;
        this.times = 1;
    }

    public Result(Graph graph, Object output, long takenTime, String algorithmName, String datasetName) {
        this.graph = graph;
        this.output = output;
        this.takenTime = takenTime;
        this.algorithmName = algorithmName;
        this.datasetName = datasetName;
        this.times = 1;
    }


    /**
     * Getter() and Setter()
     */

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }


    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public long getTakenTime() {
        return takenTime;
    }

    public void setTakenTime(long takenTime) {
        this.takenTime = takenTime;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getThreadNums() {
        return threadNums;
    }

    public void setThreadNums(int threadNums) {
        this.threadNums = threadNums;
    }

    public LinkedList<Integer> getTdsSizeList() {
        return tdsSizeList;
    }

    public void setTdsSizeList(LinkedList<Integer> tdsSizeList) {
        this.tdsSizeList = tdsSizeList;
    }
}
