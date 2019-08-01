package com.main.alz2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class RelationForm extends AppCompatActivity {
    final Context mContext = this;
    SharedPreferences alzpref;
    Bundle passedData;

    /*Edit Vars*/
    private int editRelationId;
    private int editUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation_form);
        alzpref = this.getSharedPreferences( ALZUrl.ALZ_CONFIG ,MODE_PRIVATE);

        passedData = this.getIntent().getExtras();

        setTitle("View/Update Relationship");
        editRelationId = passedData.getInt("relation_id");
        editUserId = passedData.getInt("user_id");

        initForm();
    }

    void initForm(){

        TextView relationPersonName = (TextView) findViewById(R.id.relationPersonName);

        TextView relationDesc = (TextView) findViewById(R.id.relationDesc);
        TextView relationUsername = (TextView) findViewById(R.id.relationUsername);
        TextView bluetoothHandle = (TextView) findViewById(R.id.bluetoothHandle);
        Spinner personType = (Spinner) findViewById(R.id.relationType);

        if(passedData.getBoolean("isPatient")){
            TextView txtdesc = (TextView) findViewById(R.id.txtDesc);
            txtdesc.setText( "Person described you as" );
        }

        relationDesc.setText( passedData.getString("description") );
        relationPersonName.setText( passedData.getString("last_name") + " , " + passedData.getString("first_name") + " " + passedData.getString("middle_name") );
        relationUsername.setText(passedData.getString("username"));
        bluetoothHandle.setText(passedData.getString("bluetooth_handle"));

        int selectedType = ( passedData.getString("related_user_type").equals("patient") ? 0 : 1  );
        personType.setSelection(selectedType);

        relationPersonName.setFocusableInTouchMode(false);
        relationPersonName.setFocusable(false);

        relationUsername.setFocusableInTouchMode(false);
        relationUsername.setFocusable(false);

        personType.setFocusableInTouchMode(false);
        personType.setFocusable(false);

        bluetoothHandle.setEnabled(false);
        bluetoothHandle.setClickable(false);

        personType.setEnabled(false);
        personType.setClickable(false);

    }

    public void updateRelationship(View view) throws UnsupportedEncodingException {

        TextView description = (TextView)findViewById(R.id.relationDesc);

        final CallBack callback = new CallBack() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onResult(String result) {
                JSONArray content = null;
                try {

                    JSONObject jsonRootObject = new JSONObject(result);

                    if (!jsonRootObject.getString("response").equals("success")) {
                        Toast.makeText(mContext, jsonRootObject.getString("data"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent data = new Intent();
                    setResult(Activity.RESULT_OK, data);
                    finish();


                }catch(JSONException e){

                }
            }

            @Override
            public void onCancel() {

            }
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL;

        JSONObject getData = new JSONObject();

        HashMap<Object, Object> data = new HashMap<>();
        data.put("r", ALZUrl.ALZ_EDIT_RELATION);
        data.put("relationid", editRelationId);
        data.put("description", URLEncoder.encode(description.getText().toString(), "utf-8"));


        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }

    public void goToMemoryWallet(View view){
        Intent intent = new Intent(mContext, MemoryMain.class);
        intent.putExtra("relationid", editRelationId);
        startActivity(intent);
    }
}
