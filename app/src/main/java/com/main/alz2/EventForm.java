package com.main.alz2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
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
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventForm extends AppCompatActivity {

    EventMenuItem currentEvent;
    private static final String TAG = MainActivity.class.getSimpleName();

    int YEAR;
    int MONTH;
    int DAY;

    Date dateInProcess;

    String[] arraySpinner;

    final Context mContext = this;

    private String SERVER_URL = "";
    private String USER_ID = "";
    SharedPreferences sharedPref;
    boolean isViewing = false;

    private SaveEvent saveEvent;
    int reminder;
    int color;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerReminder);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.reminder_array, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
            spinner.setAdapter(adapter);

        spinner.setSelection(adapter.getPosition("1 min before"));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                // Showing selected spinner item
                // Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();

                switch (item) {
                    case "On time":
                        reminder = 0;
                        break;
                    case "1 min before":
                        reminder = 1;
                        break;
                    case "5 mins before":
                        reminder = 5;
                        break;
                    case "10 mins before":
                        reminder = 10;
                        break;
                    case "15 mins before":
                        reminder = 15;
                        break;
                    case "20 mins before":
                        reminder = 20;
                        break;
                    case "25 mins before":
                        reminder = 25;
                        break;
                    case "30 mins before":
                        reminder = 30;
                        break;
                    case "45 mins before":
                        reminder = 45;
                        break;
                    case "1 hour before":
                        reminder = 60;
                        break;
                    case "2 hours before":
                        reminder = 120;
                        break;
                    case "3 hours before":
                        reminder = 180;
                        break;
                    case "1 day before":
                        reminder = 1440;
                        break;
                    case "2 days before":
                        reminder = 2880;
                        break;
                    case "1 week before":
                        reminder = 10080;
                        break;
                    default:
                        reminder = 0;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Bundle bundle = this.getIntent().getExtras();

        sharedPref = this.getSharedPreferences("com.alz.pref", Context.MODE_PRIVATE);
        SERVER_URL = sharedPref.getString(ALZUrl.ALZ_SERVER,"localhost");
        USER_ID = sharedPref.getString("user_id", "0");

        if( bundle.getBoolean("isView") == true ){
            isViewing=true;
            int eventId = bundle.getInt("event_id");
            String detail = bundle.getString("detail");
            String to = bundle.getString("to");
            String from = bundle.getString("from");
            String eventDate = bundle.getString("startDate");
            String location = bundle.getString("location");
            int reminder = bundle.getInt("reminder");
            int color = bundle.getInt("color");

            setTitle( "Event: " + detail );
            switch (reminder)
            {
                case 0:
                    spinner.setSelection(adapter.getPosition("On time"));
                    break;
                case 1:
                    spinner.setSelection(adapter.getPosition("1 min before"));
                    break;
                case 5:
                    spinner.setSelection(adapter.getPosition("5 mins before"));
                    break;
                case 10:
                    spinner.setSelection(adapter.getPosition("10 mins before"));
                    break;
                case 15:
                    spinner.setSelection(adapter.getPosition("15 mins before"));
                    break;
                case 20:
                    spinner.setSelection(adapter.getPosition("20 mins before"));
                    break;
                case 25:
                    spinner.setSelection(adapter.getPosition("25 mins before"));
                    break;
                case 30:
                    spinner.setSelection(adapter.getPosition("30 mins before"));
                    break;
                case 45:
                    spinner.setSelection(adapter.getPosition("45 mins before"));
                    break;
                case 60:
                    spinner.setSelection(adapter.getPosition("1 hour before"));
                    break;
                case 120:
                    spinner.setSelection(adapter.getPosition("2 hours before"));
                    break;
                case 180:
                    spinner.setSelection(adapter.getPosition("3 hours before"));
                    break;
                case 1440:
                    spinner.setSelection(adapter.getPosition("1 day before"));
                    break;
                case 2880:
                    spinner.setSelection(adapter.getPosition("2 days before"));
                    break;
                case 10080:
                    spinner.setSelection(adapter.getPosition("1 week before"));
                    break;
                default:
                    spinner.setSelection(adapter.getPosition("On time"));
            }


            Button eventSave = (Button) findViewById(R.id.eventSave);
            eventSave.setText("Update Event");

            currentEvent = new EventMenuItem( eventId, detail, to, from, eventDate, location,reminder,color);

        }else{
            currentEvent = new EventMenuItem();
            Date d = new Date();
            dateInProcess = d;
            currentEvent.setFrom(sharedPref.getString("username", ""));
            DateFormat mainFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentEvent.setEventDate( mainFormat.format(d) );



            Button eventSave = (Button) findViewById(R.id.eventSave);
            eventSave.setText("Save Event");
        }



        renderForm();
    }



    private void renderForm(){

        EditText toName = (EditText) findViewById(R.id.toName);
        EditText fromName = (EditText) findViewById(R.id.fromName);
        EditText detail = (EditText) findViewById(R.id.detail);
        EditText location = (EditText) findViewById(R.id.location);
        Button datePick = (Button) findViewById(R.id.pickDate);
        Button timePick = (Button) findViewById(R.id.pickTime);

        try {

            DateFormat mainFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = mainFormat.parse(currentEvent.getEventDate());

            dateInProcess = date;

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            datePick.setText(dateFormat.format(date));

            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timePick.setText(timeFormat.format(date));

            toName.setText( currentEvent.getTo() );
            fromName.setText( currentEvent.getFrom() );

            detail.setText( currentEvent.getDetail());
            location.setText( currentEvent.getLocation());



            Log.d(TAG,"color: "+currentEvent.getColor());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void setDate(View view){
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateInProcess);
        YEAR = cal.get(Calendar.YEAR);
        MONTH = cal.get(Calendar.MONTH);
        DAY = cal.get(Calendar.DAY_OF_MONTH);

        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, YEAR, MONTH, DAY);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {

            YEAR = arg1;
            MONTH = arg2+1;
            DAY = arg3;

            Button btn = (Button)findViewById( R.id.pickDate );
            btn.setText( YEAR + "/" + (MONTH) + "/" + DAY );
        }
    };

    public void setTime(View view){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(EventForm.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Button btn = (Button)findViewById( R.id.pickTime );
                btn.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void submitForm(View view){

        EditText toName = (EditText) findViewById(R.id.toName);
        EditText fromName = (EditText) findViewById(R.id.fromName);
        EditText detail = (EditText) findViewById(R.id.detail);
        EditText location = (EditText) findViewById(R.id.location);
        Button datePick = (Button) findViewById(R.id.pickDate);
        Button timePick = (Button) findViewById(R.id.pickTime);



        currentEvent.setTo(toName.getText().toString());
        currentEvent.setFrom(fromName.getText().toString());
        currentEvent.setDetail(detail.getText().toString());
        currentEvent.setLocation(location.getText().toString());
        currentEvent.setEventDate(datePick.getText().toString());
        currentEvent.setTime(timePick.getText().toString());
        currentEvent.setReminder(reminder);


        if (saveEvent != null) {
            return;
        }

        saveEvent = new SaveEvent();
        saveEvent.execute();

    }

    void endSaveEvent(String res){

        String msg = "Event Saved!";

        if(isViewing){
            msg = "Edited Sucessfully";
        }


        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, EventActivity.class);
        setResult(Activity.RESULT_OK,intent);
        startActivity(intent);
        //setResult(Activity.RESULT_OK, intent);
        //finish();
    }

    class SaveEvent extends AsyncTask<String, Void, String> {
        private Exception exception;
        @Override
        protected String doInBackground(String... params) {
            return startSend();
        }
        protected void onPostExecute(String response)
        {
            saveEvent = null;
            endSaveEvent( response );
        }
        private String buildRequest() throws IOException {
            String url = "";
            url += "from=" + URLEncoder.encode(currentEvent.getFrom() + "", "utf-8");
            url += "&to=" + URLEncoder.encode( currentEvent.getTo(), "utf-8");
            url += "&detail=" + URLEncoder.encode( currentEvent.getDetail(), "utf-8");
            url += "&date=" + URLEncoder.encode( currentEvent.getEventDate(), "utf-8");
            url += "&location=" + URLEncoder.encode( currentEvent.getLocation(), "utf-8");
            url += "&time=" + URLEncoder.encode( currentEvent.getTime(), "utf-8");
            url += "&reminder=" + URLEncoder.encode( currentEvent.getReminder()+"", "utf-8");
            if(isViewing)
            {
                url += "&id=" + currentEvent.getEventId();
            }
            return url;
        }
        public String startSend() {
            // TODO: attempt authentication against a network service.
            HttpURLConnection con;
            String ret = "";
            JSONArray content = null;
            try
            {
                String url = "http://" + SERVER_URL + "/alzserver/web/?r=events/saveevent&" +
                        buildRequest();
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                con.setRequestProperty("Accept", "*/*");
                InputStream response = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                for (String line; (line = reader.readLine()) != null; )
                {
                    ret += line;
                }
                reader.close();
                try
                {
                    JSONObject jsonRootObject = new JSONObject(ret);
                    if (jsonRootObject.getString("message").equals("success")) {
                        return "success";
                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();
                }
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

}

