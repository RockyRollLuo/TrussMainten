package util;

import java.io.Serializable;


public class Edge implements Comparable<Edge>, Cloneable, Serializable {
    private int v1;
    private int v2;

    public Edge(int v1, int v2) {
        this.v1 = Math.min(v1, v2);
        this.v2 = Math.max(v1, v2);
    }

    public int getV1() {
        return v1;
    }

    public void setV1(int v1) {
        this.v1 = v1;
    }

    public int getV2() {
        return v2;
    }

    public void setV2(int v2) {
        this.v2 = v2;
    }

    @Override
    public int hashCode() {
        return (v1 * 31) + v2;
    }

    @Override
    public boolean equals(Object obj) {
        Edge e2 = (Edge) obj;
        return (this.v1 == e2.getV1() && this.v2 == e2.getV2()) || (this.v1 == e2.getV2() && this.v2 == e2.getV1());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        int v3 = this.v1;
        int v4 = this.v2;
        return new Edge(v3, v4);
    }

    @Override
    public int compareTo(Edge e2) {
        if (this.v1 == e2.v1 && this.v2 == e2.v2)
            return 0;
        else if (this.v1 == e2.v2 && this.v2 == e2.v1)
            return 0;
        else
            return -1;
    }

    @Override
    public String toString() {
        return "(" + v1 + "," + v2 + ")";
    }
}
