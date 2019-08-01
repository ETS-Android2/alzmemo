package com.main.alz2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.lib.CallBack;
import com.main.alz2.lib.HTTPAsyncTask;
import com.main.alz2.lib.ImageLoadTask;
import com.main.alz2.lib.JSONWrapper;
import com.main.alz2.pojo.Memory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class RegisterActivity extends AppCompatActivity {
    final Context mContext = this;
    SharedPreferences alzpref;
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String selectedImagePath;
    private ImageView imageView;
    private int relationId;
    private String imageUrl;
    private String imageNumber;
    private String imageName;
    Bitmap pickedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        imageView = (ImageView)findViewById(R.id.imageView);

        alzpref = this.getSharedPreferences(ALZUrl.ALZ_CONFIG, MODE_PRIVATE);
        setTitle("Create an Account");

        ((ImageView) findViewById(R.id.imageView))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Choose Image Source");
                        builder.setItems(new CharSequence[] {"Gallery", "Camera"},
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent intent = new Intent();
                                                intent.setType("image/*");
                                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                                startActivityForResult(Intent.createChooser(intent,
                                                        "Select Picture"), SELECT_PICTURE);

                                                break;

                                            case 1:
                                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                startActivityForResult(cameraIntent, TAKE_PICTURE);

                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                });

                        builder.show();
                    }
                });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    selectedImagePath = getPath(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    pickedImage = getResizedBitmap(bitmap, 500);
                    imageView.setImageBitmap(pickedImage);

                }
                else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        pickedImage = getResizedBitmap(image, 500);
                        parcelFileDescriptor.close();
                        imageView.setImageBitmap(pickedImage);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
                    }
    }

    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void uploadImage(View view){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pickedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bitmap is required image which have to send  in Bitmap form
        byte[] byteArray = baos.toByteArray();

        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        taskUploadImage(encodedImage);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void registerStart(View view)
    {
        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);
        EditText fname = (EditText) findViewById(R.id.fname);
        EditText mname = (EditText) findViewById(R.id.mname);
        EditText lname = (EditText) findViewById(R.id.lname );
        EditText btooth = (EditText) findViewById(R.id.bluetooth );
        String usertype = "relative";
        Switch isPatient = (Switch) findViewById(R.id.isPatient);
        if(isPatient.isChecked())
        {
            usertype = "patient";
        }
        imageUrl = ALZUrl.ALZ_HTTP;
        imageUrl += alzpref.getString(ALZUrl.ALZ_SERVER, "localhost");
        imageUrl += ALZUrl.DEFAULT_FILE_UPLOADS;
        imageUrl += username;
        uploadImage(imageView);

        registerUser(username.getText().toString(), password.getText().toString(), fname.getText().toString(), mname.getText().toString(), lname.getText().toString(), btooth.getText().toString(), usertype);
    }

    void registerUser(
            String username,
            String password,
            String fname,
            String mname,
            String lname,
            String btooth,
            String usertype
    ){
        final CallBack callback = new CallBack() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onResult(String result) {
                JSONWrapper dataObj = null;
                try {

                    JSONObject jsonRootObject = new JSONObject(result);

                    if (!jsonRootObject.getString("message").equals("success")) {
                        Toast.makeText(mContext, "Username already exists", Toast.LENGTH_LONG).show();
                        return;
                    }


                    Toast.makeText(mContext, "You can now login using your username and password!", Toast.LENGTH_LONG).show();
                    finish();
                    return;

                }catch(JSONException e){

                }
            }

            @Override
            public void onCancel() {

            }
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL;

        JSONObject getData = new JSONObject();

        HashMap<Object, Object> data = new HashMap<>();

        data.put("r", ALZUrl.ALZ_REGISTER);
        data.put("username", username);
        data.put("password", password);
        data.put("firstname", fname);
        data.put("middlename", mname);
        data.put("lastname", lname);
        data.put("bluetoothhandle", btooth);
        data.put("usertype", usertype);

        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "GET");
        asyncTask.execute(url);



    }


    void taskUploadImage( String encodedImage ){
        final CallBack callback = new CallBack() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onResult(String result) {
                JSONArray content = null;
                try {
                        JSONObject jsonRootObject = new JSONObject(result);
                        if (!jsonRootObject.getString("message").equals("success"))
                        {
                            Toast.makeText(mContext, "Error in upload", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }catch(JSONException e){

                }
            }

            @Override
            public void onCancel() {

            }
        };
        String url = ALZUrl.ALZ_HTTP;
        url += alzpref.getString(ALZUrl.ALZ_SERVER,"localhost");
        url += ALZUrl.DEFAULT_URL_UPLOADER_PROFILE;

        JSONObject getData = new JSONObject();

        HashMap<Object, Object> data = new HashMap<>();
        data.put("encodedImage", encodedImage);
        EditText username = (EditText) findViewById(R.id.username);
        String uname= username.getText().toString();
        data.put("imageNumber", uname);
        HTTPAsyncTask asyncTask = new HTTPAsyncTask(mContext,callback, data, null, "POST");
        asyncTask.execute(url);
    }
}



