package com.example.model;

public class DataRequest {
    private Object data;

    public DataRequest() {}

    public DataRequest(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}