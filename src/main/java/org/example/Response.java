package org.example;

import org.json.JSONObject;

public class Response {
//    private String data;
    private JSONObject data;
    public void setData(JSONObject Data) {
        this.data = Data;
    }

    public JSONObject getData() {
        System.out.println(data);
        return data;
    }

}
