package com.vishnu.quote.generator;

import com.vishnu.quote.domain.QuoteGenerator;
import com.vishnu.quote.domain.QuoteRepository;

import java.util.Objects;

public final class RepositoryMotivationalQuoteGenerator implements QuoteGenerator {

    private final QuoteRepository repository;

    public RepositoryMotivationalQuoteGenerator(QuoteRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public String generate() {
        return repository.randomQuote()
                .orElseThrow(() -> new IllegalStateException(
                        "No motivational quote available from repository: " + repository.description()));
    }
}
