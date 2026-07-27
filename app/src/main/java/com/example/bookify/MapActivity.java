package com.example.bookify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.MarkerOptions;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String EXTRA_RESULT_LOCATION_NAME = "result_location_name";
    public static final String EXTRA_RESULT_LATITUDE = "result_latitude";
    public static final String EXTRA_RESULT_LONGITUDE = "result_longitude";

    private MapView mapView;
    private MapLibreMap mMap;
    private String locationName;
    private LatLng eventLatLng = new LatLng(-6.7924, 39.2083);
    private static final String MAPTILER_KEY = "JhccbJYsCtrTEkg1KAQt";
    private boolean pickMode;
    private boolean locationPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MapLibre with context before setContentView
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_map);

        pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);
        locationName = getIntent().getStringExtra("location_name");
        if (locationName == null) locationName = "Tanzania";

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.btn_back_map).setOnClickListener(v -> finish());

        if (pickMode) {
            findViewById(R.id.btn_confirm_location).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.tv_map_hint).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.btn_confirm_location).setEnabled(false);
            findViewById(R.id.btn_confirm_location).setOnClickListener(v -> confirmPickedLocation());
        } else {
            geocodeLocation();
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

            if (pickMode) {
                mMap.addOnMapClickListener(latLng -> {
                    eventLatLng = latLng;
                    locationPicked = true;
                    findViewById(R.id.btn_confirm_location).setEnabled(true);
                    updateMapPosition();
                    reverseGeocode(latLng);
                    return true;
                });
            }
        });
    }

    private void reverseGeocode(LatLng latLng) {
        new Thread(() -> {
            String resolvedName = String.format(Locale.US, "%.5f, %.5f", latLng.getLatitude(), latLng.getLongitude());
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressLine = addresses.get(0).getAddressLine(0);
                    if (addressLine != null && !addressLine.isEmpty()) resolvedName = addressLine;
                }
            } catch (IOException e) {
                Log.e("MapActivity", "Reverse geocoding failed", e);
            }
            final String finalName = resolvedName;
            runOnUiThread(() -> {
                locationName = finalName;
                updateMapPosition();
            });
        }).start();
    }

    private void confirmPickedLocation() {
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT_LOCATION_NAME, locationName);
        result.putExtra(EXTRA_RESULT_LATITUDE, eventLatLng.getLatitude());
        result.putExtra(EXTRA_RESULT_LONGITUDE, eventLatLng.getLongitude());
        setResult(RESULT_OK, result);
        finish();
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
        locationComponent.setCameraMode(CameraMode.NONE); // Don't snap camera to user automatically
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null && mMap.getStyle() != null) {
                enableLocationComponent(mMap.getStyle());
            }
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMapPosition() {
        if (mMap == null) return;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(eventLatLng).title(locationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12));
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
