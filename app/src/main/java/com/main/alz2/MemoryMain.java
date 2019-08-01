package com.main.alz2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.main.alz2.lib.ALZUrl;

public class MemoryMain extends AppCompatActivity {
    final Context mContext = this;
    SharedPreferences alzpref;
    Bundle passedData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_main);

        setTitle("Memory Wallet for Relation");
        alzpref = this.getSharedPreferences( ALZUrl.ALZ_CONFIG ,MODE_PRIVATE);

        passedData = this.getIntent().getExtras();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        WebView myWebView = (WebView) findViewById(R.id.galleryweb);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL_UPLOADER_SLIDE;

        url += "?relationid=" + passedData.get("relationid");

        myWebView.clearCache(true);
        myWebView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memorymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_upload:
                Intent intent = new Intent(mContext, MemoryEdit.class);
                intent.putExtra("relationid",passedData.get("relationid").toString());
                intent.putExtra("usertype",passedData.get("usertype").toString());
                startActivity(intent);;
                return true;
            default:
                return true;
        }
    }
}
