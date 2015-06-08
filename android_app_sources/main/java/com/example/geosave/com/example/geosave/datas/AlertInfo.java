package com.example.geosave.com.example.geosave.datas;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;

/**
 * Created by thibaut on 21/01/15.
 */
public class AlertInfo {

    private String address;
    private Coordinates coords;
    private String jsonDescription;
    private String shortAddress;


    public AlertInfo(String jsonInput) throws JSONException {

        this.jsonDescription = jsonInput;
        JSONObject json = new JSONObject(jsonInput);
        this.address = json.getString("address");
        StringTokenizer st = new StringTokenizer(address, ",");
        shortAddress = st.nextToken() + ", " + st.nextToken();
        this.coords = new Coordinates(Double.parseDouble(json.getJSONObject("position").getString("lat")), Double.parseDouble(json.getJSONObject("position").getString("lng")));

    }

    public String getShortAddress() {
        return shortAddress;
    }

    public String getJsonDescription() {
        return jsonDescription;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }
}
