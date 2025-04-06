package com.example.controller;

import com.example.model.BlockchainDocument;
import com.example.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<BlockchainDocument> getAllDocuments() {
        return documentService.fetchAllDocuments();
    }

    @GetMapping("/{id}")
    public Optional<BlockchainDocument> getDocumentById(@PathVariable String id) {
        return documentService.fetchDocumentById(id);
    }
    
    @GetMapping("/type/{dataType}")
    public ResponseEntity<?> getDocumentsByDataType(@PathVariable String dataType) {
        logger.info("Received request for documents with dataType: {}", dataType);
        try {
            List<BlockchainDocument> documents = documentService.fetchDocumentsByDataType(dataType);
            
            if (documents.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "not_found");
                response.put("message", "No documents found with dataType: " + dataType);
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(documents);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error retrieving documents by dataType: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Find documents by identifier.
     * Example: GET /documents/identifier/chain_start
     */
    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<?> getDocumentsByIdentifier(@PathVariable String identifier) {
        logger.info("Received request for documents with identifier: {}", identifier);
        try {
            List<BlockchainDocument> documents = documentService.fetchDocumentsByIdentifier(identifier);
            
            if (documents.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "not_found");
                response.put("message", "No documents found with identifier: " + identifier);
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(documents);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error retrieving documents by identifier: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
