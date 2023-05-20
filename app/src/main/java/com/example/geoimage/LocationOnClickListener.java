package com.example.geoimage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class LocationOnClickListener implements View.OnClickListener {
    private final MainActivity mainActivity;

    public LocationOnClickListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(mainActivity,
                                              android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mainActivity,
                                                                                       android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager =
                    (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

            // Check if GPS_PROVIDER is enabled on the device
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i("CIO", "GPS Provider is not enabled");
                return;
            }

            Button locationButton = mainActivity.findViewById(R.id.get_coordinates_button);
            locationButton.setEnabled(false);
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    TextView latitudeTextView = mainActivity.findViewById(R.id.lat);
                    TextView longitudeTextView = mainActivity.findViewById(R.id.lon);
                    latitudeTextView.setText(String.valueOf(latitude));
                    longitudeTextView.setText(String.valueOf(longitude));
                    locationButton.setEnabled(true);
                    locationManager.removeUpdates(this);
                    mainActivity.locationChanged(longitude, latitude);
                }

                // Add other required methods for the LocationListener interface...
            });
        } else {
            Log.i("CIO", "Permissions denied. SDK_INT=" + Build.VERSION.SDK_INT);
            Log.i("CIO", "ACCESS_COARSE_LOCATION: " +
                    ContextCompat.checkSelfPermission(mainActivity,
                                                      android.Manifest.permission.ACCESS_COARSE_LOCATION));
            Log.i("CIO", "ACCESS_FINE_LOCATION: " + ContextCompat.checkSelfPermission(mainActivity,
                                                                                      android.Manifest.permission.ACCESS_FINE_LOCATION));
            Log.i("CIO", "PackageManager.PERMISSION_GRANTED=" + PackageManager.PERMISSION_GRANTED);
        }
    }


}
