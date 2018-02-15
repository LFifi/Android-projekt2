package com.example.roxi.sensorprojekt2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap2;
    private double longitude = 0;
    private double latitude = 0;
    private ArrayList<Location> route;
    private SupportMapFragment supportMapFragment;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Boolean isProviderEnabled = false;
    private boolean isActivityOpen = false;
    private LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        isActivityOpen = true;
        longitude = getIntent().getDoubleExtra("longitude", 21.020 );
        latitude = getIntent().getDoubleExtra("latitude", 52.259);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("NO PERMISSION!!!");
            Toast.makeText(this, "no permision", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isActivityOpen == true) {
                    route.add(location);
                    drawPrimaryLinePath();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                isProviderEnabled = true;
                Toast.makeText(MapsActivity.this, "GPS is ON.", Toast.LENGTH_LONG).show();
                Log.d(" isProviderEnabled: ", isProviderEnabled.toString());
            }

            @Override
            public void onProviderDisabled(String provider) {
                isProviderEnabled = false;
                Toast.makeText(MapsActivity.this, "GPS is OFF.", Toast.LENGTH_LONG).show();
                Log.d(" isProviderEnabled: ", isProviderEnabled.toString());
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        route = new ArrayList<Location>();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (isActivityOpen == true) {
            googleMap2 = googleMap;

            LatLng latLng = new LatLng(latitude, longitude);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            googleMap2.setMyLocationEnabled(true);
            googleMap2.addMarker(new MarkerOptions().position(latLng).title("Location"));

            googleMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));


        }
    }

    private void drawPrimaryLinePath() {
        if (googleMap2 == null) {
            return;
        }
        if (route.size() > 2) {
            googleMap2.clear();

            PolylineOptions options = new PolylineOptions();

            options.color(Color.parseColor("#FFFF0000"));
            options.width(10);
            options.visible(true);

            for (int i = 1; i < route.size() - 1; i++) {
                Location loc = route.get(i);
                options.add(new LatLng(loc.getLatitude(), loc.getLongitude()));

                googleMap2.addPolyline(options);
            }
        }

    }
}