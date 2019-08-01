package com.main.alz2;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.main.alz2.lib.ALZUrl;
import com.main.alz2.pojo.DeviceItem;

/**
 * Created by Jeff on 12/31/2015.
 */
public class BluetoothReceiver extends BroadcastReceiver {

    SharedPreferences alzpref;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Create a new device item
            DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
            // Add it to our adapter
            Log.d("PatientBluetooth", "Device found " + device.getName() + " - " + device.getAddress());

            alzpref = context.getSharedPreferences( ALZUrl.ALZ_CONFIG , Context.MODE_PRIVATE);

            String latestSearchedDevices = alzpref.getString("latestSearched", "");
            latestSearchedDevices += device.getName() + ",";

            SharedPreferences.Editor editor = alzpref.edit();
            editor.putString("latestSearched", latestSearchedDevices);
            editor.commit();
            //mAdapter.add(newDevice);
        }
    }
}
