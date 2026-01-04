package com.vishnu.quote.application;

import com.vishnu.quote.domain.AiTextClient;
import com.vishnu.quote.domain.QuoteGenerator;
import com.vishnu.quote.generator.FallbackQuoteGenerator;
import com.vishnu.quote.generator.MotivationalQuoteGenerator;
import com.vishnu.quote.generator.RepositoryMotivationalQuoteGenerator;
import com.vishnu.quote.infrastructure.repository.ClasspathQuoteRepository;

import java.util.Objects;

public final class QuoteGenerators {

    private QuoteGenerators() {
    }

    public static QuoteGenerator aiOnly(AiTextClient aiTextClient) {
        Objects.requireNonNull(aiTextClient, "aiTextClient");
        return new MotivationalQuoteGenerator(aiTextClient);
    }

    public static QuoteGenerator classpathOnly(String resourceName) {
        return new RepositoryMotivationalQuoteGenerator(new ClasspathQuoteRepository(resourceName));
    }

    public static QuoteGenerator aiWithClasspathFallback(AiTextClient aiTextClient, String resourceName) {
        Objects.requireNonNull(aiTextClient, "aiTextClient");
        Objects.requireNonNull(resourceName, "resourceName");

        QuoteGenerator primary = new MotivationalQuoteGenerator(aiTextClient);
        QuoteGenerator fallback = new RepositoryMotivationalQuoteGenerator(
                new ClasspathQuoteRepository(resourceName)
        );

        return new FallbackQuoteGenerator(primary, fallback);
    }
}
