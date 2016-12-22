package com.github.cchacin.with.timeout;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;


// This class is based on Tomasz Nurkiewicz talks
// about CompletableFuture
// https://youtu.be/-MBPQ7NIL_Y
public interface WithTimeout {

    static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    static <T> CompletableFuture<T> of(final Duration duration,
                                       final CompletableFuture<T> future) {
        return future.applyToEither(failAfter(duration), Function.identity());
    }

    static <T> CompletableFuture<T> failAfter(final Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            return promise.completeExceptionally(ex);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }
}
