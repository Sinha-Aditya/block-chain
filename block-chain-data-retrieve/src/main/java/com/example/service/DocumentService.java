package com.example.service;

import com.example.dto.BlockchainVerificationResponse;
import com.example.model.BlockchainDocument;
import com.example.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RestTemplate restTemplate;

    @Value("${blockchain.verify.url}")
    private String blockchainVerifyUrl; // The verification API endpoint

    public DocumentService(DocumentRepository documentRepository, RestTemplate restTemplate) {
        this.documentRepository = documentRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Check if the blockchain is valid before allowing retrieval.
     */
    private boolean isBlockchainValid() {
        try {
            BlockchainVerificationResponse response = restTemplate.getForObject(blockchainVerifyUrl, BlockchainVerificationResponse.class);

            if (response != null && response.isIntegrity()) {
                System.out.println("✅ Blockchain is valid: " + response.getMessage());
                return true;
            } else {
                System.err.println("❌ Blockchain verification failed: " + (response != null ? response.getMessage() : "No response"));
                return false;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error verifying blockchain integrity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetch all documents if the blockchain is valid.
     * If not valid, returns a list containing a special "tampered" document.
     */
    public List<BlockchainDocument> fetchAllDocuments() {
        if (!isBlockchainValid()) {
            // Blockchain has been tampered with.  Return a list containing a "tampered" document.
            BlockchainDocument tamperedDocument = new BlockchainDocument();
            tamperedDocument.setId("TAMPERED"); // Set ID to indicate tampering
            tamperedDocument.setData("Blockchain has been tampered with!"); // Set data field
            return Arrays.asList(tamperedDocument); // Return a list with only the tampered document.
        }

        System.out.println("Fetched documents: " + documentRepository.findAll());
        return documentRepository.findAll();
    }


    /**
     * Fetch a document by ID if the blockchain is valid.
     */
    public Optional<BlockchainDocument> fetchDocumentById(String id) {
        if (!isBlockchainValid()) {
            throw new RuntimeException("Blockchain validation failed. Cannot retrieve document.");
        }
        return documentRepository.findById(id);
    }
}