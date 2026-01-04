package com.vishnu.quote.infrastructure.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.ResponsesModel;
import com.openai.models.responses.*;
import com.openai.services.blocking.ResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiResponsesTextClient")
final class OpenAiResponsesTextClientTest {

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private ResponseService responseService;

    @Mock
    private ChatModel model;

    @Nested
    @DisplayName("constructor")
    final class Constructor {

        @Test
        void should_throwNullPointerException_when_clientIsNull() {
            assertThrows(NullPointerException.class, () -> new OpenAiResponsesTextClient(null, model, 1.0, 100));
        }

        @Test
        void should_throwNullPointerException_when_modelIsNull() {
            assertThrows(NullPointerException.class, () -> new OpenAiResponsesTextClient(openAIClient, null, 1.0, 100));
        }

        @Test
        void should_throwIllegalArgumentException_when_temperatureIsBelowZero() {
            assertThrows(IllegalArgumentException.class, () -> new OpenAiResponsesTextClient(openAIClient, model, -0.1, 100));
        }

        @Test
        void should_throwIllegalArgumentException_when_temperatureIsAboveTwo() {
            assertThrows(IllegalArgumentException.class, () -> new OpenAiResponsesTextClient(openAIClient, model, 2.1, 100));
        }

        @Test
        void should_throwIllegalArgumentException_when_maxOutputTokensIsZero() {
            assertThrows(IllegalArgumentException.class, () -> new OpenAiResponsesTextClient(openAIClient, model, 1.0, 0));
        }

        @Test
        void should_throwIllegalArgumentException_when_maxOutputTokensIsNegative() {
            assertThrows(IllegalArgumentException.class, () -> new OpenAiResponsesTextClient(openAIClient, model, 1.0, -1));
        }
    }

    @Nested
    @DisplayName("generateText()")
    final class GenerateText {

        private OpenAiResponsesTextClient textClient;

        @BeforeEach
        void init() {
            textClient = new OpenAiResponsesTextClient(openAIClient, model, 1.0, 100);
        }

        @Test
        void should_throwNullPointerException_when_promptIsNull() {
            assertThrows(NullPointerException.class, () -> textClient.generateText(null));
        }

        @Test
        void should_passCorrectParametersToOpenAiClient() {
            when(openAIClient.responses()).thenReturn(responseService);
            Response response = mockEmptyResponse();
            when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
            textClient.generateText("prompt");
            ArgumentCaptor<ResponseCreateParams> captor = ArgumentCaptor.forClass(ResponseCreateParams.class);
            verify(responseService).create(captor.capture());
            ResponseCreateParams params = captor.getValue();
            ResponsesModel responsesModel = params.model().orElseThrow();
            ChatModel capturedModel = responsesModel.chat().orElseThrow();
            assertSame(model, capturedModel);
            ResponseCreateParams.Input input = params.input().orElseThrow();
            String inputText = input.text().orElseThrow();
            assertEquals("prompt", inputText);
            assertEquals(1.0, params.temperature().orElseThrow());
            Number mot = params.maxOutputTokens().orElseThrow();
            assertEquals(100, mot.intValue());
            verify(openAIClient).responses();
            verifyNoMoreInteractions(openAIClient, responseService);
        }


        @Test
        void should_returnTrimmedText_when_responseContainsOutputText() {
            when(openAIClient.responses()).thenReturn(responseService);
            Response response = mockResponseWithText();
            when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
            String result = textClient.generateText("prompt");
            assertEquals("output text", result);
            verify(openAIClient).responses();
            verify(responseService).create(any(ResponseCreateParams.class));
            verifyNoMoreInteractions(openAIClient, responseService);
        }

        @Test
        void should_returnEmptyString_when_responseHasNoOutput() {
            when(openAIClient.responses()).thenReturn(responseService);
            Response response = mockEmptyResponse();
            when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
            String result = textClient.generateText("prompt");
            assertEquals("", result);
            verify(openAIClient).responses();
            verify(responseService).create(any(ResponseCreateParams.class));
            verifyNoMoreInteractions(openAIClient, responseService);
        }

        @Test
        void should_propagateRuntimeException_from_openAiClient() {
            when(openAIClient.responses()).thenReturn(responseService);
            RuntimeException failure = new RuntimeException("failure");
            when(responseService.create(any(ResponseCreateParams.class))).thenThrow(failure);
            RuntimeException ex = assertThrows(RuntimeException.class, () -> textClient.generateText("prompt"));
            assertSame(failure, ex);
            verify(openAIClient).responses();
            verify(responseService).create(any(ResponseCreateParams.class));
            verifyNoMoreInteractions(openAIClient, responseService);
        }


        private static Response mockResponseWithText() {
            ResponseOutputText outputText = mock(ResponseOutputText.class);
            when(outputText.text()).thenReturn("  output text  ");
            ResponseOutputMessage.Content content = mock(ResponseOutputMessage.Content.class);
            when(content.outputText()).thenReturn(Optional.of(outputText));
            ResponseOutputMessage message = mock(ResponseOutputMessage.class);
            when(message.content()).thenReturn(List.of(content));
            ResponseOutputItem item = mock(ResponseOutputItem.class);
            when(item.message()).thenReturn(Optional.of(message));
            Response response = mock(Response.class);
            when(response.output()).thenReturn(List.of(item));
            return response;
        }

        private static Response mockEmptyResponse() {
            Response response = mock(Response.class);
            when(response.output()).thenReturn(List.of());
            return response;
        }
    }
}
