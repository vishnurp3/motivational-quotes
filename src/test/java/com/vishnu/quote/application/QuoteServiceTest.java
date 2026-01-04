package com.vishnu.quote.application;

import com.vishnu.quote.domain.QuoteGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteService")
final class QuoteServiceTest {

    @Mock
    private QuoteGenerator generator;

    @InjectMocks
    private QuoteService quoteService;

    @Nested
    @DisplayName("constructor")
    final class Constructor {
        @Test
        void should_throwNullPointerException_when_generatorIsNull() {
            assertThrows(NullPointerException.class, () -> new QuoteService(null));
        }
    }

    @Nested
    @DisplayName("randomMotivationalQuote()")
    final class RandomMotivationalQuote {

        @Test
        void should_delegateToGenerator_generate() {
            when(generator.generate()).thenReturn("any");
            quoteService.randomMotivationalQuote();
            verify(generator).generate();
            verifyNoMoreInteractions(generator);
        }

        @Test
        void should_returnExactlyWhatGeneratorReturns() {
            String quote = "Discipline beats motivation when motivation fades.";
            when(generator.generate()).thenReturn(quote);
            String result = quoteService.randomMotivationalQuote();
            assertSame(quote, result);
            verify(generator).generate();
            verifyNoMoreInteractions(generator);
        }

        @Test
        void should_propagateRuntimeException_when_generatorFails() {
            RuntimeException failure = new RuntimeException("failure");
            when(generator.generate()).thenThrow(failure);
            RuntimeException ex = assertThrows(RuntimeException.class, quoteService::randomMotivationalQuote);
            assertSame(failure, ex);
            verify(generator).generate();
            verifyNoMoreInteractions(generator);
        }
    }
}
