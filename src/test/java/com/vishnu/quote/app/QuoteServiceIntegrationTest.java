package com.vishnu.quote.app;

import com.vishnu.quote.application.QuoteGenerators;
import com.vishnu.quote.application.QuoteService;
import com.vishnu.quote.domain.AiTextClient;
import com.vishnu.quote.domain.QuoteGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuoteService - integration wiring")
final class QuoteServiceIntegrationTest {

    @Nested
    @DisplayName("AI only")
    final class AiOnly {

        @Test
        void should_returnAiQuote_when_aiClientReturnsValue() {
            AiTextClient ai = new FakeAiTextClient().willReturn("Keep going.");
            QuoteGenerator generator = QuoteGenerators.aiOnly(ai);
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertEquals("Keep going.", quote);
        }

        @Test
        void should_propagateRuntimeException_when_aiClientFails() {
            RuntimeException failure = new RuntimeException("upstream failure");
            AiTextClient ai = new FakeAiTextClient().willThrow(failure);
            QuoteGenerator generator = QuoteGenerators.aiOnly(ai);
            QuoteService service = new QuoteService(generator);

            RuntimeException ex = assertThrows(RuntimeException.class, service::randomMotivationalQuote);
            assertSame(failure, ex);
        }
    }

    @Nested
    @DisplayName("File only")
    final class FileOnly {

        @Test
        void should_returnAQuoteFromClasspath_when_resourceHasQuotes() {
            QuoteGenerator generator = QuoteGenerators.classpathOnly("quotes/quotes-nonempty.txt");
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertNotNull(quote);
            assertFalse(quote.isBlank());
        }

        @Test
        void should_throwIllegalStateException_when_resourceHasNoQuotes() {
            QuoteGenerator generator = QuoteGenerators.classpathOnly("quotes/quotes-empty.txt");
            QuoteService service = new QuoteService(generator);

            IllegalStateException ex = assertThrows(IllegalStateException.class, service::randomMotivationalQuote);

            // message includes repository description; keep assertion strict and meaningful
            assertEquals(
                    "No motivational quote available from repository: classpath:quotes/quotes-empty.txt",
                    ex.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("AI with classpath fallback")
    final class AiWithFileFallback {

        @Test
        void should_returnAiQuote_when_aiProducesNonBlank_andNotUseFallback() {
            FakeAiTextClient ai = new FakeAiTextClient().willReturn("  Progress compounds daily.  ");
            QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(ai, "quotes/quotes-nonempty.txt");
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertEquals("Progress compounds daily.", quote);
            assertEquals(1, ai.calls(), "AI client must be invoked exactly once.");
        }

        @Test
        void should_useFallback_when_aiReturnsBlank() {
            FakeAiTextClient ai = new FakeAiTextClient().willReturn("   ");
            QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(ai, "quotes/quotes-nonempty.txt");
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertNotNull(quote);
            assertFalse(quote.isBlank());
            assertEquals(1, ai.calls(), "AI client must be invoked exactly once.");
        }

        @Test
        void should_useFallback_when_aiReturnsNull() {
            FakeAiTextClient ai = new FakeAiTextClient().willReturn(null);
            QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(ai, "quotes/quotes-nonempty.txt");
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertNotNull(quote);
            assertFalse(quote.isBlank());
            assertEquals(1, ai.calls(), "AI client must be invoked exactly once.");
        }

        @Test
        void should_useFallback_when_aiThrowsRuntimeException() {
            FakeAiTextClient ai = new FakeAiTextClient().willThrow(new RuntimeException("ai unavailable"));
            QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(ai, "quotes/quotes-nonempty.txt");
            QuoteService service = new QuoteService(generator);

            String quote = service.randomMotivationalQuote();

            assertNotNull(quote);
            assertFalse(quote.isBlank());
            assertEquals(1, ai.calls(), "AI client must be invoked exactly once.");
        }

        @Test
        void should_throwIllegalStateException_when_aiFails_and_fallbackHasNoQuotes() {
            FakeAiTextClient ai = new FakeAiTextClient().willThrow(new RuntimeException("ai unavailable"));
            QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(ai, "quotes/quotes-empty.txt");
            QuoteService service = new QuoteService(generator);

            IllegalStateException ex = assertThrows(IllegalStateException.class, service::randomMotivationalQuote);

            assertEquals(
                    "Both primary and fallback generators failed to produce a quote.",
                    ex.getMessage()
            );
            assertEquals(1, ai.calls(), "AI client must be invoked exactly once.");
        }
    }

    /**
     * Deterministic test double for integration tests.
     * Keeps tests stable, fast, and offline.
     */
    private static final class FakeAiTextClient implements AiTextClient {

        private final AtomicInteger calls = new AtomicInteger(0);
        private String value;
        private RuntimeException failure;

        FakeAiTextClient willReturn(String value) {
            this.value = value;
            this.failure = null;
            return this;
        }

        FakeAiTextClient willThrow(RuntimeException failure) {
            this.failure = failure;
            this.value = null;
            return this;
        }

        int calls() {
            return calls.get();
        }

        @Override
        public String generateText(String prompt) {
            calls.incrementAndGet();
            if (failure != null) {
                throw failure;
            }
            return value;
        }
    }
}
