package com.vishnu.quote.generator;

import com.vishnu.quote.domain.QuoteGenerator;

import java.util.Objects;

public final class FallbackQuoteGenerator implements QuoteGenerator {

    private final QuoteGenerator primary;
    private final QuoteGenerator fallback;

    public FallbackQuoteGenerator(QuoteGenerator primary, QuoteGenerator fallback) {
        this.primary = Objects.requireNonNull(primary, "primary");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public String generate() {
        String primaryValue = null;
        try {
            primaryValue = primary.generate();
        } catch (RuntimeException ignored) {
        }

        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue.trim();
        }

        try {
            String fallbackValue = fallback.generate();
            if (fallbackValue != null && !fallbackValue.isBlank()) {
                return fallbackValue.trim();
            }
        } catch (RuntimeException ignored) {
        }
        throw new IllegalStateException("Both primary and fallback generators failed to produce a quote.");
    }
}
