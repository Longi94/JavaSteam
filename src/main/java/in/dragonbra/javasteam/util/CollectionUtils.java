package in.dragonbra.javasteam.util;

import java.util.Map;
import java.util.Objects;

/**
 * @author lngtr
 * @since 2018-02-19
 */
public class CollectionUtils {
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
