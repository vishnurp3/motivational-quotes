package com.vishnu.quote.infrastructure.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.*;
import com.vishnu.quote.domain.AiTextClient;

import java.util.Objects;
import java.util.Optional;

public final class OpenAiResponsesTextClient implements AiTextClient {
    private final OpenAIClient client;
    private final ChatModel model;
    private final double temperature;
    private final int maxOutputTokens;

    public OpenAiResponsesTextClient(OpenAIClient client, ChatModel model, double temperature, int maxOutputTokens) {
        this.client = Objects.requireNonNull(client);
        this.model = Objects.requireNonNull(model);
        if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
        }
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("maxOutputTokens must be > 0");
        }
        this.temperature = temperature;
        this.maxOutputTokens = maxOutputTokens;
    }

    @Override
    public String generateText(String prompt) {
        Objects.requireNonNull(prompt);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .input(prompt)
                .temperature(temperature)
                .maxOutputTokens(maxOutputTokens)
                .build();

        Response response = client.responses().create(params);
        return extractFirstOutputText(response)
                .map(String::trim)
                .orElse("");
    }

    private Optional<String> extractFirstOutputText(Response response) {
        return response.output().stream()
                .findFirst()
                .flatMap(ResponseOutputItem::message)
                .flatMap(m -> m.content().stream().findFirst())
                .flatMap(ResponseOutputMessage.Content::outputText)
                .map(ResponseOutputText::text);
    }

}
