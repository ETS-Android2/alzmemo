package com.main.alz2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.pojo.DeviceItem;
import com.main.alz2.pojo.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;


public class EventService extends Service implements TextToSpeech.OnInitListener{

    private HashMap<String, DeviceItem> deviceItemList;
    private ArrayList<Relation> relationList;
    private ArrayList<String> matchedList;

    private TextToSpeech tts;

    Handler mHandler = new Handler();
    NotificationManager mNotificationManager;

    private final int NOTIF_SEARCH = 4545;
    private final int NOTIF_COMP = 5656;

    int YEAR;
    int MONTH;
    int DAY;
    final Context mContext = this;
    private String SERVER_URL = "";
    private String USER_ID = "";


    FetchEvents fetchEvents;

    SharedPreferences alzpref;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, Context.MODE_PRIVATE);

        tts = new TextToSpeech(this, this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_menu_search);
        mBuilder.setContentTitle("AlzMemo");
        mBuilder.setContentText("Checking for events today...");

        mNotificationManager.notify(NOTIF_SEARCH, mBuilder.build());

        startFetchEvents();

        return START_STICKY;
    }

    //TTS


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //btnSpeak.setEnabled(true);
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut(String text) {

        // String text = txtText.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //


    void startFetchEvents() {

        SharedPreferences sharedPref = this.getSharedPreferences("com.alz.pref", Context.MODE_PRIVATE);
        SERVER_URL = sharedPref.getString(ALZUrl.ALZ_SERVER,"localhost");
        USER_ID = sharedPref.getString("user_id", "0");

        Calendar now = Calendar.getInstance();
        //
        YEAR = now.get(Calendar.YEAR);
        MONTH = now.get(Calendar.MONTH) + 1;
        DAY = now.get(Calendar.DATE);

        if (fetchEvents != null) {
            return;
        }

        fetchEvents = new FetchEvents();
        fetchEvents.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void endFetchEvents(){

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIF_SEARCH);

        if(events.size() > 0) {

            if(events.size()==1)
            {
                speakOut("You have an upcoming event today");
            }
            else
            {
                speakOut("You have " + events.size() + " upcoming events today");
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(android.R.drawable.ic_menu_search);
            mBuilder.setContentTitle("AlzMemo");
            mBuilder.setLights(Color.BLUE, 500, 500);
            long[] pattern = {500,500,500,500,500,500,500,500,500};
            mBuilder.setVibrate(pattern);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);

            mBuilder.setContentText("You have " + events.size() + " upcoming events today. Click to view");

            Intent resultIntent = new Intent(this, EventActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(EventActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            mNotificationManager.notify(NOTIF_COMP, mBuilder.build());
        }

    }


    public ArrayList<EventMenuItem> events;

    class FetchEvents extends AsyncTask<String, Void, String> {

        private Exception exception;
        private ArrayList<EventMenuItem> eventList = new ArrayList<EventMenuItem>();


        @Override
        protected String doInBackground(String... params) {
            return getMarkers();
        }

        protected void onPostExecute(String response) {
            fetchEvents = null;
            events = eventList;
            endFetchEvents();
        }

        public String getMarkers() {
            // TODO: attempt authentication against a network service.
            HttpURLConnection con;
            String ret = "";
            JSONArray content = null;
            try {
                con = (HttpURLConnection) new URL("http://" + SERVER_URL + "/alzserver/web/?r=events/fetch&user_id="+USER_ID+"&y="+YEAR+"&m="+MONTH+"&d="+DAY).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                con.setRequestProperty("Accept", "*/*");

                InputStream response = con.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                for (String line; (line = reader.readLine()) != null; ) {
                    ret += line;
                }
                reader.close();

                try {
                    JSONObject jsonRootObject = new JSONObject(ret);
                    if (jsonRootObject.getString("message").equals("success")) {
                        content = jsonRootObject.getJSONArray("data");

                        for (int i = 0; i < content.length(); i++) {
                            JSONObject item = content.getJSONObject(i);

                            int eventId = item.getInt("event_id");
                            int reminder = item.getInt("reminder");
                            int color = item.getInt("color");
                            String toName = item.getString("to_name");
                            String fromName = item.getString("from_name");
                            String location = item.getString("location");
                            String detail = item.getString("detail");
                            String start_date = item.getString("start_date");

                            EventMenuItem clinic = new EventMenuItem(eventId,detail, toName, fromName, start_date, location,reminder,color);

                            eventList.add(clinic);

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return "success";

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "error";
        }
    }




}
