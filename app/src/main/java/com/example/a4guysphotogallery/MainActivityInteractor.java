package com.example.a4guysphotogallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

public class MainActivityInteractor {

    private FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest = new LocationRequest();
    String Photo;

    @SuppressLint("MissingPermission")
    public MainActivityInteractor(Activity act) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(act);
        fusedLocationClient.getLocationAvailability()
                .addOnSuccessListener(act, new OnSuccessListener<LocationAvailability>() {
                    @Override
                    public void onSuccess(LocationAvailability locationAvailability) {
                        Log.d("locAvail", "Location is available");
                    }
                });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                }
            }
        };
    }

    protected void onResume(Activity act) {
        startLocationUpdates(act);
    }

    private void startLocationUpdates(Activity act) {
        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("permissions disabled","");
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    protected void onPause() {
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    public void picture(Activity act, final String photoName) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(act, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("lastloc", location.getLatitude() + ", " + location.getLongitude());

                            ExifInterface exif;
                            try {
                                exif = new ExifInterface(photoName);
                                Log.d("exif status", exif.TAG_FILE_SOURCE);
                                exif.setGpsInfo(location);
                                try {
                                    exif.saveAttributes();
                                    Log.d("exifsaved","exif saved");
                                    String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

                                    String lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

                                    Log.d("exiftest", "lat: " + lat + ", lng: " + lng);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d("exifsavefail","exif save failed");
                                    String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

                                    String lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

                                    Log.d("exiftestfail", "lat: " + lat + ", lng: " + lng);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("ioexception", "except io");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                Log.d("nullexception", "except null");
                            }

                        } else {
                            Log.d("lastlocfail", "locnull");
                        }
                    }
                }).addOnFailureListener(act, new OnFailureListener() {
            public void onFailure(Exception e) {
                Log.d("fusedfail", e.toString());
            }
        });
        ;
    }
}
