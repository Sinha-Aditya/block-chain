import hashlib
import json
import pymongo
import nacl.signing
import os
import time
from bson import ObjectId

# Connect to MongoDB
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["secure_db"]
collection = db["documents"]

# File containing the signing key
KEY_FILE = "signing_key.bin"

# Load existing key
if not os.path.exists(KEY_FILE):
    print("❌ Error: signing_key.bin not found. Run initialize_genesis.py first.")
    exit(1)

with open(KEY_FILE, "rb") as f:
    signing_key_bytes = f.read()
    signing_key = nacl.signing.SigningKey(signing_key_bytes)

verify_key = signing_key.verify_key.encode().hex()
print(f"Loaded signing key. Verify key: {verify_key[:8]}...")


def generate_hash(data):
    """Generate SHA-256 hash of the document data."""
    data_str = json.dumps(data, sort_keys=True)
    return hashlib.sha256(data_str.encode()).hexdigest()


def get_last_document():
    """Get the last inserted document to base the next insertion on."""
    return collection.find_one(sort=[("sequence", pymongo.DESCENDING)])


def get_document_by_sequence(seq):
    """Get a document by its sequence number."""
    return collection.find_one({"sequence": seq})


def get_document_count():
    """Get the total count of documents in the collection."""
    return collection.count_documents({})


def verify_chain_integrity():
    """Verify the integrity of the entire chain of documents."""
    # Get the total number of documents
    doc_count = get_document_count()

    # Check if there are any documents
    if doc_count == 0:
        print("❌ No documents found in the collection.")
        return False

    # Verify each document in sequence
    for seq in range(1, doc_count + 1):
        doc = get_document_by_sequence(seq)

        # Check if document exists
        if not doc:
            print(f"❌ Missing document at sequence {seq}. Chain broken.")
            return False

        # Verify the document's own hash matches its data
        computed_hash = generate_hash(doc["data"])
        if doc["hash"] != computed_hash:
            print(f"❌ Data tampered in document {seq}. Stored hash doesn't match computed hash.")
            return False

        # For all but the genesis block, verify the prev_hash
        if seq > 1:
            prev_doc = get_document_by_sequence(seq - 1)
            if not prev_doc:
                print(f"❌ Previous document at sequence {seq - 1} not found.")
                return False

            if doc["prev_hash"] != prev_doc["hash"]:
                print(f"❌ Chain broken at sequence {seq}. Previous hash doesn't match.")
                return False

    # Check for cloned data
    data_hashes = {}
    for doc in collection.find():
        data_hash = generate_hash(doc["data"])
        if data_hash in data_hashes:
            print(
                f"❌ Cloned data detected! Documents {data_hashes[data_hash]} and {doc['sequence']} contain identical data.")
            return False
        data_hashes[data_hash] = doc["sequence"]

    # Verify sequence continuity
    expected_sequence = list(range(1, doc_count + 1))
    actual_sequence = [doc["sequence"] for doc in collection.find().sort("sequence", 1)]

    if expected_sequence != actual_sequence:
        print(f"❌ Sequence mismatch: Expected {expected_sequence}, got {actual_sequence}")
        return False

    print("✅ Chain integrity verified successfully. No tampering detected.")
    return True


def insert_document(data):
    """Insert a document with cryptographic signature and link to previous document."""

    # First verify the entire chain's integrity
    if not verify_chain_integrity():
        print("⚠️ Chain integrity check failed. No transactions allowed.")
        return False

    # Check for duplicated data to prevent cloning
    data_hash = generate_hash(data)
    for doc in collection.find():
        if generate_hash(doc["data"]) == data_hash:
            print(f"⚠️ Data cloning attempt detected! This exact data already exists in document {doc['sequence']}.")
            return False

    last_doc = get_last_document()

    if last_doc is None:
        print("No initial data found. Please insert the genesis document first.")
        return False

    # Use the last document's hash as the previous hash for the new document
    prev_hash = last_doc["hash"]

    # Sign the current document's data hash
    signed_hash = signing_key.sign(data_hash.encode()).hex()

    document = {
        "data": data,
        "hash": data_hash,
        "signature": signed_hash,
        "verify_key": verify_key,
        "prev_hash": prev_hash,
        "timestamp": time.time(),
        "sequence": last_doc["sequence"] + 1
    }

    doc_id = collection.insert_one(document).inserted_id
    print(f"✅ Inserted new document with ID: {doc_id} and sequence {document['sequence']}")
    return True


# --- Main execution ---

# New data to insert
new_data = {
    "dataType": "gps",
    "identifier": "truck_114",
    "payload": {
        "latitude": 29.7041,
        "longitude": 78.1025,
        "timestamp": "2025-03-09T14:00:00"
    }
}

# Verify chain integrity before proceeding
if verify_chain_integrity():
    print("\nInserting new document...")
    insert_document(new_data)
else:
    print("\nCannot proceed with insertion due to chain integrity issues.")