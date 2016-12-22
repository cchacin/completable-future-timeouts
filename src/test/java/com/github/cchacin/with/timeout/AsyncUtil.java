package com.github.cchacin.with.timeout;

import java.time.Duration;

public class AsyncUtil {
    public static String sleep(final Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (final InterruptedException e) {
            // NO-OP
        }
        return "yey";
    }
}
