package com.example.geosave.com.example.geosave.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.geosave.com.example.geosave.activities.WaitActivity;
import com.example.geosave.com.example.geosave.datas.AlertInfo;
import com.example.geosave.com.example.geosave.datas.Coordinates;
import com.example.geosave.R;
import com.example.geosave.com.example.geosave.activities.AlertActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class BackgroundWaitService extends Service {

    final long[] patern = {200, 500, 200, 500, 200, 500};
    SocketIO socket;
    int counter = 0;
    GPSService gps;
    MediaPlayer mediaPlayer;
    TelephonyManager tMgr;
    TextToSpeech VocalAlert;

    public BackgroundWaitService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        gps = new GPSService(this);
        VocalAlert = new TextToSpeech(BackgroundWaitService.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                VocalAlert.setLanguage(Locale.FRENCH); //Géré l'erreur
                //TODO: Handle exeption
            }
        });


        try {
            socket = new SocketIO("http://etherluminifer.ddns.net:5000");
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

                    if (s.equals("alert")) {
                        Log.v("BG", "Recu");
                        JSONObject pos = new JSONObject();


                        try {
                            Log.v("BG", objects[0].toString());
                            AlertInfo a = new AlertInfo(objects[0].toString());

                            if (AmICloseToAlert(a)) {
                                try {
                                    gps.getLocation();
                                    pos.put("lat", gps.getLatitude());
                                    pos.put("lng", gps.getLongitude());
                                    socket.emit("android_position", pos);

                                } catch (JSONException e) {
                                    socket.emit("android_position", "Erreur fatal de JSON");
                                }


                                createNotify(a);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("BG", "Erreur de JSON");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

            socket.emit("androidConnection", "bob");//TODO: changer le bob

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void onDestroy() {

        gps.stopUsingGPS();
        stopService(new Intent(this, GPSService.class));
        Log.v("BG", "DESTROYED !!");
        socket.emit("androidDisconnection", "bob");
        socket.disconnect();

    }


    private boolean AmICloseToAlert(AlertInfo a) {

        boolean ret = false;


        if (gps.canGetLocation()) { // gps enabled} // return boolean true/false


            Coordinates myPosition = new Coordinates(gps.getLatitude(), gps.getLongitude());

            if (myPosition.getDistance(a.getCoords()) < 500)
                ret = true;

        }
        return ret;
    }

    //TODO : send sms
    private void createNotify(AlertInfo a) throws IOException {



        //Light up the screen !!!



        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = pm.isScreenOn();

        if(isScreenOn==false)
        {

            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");

            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }





        //Configuration de la sonnerie
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        //Configuration du sms
        String number = "0660110243";
        String message = "Alerte : " + a.getAddress();




        Intent resultIntent = new Intent(this, AlertActivity.class);

        resultIntent.putExtra("address", a.getShortAddress());
        resultIntent.putExtra("lat", a.getCoords().getLat());
        resultIntent.putExtra("lng", a.getCoords().getLng());
        resultIntent.putExtra("myLat", gps.getLatitude());
        resultIntent.putExtra("myLng", gps.getLongitude());


        resultIntent.setAction("android.intent.action.MAIN");


        //Préparation liste des intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);


        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Configuration de la notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Alerte")
                        .setContentText(a.getShortAddress())
                        .setLights(0xFFFF0000, 500, 500)
                        .setSound(uri)
                        .setVibrate(patern);


        VocalAlert.speak(message, TextToSpeech.QUEUE_FLUSH, null);




        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(counter, mBuilder.build());
        counter++;









       /* Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);







/*
        String shortAdress;
        StringTokenizer st = new StringTokenizer(a.getAddress(),",");
        shortAdress = st.nextToken();

       // String url = "http://translate.google.com/translate_tts?ie=UTF-8&q=Alerte!" + a.getAddress() +"&tl=fr";
        String url = "http://translate.google.com/translate_tts?ie=UTF-8&q=Alerte!" + shortAdress +"&tl=fr";
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare(); // might take long! (for buffering, etc)
        mediaPlayer.start();

        //send sms
        tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tMgr.getLine1Number();
        String message = "Alerte : "+ a.getAddress();
        PendingIntent piSent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("sent_msg") , 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("delivered_msg"), 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, piSent, piDelivered);



        Intent intent = new Intent(this, Alert.class);


        Log.e("YOUHOU", "lat:" + a.getCoords().getLat());
        intent.putExtra("address", a.getAddress());
        intent.putExtra("lat", a.getCoords().getLat());
        intent.putExtra("lng", a.getCoords().getLng());


        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);


        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new Notification.Builder(this)
                .setContentTitle("Alerte !")
                .setContentText(a.getAddress())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
        //.addAction(R.drawable.logo_colorized, "Call", pIntent)
        //.addAction(R.drawable.logo_colorized, "More", pIntent)
        //.addAction(R.drawable.logo_colorized, "And more", pIntent).build();


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(counter, n);
        counter++;*/

    }


}