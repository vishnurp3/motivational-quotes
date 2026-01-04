package com.vishnu.quote.generator;

import com.vishnu.quote.domain.QuoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RepositoryMotivationalQuoteGenerator")
final class RepositoryMotivationalQuoteGeneratorTest {

    @Mock
    private QuoteRepository repository;

    @InjectMocks
    private RepositoryMotivationalQuoteGenerator generator;

    @Nested
    @DisplayName("constructor")
    final class Constructor {

        @Test
        void should_throwNullPointerException_when_repositoryIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new RepositoryMotivationalQuoteGenerator(null)
            );
            assertEquals("repository", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("generate()")
    final class Generate {

        @Test
        void should_delegateToRepository_randomQuote() {
            when(repository.randomQuote()).thenReturn(Optional.of("Keep going."));

            generator.generate();

            verify(repository).randomQuote();
            verifyNoMoreInteractions(repository);
        }

        @Test
        void should_returnExactlyWhatRepositoryReturns_when_quoteIsPresent() {
            String quote = "Small steps today build big change tomorrow.";
            when(repository.randomQuote()).thenReturn(Optional.of(quote));

            String result = generator.generate();

            assertSame(quote, result);
            verify(repository).randomQuote();
            verifyNoMoreInteractions(repository);
        }

        @Test
        void should_throwIllegalStateException_when_repositoryHasNoQuote_andIncludeRepositoryDescriptionInMessage() {
            when(repository.randomQuote()).thenReturn(Optional.empty());
            when(repository.description()).thenReturn("in-memory motivational quotes");

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> generator.generate());

            assertEquals(
                    "No motivational quote available from repository: in-memory motivational quotes",
                    ex.getMessage()
            );

            verify(repository).randomQuote();
            verify(repository).description();
            verifyNoMoreInteractions(repository);
        }

        @Test
        void should_notCallDescription_when_quoteIsPresent() {
            when(repository.randomQuote()).thenReturn(Optional.of("Discipline beats motivation."));

            generator.generate();

            verify(repository).randomQuote();
            verifyNoMoreInteractions(repository);
        }

        @Test
        void should_propagateRuntimeException_when_repositoryRandomQuoteThrows() {
            RuntimeException failure = new RuntimeException("repository down");
            when(repository.randomQuote()).thenThrow(failure);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> generator.generate());

            assertSame(failure, ex);
            verify(repository).randomQuote();
            verifyNoMoreInteractions(repository);
        }

        @Test
        void should_propagateRuntimeException_when_repositoryDescriptionThrowsWhileBuildingExceptionMessage() {
            when(repository.randomQuote()).thenReturn(Optional.empty());
            RuntimeException failure = new RuntimeException("description unavailable");
            when(repository.description()).thenThrow(failure);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> generator.generate());

            assertSame(failure, ex);

            verify(repository).randomQuote();
            verify(repository).description();
            verifyNoMoreInteractions(repository);
        }
    }
}
