package com.example.geoimage;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Contract;

public class MainActivity extends AppCompatActivity {

    private Permissions permissions = Permissions.None;

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
        if (permission == Permissions.Coarse && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    public enum Permissions {
        None(0b00), Coarse(0b01), Fine(0b10), Both(0b11);

        private final int value;

        Permissions(int value) {
            this.value = value;
        }

        public boolean bothActivated() {
            return value == Both.value;
        }

        @Contract(pure = true)
        public Permissions and(@NonNull Permissions other) {
            return Permissions.from(value | other.value);
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
    }
}