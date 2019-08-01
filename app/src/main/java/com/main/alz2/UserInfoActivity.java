package com.main.alz2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.main.alz2.lib.ALZUrl;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Jeff on 12/28/2015.
 */
public class UserInfoActivity extends PreferenceActivity {

    @Override


    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        MyPreferenceFragment frag = new MyPreferenceFragment();
        frag.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        private String SERVER_URL = "";
        private String USER_ID = "";

        UpdateDocInfo taskUpdate;
        private boolean isUpdating = false;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(ALZUrl.ALZ_CONFIG);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
            addPreferencesFromResource(R.xml.preference);



            Bundle bundle = this.getArguments();
            String userType = bundle.getString("user_type");
            final String username = bundle.getString("username");
            String imageString = bundle.getString("image");
            USER_ID = bundle.getString("user_id");
            SERVER_URL = bundle.getString("SERVER");

            Preference usernamePref = (Preference) findPreference("username");
            usernamePref.setTitle(username);
            usernamePref.setSummary("This is your non-editable username");

            Preference userIdPref = (Preference) findPreference("user_id");
            userIdPref.setTitle(USER_ID);
            userIdPref.setSummary("This is your non-editable User ID");

            Preference userTypePref = (Preference) findPreference("user_type");
            userTypePref.setTitle(userType);
            userTypePref.setSummary("This is your non-editable User Type");

            Preference button = (Preference)findPreference("button");
            button.setTitle("Display Picture");
            button.setSummary("Click to change display picture");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent profileIntent = new Intent(getActivity(),UserProfilePic.class);
                    profileIntent.putExtra("username",username);
                    startActivityForResult(profileIntent,1);
                    return true;
                }
            });


            initSummary(getPreferenceScreen());
            isUpdating = true;
        }


        // method for base64 to bitmap
        public static Bitmap decodeBase64(String input) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory
                    .decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        //

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            updatePrefSummary(findPreference(key));
        }

        private void initSummary(Preference p) {
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) p;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    initSummary(pGrp.getPreference(i));
                }
            } else {
                updatePrefSummary(p);
            }
        }

        private void updatePrefSummary(Preference p) {

            if(p.getKey().equals("username") || p.getKey().equals("patient_id")){ return; }

            if (p instanceof ListPreference) {
                ListPreference listPref = (ListPreference) p;
                p.setSummary(listPref.getEntry());
            }
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                if (p.getKey().equals("password"))
                {
                    p.setSummary("******");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
            if (p instanceof MultiSelectListPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(editTextPref.getText());
            }
            if(isUpdating){
                EditTextPreference editTextPref = (EditTextPreference) p;
                taskUpdate = new UpdateDocInfo();
                taskUpdate.execute(p.getKey(), editTextPref.getText());
            }
        }

        class UpdateDocInfo extends AsyncTask<String, Void, String > {

            private Exception exception;

            private String VARS = "";
            private String response;

            @Override
            protected String doInBackground(String... params) {
                // TODO: attempt authentication against a network service.

                HttpURLConnection con;
                String ret = "";
                JSONArray content = null;
                try {

                    VARS += "user_id=" + USER_ID + "&";
                    VARS += "key=" +  URLEncoder.encode(params[0], "utf-8") + "&";
                    VARS += "value=" + URLEncoder.encode(params[1], "utf-8");

                    con = (HttpURLConnection) new URL(ALZUrl.ALZ_HTTP + SERVER_URL + ALZUrl.DEFAULT_URL + "?r=user/infoupdate&" + VARS).openConnection();

                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                    con.setRequestProperty("Accept", "*/*");

                    InputStream resp = con.getInputStream();

                    int responseCode=con.getResponseCode();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(resp));
                    for (String line ; (line = reader.readLine()) != null;) {
                        response += line;
                    }
                    reader.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            protected void onPostExecute(String response) {
                taskUpdate =  null;
            }
        }
    }

}
