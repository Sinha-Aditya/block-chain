from fastapi import FastAPI
from pydantic import BaseModel
from insert_data_last_doc import add_data
import uvicorn
from fetch_data import fetch_all_documents
import json
from typing import Union, Dict, Any

app = FastAPI()


# Define a model for the request body
# Define a flexible input model
class DataInput(BaseModel):
    data: Union[str, int, float, Dict[str, Any]]  # Accepts string, int, float, or JSON object

    # Optional: Add a method to handle the data type conversion if needed
    def get_processed_data(self):
        if isinstance(self.data, dict):
            return json.dumps(self.data)  # Convert dict to JSON string
        return str(self.data)  # Convert everything else to string

# POST endpoint
@app.post("/store_data")
async def submit_data(input_data: DataInput):
    # Access the key and data from the request
    received_data = input_data.data

    resp = add_data(received_data)
    # Process the data as needed
    # response = {
    #     "received_data": received_data,
    #     "message": "Data received successfully"
    # }

    return resp

@app.get("/get_all_data")
async def get_all_data():
    return fetch_all_documents()

# Optional: Simple GET endpoint to test the API
@app.get("/")
async def root():
    return {"message": "Welcome to the FastAPI POST example"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8011)