package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.util.compat.ObjectsCompat;

import java.util.Map;

/**
 * @author lngtr
 * @since 2018-02-19
 */
public class CollectionUtils {
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (ObjectsCompat.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
