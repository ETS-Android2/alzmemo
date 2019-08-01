package com.main.alz2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.main.alz2.lib.ALZUrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mastertabs on 2/15/2016.
 */
public class EventSummary extends Activity{
    SharedPreferences sharedPref;
    final Context mContext = this;
    private TextView title,date,loc,fromUser,reminderSummary;
    private Button editBtn;
    public final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_summary);

        Bundle bundle = this.getIntent().getExtras();

        sharedPref = this.getSharedPreferences("com.alz.pref", Context.MODE_PRIVATE);

        final int eventId = bundle.getInt("event_id");
        final String detail = bundle.getString("detail");
        final String to = bundle.getString("to");
        final String from = bundle.getString("from");
        final String eventDate = bundle.getString("startDate");
        final String location = bundle.getString("location");
        final int reminder = bundle.getInt("reminder");
        String rem;

        switch (reminder)
        {
            case 0:
                rem=(("On time"));
                break;
            case 1:
               rem=(("1 min before"));
                break;
            case 5:
                rem=(("5 mins before"));
                break;
            case 10:
                rem=(("10 mins before"));
                break;
            case 15:
                rem=(("15 mins before"));
                break;
            case 20:
                rem=(("20 mins before"));
                break;
            case 25:
                rem=(("25 mins before"));
                break;
            case 30:
                rem=(("30 mins before"));
                break;
            case 45:
                rem=(("45 mins before"));
                break;
            case 60:
                rem=(("1 hour before"));
                break;
            case 120:
                rem=(("2 hours before"));
                break;
            case 180:
                rem=(("3 hours before"));
                break;
            case 1440:
                rem=(("1 day before"));
                break;
            case 2880:
                rem=(("2 days before"));
                break;
            case 10080:
                rem=(("1 week before"));
                break;
            default:
                rem=(("On time"));
        }


        title = (TextView)findViewById(R.id.summaryTitle);
        date = (TextView) findViewById(R.id.summaryDateTime);
        loc = (TextView) findViewById(R.id.summaryLocation);
        fromUser = (TextView) findViewById(R.id.summaryFrom);
        reminderSummary = (TextView) findViewById(R.id.summaryReminder);

        //DateFormat mainFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Date d = new Date(eventDate);
        //DateFormat mainFormat = new SimpleDateFormat("EEEE,MMMM dd HH:mm:ss");
        //String neweventdate = mainFormat.format(d.getTime());

        //Log.d(TAG,"new event date"+d.getTime() );


        title.setText(detail);
        date.setText(eventDate);
        loc.setText(location);
        fromUser.setText(from);
        reminderSummary.setText("Reminder : "+rem+"/Notification");

        editBtn = (Button) findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, EventForm.class);
                intent.putExtra("event_id",eventId);
                intent.putExtra("to", to);
                intent.putExtra("from", from);
                intent.putExtra("detail", detail);
                intent.putExtra("location", location);
                intent.putExtra("startDate", eventDate);
                intent.putExtra("reminder", reminder);
                intent.putExtra("isView", true);
                startActivityForResult(intent, 1);
            }
        });


    }

}
