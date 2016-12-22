package com.github.cchacin.with.timeout;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import java.time.Duration;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AppTest extends Assertions {

    private static class Callee {

        public String delayedHello(final Duration delay) {
            try {
                Thread.sleep(delay.toMillis());
            }
            catch (final InterruptedException ignored) {
                // NO-OP
            }
            return "hello";
        }
    }

    private static class Caller {
        private final Callee callee;

        private Caller(final Callee callee) {
            this.callee = callee;
        }

        public String dependentMethod(final Duration delay) {
            // executor argument ommited here for simplicity
            // but recommended to pass the executor to the
            // CompletableFuture.supplyAsync method
            return WithTimeout.of(delay, supplyAsync(() -> this.callee.delayedHello(Duration.ofMillis(300))))
                    .join();
        }
    }

    @Test
    public void testNoTimeout() throws Exception {
        // Given
        final Callee callee = new Callee();

        // When
        final String s = new Caller(callee).dependentMethod(Duration.ofMillis(350));

        // Then
        assertThat(s).isEqualTo("hello");
    }

    @Test
    public void testWithTimeout() throws Exception {
        // Given
        final Callee callee = new Callee();

        // When
        final ThrowableAssert.ThrowingCallable exec = () -> new Caller(callee).dependentMethod(Duration.ofMillis(200));

        // Then
        assertThatThrownBy(exec)
                .hasMessage("java.util.concurrent.TimeoutException: Timeout after PT0.2S");
    }
}
