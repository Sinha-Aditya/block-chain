package com.example.service;

import com.example.dto.BlockchainVerificationResponse;
import com.example.model.BlockchainDocument;
import com.example.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    
    private final DocumentRepository documentRepository;
    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${blockchain.verify.url}")
    private String blockchainVerifyUrl; // The verification API endpoint

    public DocumentService(DocumentRepository documentRepository, RestTemplate restTemplate, MongoTemplate mongoTemplate) {
        this.documentRepository = documentRepository;
        this.restTemplate = restTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Check if the blockchain is valid before allowing retrieval.
     * Calls the FastAPI endpoint to verify chain integrity.
     */
    private boolean isBlockchainValid() {
        try {
            logger.info("Verifying blockchain integrity at URL: {}", blockchainVerifyUrl);
            BlockchainVerificationResponse response = restTemplate.getForObject(blockchainVerifyUrl, BlockchainVerificationResponse.class);

            if (response != null && response.isIntegrity()) {
                logger.info("✅ Blockchain is valid: {}", response.getMessage());
                return true;
            } else {
                logger.error("❌ Blockchain verification failed: {}", (response != null ? response.getMessage() : "No response"));
                return false;
            }

        } catch (Exception e) {
            logger.error("⚠️ Error verifying blockchain integrity: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fetch all documents if the blockchain is valid.
     * If not valid, returns a list containing a special "tampered" document.
     */
    public List<BlockchainDocument> fetchAllDocuments() {
        if (!isBlockchainValid()) {
            // Blockchain has been tampered with. Return a list containing a "tampered" document.
            logger.warn("Blockchain validation failed. Returning tampered document.");
            BlockchainDocument tamperedDocument = new BlockchainDocument();
            tamperedDocument.setId("TAMPERED"); // Set ID to indicate tampering
            tamperedDocument.setData("Blockchain has been tampered with!"); // Set data field
            return Arrays.asList(tamperedDocument); // Return a list with only the tampered document.
        }

        logger.info("Blockchain validation successful. Fetching all documents.");
        List<BlockchainDocument> documents = documentRepository.findAll();
        logger.info("Fetched {} documents", documents.size());
        return documents;
    }


    /**
     * Fetch a document by ID if the blockchain is valid.
     * Throws an exception if the blockchain is not valid.
     */
    public Optional<BlockchainDocument> fetchDocumentById(String id) {
        if (!isBlockchainValid()) {
            logger.error("Blockchain validation failed. Cannot retrieve document with ID: {}", id);
            throw new RuntimeException("Blockchain validation failed. Cannot retrieve document.");
        }
        
        logger.info("Blockchain validation successful. Fetching document with ID: {}", id);
        Optional<BlockchainDocument> document = documentRepository.findById(id);
        if (document.isPresent()) {
            logger.info("Document found with ID: {}", id);
        } else {
            logger.warn("Document not found with ID: {}", id);
        }
        return document;
    }
    
    /**
     * Fetch documents by dataType if the blockchain is valid.
     * Throws an exception if the blockchain is not valid.
     * 
     * @param dataType The dataType to search for (e.g., "genesis")
     * @return List of documents with the specified dataType
     */
    public List<BlockchainDocument> fetchDocumentsByDataType(String dataType) {
        if (!isBlockchainValid()) {
            logger.error("Blockchain validation failed. Cannot retrieve documents with dataType: {}", dataType);
            throw new RuntimeException("Blockchain validation failed. Cannot retrieve documents.");
        }
        
        logger.info("Blockchain validation successful. Fetching documents with dataType: {}", dataType);
        
        // Create a query to find documents where data.dataType equals the specified dataType
        Query query = new Query(Criteria.where("data.dataType").is(dataType));
        List<BlockchainDocument> documents = mongoTemplate.find(query, BlockchainDocument.class);
        
        logger.info("Found {} documents with dataType: {}", documents.size(), dataType);
        return documents;
    }

    /**
     * Fetch documents by identifier if the blockchain is valid.
     * Throws an exception if the blockchain is not valid.
     * 
     * @param identifier The identifier to search for (e.g., "chain_start")
     * @return List of documents with the specified identifier
     */
    public List<BlockchainDocument> fetchDocumentsByIdentifier(String identifier) {
        if (!isBlockchainValid()) {
            logger.error("Blockchain validation failed. Cannot retrieve documents with identifier: {}", identifier);
            throw new RuntimeException("Blockchain validation failed. Cannot retrieve documents.");
        }
        
        logger.info("Blockchain validation successful. Fetching documents with identifier: {}", identifier);
        
        // Create a query to find documents where data.identifier equals the specified identifier
        Query query = new Query(Criteria.where("data.identifier").is(identifier));
        List<BlockchainDocument> documents = mongoTemplate.find(query, BlockchainDocument.class);
        
        logger.info("Found {} documents with identifier: {}", documents.size(), identifier);
        return documents;
    }
}