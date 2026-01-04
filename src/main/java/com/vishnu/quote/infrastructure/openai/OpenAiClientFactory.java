package com.vishnu.quote.infrastructure.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public final class OpenAiClientFactory {

    private OpenAiClientFactory() {
    }

    public static OpenAIClient fromEnvironment() {
        return OpenAIOkHttpClient.fromEnv();
    }
}