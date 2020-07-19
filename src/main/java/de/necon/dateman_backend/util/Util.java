package de.necon.dateman_backend.util;

public class Util {

    public static boolean equal(Object a, Object b) {
        if (a == b) return true;
        if (a != null) {
            return a.equals(b);
        }
        return false;
    }
}
