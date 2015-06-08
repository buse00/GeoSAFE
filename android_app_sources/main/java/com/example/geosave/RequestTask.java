package com.example.geosave;

import android.os.AsyncTask;

import com.example.geosave.com.example.geosave.activities.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class RequestTask extends AsyncTask<String, String, String> {


    private MainActivity ma;

    public RequestTask(MainActivity context) {
        this.ma = context;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;


        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());

            }
        } catch (ClientProtocolException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //DEAL WITH RESPONSE
        if (result != null) { //Prevent from nullPointer exception
            //TODO: Use try-catch


            if (result.equals("youhouend")) {
                this.ma.jumpToWaitClass();
            } else {
                this.ma.displayConnectionFail("Merci de vérifier votre mot de passe");
            }

        }

        //displayFail();

    }


    private void displayFail() {
        this.ma.displayConnectionFail("Impossible de joindre le serveur");
    }


}