# retrieval_with_integrity.py

import hashlib
import json
import pymongo
import nacl.signing
import nacl.secret
import nacl.utils
import os
import time
from bson import ObjectId
from dotenv import load_dotenv

# Import specific functions from insert_data_last_doc.py
from insert_data_last_doc import (
    generate_hash,
    get_last_document,
    get_document_by_sequence,
    get_document_count,
    verify_chain_integrity,
    insert_document  # Optional, included for completeness
)

# Load environment variables and setup
load_dotenv()
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["secure_db"]
collection = db["documents"]

# Load signing key
KEY_FILE = "signing_key.bin"
if not os.path.exists(KEY_FILE):
    print("❌ Error: signing_key.bin not found. Run initialize_genesis.py first.")
    exit(1)
with open(KEY_FILE, "rb") as f:
    signing_key_bytes = f.read()
    signing_key = nacl.signing.SigningKey(signing_key_bytes)
verify_key = signing_key.verify_key.encode().hex()

# Cache setup
CACHE_FILE = "last_hash_cache.bin"
CACHE_ENCRYPTION_KEY = os.getenv("CACHE_ENCRYPTION_KEY")
if CACHE_ENCRYPTION_KEY is None:
    CACHE_ENCRYPTION_KEY = nacl.utils.random(nacl.secret.SecretBox.KEY_SIZE).hex()
    print("Generated new CACHE_ENCRYPTION_KEY")
CACHE_ENCRYPTION_KEY = bytes.fromhex(CACHE_ENCRYPTION_KEY)
secret_box = nacl.secret.SecretBox(CACHE_ENCRYPTION_KEY)


# Retrieval functions with integrity checking
def fetch_all_documents():
    """Retrieve all documents after verifying chain integrity."""
    if not verify_chain_integrity():
        print("❌ Cannot retrieve documents: Chain integrity verification failed")
        return None

    try:
        documents = list(collection.find().sort("sequence", pymongo.ASCENDING))
        result = [dict(doc, _id=str(doc['_id'])) for doc in documents]
        print(f"✅ Retrieved {len(result)} documents with verified integrity")
        return result
    except Exception as e:
        print(f"❌ Error fetching documents: {e}")
        return None


def fetch_document_by_id(doc_id):
    """Retrieve document by ID after verifying chain integrity."""
    if not verify_chain_integrity():
        print("❌ Cannot retrieve document: Chain integrity verification failed")
        return None

    try:
        doc = collection.find_one({"_id": ObjectId(doc_id)})
        if not doc:
            print(f"ℹ️ No document found with ID: {doc_id}")
            return None
        result = dict(doc, _id=str(doc['_id']))
        print(f"✅ Retrieved document {doc_id} with verified integrity")
        return result
    except Exception as e:
        print(f"❌ Error fetching document by ID: {e}")
        return None


def fetch_documents_by_data_type(data_type):
    """Retrieve documents by dataType after verifying chain integrity."""
    if not verify_chain_integrity():
        print("❌ Cannot retrieve documents: Chain integrity verification failed")
        return None

    try:
        documents = list(collection.find({"data.dataType": data_type}).sort("sequence", pymongo.ASCENDING))
        result = [dict(doc, _id=str(doc['_id'])) for doc in documents]
        print(f"✅ Retrieved {len(result)} documents of type {data_type} with verified integrity")
        return result
    except Exception as e:
        print(f"❌ Error fetching documents by dataType: {e}")
        return None


def fetch_documents_by_identifier(identifier):
    """Retrieve documents by identifier after verifying chain integrity."""
    if not verify_chain_integrity():
        print("❌ Cannot retrieve documents: Chain integrity verification failed")
        return None

    try:
        documents = list(collection.find({"data.identifier": identifier}).sort("sequence", pymongo.ASCENDING))
        result = [dict(doc, _id=str(doc['_id'])) for doc in documents]
        print(f"✅ Retrieved {len(result)} documents for identifier {identifier} with verified integrity")
        return result
    except Exception as e:
        print(f"❌ Error fetching documents by identifier: {e}")
        return None


def fetch_latest_document():
    """Retrieve latest document after verifying chain integrity."""
    if not verify_chain_integrity():
        print("❌ Cannot retrieve document: Chain integrity verification failed")
        return None

    try:
        doc = get_last_document()  # Using imported function
        if not doc:
            print("ℹ️ No documents in collection")
            return None
        result = dict(doc, _id=str(doc['_id']))
        print(f"✅ Retrieved latest document (sequence {result['sequence']}) with verified integrity")
        return result
    except Exception as e:
        print(f"❌ Error fetching latest document: {e}")
        return None


# Example usage
if __name__ == "__main__":
    # # Optional: Insert a test document
    # data = {"sex": "male"}
    # new_data = {"data": data}
    # new_data.update({"timestamp": time.time()})
    # insert_document(new_data)

    # Test retrieval functions
    # print("\nFetching all documents:")
    # all_docs = fetch_all_documents()
    # if all_docs:
    #     print(f"First document: {json.dumps(all_docs[0], indent=2)}")

    print("\nFetching latest document:")
    latest = fetch_latest_document()
    if latest:
        print(f"Latest: {json.dumps(latest, indent=2)}")

    print("\nFetching by dataType 'gps':")
    gps_docs = fetch_documents_by_data_type("gps")
    if gps_docs and len(gps_docs) > 0:
        print(f"First GPS doc: {json.dumps(gps_docs[0], indent=2)}")

    # Test with a known ID if you have one
    # doc = fetch_document_by_id("67c6024858ae3c69c76cdbb0")