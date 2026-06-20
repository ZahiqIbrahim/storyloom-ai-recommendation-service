package com.example.storyloom_ai_recommendation_service.externalInterfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("STORYLOOM-CATALOG-SERVICE")
public interface StoryloomCatalogService {


    @PostMapping("/books/getBook")
    public ResponseEntity<?> getBook(@RequestBody String bookTitle);

    @PostMapping("/books/getBooks")
    public ResponseEntity<?> getBooks(@RequestBody List<String> bookTitles);

    @PostMapping("/movies/getMovie")
    public ResponseEntity<?> getMovie(@RequestBody String movieTitle);

    @PostMapping("/movies/getMovies")
    public ResponseEntity<?> getMovies(@RequestBody List<String> movieTitles);




}
