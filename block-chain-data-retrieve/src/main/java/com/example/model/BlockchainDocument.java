package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Document(collection = "documents")
public class BlockchainDocument {

    @Id
    private String id;
    private String prevHash;
    private String hash;
    private int sequence;
    private DocumentData data;

    // Constructors
    public BlockchainDocument() {}

    public BlockchainDocument(String prevHash, int sequence, DocumentData data) {
        this.prevHash = prevHash;
        this.sequence = sequence;
        this.data = data;
        this.hash = generateHash();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public DocumentData getData() {
        return data;
    }

    public void setData(DocumentData data) {
        this.data = data;
    }

    /**
     * Generates a SHA-256 hash for the document.
     */
    public String generateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = prevHash + sequence + data.toString();
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    @Override
    public String toString() {
        return "BlockchainDocument{" +
                "id='" + id + '\'' +
                ", prevHash='" + prevHash + '\'' +
                ", hash='" + hash + '\'' +
                ", sequence=" + sequence +
                ", data=" + data +
                '}';
    }
}
