package com.example.storyloom_ai_recommendation_service.controller;

import com.example.storyloom_ai_recommendation_service.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/books")
    public ResponseEntity<?> recommendBooks(@RequestBody String text) {

        try{
            return ResponseEntity.ok(geminiService.recommendBooks(text));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @PostMapping("/movies")
    public ResponseEntity<?> recommendMovies(@RequestBody String text) {
        try{
            return ResponseEntity.ok(geminiService.recommendMovies(text));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }

    }
}
