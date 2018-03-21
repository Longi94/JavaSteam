package in.dragonbra.javasteam.util.compat;

/**
 * @author steev
 * @since 2018-03-21
 */
public class ObjectsCompat {
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}