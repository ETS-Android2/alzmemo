
    package com.main.alz2;

    import android.app.Activity;
    import android.app.DatePickerDialog;
    import android.app.Dialog;
    import android.content.ContentResolver;
    import android.content.ContentUris;
    import android.content.ContentValues;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.database.Cursor;
    import android.graphics.Color;
    import android.net.Uri;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.provider.CalendarContract;
    import android.support.design.widget.FloatingActionButton;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.Toolbar;

    import android.util.Log;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.CalendarView;
    import android.widget.DatePicker;
    import android.widget.ListView;
    import android.widget.Toast;

    import com.main.alz2.lib.ALZUrl;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.text.DateFormat;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.GregorianCalendar;
    import java.util.Locale;
    import java.util.TimeZone;
    import java.util.jar.Manifest;

    public class EventActivity extends AppCompatActivity {

        int YEAR;
        int MONTH;
        int DAY;
        final Context mContext = this;
        private String SERVER_URL = "";
        private String USER_ID = "";
        long eventID;

        int color;
        private static final String TAG = MainActivity.class.getSimpleName();

        EventMenuAdapter adapter;

        public ArrayList<EventMenuItem> events;

        FetchEvents fetchEvents;



        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == 1) {

                adapter.clear();

                startFetchEvents();

            }
        }//onActivityResult

        void startFetchEvents() {
            if (fetchEvents != null) {
                return;
            }

            fetchEvents = new FetchEvents();
            fetchEvents.execute();
        }

        void endFetchEvents(){
                adapter = new EventMenuAdapter(this, events);


                ListView listview = (ListView) findViewById(R.id.eventsList);
                listview.setAdapter(adapter);



                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        EventMenuItem item = (EventMenuItem) parent.getAdapter().getItem(position);

                        Intent intent = new Intent(mContext, EventSummary.class);
                        intent.putExtra("event_id", item.getEventId());
                        intent.putExtra("to", item.getTo());
                        intent.putExtra("from", item.getFrom());
                        intent.putExtra("detail", item.getDetail());
                        intent.putExtra("location", item.getLocation());
                        intent.putExtra("startDate", item.getEventDate());
                        intent.putExtra("reminder", item.getReminder());
                        intent.putExtra("isView", true);
                        startActivityForResult(intent, 1);

                    }
                });


        }

        /*
        void updateBtnText() throws ParseException{

            Button btn = (Button)findViewById( R.id.buttonDate );
            String string = btn.getText().toString();
            DateFormat format = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
            Date date = format.parse(string);

            DateFormat dfto = new SimpleDateFormat("EEE, MMM d, yyyy");

            btn.setText( dfto.format(date) );
        }
        */

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_event);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            final CalendarView c = (CalendarView) findViewById(R.id.calendarView1);
            c.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String currentDateandTime = sdf.format(new Date());
                    Log.d(TAG, "current time: " + currentDateandTime);


                    YEAR = year;
                    MONTH = month+1;
                    DAY = dayOfMonth;


                    adapter.clear();
                    loadEventsOfDate();


                    //addEvent(mContext);


                    //delete all events first
                    Uri eventsUri;
                    int osVersion = android.os.Build.VERSION.SDK_INT;
                    if (osVersion <= 7) { //up-to Android 2.1
                        eventsUri = Uri.parse("content://calendar/events");
                    } else { //8 is Android 2.2 (Froyo) (http://developer.android.com/reference/android/os/Build.VERSION_CODES.html)
                        eventsUri = Uri.parse("content://com.android.calendar/events");
                    }
                    ContentResolver resolver = mContext.getContentResolver();
                    deleteEvent(resolver, eventsUri, 1);
                    //


                }

            });

            c.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext,"LONG PRESS",Toast.LENGTH_SHORT);
                    Intent cIntent = new Intent(Intent.ACTION_EDIT);
                    cIntent.setType("vnd.android.cursor.item/event");
                    startActivity(cIntent);
                    return true;
                }
            });


            setTitle("My Events");

            SharedPreferences sharedPref = this.getSharedPreferences("com.alz.pref", Context.MODE_PRIVATE);
            SERVER_URL = sharedPref.getString(ALZUrl.ALZ_SERVER,"localhost");
            USER_ID = sharedPref.getString("user_id", "0");

            Calendar now = Calendar.getInstance();
            //
            YEAR = now.get(Calendar.YEAR);
            MONTH = now.get(Calendar.MONTH) + 1;
            DAY = now.get(Calendar.DATE);

            /*
            Button btn = (Button)findViewById( R.id.buttonDate );
            btn.setText( YEAR + "-" + MONTH + "-" + DAY );
            try {
                updateBtnText();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            */

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, EventForm.class);
                    intent.putExtra("isView", false);
                    startActivityForResult(intent, 1);
                }
            });

            loadEventsOfDate();
        }

        void loadEventsOfDate(){
            startFetchEvents();
        }

        public void setDate(View view){
            showDialog(999);
        }

        @Override
        protected Dialog onCreateDialog(int id) {
            // TODO Auto-generated method stub
            if (id == 999) {
                return new DatePickerDialog(this, myDateListener, YEAR, MONTH-1, DAY);
            }
            return null;
        }


        private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {

                YEAR = arg1;
                MONTH = arg2+1;
                DAY = arg3;


                /*
                Button btn = (Button)findViewById( R.id.buttonDate );
                btn.setText( YEAR + "-" + (MONTH) + "-" + DAY );
                try {
                    updateBtnText();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                */


                adapter.clear();
                loadEventsOfDate();
            }
        };



        class FetchEvents extends AsyncTask<String, Void, String> {
            private Exception exception;
            private ArrayList<EventMenuItem> eventList = new ArrayList<EventMenuItem>();
            @Override
            protected String doInBackground(String... params) {
                return getMarkers();
            }
            protected void onPostExecute(String response) {
                fetchEvents = null;
                events = eventList;
                endFetchEvents();
            }
            public String getMarkers() {
                // TODO: attempt authentication against a network service.
                HttpURLConnection con;
                String ret = "";
                JSONArray content = null;
                try {
                    con = (HttpURLConnection) new URL("http://" + SERVER_URL + "/alzserver/web/?r=events/fetch&user_id=" + USER_ID + "&y=" + YEAR + "&m=" + MONTH + "&d=" + DAY).openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                    con.setRequestProperty("Accept", "*/*");

                    InputStream response = con.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                    for (String line; (line = reader.readLine()) != null; ) {
                        ret += line;
                    }
                    reader.close();

                    try {
                        JSONObject jsonRootObject = new JSONObject(ret);
                        if (jsonRootObject.getString("message").equals("success")) {
                            content = jsonRootObject.getJSONArray("data");

                            for (int i = 0; i < content.length(); i++) {
                                JSONObject item = content.getJSONObject(i);

                                int eventId = item.getInt("event_id");
                                String toName = item.getString("to_name");
                                String fromName = item.getString("from_name");
                                String location = item.getString("location");
                                int reminder = item.getInt("reminder");
                                String detail = item.getString("detail");
                                String start_date = item.getString("start_date");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = (sdf.parse(item.getString("start_date")));

                                Calendar calDate = Calendar.getInstance();
                                ContentResolver cr = mContext.getContentResolver();
                                ContentValues values = new ContentValues();

                                if(calDate.getTimeInMillis()<date.getTime())
                                {
                                    color = Color.DKGRAY;
                                }
                                else
                                {
                                    color = Color.LTGRAY;
                                }

                                //insert to calendar
                                values.put(CalendarContract.Events.DTSTART, date.getTime());
                                values.put(CalendarContract.Events.DTEND, date.getTime());
                                values.put(CalendarContract.Events.TITLE, item.getString("detail"));
                                values.put(CalendarContract.Events.EVENT_LOCATION,item.getString("location"));
                                values.put(CalendarContract.Events.CALENDAR_ID, 1);
                                values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance()
                                        .getTimeZone().getID());
                                System.out.println(Calendar.getInstance().getTimeZone().getID());
                                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                                eventID = Long.parseLong(uri.getLastPathSegment());
                                String reminderUriString = "content://com.android.calendar/reminders";
                                ContentValues reminderValues = new ContentValues();
                                reminderValues.put("event_id", eventID);
                                // Default value of the system. Minutes is a integer
                                reminderValues.put("minutes", item.getInt("reminder"));
                                // Alert Methods: Default(0), Alert(1), Email(2), SMS(3)
                                reminderValues.put("method", 1);
                                cr.insert(Uri.parse(reminderUriString), reminderValues);
                                /****end insert to calendar***/
                                EventMenuItem clinic = new EventMenuItem(eventId, detail, toName, fromName, start_date, location, reminder, color);
                                eventList.add(clinic);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return "success";
                }catch (ParseException e)
                {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "error";
            }
        }

        private void deleteEvent(ContentResolver resolver, Uri eventsUri, int calendarId) {
            Cursor cursor;
            if (android.os.Build.VERSION.SDK_INT <= 7) { //up-to Android 2.1
                cursor = resolver.query(eventsUri, new String[]{ "_id" }, "Calendars._id=" + calendarId, null, null);
            } else { //8 is Android 2.2 (Froyo) (http://developer.android.com/reference/android/os/Build.VERSION_CODES.html)
                cursor = resolver.query(eventsUri, new String[]{ "_id" }, "calendar_id=" + calendarId, null, null);
            }
            while(cursor.moveToNext()) {
                long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
                resolver.delete(ContentUris.withAppendedId(eventsUri, eventId), null, null);
            }
            cursor.close();
        }



    }


