package com.main.alz2;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.lib.JSONWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends Activity {
    SharedPreferences alzpref;
    Context mContext;
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        alzpref = this.getSharedPreferences("com.alz.pref",MODE_PRIVATE);

        mContext = this;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(mContext,"Device does not support Bluetooth",Toast.LENGTH_SHORT);
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,1);
        }


    }

    public void openRegistration(View view){
        Intent intent = new Intent(mContext, RegisterActivity.class);
        startActivity(intent);
    }

    //edited by marly for shared pref login
    @Override
    protected void onResume() {
        super.onResume();
        //In onresume fetching value from sharedpreference
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);

        //Fetching the boolean value form sharedpreferences
        loggedIn = sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false);

        //If we will get true
        if(loggedIn){
            //We will start the Profile Activity
            Intent intent = new Intent(mContext, MenuActivity.class);
            startActivity(intent);
        }
    }
    //


    public void startLogin(View view) throws JSONException {
        EditText username = (EditText) findViewById(R.id.txtUsername);
        EditText password = (EditText) findViewById(R.id.txtPassword);
        final CallBack callback = new CallBack() {
            @Override
            public void onProgress() {}
            @Override
            public void onResult(String result) {
                JSONWrapper dataObj = null;
                try
                {
                    JSONObject  jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("response").equals("success"))
                    {
                        return;
                    }
                    dataObj = new JSONWrapper( jsonRootObject.getString("data") );

                    SharedPreferences sharedPref = alzpref;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user_id", dataObj.getStringOrBlank("user_id"));
                    editor.putString("username", dataObj.getStringOrBlank("username"));
                    editor.putString("password", dataObj.getStringOrBlank("password"));
                    editor.putString("first_name", dataObj.getStringOrBlank("first_name"));
                    editor.putString("middle_name", dataObj.getStringOrBlank("middle_name"));
                    editor.putString("last_name", dataObj.getStringOrBlank("last_name"));
                    editor.putString("bluetooth_handle", dataObj.getStringOrBlank("bluetooth_handle"));
                    editor.putString("user_type", dataObj.getStringOrBlank("user_type"));
                    editor.commit();

                    Intent intent = new Intent(mContext, MenuActivity.class);
                    intent.putExtra("name",dataObj.getStringOrBlank("first_name"));
                    startActivity(intent);
                }catch (JSONException e) {}
            }
            @Override
            public void onCancel() {}
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL;
        JSONObject getData = new JSONObject();
        HashMap<Object, Object> data = new HashMap<>();
        data.put("r", ALZUrl.ALZ_LOGIN);
        data.put("username", username.getText());
        data.put("password", password.getText());
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }


    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }
    //

    public void updateServer(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input server address");

// Set up the input
        final EditText input = new EditText(this);

        input.setText( alzpref.getString(ALZUrl.ALZ_SERVER,"") );
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = alzpref.edit();
                editor.putString(ALZUrl.ALZ_SERVER, input.getText().toString());
                editor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}


