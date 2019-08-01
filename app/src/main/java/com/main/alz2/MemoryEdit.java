package com.main.alz2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.lib.ImageLoadTask;
import com.main.alz2.lib.JSONWrapper;
import com.main.alz2.pojo.Memory;
import com.main.alz2.pojo.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MemoryEdit extends AppCompatActivity {

    int relationId;
    final Context mContext = this;
    SharedPreferences alzpref;
    Bundle passedData;
    private Memory currentMemory;
    ImageView im1,im2,im3,im4,im5;

    String prefixUrl;
    private final int UPDATED_MEMORY = 1;


    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button vrBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_memory_edit);
        setTitle("Click to edit a Memory Image");


        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, MODE_PRIVATE);
        passedData = this.getIntent().getExtras();
        relationId = Integer.parseInt(passedData.get("relationid").toString());
        String usertype = passedData.get("usertype").toString().trim();
        Log.d(TAG, "usertyyppee  " + usertype);
        if(usertype.equals("patient"))
        {
            Log.d(TAG,"TRUE PATIENT!");
            ((Button)findViewById(R.id.btnVoiceRecord)).setVisibility(Button.GONE);
            ((Button)findViewById(R.id.btnVideo)).setVisibility(Button.GONE);
        }


        prefixUrl = ALZUrl.ALZ_HTTP;
        prefixUrl += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
        prefixUrl += ALZUrl.DEFAULT_FILE_UPLOADS;
        prefixUrl += relationId + "/";

        Log.d(TAG,"relationID:  "+relationId);
        im1 = (ImageView) findViewById(R.id.image1);
        im2 = (ImageView) findViewById(R.id.image2);
        im3 = (ImageView) findViewById(R.id.image3);
        im4 = (ImageView) findViewById(R.id.image4);
        im5 = (ImageView) findViewById(R.id.image5);

        ((Button) findViewById(R.id.btnVoiceRecord))
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        Intent intent = new Intent(mContext, VoiceRecord.class);
                        intent.putExtra("relationid", relationId);
                        startActivityForResult(intent,1);

                    }
                });
        ((Button) findViewById(R.id.btnVideo))
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        Intent intent = new Intent(mContext, Video.class);
                        intent.putExtra("relationid", relationId);
                        startActivityForResult(intent,1);

                    }
                });

        loadMemories();
    }

    public void editMemory(View view){

        ImageView tmp = (ImageView) view;

        Intent intent = new Intent(mContext, MemoryUpload.class);
        intent.putExtra("relationid", relationId);
        //intent.putExtra("imageNumber","");
        intent.putExtra("imageNumber", tmp.getTag(R.id.IMAGE_NUM).toString());
        //intent.putExtra("imageName","");
        intent.putExtra("imageName", tmp.getTag(R.id.IMAGE_NAME).toString());
        startActivityForResult(intent, UPDATED_MEMORY);
    }



    private void renderMemoryList() {

        //Call this process 5 times.
        for(int idx=1; idx<6; idx++){
            getMemoryImage(idx).setTag(R.id.IMAGE_NUM,idx);
            getMemoryImage(idx).setTag(R.id.IMAGE_NAME, currentMemory.getMemory(idx));
            if( currentMemory.getMemory(idx).equals("null") || currentMemory.getMemory(idx).length() <= 0 ){
                getMemoryImage(idx).setImageResource(R.drawable.emptygallery);
                Log.d(TAG,"current memory:"+currentMemory);
            }else{
                new ImageLoadTask(prefixUrl + currentMemory.getMemory(idx), getMemoryImage(idx)).execute();


            }

        }
    }


    public ImageView getMemoryImage(int index){
        switch(index){
            case 1: return this.im1;
            case 2: return this.im2;
            case 3: return this.im3;
            case 4: return this.im4;
            default: return this.im5;
        }
    }

    void loadMemories()
    {
        final CallBack callback = new CallBack() {
        @Override
        public void onProgress() {}
            @Override
            public void onResult(String result) {
                JSONWrapper dataObj = null;
                try
                {
                    JSONObject jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("message").equals("success"))
                    {
                        return;
                    }
                    dataObj = new JSONWrapper( jsonRootObject.getString("data") );
                    Memory tmp = new Memory(Integer.parseInt(dataObj.getStringOrBlank("memory_id")), Integer.parseInt(dataObj.getStringOrBlank("relation_id")), dataObj.getStringOrBlank("file_1"), dataObj.getStringOrBlank("file_2"), dataObj.getStringOrBlank("file_3"), dataObj.getStringOrBlank("file_4"), dataObj.getStringOrBlank("file_5"));
                    currentMemory = tmp;
                    renderMemoryList();
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
        data.put("r", ALZUrl.ALZ_MEMORY_FETCH);
        data.put("relationid", relationId);
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == UPDATED_MEMORY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                loadMemories();
            }
        }
    }

}

