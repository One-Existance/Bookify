package com.example.bookify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap mMap;
    private String locationName;
    private LatLng eventLatLng = new LatLng(-6.7924, 39.2083);
    private static final String MAPTILER_KEY = "JhccbJYsCtrTEkg1KAQt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize MapLibre with context before setContentView
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_map);

        locationName = getIntent().getStringExtra("location_name");
        if (locationName == null) locationName = "Tanzania";

        double lat = getIntent().getDoubleExtra("event_lat", 0);
        double lng = getIntent().getDoubleExtra("event_lng", 0);

        if (lat != 0 && lng != 0) {
            eventLatLng = new LatLng(lat, lng);
        } else {
            geocodeLocation();
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.btn_back_map).setOnClickListener(v -> finish());
        findViewById(R.id.fab_directions).setOnClickListener(v -> openNavigation());
    }

    private void openNavigation() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + eventLatLng.getLatitude() + "," + eventLatLng.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback for devices without Google Maps
            Uri fallbackUri = Uri.parse("geo:0,0?q=" + eventLatLng.getLatitude() + "," + eventLatLng.getLongitude());
            startActivity(new Intent(Intent.ACTION_VIEW, fallbackUri));
        }
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
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        mMap = mapLibreMap;
        
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + MAPTILER_KEY;
        mMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
            enableLocationComponent(style);
            updateMapPosition();
        });
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationComponent locationComponent = mMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
        
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.NONE); 
        locationComponent.setRenderMode(RenderMode.COMPASS);
        
        // Draw initial "visual route" line if user location is available
        android.location.Location lastLoc = locationComponent.getLastKnownLocation();
        if (lastLoc != null) {
            drawVisualRoute(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()));
        }
    }

    private void drawVisualRoute(LatLng userLatLng) {
        if (mMap == null) return;
        mMap.addPolyline(new PolylineOptions()
                .add(userLatLng, eventLatLng)
                .color(Color.parseColor("#8A2BE2"))
                .width(3f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null && mMap.getStyle() != null) {
                enableLocationComponent(mMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.map_location_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMapPosition() {
        if (mMap == null) return;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(eventLatLng).title(locationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12));
        
        // Refresh line if user location is active
        if (mMap.getLocationComponent().isLocationComponentEnabled()) {
            android.location.Location lastLoc = mMap.getLocationComponent().getLastKnownLocation();
            if (lastLoc != null) {
                drawVisualRoute(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
}
