package com.main.alz2;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.ImageLoadTask;
import com.main.alz2.pojo.Relation;

import java.util.ArrayList;

public class MemoryAdapter extends ArrayAdapter<Relation> {

    Context context;
    Relation[] data;
    private static LayoutInflater inflater = null;

    public MemoryAdapter(Context context, ArrayList<Relation> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.text_image_listitem, parent, false);
        }
        // Get the data item for this position
        Relation dmItem = getItem(position);
        SharedPreferences alzpref = getContext().getSharedPreferences(ALZUrl.ALZ_CONFIG, Context.MODE_PRIVATE);
        String user_id = alzpref.getString("user_id", "0");
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView sub = (TextView) convertView.findViewById(R.id.sub);

        if( dmItem.getRelatedUserId() == Integer.parseInt(user_id) ){
            sub.setText("Described you as : " + dmItem.getDescription());
            title.setText(dmItem.getUserLName() + " , " + dmItem.getUserFName());

            //
            String imageUrl;
            imageUrl = ALZUrl.ALZ_HTTP;
            imageUrl += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            imageUrl += ALZUrl.DEFAULT_FILE_UPLOADS;
            imageUrl += dmItem.getUsername()+".jpg";

            new ImageLoadTask(imageUrl, imageView).execute();
            //

        }else{
            sub.setText(dmItem.getDescription());
            title.setText(dmItem.getRelatedUserLName() + " , " + dmItem.getRelatedUserFName());
            //
            String imageUrl;
            imageUrl = ALZUrl.ALZ_HTTP;
            imageUrl += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
            imageUrl += ALZUrl.DEFAULT_FILE_UPLOADS;
            imageUrl += dmItem.getRelatedUsername()+".jpg";

            new ImageLoadTask(imageUrl, imageView).execute();
            //
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
