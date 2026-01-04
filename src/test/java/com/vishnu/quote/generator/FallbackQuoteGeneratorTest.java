package com.vishnu.quote.generator;

import com.vishnu.quote.domain.QuoteGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FallbackQuoteGenerator")
final class FallbackQuoteGeneratorTest {

    @Nested
    @DisplayName("constructor")
    final class Constructor {

        @Test
        void should_throwNullPointerException_when_primaryIsNull() {
            QuoteGenerator fallback = mock(QuoteGenerator.class);

            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new FallbackQuoteGenerator(null, fallback)
            );

            assertEquals("primary", ex.getMessage());
            verifyNoInteractions(fallback);
        }

        @Test
        void should_throwNullPointerException_when_fallbackIsNull() {
            QuoteGenerator primary = mock(QuoteGenerator.class);

            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new FallbackQuoteGenerator(primary, null)
            );

            assertEquals("fallback", ex.getMessage());
            verifyNoInteractions(primary);
        }
    }

    @Nested
    @DisplayName("generate()")
    final class Generate {

        @Test
        void should_returnTrimmedPrimaryValue_when_primaryProducesNonBlankQuote() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn("  Keep going.  ");

            String result = generator.generate();

            assertEquals("Keep going.", result);
            verify(primary).generate();
            verifyNoInteractions(fallback);
        }

        @Test
        void should_notCallFallback_when_primaryProducesNonBlankAlreadyTrimmedQuote() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn("Progress compounds daily.");

            String result = generator.generate();

            assertEquals("Progress compounds daily.", result);
            verify(primary).generate();
            verifyNoInteractions(fallback);
        }

        @Test
        void should_fallback_when_primaryReturnsNull() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn(null);
            when(fallback.generate()).thenReturn("  Stay consistent.  ");

            String result = generator.generate();

            assertEquals("Stay consistent.", result);

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_fallback_when_primaryReturnsBlank() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn("   ");
            when(fallback.generate()).thenReturn("One step at a time.");

            String result = generator.generate();

            assertEquals("One step at a time.", result);

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_fallback_when_primaryReturnsOnlyWhitespaceCharacters() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn("\n\t  ");
            when(fallback.generate()).thenReturn("  Discipline beats motivation.  ");

            String result = generator.generate();

            assertEquals("Discipline beats motivation.", result);

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_swallowPrimaryRuntimeException_and_useFallback() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenThrow(new RuntimeException("primary failure"));
            when(fallback.generate()).thenReturn("  Adjust and continue.  ");

            String result = generator.generate();

            assertEquals("Adjust and continue.", result);

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_throwIllegalStateException_when_primaryFails_and_fallbackReturnsNull() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenThrow(new RuntimeException("primary failure"));
            when(fallback.generate()).thenReturn(null);

            IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generate);

            assertEquals("Both primary and fallback generators failed to produce a quote.", ex.getMessage());

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_throwIllegalStateException_when_primaryReturnsBlank_and_fallbackReturnsBlank() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn(" ");
            when(fallback.generate()).thenReturn(" \t ");

            IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generate);

            assertEquals("Both primary and fallback generators failed to produce a quote.", ex.getMessage());

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_throwIllegalStateException_when_primaryReturnsNull_and_fallbackReturnsBlank() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn(null);
            when(fallback.generate()).thenReturn("   ");

            IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generate);

            assertEquals("Both primary and fallback generators failed to produce a quote.", ex.getMessage());

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_returnTrimmedFallbackValue_when_primaryProducesNull() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn(null);
            when(fallback.generate()).thenReturn("  You are closer than you think.  ");

            String result = generator.generate();

            assertEquals("You are closer than you think.", result);

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_throwIllegalStateException_when_primaryProducesNull_and_fallbackThrowsRuntimeException() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenReturn(null);
            when(fallback.generate()).thenThrow(new RuntimeException("fallback failure"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generate);

            assertEquals("Both primary and fallback generators failed to produce a quote.", ex.getMessage());

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }

        @Test
        void should_throwIllegalStateException_when_primaryThrows_and_fallbackThrows() {
            QuoteGenerator primary = mock(QuoteGenerator.class);
            QuoteGenerator fallback = mock(QuoteGenerator.class);
            FallbackQuoteGenerator generator = new FallbackQuoteGenerator(primary, fallback);

            when(primary.generate()).thenThrow(new RuntimeException("primary failure"));
            when(fallback.generate()).thenThrow(new RuntimeException("fallback failure"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generate);

            assertEquals("Both primary and fallback generators failed to produce a quote.", ex.getMessage());

            InOrder inOrder = inOrder(primary, fallback);
            inOrder.verify(primary).generate();
            inOrder.verify(fallback).generate();
            verifyNoMoreInteractions(primary, fallback);
        }
    }
}
