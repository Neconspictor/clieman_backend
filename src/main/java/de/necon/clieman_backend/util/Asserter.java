package de.necon.clieman_backend.util;

import java.util.List;

/**
 * An utility for making assertions.
 */
public class Asserter {

    public static String AssertNotNull(String str, String errorMsg) {
        if (str == null) throw new AssertionError(errorMsg);
        return str;
    }

    public static ThrownBuilder assertException(Class<? extends Throwable> expectedThrowableClass) {
        return new ThrownBuilder(expectedThrowableClass);
    }

    public static void assertContainsError(List<String> errorList, String error) {
        for(var e : errorList) {
            if (error.equals(e)) return;
        }

        throw new AssertionError("Expected error list to contain this error: " + error);
    }


    /**
     * Builder for building exception assertions.
     */
    public static class ThrownBuilder {

        private Class<? extends Throwable> expectedThrowableClass;
        private Throwable thrownException;


        public ThrownBuilder(Class<? extends Throwable> expectedThrowableClass) {
            this.expectedThrowableClass = expectedThrowableClass;
            this.thrownException  = null;
        }

        public ThrownBuilder isThrownBy(ThrowableCallable func) {
            try {
                func.call();
            } catch(Throwable t) {
                if (!t.getClass().equals(expectedThrowableClass)) {
                    throw new AssertionError("Got execption of type '" + t.getClass() + "'\n"
                    + "But expected: \n'"
                    + expectedThrowableClass + "'");
                }
                this.thrownException = t;
                return this;
            }
            throw new AssertionError("No Throwable thrown, but expected: '" + expectedThrowableClass + "'");
        }

        public Throwable source() {
            return thrownException;
        }
    }

    public static interface ThrowableCallable {
        void call() throws Throwable;
    }
}
