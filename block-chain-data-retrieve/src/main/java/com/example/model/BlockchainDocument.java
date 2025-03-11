package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "documents")  // Replace with your actual collection name
public class BlockchainDocument {

    @Id
    private String id;  // Maps to "_id" in MongoDB

    private Object data;  // Can be String, Number, or JSON (Map)
    private String hash;
    private String signature;
    @Field("verify_key")
    private String verifyKey;
    @Field("prev_hash")
    private String prevHash;
    private double timestamp;
    private int sequence;

    // Constructors
    public BlockchainDocument() {}

    public BlockchainDocument(String id, Object data, String hash, String signature,
                              String verifyKey, String prevHash, double timestamp, int sequence) {
        this.id = id;
        this.data = data;
        this.hash = hash;
        this.signature = signature;
        this.verifyKey = verifyKey;
        this.prevHash = prevHash;
        this.timestamp = timestamp;
        this.sequence = sequence;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getVerifyKey() {
        return verifyKey;
    }

    public void setVerifyKey(String verifyKey) {
        this.verifyKey = verifyKey;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    // Helper method to get data as Map if it's JSON
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataAsMap() {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    //public void setIdentifier(String tampered) {
    //}

    // Helper method to get data as String
//    public String getDataAsString() {
//        if (data instanceof String) {
//            return (String) data;
//        }
//        return null;
//    }

    // Helper method to get data as Number
//    public Number getDataAsNumber() {
//        if (data instanceof Number) {
//            return (Number) data;
//        }
//        return null;
//    }
}