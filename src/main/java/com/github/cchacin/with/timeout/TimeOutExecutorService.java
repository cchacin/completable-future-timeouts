package com.github.cchacin.with.timeout;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeOutExecutorService extends DelegatingCompletableExecutorService {
    private final Duration                 timeout;
    private final ScheduledExecutorService schedulerExecutor;

    TimeOutExecutorService(final ExecutorService delegate, final Duration timeout) {
        super(delegate);
        this.timeout = timeout;
        this.schedulerExecutor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public <T> CompletableFuture<T> submit(final Callable<T> task) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        final Future<?> future = this.delegate.submit(() -> {
            try {
                cf.complete(task.call());
            }
            catch (final CancellationException e) {
                cf.cancel(true);
            }
            catch (final Throwable ex) {
                cf.completeExceptionally(ex);
            }
        });

        this.schedulerExecutor.schedule(() -> {
            if (!cf.isDone()) {
                cf.completeExceptionally(new CancellationException("Timeout after " + this.timeout));
                future.cancel(true);
            }
        }, this.timeout.toMillis(), TimeUnit.MILLISECONDS);
        return cf;
    }
}
