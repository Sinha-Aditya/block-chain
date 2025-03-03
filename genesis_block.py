import hashlib
import json
import pymongo
import nacl.signing
import os
import time

# Connect to MongoDB
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["secure_db"]
collection = db["documents"]

# File to store the signing key for persistence
KEY_FILE = "signing_key.bin"

# Generate new key and save it
signing_key = nacl.signing.SigningKey.generate()
with open(KEY_FILE, "wb") as f:
    f.write(bytes(signing_key))

verify_key = signing_key.verify_key.encode().hex()
print(f"Created new signing key. Verify key: {verify_key[:8]}...")

# Genesis block data
initial_data = {
    "dataType": "gps",
    "identifier": "truck_123",
    "payload": {
        "latitude": 28.7041,
        "longitude": 77.1025,
        "timestamp": "2025-02-09T12:00:00"
    }
}


def generate_hash(data):
    """Generate SHA-256 hash of the document data."""
    data_str = json.dumps(data, sort_keys=True)
    return hashlib.sha256(data_str.encode()).hexdigest()


# Clear the collection to start fresh
result = collection.delete_many({})
print(f"Reset database: {result.deleted_count} documents deleted.")

# Create and insert genesis block
data_hash = generate_hash(initial_data)
signed_hash = signing_key.sign(data_hash.encode()).hex()

genesis_document = {
    "data": initial_data,
    "hash": data_hash,
    "signature": signed_hash,
    "verify_key": verify_key,
    "prev_hash": "0" * 64,  # Genesis block has no previous hash
    "timestamp": time.time(),
    "sequence": 1
}

doc_id = collection.insert_one(genesis_document).inserted_id
print(f"✅ Inserted genesis document with ID: {doc_id}")
print("Genesis block created successfully!")