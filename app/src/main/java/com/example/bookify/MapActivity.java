package com.example.bookify;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String locationName;
    private LatLng eventLatLng = new LatLng(-6.7924, 39.2083); // Default: Dar es Salaam

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationName = getIntent().getStringExtra("location_name");
        if (locationName == null) locationName = "Tanzania";

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btn_back_map).setOnClickListener(v -> finish());
        
        geocodeLocation();
    }

    private void geocodeLocation() {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    eventLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    
                    runOnUiThread(this::updateMapPosition);
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Geocoding failed", e);
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapPosition();
    }

    private void updateMapPosition() {
        if (mMap == null) return;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(eventLatLng).title(locationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 15));
    }
}