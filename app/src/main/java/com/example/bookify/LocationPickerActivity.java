package com.example.bookify;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap mMap;
    private TextView tvAddress;
    private LatLng currentPickedLatLng;
    private String currentPickedAddress = "";
    private static final String MAPTILER_KEY = "JhccbJYsCtrTEkg1KAQt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_location_picker);

        mapView = findViewById(R.id.mapView);
        tvAddress = findViewById(R.id.tv_picked_address);
        
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.btn_confirm_location).setOnClickListener(v -> confirmLocation());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        mMap = mapLibreMap;
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + MAPTILER_KEY;
        
        mMap.setStyle(styleUrl, style -> {
            // Center on Dar es Salaam by default
            mMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(-6.7924, 39.2083))
                    .zoom(12)
                    .build());

            mMap.addOnCameraIdleListener(() -> {
                currentPickedLatLng = mMap.getCameraPosition().target;
                reverseGeocode(currentPickedLatLng);
            });
        });
    }

    private void reverseGeocode(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    currentPickedAddress = address.getAddressLine(0);
                    runOnUiThread(() -> tvAddress.setText(currentPickedAddress));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void confirmLocation() {
        if (currentPickedLatLng == null) return;
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", currentPickedLatLng.getLatitude());
        resultIntent.putExtra("longitude", currentPickedLatLng.getLongitude());
        resultIntent.putExtra("address", currentPickedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
}
