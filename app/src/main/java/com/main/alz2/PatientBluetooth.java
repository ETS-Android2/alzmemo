package com.main.alz2;

import android.app.IntentService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;


public class PatientBluetooth extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    private HashMap<String, DeviceItem> deviceItemList;
    private ArrayList<Relation> relationList;
    private ArrayList<String> matchedList;
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    Handler mHandler = new Handler();
    NotificationManager mNotificationManager;

    private final int NOTIF_SEARCH = 131313;
    private final int NOTIF_CHECK_DONE = 141414;

    final BroadcastReceiver bReciever =  new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                Log.d("PatientBluetooth", "Device found " + device.getName() + " - " + device.getAddress());
                deviceItemList.put(device.getName() + "", newDevice);
                //mAdapter.add(newDevice);
            }
        }
    };



    private final int maxSecondsToScan = 20;
    long startTime;
    long elapsedTime;
    private BluetoothAdapter BTAdapter;

    SharedPreferences alzpref;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        tts = new TextToSpeech(this, this);
        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, Context.MODE_PRIVATE);
        startTime = System.nanoTime();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_menu_search);
        mBuilder.setContentTitle("AlzMemo");
        mBuilder.setContentText("Searching for nearby relatives...");
        mNotificationManager.notify(NOTIF_SEARCH, mBuilder.build());
        this.registerReceiver(bReciever, filter);
        deviceItemList = new HashMap<>();
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        BTAdapter.startDiscovery();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                elapsedTime = System.nanoTime() - startTime;
                double seconds = (double) elapsedTime / 1000000000.0;
                if (seconds > maxSecondsToScan) {
                    stopScan();
                    return;
                }

                if (Thread.interrupted()) {
                    // We've been interrupted: no more crunching.
                    stopScan();
                    return;
                }
                mHandler.postDelayed(this, 1000);
            }
        }, 1000);
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


    //

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy()
    {
        this.unregisterReceiver(bReciever);
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void stopScan(){

        if(bReciever != null) {
            try {
                this.unregisterReceiver(bReciever);
            }catch (IllegalArgumentException e){
                Log.d("weird", "weird");
            }
        }

        BTAdapter.cancelDiscovery();
        mNotificationManager.cancel(NOTIF_SEARCH);

        searchThruRelatives();
    }

    //ASYNCTask stuffs
    void searchThruRelatives()
    {
        final CallBack callback = new CallBack() {
            @Override
            public void onProgress() {}

            @Override
            public void onResult(String result) {
                JSONArray content = null;
                relationList = new ArrayList<>();
                try {
                    JSONObject jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("message").equals("success")) {
                        return;
                    }
                    content = jsonRootObject.getJSONArray("data");
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject item = content.getJSONObject(i);
                        Boolean isAccepted = false;
                        if( item.getString("accepted").equals("1") ){
                            isAccepted = true;
                        }
                        Relation tmp = new Relation(item.getInt("relation_id"), item.getString("description"), item.getInt("user_id"), item.getString("first_name"), item.getString("middle_name"), item.getString("last_name"), item.getString("bluetooth_handle"), item.getString("username"), item.getString("user_type"), item.getInt("rel_user_id"), item.getString("rel_fname"), item.getString("rel_mname"), item.getString("rel_lname"), item.getString("rel_btooth"), item.getString("rel_username"), item.getString("rel_usertype"), isAccepted);
                        relationList.add(tmp);
                    }
                    checkMatchingRelatives();

                }catch(JSONException e){}
            }

            @Override
            public void onCancel() {}
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL;
        JSONObject getData = new JSONObject();
        HashMap<Object, Object> data = new HashMap<>();
        data.put("r", ALZUrl.ALZ_FETCH_RELATIONS);
        data.put("userid", alzpref.getString("user_id", ""));
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(this,callback, data, null, "GET");
        asyncTask.execute(url);
    }

    void checkMatchingRelatives(){

        matchedList = new ArrayList<>();

        for( Relation rl : relationList ){

            Log.d("PatientBluetooth", "Checking device" + rl.getuBluetoothHandle());

            if( deviceItemList.containsKey(rl.getuBluetoothHandle()) ){
                matchedList.add( rl.getRelationId() + "," + rl.getUserFName() + " " + rl.getUserLName() +","+rl.getDescription()+","+rl.getUsername()+ "#" );
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_menu_search);
        mBuilder.setContentTitle("AlzMemo");
        mBuilder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        if( matchedList.size() == 0 ){
            mBuilder.setContentText("You have no relatives nearby. Will search again after 5 minutes");
           // speakOut("You have no relatives nearby. Will search again after a few minutes");
        }else {

            String relative;
            if(matchedList.size()>1)
            {
                relative="relatives";
            }
            else
            {
                relative="relative";
            }
            speakOut("You have " + matchedList.size() + relative+" nearby. Click to see them.");
            mBuilder.setContentText("You have " + matchedList.size() + " relative/s nearby.        Click to see them.");
            Intent resultIntent = new Intent(this, RememberActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(RememberActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            SharedPreferences.Editor editor = alzpref.edit();
            String tmp = "";
            for( String handles : matchedList){
                tmp += handles;
            }
            editor.putString("latestSearched", tmp);
            editor.commit();
        }

        mNotificationManager.notify(NOTIF_CHECK_DONE, mBuilder.build());

    }
}
