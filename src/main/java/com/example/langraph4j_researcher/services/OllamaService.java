package com.example.langraph4j_researcher.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaChatModel ollamaChatModel;

    public String getLLMResponse(String prompt) {

        ChatClient chatClient = ChatClient.builder(ollamaChatModel).build();

        return chatClient.prompt(prompt).call().content();

    }
}
