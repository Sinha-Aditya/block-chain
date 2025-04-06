from fastapi import FastAPI, Request
from pydantic import BaseModel, Field
from insert_data_last_doc import add_data, verify_chain_integrity
import uvicorn
from fetch_data import fetch_all_documents
import json
from typing import Union, Dict, Any, Optional

app = FastAPI()

# test
# Define a model for the request body
# Define a flexible input model
class DataInput(BaseModel):
    data: Optional[Union[str, int, float, Dict[str, Any]]] = None
    
    # This allows additional fields outside of "data"
    class Config:
        extra = "allow"


# POST endpoint
@app.post("/store_data")
async def submit_data(request: Request):
    # Get the raw JSON to process it flexibly
    json_data = await request.json()
    
    # Process the incoming data
    received_data = json_data
    
    # Process with add_data function - this now adds timestamp directly to data
    resp = add_data(received_data)
    return resp


@app.get("/get_all_data")
async def get_all_data():
    return fetch_all_documents()


# Optional: Simple GET endpoint to test the API
@app.get("/")
async def root():
    return {"message": "Welcome to the FastAPI POST example"}


@app.get("/check_chain_integrity")
async def check_chain_integrity():
    resp = verify_chain_integrity()
    print(resp)
    if resp:
        return {"Integrity": True,
            "message": "Chain integrity verified"}
    
    return {"Integrity": False,
            "message": "Chain integrity not verified"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8011)
