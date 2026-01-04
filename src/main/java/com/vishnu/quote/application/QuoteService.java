package com.vishnu.quote.application;

import com.vishnu.quote.domain.QuoteGenerator;

import java.util.Objects;

public final class QuoteService {
    private final QuoteGenerator generator;

    public QuoteService(QuoteGenerator generator) {
        this.generator = Objects.requireNonNull(generator);
    }

    public String randomMotivationalQuote() {
        return generator.generate();
    }
}
