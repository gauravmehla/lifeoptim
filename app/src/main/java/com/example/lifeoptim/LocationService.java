package com.example.lifeoptim;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService extends AppCompatActivity implements LocationListener {
    private static Context c;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static List<Address> address;
    private static double lat;
    private static double lon;

    LocationService(Context c) {
        this.c = c;

        try {
            locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Geocoder geocoder = new Geocoder(c, Locale.getDefault());

            if(location != null) {

                lat = location.getLatitude();
                lon = location.getLongitude();

                address = geocoder.getFromLocation(lat, lon, 1);

                Log.d(">>", "lat :" + lat);
                Log.d(">>", "long :" + lon);
                Log.d(">>", "address :" + address.get(0).getAddressLine(0));
            }
        }
        catch(SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void getCurrentLocation() {
        Log.d(">> Current Location: ", this.lat + ", " + this.lon);
        Log.d(">> Current Address", address.get(0).getAddressLine(0));
    }

    public static boolean isGPSEnabled() {
        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("->", isEnabled + "");
        return isEnabled;
    }

    public static void turnOnGPS() {

        Log.d(">>", "Enabling GPS");


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();

        Log.d(">> Location changed: ", location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
