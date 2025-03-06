package com.example.controller;

import com.example.model.DataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    private final WebClient webClient;

    @Autowired
    public DataController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> sayHello() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Spring Boot REST API"));
    }

    @PostMapping("/send-data")
    public ResponseEntity<Map<String, Object>> sendData(@RequestBody Map<String, String> requestBody) {
        try {
            String data = requestBody.get("data");
            if (data == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing 'data' field in request body"));
            }

            // Parse input based on type
            Object parsedData;
            try {
                parsedData = Integer.parseInt(data);
            } catch (NumberFormatException e1) {
                try {
                    parsedData = Double.parseDouble(data);
                } catch (NumberFormatException e2) {
                    try {
                        parsedData = new org.json.JSONObject(data).toMap();
                    } catch (Exception e3) {
                        parsedData = data;
                    }
                }
            }

            DataRequest request = new DataRequest(parsedData);

            Mono<String> response = webClient.post()
                    .uri("/store_data")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class);

            String result = response.block();
            return ResponseEntity.ok(Map.of(
                    "sentData", data,
                    "response", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error processing request: " + e.getMessage()));
        }
    }

    @GetMapping("/get-all-data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        try {
            Mono<String> response = webClient.get()
                    .uri("/get_all_data")
                    .retrieve()
                    .bodyToMono(String.class);

            String result = response.block();
            return ResponseEntity.ok(Map.of("response", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error fetching data: " + e.getMessage()));
        }
    }
    @GetMapping("/check_chain_integrity")
    public ResponseEntity<Map<String, Object>>checkChainIntegrity(){
        try {
            Mono<String> response = webClient.get()
                    .uri("/check_chain_integrity")
                    .retrieve()
                    .bodyToMono(String.class);

            String result = response.block();
            return ResponseEntity.ok(Map.of("response", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error fetching data: " + e.getMessage()));
        }
    }
}