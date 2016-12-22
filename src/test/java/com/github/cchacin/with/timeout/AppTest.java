package com.github.cchacin.with.timeout;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AppTest extends Assertions {
    // Given
    final TimeOutExecutorService timeoutExecutor = new TimeOutExecutorService(Executors.newFixedThreadPool(10),
                                                                              Duration.ofMillis(200));

    @Test
    public void testNoTimeout() throws Exception {
        // When
        final CompletableFuture<String> cf1 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(100)));

        final CompletableFuture<String> cf2 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(100)));

        // Then
        assertThat(cf1.thenCombine(cf2, (a, b) -> a.concat(b)).join()).isEqualTo("yeyyey");
    }

    @Test
    public void testWith1Timeout() throws Exception {
        // When
        final CompletableFuture<String> cf1 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(100)));

        final CompletableFuture<String> cf2 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(300)));

        // Then
        assertThatThrownBy(() -> System.out.println(cf1.thenCombine(cf2, (a, b) -> a.concat(b)).join()))
                .hasMessage("java.util.concurrent.CancellationException: Timeout after PT0.2S");
        assertThat(cf1).isCompleted();
        assertThat(cf2).isCancelled();
    }

    @Test
    public void testWith2Timeouts() throws Exception {
        // When
        final CompletableFuture<String> cf1 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(300)));

        final CompletableFuture<String> cf2 = this.timeoutExecutor.submit(() -> AsyncUtil.sleep(Duration.ofMillis(300)));

        // Then
        assertThatThrownBy(() -> System.out.println(cf1.thenCombine(cf2, (a, b) -> a.concat(b)).join()))
                .hasMessage("java.util.concurrent.CancellationException: Timeout after PT0.2S");
        assertThat(cf1).isCancelled();
        assertThat(cf2).isCancelled();
    }
}
