package de.necon.dateman_backend.util;

import de.necon.dateman_backend.exception.ServerErrorList;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class Asserter {

    public static String AssertNotNull(String str, String errorMsg) {
        if (str == null) throw new AssertionError(errorMsg);
        return str;
    }

    public static ThrownBuilder assertException(Class<? extends Throwable> expectedThrowableClass) {
        return new ThrownBuilder(expectedThrowableClass);
    }

    public static void assertContainsError(ServerErrorList errorList, String error) {
        for(var e : errorList.getErrors()) {
            if (error.equals(error)) return;
        }

        throw new AssertionError("Expected error list to contain this error: " + error);
    }


    public static class ThrownBuilder {

        private Class<? extends Throwable> expectedThrowableClass;
        private Throwable thrownException;


        public ThrownBuilder(Class<? extends Throwable> expectedThrowableClass) {
            this.expectedThrowableClass = expectedThrowableClass;
            this.thrownException  = null;
        }

        public ThrownBuilder isThrownBy(Runnable func) {
            try {
                func.run();
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
}
