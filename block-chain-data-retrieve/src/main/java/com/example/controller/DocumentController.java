package com.example.controller;

import com.example.model.BlockchainDocument;
import com.example.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
public class DocumentController {

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
}
