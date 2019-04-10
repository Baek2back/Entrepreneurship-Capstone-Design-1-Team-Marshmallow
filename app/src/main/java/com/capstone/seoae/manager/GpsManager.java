package com.capstone.seoae.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GpsManager {
    private static final int REQUEST_LOCATION = 0x123456;
    private static boolean init = false;

    private static GpsManager instance;

    private LocationManager locManager;
    private Activity appContext;

    private Location lastLocation;
    private LocationListener locationListener;
    private static GnssStatus.Callback mGnssStatusCallback;


    private static int satelliteCount = 0;

    private GpsManager() {}

    public static GpsManager getInstance() throws Exception {
        if (!init) {
            throw new Exception("you must initialize before using gps manager.");
        }
        return instance;
    }

    public static void init(Context context) {
        if (!init) {
            try {
                instance = new GpsManager();
                instance.locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
                instance.appContext = (Activity)context;

                init = true;
            } catch (Exception ex) {
                Log.d("Exception: ", "Failed to initialize GPS");
            }
        }
    }

    public void setOnLocationListener(LocationListener locationListener) {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(appContext, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        this.locationListener = locationListener;
        mGnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                satelliteCount = status.getSatelliteCount();
            }
        };
        try {
            instance.locManager.registerGnssStatusCallback(mGnssStatusCallback);
        }catch (SecurityException se){Log.d("SecurityException: ", "Failed to register GnssStatusCallback");}
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, locationListener);
    }

    public Location getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(appContext, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        Location location;

        if(this.satelliteCount < 4) {
            location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (isBetterLocation(location, lastLocation)) {
            this.lastLocation = location;
            return location;
        } else {
            return lastLocation;
        }

    }

    public Location getLastLocation() {
        if(lastLocation == null) {
            lastLocation = getCurrentLocation();
        }
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    /** Maintaining a current best estimate(isBetterLocation)
     *  (구글이 생각하는 의미있는 현재 위치 측위 방법) **/
    private static final int ONE_SECONDS = 1000 * 1;

    private static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -ONE_SECONDS;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks Whether two providers are same **/
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
