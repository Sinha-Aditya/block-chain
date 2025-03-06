package com.example.controller;

import com.example.model.BlockchainDocument;
import com.example.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public List<BlockchainDocument> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public BlockchainDocument getDocumentById(@PathVariable String id) {
        return documentService.getDocumentById(id);
    }

    @GetMapping("/type/{dataType}")
    public List<BlockchainDocument> getDocumentsByType(@PathVariable String dataType) {
        return documentService.getDocumentsByType(dataType);
    }

    @GetMapping("/identifier/{identifier}")
    public List<BlockchainDocument> getDocumentsByIdentifier(@PathVariable String identifier) {
        return documentService.getDocumentsByIdentifier(identifier);
    }

    @GetMapping("/latest")
    public BlockchainDocument getLatestDocument() {
        return documentService.getLatestDocument();
    }
}
