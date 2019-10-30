package util;


public class Result {
    private Object output;
    private long takenTime;
    private String algorithmName;
    private String datasetName;

    public Result(Object output, long takenTime) {
        this.output = output;
        this.takenTime = takenTime;
    }

    public Result(Object output, long takenTime, String algorithmName) {
        this.output = output;
        this.takenTime = takenTime;
        this.algorithmName = algorithmName;
    }

    public Result(Object output, long takenTime, String algorithmName, String datasetName) {
        this.output = output;
        this.takenTime = takenTime;
        this.algorithmName = algorithmName;
        this.datasetName = datasetName;
    }

    /**
     * Getter() and Setter()
     */
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
}
