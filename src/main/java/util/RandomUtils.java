package util;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    private static Logger LOGGER = Logger.getLogger(RandomUtils.class);


    private static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    public static int getRandomInt(int max) {
        return getRandom().nextInt(max);
    }

    public static int getRandomInt(int min, int max) {
        return getRandom().nextInt(max - min + 1) + min;
    }

    public static <E> E getRandomElement(List<E> list) {
        return list.get(getRandomInt(list.size()));
    }

    public static <E> E getRandomElement(Set<E> set) {
        int rn = getRandomInt(set.size());
        int i = 0;
        for (E e : set) {
            if (i == rn) {
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
            if (i == rn) {
                return key;
            }
            i++;
        }
        return null;
    }

    /**
     * random choose k form N
     * Reservoir sampling
     *
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
     *
     * @param list
     * @param nums     the size of small batch set
     * @return
     */
    public static <E> LinkedList<E> getRandomSetFromSet(LinkedList<E> list, int nums) {
        LOGGER.info("Start get random set from set - assume:" + nums + "/" + list.size());
        int N = list.size();

        if (nums == N) {
            return new LinkedList<>(list);
        }

        LinkedList<E> dynamicElements = new LinkedList<>();
        for (int i = 0; i < nums; i++) {
            dynamicElements.add(list.get(i));
        }

        for (int i = nums; i < N; i++) {
            int r = getRandomInt(i + 1);
            if (r < nums) {
                dynamicElements.add(r, list.get(i));
                dynamicElements.remove(r + 1);
            }
        }

        LOGGER.info("End get random set from set - real:" + dynamicElements.size() + "/" + list.size());
        return dynamicElements;
    }

}

