package com.example.storyloom_ai_recommendation_service.externalInterfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "geminiClient", url =  "${gemini.api.base-url}")
public interface GeminiClient {

    @PostMapping("/v1/models/gemini-2.5-flash:generateContent")
    Map<String, Object> generateContent(
            @RequestParam("key") String apiKey,
            @RequestBody Map<String, Object> body
    );}
