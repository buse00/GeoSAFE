package com.example.geosave.com.example.geosave.activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.geosave.com.example.geosave.datas.AlertInfo;
import com.example.geosave.com.example.geosave.datas.Coordinates;
import com.example.geosave.DownloadTask;
import com.example.geosave.R;
import com.example.geosave.com.example.geosave.services.BackgroundAlertService;
import com.example.geosave.com.example.geosave.services.BackgroundWaitService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AlertActivity extends FragmentActivity implements OnMapReadyCallback {


    private AlertInfo a;
    private TextView addressView;
    private String address;
    private Button buttonYes;
    private Button buttonNo;
    private Vibrator vib;
    private GoogleMap map;
    private LinearLayout myLayout;
    private LinearLayout layout1;
    private MapFragment mapFragment;
    private double lat;
    private double lng;
    private double myLat;
    private double myLng;


    private String API_KEY = "AIzaSyAsfBJeSZMmR3FFHTRkExoscZb9V9PLNFs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopService(new Intent(this, BackgroundWaitService.class));
        setContentView(R.layout.activity_alert);

        //Get datas from calling intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString("address");
            lat = extras.getDouble("lat");
            lng = extras.getDouble("lng");
            myLat = extras.getDouble("myLat");
            myLng = extras.getDouble("myLng");
        }


        addressView = (TextView) findViewById(R.id.textAddress);
        buttonYes = (Button) findViewById(R.id.button_go);
        buttonNo = (Button) findViewById(R.id.button_dontGo);
        myLayout = (LinearLayout) findViewById(R.id.linearLayoutMap);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        addressView.setText(address);
        mapFragment.getMapAsync(this);


        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.vibrate(100);
                myLayout.setVisibility(View.GONE);
                layout1.setWeightSum(8);
                startService(new Intent(AlertActivity.this, BackgroundAlertService.class));

            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.vibrate(100);
                deconnectionPopup();

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.map = mapFragment.getMap();
        String url = getUrl(myLat, myLng, lat, lng);
        DownloadTask downloadTask = new DownloadTask(map);
        downloadTask.execute(url);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
                .title("Alerte"))
                .showInfoWindow();


        map.addMarker(new MarkerOptions()
                .position(new LatLng(myLat, myLng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Secouriste"));

        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lng), 16, 45, (float) Coordinates.bearing(myLat, myLng, lat, lng))));


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(48.856638, 2.3100))
                .zoom(16)
                .build();

        MapFragment.newInstance(new GoogleMapOptions()
                .camera(cameraPosition));

    }


    public String getUrl(double myLat, double myLng, double lat, double lng) {
        StringBuffer urlString = new StringBuffer();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=");
        urlString.append(Double.toString(myLat));
        urlString.append(",");
        urlString.append(Double.toString(myLng));
        urlString.append("&destination=");
        urlString.append(Double.toString(lat));
        urlString.append(",");
        urlString.append(Double.toString(lng));
        urlString.append("&mode=walking&key=");
        urlString.append(API_KEY);
        return urlString.toString();
    }

    public void deconnectionPopup(){

        new AlertDialog.Builder(this)
                .setTitle("Vous allez être déconnecté")
                .setPositiveButton("Quitter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}