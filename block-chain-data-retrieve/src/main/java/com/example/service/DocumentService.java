package com.example.service;

import com.example.model.BlockchainDocument;
import com.example.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    public DocumentService(DocumentRepository documentRepository, MongoTemplate mongoTemplate) {
        this.documentRepository = documentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieve all documents, ensuring chain integrity.
     */
    public List<BlockchainDocument> getAllDocuments() {
        ensureChainIntegrity();
        return documentRepository.findAll();
    }

    /**
     * Retrieve a document by ID.
     */
    public BlockchainDocument getDocumentById(String id) {
        ensureChainIntegrity();
        return documentRepository.findById(id).orElseThrow(() ->
                new ChainIntegrityException("Document with ID " + id + " not found")
        );
    }

    /**
     * Retrieve documents by data type.
     */
    public List<BlockchainDocument> getDocumentsByType(String dataType) {  // Renamed for consistency
        ensureChainIntegrity();
        Query query = new Query();
        query.addCriteria(Criteria.where("data.dataType").is(dataType));
        return mongoTemplate.find(query, BlockchainDocument.class);
    }

    /**
     * Retrieve documents by identifier.
     */
    public List<BlockchainDocument> getDocumentsByIdentifier(String identifier) {
        ensureChainIntegrity();
        Query query = new Query();
        query.addCriteria(Criteria.where("data.identifier").is(identifier));
        return mongoTemplate.find(query, BlockchainDocument.class);
    }

    /**
     * Retrieve the latest document based on sequence number.
     */
    public BlockchainDocument getLatestDocument() {
        ensureChainIntegrity();
        Query query = new Query();
        query.limit(1).with(Sort.by(Sort.Direction.DESC, "sequence"));
        return mongoTemplate.find(query, BlockchainDocument.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ChainIntegrityException("No documents available"));
    }

    /**
     * Ensures blockchain integrity before retrieving documents.
     * Throws an exception if integrity is compromised.
     */
    private void ensureChainIntegrity() {
        List<BlockchainDocument> blockchainDocuments = documentRepository.findAll();
        if (blockchainDocuments.isEmpty()) {
            logger.info("No documents found. Integrity check passed.");
            return;
        }

        for (int i = 1; i < blockchainDocuments.size(); i++) {
            BlockchainDocument currentDoc = blockchainDocuments.get(i);
            BlockchainDocument previousDoc = blockchainDocuments.get(i - 1);

            // Validate the hash of the current document
            String computedHash = currentDoc.generateHash();
            if (!computedHash.equals(currentDoc.getHash())) {
                logger.error("❌ Data tampered in document sequence {}", currentDoc.getSequence());
                throw new ChainIntegrityException("Data tampered at sequence " + currentDoc.getSequence());
            }

            // Validate the chain link (prevHash)
            if (!currentDoc.getPrevHash().equals(previousDoc.getHash())) {
                logger.error("❌ Chain broken at sequence {}", currentDoc.getSequence());
                throw new ChainIntegrityException("Chain broken at sequence " + currentDoc.getSequence());
            }
        }

        logger.info("✅ Chain integrity verified successfully.");
    }

    /**
     * Custom exception for blockchain integrity violations.
     */
    public static class ChainIntegrityException extends RuntimeException {
        public ChainIntegrityException(String message) {
            super(message);
        }
    }
}
