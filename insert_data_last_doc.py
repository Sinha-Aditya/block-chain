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
CACHE_ENCRYPTION_KEY = os.getenv("CACHE_ENCRYPTION_KEY")  # Get as string from .env
if CACHE_ENCRYPTION_KEY is None:
    # Generate a random key if not set, and save it to .env
    CACHE_ENCRYPTION_KEY = nacl.utils.random(nacl.secret.SecretBox.KEY_SIZE).hex()  # Hex string for storage
    set_key(".env", "CACHE_ENCRYPTION_KEY", CACHE_ENCRYPTION_KEY)
    print("Generated and saved new CACHE_ENCRYPTION_KEY to .env")
CACHE_ENCRYPTION_KEY = bytes.fromhex(CACHE_ENCRYPTION_KEY)  # Convert hex string to bytes
secret_box = nacl.secret.SecretBox(CACHE_ENCRYPTION_KEY)


# Your existing functions
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
    """Verify chain integrity, including last document against encrypted cache."""
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
    """Insert a document and update the encrypted cache."""
    if not verify_chain_integrity():
        print("⚠️ Chain integrity check failed. No transactions allowed.")
        return False

    data_hash = generate_hash(data)
    cur_time = time.time()
    time_hash = generate_hash(cur_time)
    for doc in collection.find():
        """
        {'_id': ObjectId('67c6024858ae3c69c76cdbb0'), 'data': {'dataType': 'genesis', 'identifier': 'chain_start', 'payload': {'message': 'Genesis block', 'timestamp': '2025-03-03T00:00:00'}}, 'hash': 'ecca6364f98ad8d96416351befc7a5c105f53fa86b93f3fa6175eb5b9db108ed', 'signature': '872df94ce369e6611c45a7c4b69b2b59c4fee6a3031bc8b09e0550f5ee37812e8e42f6f2d46d78bcfd5dd8fff59acfdbdd06b959eee21ac93888e93d3211f00a65636361363336346639386164386439363431363335316265666337613563313035663533666138366239336633666136313735656235623964623130386564', 'verify_key': '61f0efa54c54bd2c550a9dc87117dea054a6b480678f8d6ac417c707cedfd43b', 'prev_hash': '0000000000000000000000000000000000000000000000000000000000000000', 'timestamp': 1741029960.8737445, 'sequence': 1}

        """
        # if generate_hash(doc["data"]) == data_hash and generate_hash("timestamp") == time_hash:
        if generate_hash(doc["data"]) == data_hash:
            print(f"⚠️ Data cloning detected in document {doc['sequence']}.")
            return False

    last_doc = get_last_document()
    if last_doc is None:
        print("No initial data found. Insert genesis document first.")
        return False

    prev_hash = last_doc["hash"]
    signed_hash = signing_key.sign(data_hash.encode()).hex()

    document = {
        "data": data,
        "hash": data_hash,
        "signature": signed_hash,
        "verify_key": verify_key,
        "prev_hash": prev_hash,
        "timestamp": cur_time,
        "sequence": last_doc["sequence"] + 1
    }

    doc_id = collection.insert_one(document).inserted_id
    print(f"✅ Inserted new document with ID: {doc_id} and sequence {document['sequence']}")

    encrypted_hash = secret_box.encrypt(data_hash.encode())
    with open(CACHE_FILE, "wb") as f:
        f.write(encrypted_hash)
    print(f"✅ Updated last document hash in {CACHE_FILE}")

    return True


def add_data(data):
    try:
        new_data = {"data": data}
        # new_data.add({"timestamp": time.time()})
        new_data.update({"timestamp": time.time()})
        resp = insert_document(new_data)
        print("ADI", resp)
        if resp:
            return{"message": "Data added successfully",
                   "data": new_data}
        return {"message": "Data not added integrity error"}
    except Exception as e:
        print(e)
# Execute
if __name__ == "__main__":
    add_data({"test":1})
# new_data = {
#     "dataType": "gps",
#     "identifier": "truck_1214",
#     "payload": {
#         "latitude": 29.7041,
#         "longitude": 78.1025,
#         "timestamp": "2025-03-09T14:00:00"
#     }
# }
# new_data = {"dataType": "gps", "identifier": "truck_1214", "payload": {"latitude": 29.7041, "longitude": 78.1025}}
#
#     data = {"sex": "male"}
#     new_data = {"data": data}
#     # new_data.add({"timestamp": time.time()})
#     new_data.update({"timestamp": time.time()})
#     insert_document(new_data)
