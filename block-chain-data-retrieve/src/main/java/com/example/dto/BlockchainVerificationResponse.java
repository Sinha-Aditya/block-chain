package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO class to map the response from the blockchain verification API.
 * Maps to the response from the FastAPI endpoint at /check_chain_integrity
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockchainVerificationResponse {

    @JsonProperty("Integrity") // Ensures correct mapping to the "Integrity" field in the response
    private boolean integrity;

    @JsonProperty("message")
    private String message;

    // Getters and Setters
    public boolean isIntegrity() {
        return integrity;
    }

    public void setIntegrity(boolean integrity) {
        this.integrity = integrity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BlockchainVerificationResponse{" +
                "integrity=" + integrity +
                ", message='" + message + '\'' +
                '}';
    }
}
