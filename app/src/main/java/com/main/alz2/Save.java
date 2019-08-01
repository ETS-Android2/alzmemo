package com.main.alz2;

/**
 * Created by mastertabs on 1/28/2016.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class Save {

    private Context TheThis;
    private String NameOfFolder = "/AlzMemo";
    private String NameOfFile   = "AlzMemo";

    public void SaveImage(Context context,Bitmap ImageToSave)
    {
        TheThis = context;
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()+ NameOfFolder;
        String CurrentDateAndTime= getCurrentDateAndTime();
        File dir = new File(file_path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dir, NameOfFile +CurrentDateAndTime+ ".jpg");
        try
        {
            FileOutputStream fOut = new FileOutputStream(file);
            ImageToSave.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MakeSureFileWasCreatedThenMakeAvabile(file);
            AbleToSave();

        }
        catch (FileNotFoundException e) {UnableToSave();}
        catch (IOException e){UnableToSave();}
    }



    private void MakeSureFileWasCreatedThenMakeAvabile(File file) {
        MediaScannerConnection.scanFile(TheThis,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("ExternalStorage", "Scanned " + path + ":");
                        Log.e("ExternalStorage", "-> uri=" + uri);

                    }
                });

    }



    private String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }


    private void UnableToSave() {
        Toast.makeText(TheThis, "Picture cannot be saved to gallery", Toast.LENGTH_SHORT).show();

    }

    private void AbleToSave() {
        Toast.makeText(TheThis, "Image saved to gallery", Toast.LENGTH_SHORT).show();

    }
}
