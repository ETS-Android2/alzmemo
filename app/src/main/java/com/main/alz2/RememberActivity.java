package com.main.alz2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public class RememberActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    SharedPreferences alzpref;
    String relations[];
    private TextToSpeech tts;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;
    String id;
    String name;

    private MediaPlayer myPlayer;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    int pointer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remember);


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        tts = new TextToSpeech(this, this);



        setTitle("A Relative is nearby");

        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, Context.MODE_PRIVATE);

        String latestSearchedStr = alzpref.getString("latestSearched", "");
        latestSearchedStr = latestSearchedStr.substring(0, latestSearchedStr.length() - 1);

        String[] tmp = latestSearchedStr.split("#");

        relations = new String[tmp.length];

        for(int x=0; x<tmp.length; x++) {
            relations[x] = tmp[x];
        }

        renderRelation(pointer);
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

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
               // btnSpeak.setEnabled(true);
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut(String text) {

        //String text = txtText.getText().toString();
        if(text!=null) {
            HashMap<String, String> myHashAlarm = new HashMap<String, String>();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
        }
    }

    void renderRelation(int idx){

        try {
            String relData = relations[idx];
             id = relData.split(",")[0];
            String name = relData.split(",")[1];

            Log.d(TAG, relData);
            String desc = relData.split(",")[2];
            //name+=desc;
            String username = relData.split(",")[3];

            WebView myWebView = (WebView) findViewById(R.id.relationweb);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            WebView webViewPic = (WebView) findViewById(R.id.webView);
            webViewPic.setBackgroundColor(Color.TRANSPARENT);
            WebSettings webViewPicSettings = myWebView.getSettings();
            webViewPicSettings.setJavaScriptEnabled(true);

            String url = ALZUrl.ALZ_HTTP;
            url += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            url += ALZUrl.DEFAULT_URL_UPLOADER_SLIDE;

            url += "?relationid=" + id+"&relative=" + username ;

            Log.d(TAG, url);

            myWebView.clearCache(true);
            myWebView.loadUrl(url);


            url = ALZUrl.ALZ_HTTP+alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            url += ALZUrl.DEFAULT_URL_UPLOADER_SLIDE_PIC;

            url += "?relationid=" + id+"&relative=" + username ;
            Log.d(TAG,url);
            webViewPic.clearCache(true);
            webViewPic.loadUrl(url);


            TextView nametxt = (TextView) findViewById(R.id.relationName);
            TextView desctxt = (TextView) findViewById(R.id.relationDesc);
            desctxt.setText("Described you as: "+desc);
            nametxt.setText(name);

            //tts

            //speakOut(name+" described you as "+ desc + " is nearby. Do you remember this person?");
            //


        }catch(Exception e){
            Toast.makeText(this, "No more relatives in queue. Will search again later.", Toast.LENGTH_LONG).show();
            finish();
        }


    }

    public void onYes(View view){
        pointer++; renderRelation(pointer); }

    public void onNo(View view){

         name = ((TextView)findViewById(R.id.relationName)).getText().toString();
        String desc = ((TextView)findViewById(R.id.relationDesc)).getText().toString();

        speakOut(name+"           "+ desc + " is nearby.      Do you remember this person?       Here is a message from this person");

        //speakOut("SAMPLE TEXT");
       // pointer++; renderRelation(pointer);

        try{


            String outputFileNew = ALZUrl.ALZ_HTTP;
            outputFileNew += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            outputFileNew += ALZUrl.DEFAULT_FILE_UPLOADS;
            outputFileNew += id+"/VoiceRecord.3gpp";

            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //HttpURLConnection.setInstanceFollowRedirects(false)

            HttpURLConnection con =  (HttpURLConnection) new URL(outputFileNew).openConnection();
            con.setRequestMethod("HEAD");
            if( (con.getResponseCode() == HttpURLConnection.HTTP_OK) ) {
                Log.d("FILE_EXISTS", "true");
            }
            else
            {
                Log.d("FILE_EXISTS", "false");
                Toast.makeText(this, "You don't have a recording yet!", Toast.LENGTH_SHORT);
            }
            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFileNew);
            myPlayer.prepare();

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "DONE!:" + utteranceId);
                    myPlayer.start();
                    Toast.makeText(getApplicationContext(), name + "'s voice record playing...",
                            Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onError(String utteranceId) {

                }
            });




        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "You don't have a recording yet!", Toast.LENGTH_SHORT);
        }
    }



}
