package com.main.alz2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;

import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by mastertabs on 2/9/2016.
 */
public class Video extends Activity{
    final Context mContext = this;
    private VideoView myVideoView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;
    private ProgressBar progressBar;
    private TextView txtPercentage;
    long totalSize=0;

    private static final String TAG = MainActivity.class.getSimpleName();
    int relationId;
    SharedPreferences alzpref;
    Bundle passedData;
    private String selectedImagePath;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // set the main layout of the activity
        setContentView(R.layout.video);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        //initialize the VideoView
        myVideoView = (VideoView) findViewById(R.id.VideoView);


        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, MODE_PRIVATE);
        passedData = this.getIntent().getExtras();

        relationId = Integer.parseInt(passedData.get("relationid").toString());

        ((Button) findViewById(R.id.browseBtn))
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Video"), 1);
                    }
                });

        //edited by marly lol
        ((Button) findViewById(R.id.videoBtn))
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        // start the image capture Intent
                        startActivityForResult(intent, 2);
                    }
                });



        try{

            String outputFileNew = ALZUrl.ALZ_HTTP;
            outputFileNew += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            outputFileNew += ALZUrl.DEFAULT_FILE_UPLOADS;
            outputFileNew += relationId+"/video.mp4";

            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //HttpURLConnection.setInstanceFollowRedirects(false)

            HttpURLConnection con =  (HttpURLConnection) new URL(outputFileNew).openConnection();
            con.setRequestMethod("HEAD");
            if( (con.getResponseCode() == HttpURLConnection.HTTP_OK) ) {
                Log.d("FILE_EXISTS", "true");

                //set the media controller buttons
                if (mediaControls == null) {
                    mediaControls = new MediaController(this);
                }


                // create a progress bar while the video file is loading
                progressDialog = new ProgressDialog(this);
                // set a title for the progress bar
                progressDialog.setTitle("Video");
                // set a message for the progress bar
                progressDialog.setMessage("Loading...");
                //set the progress bar not cancelable on users' touch
                progressDialog.setCancelable(false);
                // show the progress bar
                progressDialog.show();

                try {
                    //set the media controller in the VideoView
                    myVideoView.setMediaController(mediaControls);

                    //set the uri of the video to be played
                    myVideoView.setVideoPath(outputFileNew);
                    //myVideoView.setVideoPath("/phone/Movies/English_Only_Please__23-05-15_19-44");

                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                myVideoView.requestFocus();
                //we also set an setOnPreparedListener in order to know when the video file is ready for playback
                myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer mediaPlayer) {
                        // close the progress bar and play the video
                        progressDialog.dismiss();
                        //if we have a position on savedInstanceState, the video playback should start from here
                        myVideoView.seekTo(position);
                        if (position == 0) {
                            myVideoView.start();
                        } else {
                            //if we come from a resumed activity, video playback will be paused
                            myVideoView.pause();
                        }
                    }
                });


            }
            else
            {
                Log.d("FILE_EXISTS", "false");
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "You don't have a recording yet!", Toast.LENGTH_SHORT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1 || requestCode == 2) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.d(TAG,"URI NG VIDEO: "+selectedImageUri);


                //set the media controller buttons
                if (mediaControls == null) {
                    mediaControls = new MediaController(this);
                }


                // create a progress bar while the video file is loading
                progressDialog = new ProgressDialog(this);
                // set a title for the progress bar
                progressDialog.setTitle("Video");
                // set a message for the progress bar
                progressDialog.setMessage("Loading...");
                //set the progress bar not cancelable on users' touch
                progressDialog.setCancelable(false);
                // show the progress bar
                progressDialog.show();

                try {
                    //set the media controller in the VideoView
                    myVideoView.setMediaController(mediaControls);

                    //set the uri of the video to be played
                    myVideoView.setVideoURI(selectedImageUri);
                    //myVideoView.setVideoPath("/phone/Movies/English_Only_Please__23-05-15_19-44");

                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                myVideoView.requestFocus();
                //we also set an setOnPreparedListener in order to know when the video file is ready for playback
                myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer mediaPlayer) {
                        // close the progress bar and play the video
                        progressDialog.dismiss();
                        //if we have a position on savedInstanceState, the video playback should start from here
                        myVideoView.seekTo(position);
                        if (position == 0) {
                            myVideoView.start();
                        } else {
                            //if we come from a resumed activity, video playback will be paused
                            myVideoView.pause();
                        }
                    }
                });
                {/*
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {

                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        myVideoView.setVideoPath(selectedImagePath);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/
                }
            }
            //edited by marly loll
            /*else if (requestCode == DEL_PICTURE)
            {
                Toast.makeText(mContext, "del Successful!", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent(mContext,MenuActivity.class);
                startActivity(returnIntent);

            }
            /**********************/
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void upload(View view)
    {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = selectedImagePath;


        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "==="+System.currentTimeMillis();
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        //int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        String url1 = ALZUrl.ALZ_HTTP;
        url1 += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url1 += ALZUrl.DEFAULT_URL_UPLOADER_VIDEO;
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
            bufferSize = Math.min(bytesAvailable, 1000000000);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, 1000000000);
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

/*
    public void upload (View view)
    {
        new UploadFileToServer().execute();
    }


    //@Override
    protected void onPreExecute() {
        // setting progress bar to zero
        progressBar.setProgress(0);
        //super.onPreExecute();
    }

    protected void onProgressUpdate(Integer... progress) {
        // Making progress bar visible
        progressBar.setVisibility(View.VISIBLE);

        // updating progress bar value
        progressBar.setProgress(progress[0]);

    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
           // return null;
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            String id = "" + relationId;
            String url = ALZUrl.ALZ_HTTP;
            url += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            url += ALZUrl.DEFAULT_URL_UPLOADER_VIDEO;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(selectedImagePath);

                // Adding file data to http body
                entity.addPart("uploadedFile", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("relationId", new StringBody(id));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response

                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

                Log.d(TAG, responseString);

            } catch (ClientProtocolException e) {
                responseString = e.toString();
                Log.d(TAG, responseString);
            } catch (IOException e) {
                responseString = e.toString();
                Log.d(TAG, responseString);
            }

            return responseString;


        }
    }
    */
    /*
    public void upload(View view) {



        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL_UPLOADER_VIDEO;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            File sourceFile = new File(selectedImagePath);


            String id = ""+relationId;
            // Adding file data to http body
            entity.addPart("uploadedFile", new FileBody(sourceFile));
            entity.addPart("relationId", new StringBody(id));


            //totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                Log.d(TAG, EntityUtils.toString(r_entity));
            } else {
                Log.d(TAG,"Error occurred! Http Status Code: "
                        + statusCode);
            }

        } catch (ClientProtocolException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG,e.toString());
        }

    }
    */
/*
    public void upload(View view){
        final CallBack callback = new CallBack() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onResult(String result) {
                JSONArray content = null;
                try {

                    JSONObject jsonRootObject = new JSONObject(result);

                    if (!jsonRootObject.getString("message").equals("success")) {
                        Toast.makeText(mContext, "Error in upload", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(mContext, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result",result);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();



                }catch(JSONException e){

                }
            }

            @Override
            public void onCancel() {

            }
        };

        File sourceFile = new File(selectedImagePath);


        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL_UPLOADER_VIDEO;
       // url +="?relationId="+relationId+"&uploadedfile="+sourceFile;


        JSONObject getData = new JSONObject();
            HashMap<Object, Object> data = new HashMap<>();
            data.put("uploadedfile", sourceFile);
            data.put("relationId", relationId);
            HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext, callback, data, null, "POST");
            asyncTask.execute(url);

    }
    */

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //we use onSaveInstanceState in order to store the video playback position for orientation change
        savedInstanceState.putInt("Position", myVideoView.getCurrentPosition());
        myVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //we use onRestoreInstanceState in order to play the video playback from the stored position
        position = savedInstanceState.getInt("Position");
        myVideoView.seekTo(position);
    }
}
