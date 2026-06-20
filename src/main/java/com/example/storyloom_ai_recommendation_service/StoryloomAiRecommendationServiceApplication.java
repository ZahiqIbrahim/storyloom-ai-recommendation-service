package com.example.storyloom_ai_recommendation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StoryloomAiRecommendationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryloomAiRecommendationServiceApplication.class, args);
	}

}
