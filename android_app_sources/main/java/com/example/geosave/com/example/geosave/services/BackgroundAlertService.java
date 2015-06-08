package com.example.geosave.com.example.geosave.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.geosave.com.example.geosave.activities.WaitActivity;
import com.example.geosave.com.example.geosave.datas.AlertInfo;
import com.example.geosave.com.example.geosave.datas.Coordinates;
import com.example.geosave.R;
import com.example.geosave.com.example.geosave.activities.AlertActivity;
import com.google.android.gms.maps.model.TileOverlay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class BackgroundAlertService extends Service implements Runnable {

    SocketIO socket;
    GPSService gps;
    JSONObject pos;
    Thread mythread;

    public BackgroundAlertService() {


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {

        gps = new GPSService(this);
        pos = new JSONObject();

        try {
            socket = new SocketIO("http://etherluminifer.ddns.net:5000");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        socket.connect(new IOCallback() {
                @Override
                public void onDisconnect() {

                }

                @Override
                public void onConnect() {

                }

                @Override
                public void onMessage(String s, IOAcknowledge ioAcknowledge) {

                }

                @Override
                public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {

                }

                @Override
                public void onError(SocketIOException e) {

                }

                //Methode de reception d'une information du serveur
                @Override
                public void on(String s, IOAcknowledge ioAcknowledge, Object... objects) {


                }
            });

            socket.emit("androidConnection", "bobFROMALERTSERVICE");//TODO: changer le bob



            mythread = new Thread(this);
            mythread.run();

     /*   try {
            pos.put("id", "BOB"); //TODO : implementer bdd pour avoir id
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

    }

    public void onDestroy() {

        gps.stopUsingGPS();
        Log.v("ALERT SERVICE ", "DESTROYED !!");
        socket.emit("androidDisconnection", "bobFROMALERTSERVICE");
        socket.disconnect();

    }

    @Override
    public void run() {

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if(gps.canGetLocation()) {

                                gps.getLocation();

                                pos.put("lat", gps.getLatitude());
                                pos.put("lng", gps.getLongitude());
                                socket.emit("updatePos", pos);
                            }
                            else{
                                pos.put("lat", "can't get location !");
                                pos.put("lng", "can't get location !");
                                socket.emit("updatePos", pos);
                            }


                    }
                    catch (JSONException e) {
                        e.printStackTrace();

                    }}
                },0,2000);


    }


}


