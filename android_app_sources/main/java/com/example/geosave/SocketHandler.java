package com.example.geosave;

import android.util.Log;

import com.example.geosave.com.example.geosave.activities.WaitActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

/**
 * Created by thibaut on 18/01/15.
 */
public class SocketHandler implements IOCallback {

    private WaitActivity wa;

    public SocketHandler() {
    }

    ;

    public SocketHandler(WaitActivity w) {
        this.wa = w;
    }

    @Override
    public void onMessage(JSONObject json, IOAcknowledge ack) {
        try {
            System.out.println("Server said:" + json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String data, IOAcknowledge ack) {
        //System.out.println("Server said: " + data);
        //Log.v(,"","Server said " + data);

    }

    @Override
    public void onError(SocketIOException socketIOException) {
        System.out.println("an Error occured");
        socketIOException.printStackTrace();
    }

    @Override
    public void onDisconnect() {
        System.out.println("Connection terminated.");
    }

    @Override
    public void onConnect() {
        Log.v("WaitActivity", "Connection established");
    }

    @Override
    public void on(String event, IOAcknowledge ack, Object... args) {

        if (event.equals("alert")) {

            JSONObject json = null;
            try {
                json = new JSONObject(args[0].toString());

                Log.v("WaitActivity", json.getString("address"));

                // wa.jumpToAlertClass(json.getString("address"));

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // TODO Auto-generated catch block

        }
    }
}
