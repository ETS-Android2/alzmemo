package com.main.alz2;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jeff on 11/21/2015.
 */
public class EventMenuAdapter extends ArrayAdapter<EventMenuItem> {

    Context context;
    EventMenuItem[] data;
    private static LayoutInflater inflater = null;
    private static final String TAG = MainActivity.class.getSimpleName();

    public EventMenuAdapter(Context context, ArrayList<EventMenuItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dashboard_menu_listitem, parent, false);
        }
        // Get the data item for this position
        EventMenuItem dmItem = getItem(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView sub = (TextView) convertView.findViewById(R.id.sub);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

        title.setText(dmItem.getDetail() );
        sub.setText(dmItem.getItemDesc());


        LinearLayout bg = (LinearLayout)convertView.findViewById(R.id.linearLayout2);
        bg.setBackgroundColor(dmItem.getColor());

        TextView day = (TextView) convertView.findViewById(R.id.weekdayText);
        TextView dateTxt = (TextView) convertView.findViewById(R.id.dateText);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateInString = dmItem.getEventDate();
        Date dateObj=null;
        try {

            dateObj = formatter.parse(dateInString);
            Log.d(TAG,"dateInString: "+dateObj.getTime());
            Log.d(TAG,"date: "+dmItem.getEventDate());
            Log.d(TAG,"time: "+dmItem.getTime());
            Log.d(TAG,"dateNow: "+Calendar.getInstance().getTimeInMillis());


        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateObj);

        String[] days =
                {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        day.setText( days[cal.get( Calendar.DAY_OF_WEEK ) - 1] );
        dateTxt.setText( cal.get( Calendar.DAY_OF_MONTH ) + "" );

        // Return the completed view to render on screen
        return convertView;
    }

}
