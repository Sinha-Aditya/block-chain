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
# test
# Load environment variables from .env file
load_dotenv()

# Connect to MongoDB
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

# Cache file and encryption setup
CACHE_FILE = "last_hash_cache.bin"
CACHE_ENCRYPTION_KEY = os.getenv("CACHE_ENCRYPTION_KEY")
if CACHE_ENCRYPTION_KEY is None:
    CACHE_ENCRYPTION_KEY = nacl.utils.random(nacl.secret.SecretBox.KEY_SIZE).hex()
    set_key(".env", "CACHE_ENCRYPTION_KEY", CACHE_ENCRYPTION_KEY)
    print("Generated and saved new CACHE_ENCRYPTION_KEY to .env")
CACHE_ENCRYPTION_KEY = bytes.fromhex(CACHE_ENCRYPTION_KEY)
secret_box = nacl.secret.SecretBox(CACHE_ENCRYPTION_KEY)

# Existing functions
def generate_hash(data):
    data_str = json.dumps(data, sort_keys=True)
    return hashlib.sha256(data_str.encode()).hexdigest()

def get_last_document():
    return collection.find_one(sort=[("sequence", pymongo.DESCENDING)])

def get_document_by_sequence(seq):
    return collection.find_one({"sequence": seq})

def get_document_count():
    return collection.count_documents({})

def verify_chain_integrity():
    doc_count = get_document_count()
    if doc_count == 0:
        print("❌ No documents found in the collection.")
        return False

    for seq in range(1, doc_count + 1):
        doc = get_document_by_sequence(seq)
        if not doc:
            print(f"❌ Missing document at sequence {seq}. Chain broken.")
            return False
        computed_hash = generate_hash(doc["data"])
        if doc["hash"] != computed_hash:
            print(f"❌ Data tampered in document {seq}.")
            return False
        if seq > 1:
            prev_doc = get_document_by_sequence(seq - 1)
            if doc["prev_hash"] != prev_doc["hash"]:
                print(f"❌ Chain broken at sequence {seq}. Previous hash mismatch.")
                return False

    last_doc = get_last_document()
    if not last_doc:
        print("❌ Last document not found.")
        return False

    computed_last_hash = generate_hash(last_doc["data"])
    if not os.path.exists(CACHE_FILE):
        print("❌ Cache file missing. Possible tampering.")
        return False

    try:
        with open(CACHE_FILE, "rb") as f:
            encrypted_hash = f.read()
        cached_hash = secret_box.decrypt(encrypted_hash).decode()
        if cached_hash != computed_last_hash:
            print("❌ Last document tampered. Cached hash does not match computed hash.")
            return False
    except Exception as e:
        print(f"❌ Cache verification failed: {e}")
        return False

    print("✅ Chain integrity verified successfully, including last document.")
    return True

def insert_document(data):
    """Insert a document and check for duplicates based on data and document timestamp."""
    if not verify_chain_integrity():
        print("⚠️ Chain integrity check failed. No transactions allowed.")
        return False

    last_doc = get_last_document()
    if last_doc is None:
        print("No initial data found. Insert genesis document first.")
        return False

    # Create the new document (without inserting yet)
    data_hash = generate_hash(data)
    prev_hash = last_doc["hash"]
    signed_hash = signing_key.sign(data_hash.encode()).hex()
    timestamp = time.time()  # This is the timestamp you're referring to (e.g., 1741029708.666158)

    new_document = {
        "data": data,
        "hash": data_hash,
        "signature": signed_hash,
        "verify_key": verify_key,
        "prev_hash": prev_hash,
        "timestamp": timestamp,
        "sequence": last_doc["sequence"] + 1
    }

    # Check for duplicates based on data and document timestamp
    for doc in collection.find():
        if (generate_hash(doc["data"]) == data_hash and
            abs(doc["timestamp"] - timestamp) < 1e-6):  # Check the document's timestamp
            print(f"⚠️ Duplicate data and timestamp ({timestamp}) detected in document {doc['sequence']}. Possible tampering.")
            return False

    # If no duplicates, insert the document
    doc_id = collection.insert_one(new_document).inserted_id
    print(f"✅ Inserted new document with ID: {doc_id} and sequence {new_document['sequence']}")

    # Update encrypted cache
    encrypted_hash = secret_box.encrypt(data_hash.encode())
    with open(CACHE_FILE, "wb") as f:
        f.write(encrypted_hash)
    print(f"✅ Updated last document hash in {CACHE_FILE}")

    return True

# Execute
if __name__ == "__main__":
    new_data = {
        "dataType": "gps",
        "identifier": "truck_114",
        "payload": {
            "latitude": 29.7041,
            "longitude": 78.1025,
            "timestamp": "2025-03-09T14:00:00"  # Ignored for duplicate check
        }
    }
    insert_document(new_data)
