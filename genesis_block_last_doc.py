import hashlib
import json
import pymongo
import nacl.signing
import nacl.secret
import nacl.utils
import os
import time
from bson import ObjectId
from dotenv import load_dotenv, set_key

# Load environment variables from .env file
load_dotenv()

# Connect to MongoDB
client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["secure_db"]
collection = db["documents"]

KEY_FILE = "signing_key.bin"


def generate_signing_key():
    """Generate a new signing key and save it to signing_key.bin."""
    # Generate a new signing key
    signing_key = nacl.signing.SigningKey.generate()

    # Save the signing key to a binary file
    with open(KEY_FILE, "wb") as f:
        f.write(signing_key.encode())
    print(f"✅ Signing key generated and saved to {KEY_FILE}")

    # Optionally, display the verify key (public key) for reference
    verify_key = signing_key.verify_key.encode().hex()
    print(f"🔑 Verify key (public key): {verify_key}")


# Load signing key
if not os.path.exists(KEY_FILE):
    print("❌ Error: signing_key.bin not found. Run initialize_genesis.py first.")
    generate_signing_key()
    # exit(1)
with open(KEY_FILE, "rb") as f:
    signing_key_bytes = f.read()
    signing_key = nacl.signing.SigningKey(signing_key_bytes)
verify_key = signing_key.verify_key.encode().hex()

# Cache file and encryption setup
CACHE_FILE = "last_hash_cache.bin"
CACHE_ENCRYPTION_KEY = os.getenv("CACHE_ENCRYPTION_KEY")  # Get as string from .env
if CACHE_ENCRYPTION_KEY is None:
    # Generate a random key if not set, and save it to .env
    CACHE_ENCRYPTION_KEY = nacl.utils.random(nacl.secret.SecretBox.KEY_SIZE).hex()  # Hex string for storage
    set_key(".env", "CACHE_ENCRYPTION_KEY", CACHE_ENCRYPTION_KEY)
    print("Generated and saved new CACHE_ENCRYPTION_KEY to .env")
CACHE_ENCRYPTION_KEY = bytes.fromhex(CACHE_ENCRYPTION_KEY)  # Convert hex string to bytes
secret_box = nacl.secret.SecretBox(CACHE_ENCRYPTION_KEY)


def generate_hash(data):
    data_str = json.dumps(data, sort_keys=True)
    return hashlib.sha256(data_str.encode()).hexdigest()


def initialize_genesis():
    """Initialize the chain with a genesis document and set up the encrypted cache."""
    if collection.count_documents({}) > 0:
        print("⚠️ Collection already contains documents. Genesis not inserted.")
        return False

    genesis_data = {
        "dataType": "genesis",
        "identifier": "chain_start",
        "payload": {"message": "Genesis block", "timestamp": "2025-03-03T00:00:00"}
    }
    data_hash = generate_hash(genesis_data)
    signed_hash = signing_key.sign(data_hash.encode()).hex()

    genesis_doc = {
        "data": genesis_data,
        "hash": data_hash,
        "signature": signed_hash,
        "verify_key": verify_key,
        "prev_hash": "0" * 64,
        "timestamp": time.time(),
        "sequence": 1
    }

    doc_id = collection.insert_one(genesis_doc).inserted_id
    print(f"✅ Genesis document inserted with ID: {doc_id} and sequence 1")

    encrypted_hash = secret_box.encrypt(data_hash.encode())
    with open(CACHE_FILE, "wb") as f:
        f.write(encrypted_hash)
    print(f"✅ Last document hash cached in {CACHE_FILE}")

    return True


# Execute
if __name__ == "__main__":
    initialize_genesis()
