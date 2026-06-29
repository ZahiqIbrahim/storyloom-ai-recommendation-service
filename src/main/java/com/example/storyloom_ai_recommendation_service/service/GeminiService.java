package com.example.storyloom_ai_recommendation_service.service;

import com.example.storyloom_ai_recommendation_service.externalInterfaces.GeminiClient;
import com.example.storyloom_ai_recommendation_service.externalInterfaces.StoryloomCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    private final GeminiClient geminiClient;

    @Autowired
    private StoryloomCatalogService storyloomCatalogService;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    public GeminiService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public Object recommendBooks(String userInput) {

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text",
                                                """
                                                Recommend Books based on: %s
    
                                                You are a strict API.
                                                recommend at least 10 books 
                                                Return ONLY a valid JSON array of Book titles.
                                                No explanation.
                                                No markdown.
                                                No extra text.
    
                                                Example:
                                                ["Clean code","Harry potter"]
                                                """.formatted(userInput))
                                )
                        )
                )
        );

        Map<String, Object> response = geminiClient.generateContent(apiKey, request);

        String text = extractText(response);

        try {
            List<String> books = objectMapper.readValue(
                    text,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );

            return storyloomCatalogService.getBooks(books).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini Book response: " + text, e);
        }
    }

    public Object recommendMovies(String userInput) {

        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text",
                                                """
                                                Recommend movies based on: %s
    
                                                You are a strict API.
                                                recommend at least 10 movies
                                                Return ONLY a valid JSON array of movie titles.
                                                No explanation.
                                                No markdown.
                                                No extra text.
    
                                                Example:
                                                ["Interstellar","The Dark Knight","The Lord of the Rings: The Fellowship of the Ring"]
                                                """.formatted(userInput))
                                )
                        )
                )
        );

        Map<String, Object> response = geminiClient.generateContent(apiKey, request);

        String text = extractText(response);

        try {
            List<String> movies = objectMapper.readValue(
                    text,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );

            return storyloomCatalogService.getMovies(movies).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini movie response: " + text, e);
        }
    }

    private String extractText(Map<String, Object> body) {
        try {
            List<?> candidates = (List<?>) body.get("candidates");
            Map<?, ?> first = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) first.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> part = (Map<?, ?>) parts.get(0);

            return part.get("text").toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Gemini response", e);
        }
    }

    private List<String> parseTitles(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(title -> title.replaceAll("^\"|\"$", "")) // remove starting/ending quotes
                .filter(title -> !title.isBlank())
                .toList();
    }
}
