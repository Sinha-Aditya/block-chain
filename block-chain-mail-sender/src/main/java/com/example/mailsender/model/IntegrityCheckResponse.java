package com.example.mailsender.model;

import java.util.Map;

public class IntegrityCheckResponse {
    private Boolean integrity;
    private Map<String, Object> additionalInfo;
    
    // Standard getters and setters
    public Boolean getIntegrity() {
        return integrity;
    }
    
    public void setIntegrity(Boolean integrity) {
        this.integrity = integrity;
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    // This method helps with deserialization when there are additional fields
    public void setAdditionalProperties(Map<String, Object> properties) {
        this.additionalInfo = properties;
    }
} 