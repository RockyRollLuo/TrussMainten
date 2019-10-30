package test;

import java.util.Hashtable;
import java.util.TreeSet;

public class Test {

    public static void main(String[] args) {
        Hashtable<Integer, TreeSet<Integer>> map = new Hashtable<>();
        TreeSet<Integer> set1 = new TreeSet<>();
        set1.add(1);
        set1.add(2);
        set1.add(3);
        set1.add(4);
        set1.add(5);

        TreeSet<Integer> set2 = new TreeSet<>();
        set2.add(6);
        set2.add(7);
        set2.add(8);
        set2.add(9);
        set2.add(10);

        map.put(1, set1);
        map.put(2, set2);

        System.out.println(map.toString());

        ChangeMap thread1 = new ChangeMap("thread1", map, 1);
        thread1.run();

        ChangeMap thread2 = new ChangeMap("thread2", map, 2);
        thread2.run();

        System.out.println(map.toString());

    }
}

class ChangeMap extends Thread {
    private Thread t;
    private String threadName;

    private Hashtable<Integer, TreeSet<Integer>> map;
    private Integer index;


    public ChangeMap(String threadName, Hashtable<Integer, TreeSet<Integer>> map, Integer index) {
        this.threadName = threadName;
        this.map = map;
        this.index = index;
        System.out.println("Creating thread:"+threadName);
    }

    @Override
    public void run() {
        TreeSet<Integer> set = map.get(index);
        System.out.println(threadName+":"+map);

    }


}


