package in.dragonbra.javasteam.util;

import java.util.Map;
import java.util.Objects;

/**
 * @author lngtr
 * @since 2018-02-19
 */
public class CollectionUtils {
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
