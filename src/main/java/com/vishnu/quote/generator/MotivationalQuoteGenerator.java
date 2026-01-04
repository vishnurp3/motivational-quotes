package com.vishnu.quote.generator;

import com.vishnu.quote.domain.AiTextClient;
import com.vishnu.quote.domain.QuoteGenerator;

import java.util.Objects;

public final class MotivationalQuoteGenerator implements QuoteGenerator {
    private final AiTextClient aiTextClient;

    public MotivationalQuoteGenerator(AiTextClient aiTextClient) {
        this.aiTextClient = Objects.requireNonNull(aiTextClient);
    }

    @Override
    public String generate() {
        return aiTextClient.generateText("""
                Generate exactly one short motivational quote.
                Requirements:
                - 1 sentence
                - No author name
                - No surrounding quotation marks
                """);
    }
}
