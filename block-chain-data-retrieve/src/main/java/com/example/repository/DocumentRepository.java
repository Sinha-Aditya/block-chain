package com.example.repository;

import com.example.model.BlockchainDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<BlockchainDocument, String> {
}
