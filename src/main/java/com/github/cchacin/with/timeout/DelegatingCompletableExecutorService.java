package com.github.cchacin.with.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.callable;

class DelegatingCompletableExecutorService extends DelegatingExecutorService {

    DelegatingCompletableExecutorService(final ExecutorService executorService) {
        super(executorService);
    }

    @Override
    public <T> CompletableFuture<T> submit(final Callable<T> task) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        this.delegate.submit(() -> {
            try {
                cf.complete(task.call());
            }
            catch (final CancellationException e) {
                cf.cancel(true);
            }
            catch (final Exception e) {
                cf.completeExceptionally(e);
            }
        });
        return cf;
    }

    @Override
    public <T> CompletableFuture<T> submit(final Runnable task, final T result) {
        return submit(callable(task, result));
    }

    @Override
    public CompletableFuture<?> submit(final Runnable task) {
        return submit(callable(task));
    }
}
