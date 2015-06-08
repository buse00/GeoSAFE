package com.example.geosave.com.example.geosave.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.geosave.R;
import com.example.geosave.RequestTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


public class MainActivity extends Activity {

    TextView t = null;
    Button b = null;
    EditText c_pseudo = null;
    EditText c_pw = null;
    ProgressDialog spinner;

    private static String encryptPassword(String password) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout
        setContentView(R.layout.activity_connexion);

        //Get the objects...
        // t = (TextView) findViewById(R.id.message_accueil);
        b = (Button) findViewById(R.id.button_connexion);
        c_pseudo = (EditText) findViewById(R.id.champ_utilisateur);
        c_pw = (EditText) findViewById(R.id.champ_mdp);

        // t.setText("GéoSave");

        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                connect();

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * NEW ***
     *
     * @Override protected void onResume() {
     * <p/>
     * super.onResume();
     * this.onCreate(null);
     * }
     */


    public void connect() {


        //Test
        spinner = new ProgressDialog(MainActivity.this);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setMessage("Connexion en cours...");
        spinner.setIndeterminate(true);
        spinner.setCanceledOnTouchOutside(false);
        spinner.show();


        //Disable connexion button
        //this.b.setEnabled(false);


        String sentPseudo = c_pseudo.getText().toString();
        //sentPseudo = sentPseudo.replaceAll("\\s+",""); //Remove spaces

        Log.e("PSEUDO :", sentPseudo);
        sentPseudo =  URLEncoder.encode(sentPseudo); //Url encode

         RequestTask r = new RequestTask(this);
         r.execute("http://etherluminifer.ddns.net:5000/connexion/" + sentPseudo + "/" + encryptPassword(c_pseudo.getText().toString() + c_pw.getText().toString() + "gs2015"));


        //localhost
        //  r.execute("http://192.168.0.12:5000/connexion/" + c_pseudo.getText().toString() + "/" + encryptPassword(c_pseudo.getText().toString() + c_pw.getText().toString() + "gs2015"));

    }

    public void displayConnectionFail(String message) {


        //REEnable button
        b.setEnabled(true);

        //Dismiss spinner
        spinner.dismiss();

        new AlertDialog.Builder(this)
                .setTitle("Echec de connexion")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }

    public void jumpToWaitClass() {

        Intent appel = new Intent(getApplicationContext(), WaitActivity.class);
        appel.putExtra("pseudo", c_pseudo.getText().toString());
        Log.v("MainActivity", "pseudo :" + c_pseudo.getText().toString());


        //Test
        spinner.dismiss();
        b.setEnabled(true);
        c_pw.setText("");

        startActivity(appel);


    }

    public MainActivity returnThis() {
        return this;
    }


}
