package com.main.alz2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mastertabs on 2/8/2016.
 */
public class VoiceRecord extends Activity {
    final Context mContext = this;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    private Button startBtn;
    private Button stopBtn;
    private Button playBtn;
    private Button stopPlayBtn;
    private Button uploadBtn;
    private Button playNew;
    private Button stopNew;
    private TextView text;

    private File newFile;


    int relationId;
    SharedPreferences alzpref;
    Bundle passedData;


    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicerecorder);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, MODE_PRIVATE);
        passedData = this.getIntent().getExtras();

        relationId = Integer.parseInt(passedData.get("relationid").toString());
        text = (TextView) findViewById(R.id.text1);
        // store it to sd card
        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/VoiceRecord.3gpp";


        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setOutputFile(outputFile);

        Log.d(TAG, "relation ID!:   " + relationId);
        Log.d(TAG, "output FILE:   " + outputFile);

        playNew = (Button)findViewById(R.id.playnew);
        playNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                playnew(v);
            }
        });
        startBtn = (Button)findViewById(R.id.start);
        startBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                start(v);
            }
        });


        stopBtn = (Button)findViewById(R.id.stop);
        stopBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stop(v);
            }
        });


        uploadBtn = (Button)findViewById(R.id.upload);
        uploadBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                upload(v);
            }
        });

        playBtn = (Button)findViewById(R.id.play);
        playBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                play(v);
            }
        });

        /*
        stopPlayBtn = (Button)findViewById(R.id.stopPlay);
        stopPlayBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stopPlay(v);
            }
        });*/

        stopNew = (Button)findViewById(R.id.stopPlayNew);
        stopNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stopPlay(v);
            }
        });
    }



    public void start(View view){
        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

        text.setText("Recording Point: Recording");

        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();
    }

    public void upload(View view)
    {
        text.setText("Recording Point: Uploading");

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecord.3gpp";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        String url1 = ALZUrl.ALZ_HTTP;
        url1 += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url1 += ALZUrl.DEFAULT_URL_UPLOADER_RECORD;
        url1 +="?relationId="+relationId;

        try {

            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(url1);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            Log.e("Debug", "File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();
            Toast.makeText(mContext,"Upload Successful!",Toast.LENGTH_SHORT);
            text.setText("Recording Point: Uploaded");

        } catch (MalformedURLException ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }

        //------------------ read the SERVER RESPONSE
        try {

            inStream = new DataInputStream(conn.getInputStream());
            String str;

            while ((str = inStream.readLine()) != null) {

                Log.e("Debug", "Server Response " + str);

            }

            inStream.close();

        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }

    }



    public void playnew (View view)
    {
        try{

            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();
            text.setText("Recording Point: Playing");
            Toast.makeText(getApplicationContext(), "Playing new voice record...",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }
    public void play(View view) {
        try{

            String outputFileNew = ALZUrl.ALZ_HTTP;
            outputFileNew += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            outputFileNew += ALZUrl.DEFAULT_FILE_UPLOADS;
            outputFileNew += relationId+"/VoiceRecord.3gpp";

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
                Toast.makeText(this, "You dont have a recording yet!", Toast.LENGTH_SHORT);
                outputFile = Environment.getExternalStorageDirectory().
                        getAbsolutePath() + "/VoiceRecord.3gpp";
            }
            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFileNew);
            myPlayer.prepare();
            myPlayer.start();
            text.setText("Recording Point: Playing Preview");
            Toast.makeText(getApplicationContext(), "Uploaded voice record playing...",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "You dont have a recording yet!", Toast.LENGTH_SHORT);
        }
    }

    public void stopPlay(View view) {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer = null;
                text.setText("Recording Point: Stop");

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void stop(View view){
        try {
            myRecorder.stop();
            myRecorder  = null;

            text.setText("Recording Point: Stop Recording");

            Toast.makeText(getApplicationContext(), "Stop recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }
    }


}
