package com.example.langraph4j_researcher.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class ResponseUtils {

    private ResponseUtils() {}

    public static Map<String, Object> extractJsonFromLLMResponse(String llmResponse) {
        // Find the content between <json> tags
        int jsonStart = llmResponse.indexOf("<json>");
        int jsonEnd = llmResponse.indexOf("</json>");

        if (jsonStart == -1 || jsonEnd == -1) {
            throw new IllegalArgumentException("No JSON tags found in LLM response [" + llmResponse + "]");
        }

        // Extract the JSON string from between the tags (add 6 to skip "<json>" tag)
        String jsonStr = llmResponse.substring(jsonStart + 6, jsonEnd).trim();

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert JSON string to Map
            return objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON from LLM response [ " + jsonStr + " ]", e);
        }
    }
}
