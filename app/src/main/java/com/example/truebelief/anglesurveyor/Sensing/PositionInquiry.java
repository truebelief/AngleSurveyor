package com.example.truebelief.anglesurveyor.Sensing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

/**
 * Created by truebelief on 2015/7/5.
 */
public class PositionInquiry implements LocationListener {
    public double lon;
    public double lat;
    public double ele;
    public Location loc;
    public LocationManager locationManager;
    public Activity act;

    public PositionInquiry(Activity _act){
        act=_act;
        locationManager = (LocationManager)_act.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,this);
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    public void onLocationChanged(Location location) {
        loc=location;
    }
    public Location getLocation(){
        return loc;
    }

    public void Cancel(){
        if (locationManager!=null){
            locationManager.removeUpdates(PositionInquiry.this);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                act.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }



}
