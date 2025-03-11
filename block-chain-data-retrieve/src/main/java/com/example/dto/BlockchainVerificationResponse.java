package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockchainVerificationResponse {

    @JsonProperty("Integrity") // Ensures correct mapping
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
}
