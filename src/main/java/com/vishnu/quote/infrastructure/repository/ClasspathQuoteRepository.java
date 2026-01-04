package com.vishnu.quote.infrastructure.repository;

import com.vishnu.quote.domain.QuoteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class ClasspathQuoteRepository implements QuoteRepository {

    private final String resourceName;
    private List<String> cached;

    public ClasspathQuoteRepository(String resourceName) {
        this.resourceName = validateResourceName(resourceName);
    }

    @Override
    public Optional<String> randomQuote() {
        List<String> quotes = quotes();
        if (quotes.isEmpty()) return Optional.empty();
        int idx = ThreadLocalRandom.current().nextInt(quotes.size());
        return Optional.of(quotes.get(idx));
    }

    @Override
    public String description() {
        return "classpath:" + resourceName;
    }

    private synchronized List<String> quotes() {
        if (cached == null) {
            cached = load(resourceName);
        }
        return cached;
    }

    private static String validateResourceName(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");
        String name = resourceName.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("resourceName must not be blank");
        }
        if (name.startsWith("/")) {
            throw new IllegalArgumentException("resourceName must not start with '/': " + name);
        }
        return name;
    }

    private static List<String> load(String resourceName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (var in = cl.getResourceAsStream(resourceName)) {
            if (in == null) {
                return List.of();
            }
            try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .filter(s -> !s.startsWith("#"))
                        .toList();
            }
        } catch (IOException e) {
            return List.of();
        }
    }
}
