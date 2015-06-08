package com.example.geosave.com.example.geosave.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.geosave.R;
import com.example.geosave.com.example.geosave.services.BackgroundWaitService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class WaitActivity extends ActionBarActivity {


    private ImageView logo;
    private LinearLayout layout;
    private TextView available;
    private TextView info;
    private TextView dispo;
    private boolean status;
    private Vibrator vib;
    private String pseudo;
    private SocketIO socket;
    private Handler handler;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        //Get datas from previous activity
        Intent t = getIntent();
        pseudo = t.getStringExtra("pseudo");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        dispo = (TextView) findViewById(R.id.dispo_message);
        logo = (ImageView) findViewById(R.id.dispo_logo);
        info = (TextView) findViewById(R.id.dispo_text);
        layout = (LinearLayout) findViewById(R.id.dispo_layout);

        status = false;
        layout.setBackgroundColor(Color.parseColor("#BB473E")); //#bb473e
        dispo.setText("Vous n'êtes pas disponible");
        info.setText("Touchez le logo pour devenir disponible");

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        logo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Vibrate!
                vib.vibrate(100);

                //Toggle status
                status = !status;

                if (status) {

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showToast();
                        layout.setBackgroundColor(Color.parseColor("#2b8244")); //#2b8244
                        dispo.setText("Vous êtes disponible !");
                        info.setText("Touchez le logo pour devenir indisponible");

                        //Start BG Service
                        startBackgroundWaitService();

                    } else {
                        showGPSDisabledAlertToUser();
                        //Toggle status
                        status = !status;
                    }



                } else {
                    layout.setBackgroundColor(Color.parseColor("#BB473E"));
                    dispo.setText("Vous n'êtes pas disponible");
                    info.setText("Touchez le logo pour devenir disponible");
                    stopBackgroundWaitService();

                }
            }

        });


    }

    private void showGPSDisabledAlertToUser() {

        new AlertDialog.Builder(this)
                .setTitle("Activation de la localisation")
                .setMessage("Vous devez activer la localisation pour utiliser GéoSAVE. Votre position n'est pas communiquée à nos serveurs tant que vous n'êtes pas appelé pour une intervention")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public void jumpToAlertClass(String adresse) {

        if (status) {
            Intent appel = new Intent(getApplicationContext(), AlertActivity.class);
            appel.putExtra("adresse", adresse);
            startActivity(appel);
        }
    }


    @Override
    public void onBackPressed() {


        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Vous allez être déconnecté.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopBackgroundWaitService();
                        finish();
                    }
                })
                .show();
    }


    private void startBackgroundWaitService() {
        startService(new Intent(WaitActivity.this, BackgroundWaitService.class));
    }

    private void stopBackgroundWaitService() {
        stopService(new Intent(WaitActivity.this, BackgroundWaitService.class));
    }

    private void showToast(){
        Toast.makeText(this, "Localisation activée", Toast.LENGTH_SHORT).show();

    }

}
