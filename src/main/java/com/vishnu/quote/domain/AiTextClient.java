package com.vishnu.quote.domain;

@FunctionalInterface
public interface AiTextClient {
    String generateText(String prompt);
}