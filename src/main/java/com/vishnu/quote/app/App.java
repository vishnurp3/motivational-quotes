package com.vishnu.quote.app;

import com.openai.models.ChatModel;
import com.vishnu.quote.application.QuoteGenerators;
import com.vishnu.quote.application.QuoteService;
import com.vishnu.quote.domain.AiTextClient;
import com.vishnu.quote.domain.QuoteGenerator;
import com.vishnu.quote.infrastructure.openai.OpenAiClientFactory;
import com.vishnu.quote.infrastructure.openai.OpenAiResponsesTextClient;

public final class App {

    public static void main(String[] args) {
        var openAiClient = OpenAiClientFactory.fromEnvironment();
        AiTextClient aiTextClient = new OpenAiResponsesTextClient(
                openAiClient, ChatModel.GPT_4_1_MINI, 0.9, 60
        );

        QuoteGenerator generator = QuoteGenerators.aiWithClasspathFallback(
                aiTextClient, "quotes.txt"
        );
        QuoteService service = new QuoteService(generator);

        System.out.println(service.randomMotivationalQuote());
    }
}
