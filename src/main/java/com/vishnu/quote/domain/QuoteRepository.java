package com.vishnu.quote.domain;

import java.util.Optional;

public interface QuoteRepository {
    Optional<String> randomQuote();

    default String description() {
        return getClass().getSimpleName();
    }
}
