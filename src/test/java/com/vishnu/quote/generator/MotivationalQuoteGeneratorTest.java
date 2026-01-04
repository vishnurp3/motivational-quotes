package com.vishnu.quote.generator;

import com.vishnu.quote.domain.AiTextClient;
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
@DisplayName("MotivationalQuoteGenerator")
final class MotivationalQuoteGeneratorTest {

    private static final String EXPECTED_PROMPT = """
            Generate exactly one short motivational quote.
            Requirements:
            - 1 sentence
            - No author name
            - No surrounding quotation marks
            """;

    @Mock
    private AiTextClient aiTextClient;

    @InjectMocks
    private MotivationalQuoteGenerator quoteGenerator;

    @Nested
    @DisplayName("constructor")
    final class Constructor {
        @Test
        void should_throwNullPointerException_when_aiTextClientIsNull() {
            assertThrows(NullPointerException.class, () -> new MotivationalQuoteGenerator(null));
        }
    }

    @Nested
    @DisplayName("generate()")
    final class Generate {

        @Test
        void should_delegateToAiTextClient_withExactPrompt() {
            String generated = "Keep going; progress compounds daily.";
            when(aiTextClient.generateText(EXPECTED_PROMPT)).thenReturn(generated);
            quoteGenerator.generate();
            verify(aiTextClient).generateText(EXPECTED_PROMPT);
            verifyNoMoreInteractions(aiTextClient);
        }

        @Test
        void should_returnExactlyWhatAiTextClientReturns() {
            String generated = "Small steps today build big change tomorrow.";
            when(aiTextClient.generateText(EXPECTED_PROMPT)).thenReturn(generated);
            String result = quoteGenerator.generate();
            assertSame(generated, result);
            verify(aiTextClient).generateText(EXPECTED_PROMPT);
            verifyNoMoreInteractions(aiTextClient);
        }

        @Test
        void should_propagateRuntimeException_when_aiTextClientFails() {
            RuntimeException failure = new RuntimeException("upstream failure");
            when(aiTextClient.generateText(EXPECTED_PROMPT)).thenThrow(failure);
            RuntimeException ex = assertThrows(RuntimeException.class, () -> quoteGenerator.generate());
            assertSame(failure, ex);
            verify(aiTextClient).generateText(EXPECTED_PROMPT);
            verifyNoMoreInteractions(aiTextClient);
        }
    }
}
