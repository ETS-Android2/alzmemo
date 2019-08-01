package com.main.alz2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.pojo.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryAlbum extends AppCompatActivity {

    final Context mContext = this;
    SharedPreferences alzpref;

    private ArrayList<Relation> relationList;
    MemoryAdapter adapter;
    String usertype;

    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_album);



        alzpref = this.getSharedPreferences( ALZUrl.ALZ_CONFIG ,MODE_PRIVATE);

        usertype = alzpref.getString("user_type","");
        if(alzpref.getString("user_type", "").equals("patient")){
            setTitle("Choose a relative's memory album");
        }else{
            setTitle("Choose a patient's memory album");
        }

        relationList = new ArrayList<>();

        loadRelations();
    }

    void loadRelations(){
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
                        return;
                    }

                    content = jsonRootObject.getJSONArray("data");

                    for (int i = 0; i < content.length(); i++) {
                        JSONObject item = content.getJSONObject(i);

                        Boolean isAccepted = false;
                        if( item.getString("accepted").equals("1") ){
                            isAccepted = true;
                        }

                        Relation tmp = new Relation(
                                item.getInt("relation_id"),
                                item.getString("description"),
                                item.getInt("user_id"),
                                item.getString("first_name"),
                                item.getString("middle_name"),
                                item.getString("last_name"),
                                item.getString("bluetooth_handle"),
                                item.getString("username"),
                                item.getString("user_type"),
                                item.getInt("rel_user_id"),
                                item.getString("rel_fname"),
                                item.getString("rel_mname"),
                                item.getString("rel_lname"),
                                item.getString("rel_btooth"),
                                item.getString("rel_username"),
                                item.getString("rel_usertype"),
                                isAccepted);
                        relationList.add(tmp);
                    }

                    renderAdapterList();

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
        data.put("r", ALZUrl.ALZ_FETCH_RELATIONS);
        data.put("userid", alzpref.getString("user_id", "0"));
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }

    void renderAdapterList(){
        adapter = new MemoryAdapter(this, relationList);
        ListView listview = (ListView) findViewById(R.id.relationListView);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Relation item = (Relation) parent.getAdapter().getItem(position);

                if(!item.getIsAccepted()){
                    Toast.makeText(mContext, "Relationship must be confirmed first before you can view", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(mContext, MemoryMain.class);
                intent.putExtra("relationid", item.getRelationId());
                intent.putExtra("usertype",usertype);

                Log.d(TAG,usertype);
                startActivityForResult(intent, 1);

            }
        });
    }

}
