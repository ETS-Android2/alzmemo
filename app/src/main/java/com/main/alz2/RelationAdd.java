package com.main.alz2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.pojo.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class RelationAdd extends AppCompatActivity {
    final Context mContext = this;
    SharedPreferences alzpref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation_add);

        setTitle("Add Patient");
        alzpref = this.getSharedPreferences( ALZUrl.ALZ_CONFIG ,MODE_PRIVATE);
    }

    public void addRelationship(View view) throws UnsupportedEncodingException
    {
        TextView username = (TextView)findViewById(R.id.relationUsername);
        TextView description = (TextView)findViewById(R.id.relationDesc);
        final CallBack callback = new CallBack()
        {
            @Override
            public void onProgress() {}

            @Override
            public void onResult(String result)
            {
                JSONArray content = null;
                try
                {
                    JSONObject jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("response").equals("success")) {
                        Toast.makeText(mContext, jsonRootObject.getString("data"), Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent data = new Intent();
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }catch(JSONException e)
                {}
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
        data.put("r", ALZUrl.ALZ_ADD_RELATION);
        data.put("userid", alzpref.getString("user_id", "0"));
        data.put("relativeusername", username.getText().toString());
        data.put("description", URLEncoder.encode(description.getText().toString(), "utf-8"));
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }
}
