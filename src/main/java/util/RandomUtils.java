package util;

import java.awt.image.Kernel;
import java.util.*;
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

    /**
     * random choose k form N
     * Reservoir sampling
     * @param list
     * @param k
     * @return
     */
    public static LinkedList<Integer> getRandomKFormN(LinkedList<Integer> list, int k) {
        int N = list.size();
        LinkedList<Integer> kList = new LinkedList<>();

        for (int i = 0; i < k; i++) {
            kList.add(list.get(i));
        }
        for (int i = k; i < N; i++) {
            int r = getRandomInt(k + 1);
            if (r < k) {
                kList.add(r, list.get(i));
                kList.remove(r + 1);
            }
        }
        return kList;
    }

    /**
     * random choose a small batch set from a set
     * If it's more than half, take the other half
     * @param edgeList
     * @param nums the size of small batch set
     * @return
     */
    public static LinkedList<Edge> getRandomSetFromSet(LinkedList<Edge> edgeList, int nums) {
        int N = edgeList.size();

        LinkedList<Edge> dynamicEdges = new LinkedList<>();
        for (int i = 0; i < nums; i++) {
            dynamicEdges.add(edgeList.get(i));
        }

        for (int i = nums; i < N; i++) {
            int r = getRandomInt(i + 1);
            if (r < nums) {
                dynamicEdges.add(r, edgeList.get(i));
                dynamicEdges.remove(r + 1);
            }
        }
        return dynamicEdges;
    }


}

