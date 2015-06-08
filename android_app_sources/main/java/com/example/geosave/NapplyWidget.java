package com.example.geosave;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

//Tuto pour widget
//http://www.miximum.fr/creer-un-widget-pour-android-exemples-et-bonnes-pratiques.html

public class NapplyWidget extends AppWidgetProvider {
    //Nom de l'intent à lancer lors du clique sur le widget
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.SHOW_NOTIFICATION";

    /**
     * Update the widget
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prepare la vue du widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.napply_widget_layout);
        // views.setTextViewText(R.id.nap_time, "Devenir indisponible");
        // views.setTextViewText(R.id.widget_dispo, "Disponible");
        Log.e("update", "Update du widget");


        /*Si l'utilisateur est connecté, update du widget
        if(status)
        {
            Log.e("update", "statut reçu = true");
        }
        //S'il ne l'est pas
        else
        {
            Log.e("update", "statut reçu = false");
        }

*/
        // Prepare l'intent à lancer au clique
        Intent intent = new Intent(context, NapplyWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction(ACTION_SHOW_NOTIFICATION);

        // Lancement de l'intent lors du clique
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.napply_widget, pendingIntent);


        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;


        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Handle new messages
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);


        if (ACTION_SHOW_NOTIFICATION.equals(intent.getAction())) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.napply_widget_layout);


            showNotification(context);
        }
    }

    /**
     * Displays a notification message
     *
     * @param context
     */
    protected void showNotification(Context context) {
        CharSequence message = "Changement de statut...";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();

    }
}