package util;

public class Edge implements Comparable<Edge>, Cloneable {

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

    //TODO：manybe wrong
    @Override
    public int hashCode() {
        int hash=0;
        hash=v1*31+v2;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        Edge e2 = (Edge) obj;
        if ((this.v1== e2.getV1() && this.v2 == e2.getV2()) || (this.v1 == e2.getV2() && this.v2 == e2.getV1())) {
            return true;
        }
        return false;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        int v3 = new Integer(this.v1);
        int v4 = new Integer(this.v2);
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

    public static void main(String[] args) {
        Edge e1 = new Edge(1, 2);
        Edge e2 = new Edge(2, 1);
        Edge e3 = new Edge(1, 2);

        if (e1.equals(e2)) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }

        System.out.printf("e1.hashcode:%s:%n",e1.hashCode());
        System.out.printf("e2.hashcode:%s:%n",e2.hashCode());
        System.out.printf("e3.hashcode:%s:%n",e3.hashCode());
    }
}
