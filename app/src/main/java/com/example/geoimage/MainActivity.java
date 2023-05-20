package com.example.geoimage;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Bitmap> bitmapList = new ArrayList<>();
    private Permissions permissions = Permissions.None;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button locationButton = findViewById(R.id.get_coordinates_button);
        locationButton.setEnabled(false);

        if (ContextCompat.checkSelfPermission(this,
                                              android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                                              new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                              Permissions.Coarse.value);

        } else {
            this.permissions = this.permissions.and(Permissions.Coarse);
            requestFineLocationPermission();  // If Coarse permission is already granted, request for Fine
        }

        ViewPager2 viewPager2 = findViewById(R.id.image_pager);
        adapter = new ImageAdapter(this, bitmapList);
        viewPager2.setAdapter(adapter);
    }

    private void requestFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                                              android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                                              new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                              Permissions.Fine.value);
        } else {
            this.permissions = this.permissions.and(Permissions.Fine);
            activateButton();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permissions permission = Permissions.from(requestCode);
        Log.i("CIO", "onRequestPermissionsResult: " + permission);
        Log.i("CIO", "requestCode: " + requestCode);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.permissions = this.permissions.and(permission);
            activateButton();
        }

        // If Coarse permission is granted, request for Fine
        if (permission == Permissions.Coarse && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestFineLocationPermission();
        }
    }

    private void activateButton() {
        if (this.permissions.bothActivated()) {
            Button locationButton = findViewById(R.id.get_coordinates_button);
            locationButton.setEnabled(true);
            locationButton.setOnClickListener(new LocationOnClickListener(this));
        }
    }

    // When location changes, send the request to Flickr
    public void locationChanged(double longitude, double latitude) {
        Log.i("CIO", "locationChanged: " + longitude + ", " + latitude);
        bitmapList.clear();
        new AsyncFlickrJSONData(this::imageListReceived).execute(
                "https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                        "&has_geo=true&format=json&per_page=10&api_key=" +
                        BuildConfig.FLICKR_API_KEY + "&lat=" + latitude + "&lon=" + longitude);
    }

    public void imageListReceived(JSONObject json) {
        if (json == null) {
            return;
        }
        Log.i("CIO", "imageListReceived: " + json);
        try {
            JSONArray photos = json.getJSONObject("photos").getJSONArray("photo");
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                String id = photo.getString("id");
                String secret = photo.getString("secret");
                String serverId = photo.getString("server");
                String url = "https://live.staticflickr.com/" + serverId + "/" + id + "_" + secret +
                        ".jpg";
                new AsyncFlickerBitmapLoader(this::imageReceived).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, url);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void imageReceived(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        bitmapList.add(bitmap);
        adapter.notifyDataSetChanged();
    }

    public enum Permissions {
        None(0b00), Coarse(0b01), Fine(0b10), Both(0b11);

        private final int value;

        Permissions(int value) {
            this.value = value;
        }

        public static Permissions from(int value) {
            int perm = value & 0b11;
            switch (perm) {
                case 0b01:
                    return Coarse;
                case 0b10:
                    return Fine;
                case 0b11:
                    return Both;
                default:
                    return None;
            }
        }

        public boolean bothActivated() {
            return value == Both.value;
        }

        @Contract(pure = true)
        public Permissions and(@NonNull Permissions other) {
            return Permissions.from(value | other.value);
        }
    }
}