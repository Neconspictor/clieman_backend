package de.necon.dateman_backend.util;

public class Asserter {

    public static String AssertNotNull(String str, String errorMsg) {
        if (str == null) throw new AssertionError(errorMsg);
        return str;
    }
}
