package com.example.repository;

import com.example.model.BlockchainDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<BlockchainDocument, String> {

    Optional<BlockchainDocument> findById(String id);

    List<BlockchainDocument> findByDataDataType(String dataType);  // Find by nested "dataType" field

    List<BlockchainDocument> findByDataIdentifier(String identifier); // Find by nested "identifier" field

    BlockchainDocument findFirstByOrderBySequenceDesc();  // Fetch the latest document

}
