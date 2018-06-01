package com.example.appty.myapplication;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by appty on 28/05/18.
 */

public class GetLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    private static final int LOCATION_REQUEST_CONSTANT = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final String TAG = "GetLocation";
    GoogleApiClient apiClient;
    Location currentLoc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.lmap);

        mapFragment.getMapAsync(this);

        buildClient();

        LocationRequest request = new LocationRequest();

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        request.setNumUpdates(10);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        PendingResult<LocationSettingsResult> results = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());

        results.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                // checks if location is enabled or not
                final Status status = locationSettingsResult.getStatus();
                // checks for the degree of the location (PRIORITY_HIGH_ACCURACY or something else)
                final LocationSettingsStates LS_States = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            status.startResolutionForResult(GetLocation.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;

                    case LocationSettingsStatusCodes.SUCCESS:

                        if (ContextCompat.checkSelfPermission(GetLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(GetLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_REQUEST_CONSTANT);
                            ActivityCompat.requestPermissions(GetLocation.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATION_REQUEST_CONSTANT);

                            currentLoc = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                        } else {
                            currentLoc = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(GetLocation.this, "settings unavailable", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);

        checkPermission();
    }

    private void checkPermission() {

        // Check if the permission was not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CONSTANT);
            //check if the permission was granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && mMap != null) {
                // Enable/Add the location button
                mMap.setMyLocationEnabled(true);
            }
        } else if (mMap != null) {
            // Enable/Add the location button
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Please have location enabled to use this feature", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mMap.setTrafficEnabled(true);
        return false;
    }

    private void buildClient() {
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        apiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Connection Failed " + connectionResult.getErrorMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (apiClient.isConnected()) {
            apiClient.disconnect();
        }
    }
}
