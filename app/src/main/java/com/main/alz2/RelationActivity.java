package com.main.alz2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.lib.JSONWrapper;
import com.main.alz2.pojo.Relation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RelationActivity extends AppCompatActivity {

    final Context mContext = this;
    SharedPreferences alzpref;

    private ArrayList<Relation> relationList;
    RelationAdapter adapter;


    /*Accept and Delete vars*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        alzpref = this.getSharedPreferences( ALZUrl.ALZ_CONFIG ,MODE_PRIVATE);

        if(alzpref.getString("user_type", "").equals("patient")){
            setTitle("My Relatives");
        }else{
            setTitle("My Patients");
        }

        relationList = new ArrayList<>();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPatient = new Intent(mContext, RelationAdd.class);
                startActivityForResult(addPatient, 1);
            }
        });

        loadRelations();
    }


    void resetList(){
        this.recreate();
    }

    void renderAdapterList(){
        adapter = new RelationAdapter(this, relationList);
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

                Intent intent = new Intent(mContext, RelationForm.class);

                intent.putExtra("action", "update");

                if(alzpref.getString("user_type", "").equals("relative")) {
                    intent.putExtra("relation_id", item.getRelationId());
                    intent.putExtra("description", item.getDescription());
                    intent.putExtra("user_id", item.getUserId());
                    intent.putExtra("related_user_id", item.getRelatedUserId());
                    intent.putExtra("username", item.getRelatedUsername());
                    intent.putExtra("first_name", item.getRelatedUserFName());
                    intent.putExtra("middle_name", item.getRelatedUserMName());
                    intent.putExtra("last_name", item.getRelatedUserLName());
                    intent.putExtra("bluetooth_handle", item.getRelatedBluetoothHandle());
                    intent.putExtra("related_user_type", item.getRelatedUserType());
                    intent.putExtra("isPatient", false);
                }else{
                    intent.putExtra("relation_id", item.getRelationId());
                    intent.putExtra("description", item.getDescription());
                    intent.putExtra("user_id", item.getUserId());
                    intent.putExtra("related_user_id", item.getRelatedUserId());
                    intent.putExtra("username", item.getUsername());
                    intent.putExtra("first_name", item.getUserFName());
                    intent.putExtra("middle_name", item.getUserMName());
                    intent.putExtra("last_name", item.getUserLName());
                    intent.putExtra("bluetooth_handle", item.getuBluetoothHandle());
                    intent.putExtra("related_user_type", item.getUserType());
                    intent.putExtra("isPatient", true);
                }



                startActivityForResult(intent, 1);

            }
        });
    }

    void loadRelations(){
        final CallBack callback = new CallBack() {
            @Override
            public void onProgress() {}

            @Override
            public void onResult(String result) {
                JSONArray content = null;
                try
                {
                    JSONObject jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("message").equals("success"))
                    {
                        return;
                    }
                    content = jsonRootObject.getJSONArray("data");
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject item = content.getJSONObject(i);
                        Boolean isAccepted = false;
                        if( item.getString("accepted").equals("1") )
                        {
                            isAccepted = true;
                        }
                        Relation tmp = new Relation(item.getInt("relation_id"), item.getString("description"), item.getInt("user_id"), item.getString("first_name"), item.getString("middle_name"), item.getString("last_name"), item.getString("bluetooth_handle"), item.getString("username"), item.getString("user_type"), item.getInt("rel_user_id"), item.getString("rel_fname"), item.getString("rel_mname"), item.getString("rel_lname"), item.getString("rel_btooth"), item.getString("rel_username"), item.getString("rel_usertype"), isAccepted);
                        relationList.add(tmp);
                    }
                    renderAdapterList();
                }catch(JSONException e)
                {}
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
        data.put("userid", alzpref.getString("user_id", "0"));
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }

    public void onClickDeleteRelation(View view){
        Button btn = (Button) view;
        final String selectedRelationId = btn.getTag().toString();

        DialogInterface.OnClickListener dialogRelationDelete = new DialogInterface.OnClickListener() {

            String relationId = selectedRelationId;

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        doAction( relationId ,ALZUrl.ALZ_DELETE_RELATION );
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to delete this relationship?").setPositiveButton("Yes", dialogRelationDelete)
                .setNegativeButton("No", dialogRelationDelete).show();
    }



    public void onClickConfirmRelation(View view){
        Button btn = (Button) view;
        final String selectedRelationId = btn.getTag().toString();

        DialogInterface.OnClickListener dialogRelationConfirm = new DialogInterface.OnClickListener() {
            String relationId = selectedRelationId;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        doAction( relationId ,ALZUrl.ALZ_ACCEPT_RELATION );
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Do you want to confirm this relationship?").setPositiveButton("Yes", dialogRelationConfirm)
                .setNegativeButton("No", dialogRelationConfirm).show();
    }


    void doAction(String relationId, String yiiURL){
        final CallBack callback = new CallBack() {
            @Override
            public void onProgress() {}

            @Override
            public void onResult(String result) {
                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
                JSONWrapper dataObj = null;
                try
                {
                    JSONObject  jsonRootObject = new JSONObject(result);
                    if (!jsonRootObject.getString("response").equals("success"))
                    {
                        return;
                    }
                    resetList();
                }catch (JSONException e){}
            }
            @Override
            public void onCancel() {}
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL;
        JSONObject getData = new JSONObject();
        HashMap<Object, Object> data = new HashMap<>();
        data.put("r", yiiURL);
        data.put("relationid", relationId);
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        this.resetList();
    }
}
