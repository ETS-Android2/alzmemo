package com.main.alz2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.main.alz2.lib.ALZUrl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class MenuActivity extends AppCompatActivity {
    SharedPreferences alzpref;
    Context mContext;

    private final int SEARCH_RELATIVE_PENDING = 121212;
    Drawable btnMyRelatives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        alzpref = this.getSharedPreferences("com.alz.pref", MODE_PRIVATE);
        mContext = this;

        Button btnRelations = (Button) findViewById(R.id.btnRelation);

        String name = getIntent().getExtras().getString("name");
        TextView nametxt = (TextView) findViewById(R.id.name);
        nametxt.setText(name+"!");


        Calendar c = Calendar.getInstance();
        SimpleDateFormat dfmonth = new SimpleDateFormat("MMMM");
        String month = dfmonth.format(c.getTime());
        SimpleDateFormat dfday = new SimpleDateFormat("dd");
        String day = dfday.format(c.getTime());
        SimpleDateFormat dfyear = new SimpleDateFormat("yyyy");
        String year = dfyear.format(c.getTime());

        SimpleDateFormat dftime = new SimpleDateFormat("EEEE");
        String timeFormat = dftime.format(c.getTime());

        //    // Now we display formattedDate value in TextView
        TextView txtDate = (TextView)findViewById(R.id.txtDate);
        txtDate.setText(month+" "+day+", "+year);
        TextView txtTime = (TextView)findViewById(R.id.txtTime);
        txtTime.setText(timeFormat);


        if (alzpref.getString("user_type", "").equals("patient")) {
            setTitle("Welcome, patient!");

            Calendar cal1 = Calendar.getInstance();

            Intent intent = new Intent(MenuActivity.this, PatientBluetooth.class);
            PendingIntent pintent = PendingIntent.getService(MenuActivity.this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), 120 * 1000, pintent);
        } else {
            setTitle("Welcome, relative!");
            btnRelations.setBackgroundResource(R.drawable.btnmypatients2);
        }

        Calendar cal1 = Calendar.getInstance();
        int dummyuniqueInt = new Random().nextInt(543254);

        /*
        Intent intent2 = new Intent(MenuActivity.this, EventService.class);
        intent2.setAction(Long.toString(System.currentTimeMillis()));
        PendingIntent pintent2 = PendingIntent.getService(MenuActivity.this, dummyuniqueInt, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm2.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), 300 * 1000, pintent2);*/

    }

    public void startUserPrefActivity(View view){

        Intent intent = new Intent(mContext, UserInfoActivity.class);
        intent.putExtra("user_type", alzpref.getString("user_type", ""));
        intent.putExtra("username", alzpref.getString("username", ""));
        intent.putExtra("user_id", alzpref.getString("user_id", ""));
        intent.putExtra( "SERVER", alzpref.getString(ALZUrl.ALZ_SERVER,"" ));
        startActivity(intent);
    }

    public void startManageRelationsActivity(View view){

        Intent intent = new Intent(mContext, RelationActivity.class);
        startActivity(intent);
    }

    public void startMemoryWalletActivity(View view){
        Intent intent = new Intent(mContext, MemoryAlbum.class);
        startActivity(intent);
    }

    public void openCalendar(View view){
        Intent intent = new Intent(mContext, EventActivity.class);
        startActivity(intent);
    }
}
