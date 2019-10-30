package util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    private static ThreadLocalRandom getRandom(){
        return ThreadLocalRandom.current();
    }

    public static int getRandomInt(int max) {
        return getRandom().nextInt(max);
    }

    public static int getRandomInt(int min, int max) {
        return getRandom().nextInt(max-min+1) + min;
    }

    public static <E> E getRandomElement(List<E> list){
        return list.get(getRandomInt(list.size()));
    }

    public static <E> E getRandomElement(Set<E> set){
        int rn = getRandomInt(set.size());
        int i = 0;
        for (E e : set) {
            if(i==rn){
                return e;
            }
            i++;
        }
        return null;
    }

    public static <K, V> K getRandomKeyFromMap(Map<K, V> map) {
        int rn = getRandomInt(map.size());
        int i = 0;
        for (K key : map.keySet()) {
            if(i==rn){
                return key;
            }
            i++;
        }
        return null;
    }


    public static TreeSet<Edge> getRandomSetFromSet(TreeSet<Edge> edgeList, int nums) {
        TreeSet<Edge> deltaEdges = new TreeSet<>();





        return deltaEdges;
    }

}

