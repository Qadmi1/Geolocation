package com.example.appty.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class GetLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    // Constant that act as an identifier
    private static final int LOCATION_REQUEST_CONSTANT = 1;
    // Constant that act as an identifier
    private static final int REQUEST_CHECK_SETTINGS = 2;
    // Constant that act as an identifier
    private static final int REQUEST_CAMERA = 3;
    private static final String TAG = "GetLocation";
    GoogleApiClient apiClient;
    Location currentLoc;
    LatLng position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_location);
        // Find the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.lmap);

        // Listener that runs when the map is ready
        mapFragment.getMapAsync(this);
        // As the name suggest this method will build the client
        buildClient();

        // Create a(custom) location request
        LocationRequest request = new LocationRequest();
        // Make the request with high accuracy priority
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // The interval is the time between every update which is 1 second here (1000 milli second)
        request.setInterval(1000);
        // The request asks for 10 times
        request.setNumUpdates(10);

        // Check if the request fulfills our location settings request and if it does build that
        // location settings request which means it's case SUCCESS if it does not fulfill then
        // it's case RESOLUTION_REQUIRED
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        // Ask for the list of results for the request for apiClient from the Android system
        PendingResult<LocationSettingsResult> results = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());

        // This Callback is called when result for the request is ready
        results.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                // checks if location is enabled or not
                final Status status = locationSettingsResult.getStatus();
                // checks for the degree of the location (PRIORITY_HIGH_ACCURACY or something else)
//                final LocationSettingsStates LS_States = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    // If case is RESOLUTION_REQUIRED i.e. the request didn't fulfill the location
                    // settings request
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            // This is very similar to an intent with call startActivityForResult()
                            // with a(Constant) identifier
                            status.startResolutionForResult(GetLocation.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    // If case is SUCCESS i.e. the request fulfilled the location settings request
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Check if the permission was not granted
                        if (ContextCompat.checkSelfPermission(GetLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //Ask for the permissions
                            ActivityCompat.requestPermissions(GetLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_REQUEST_CONSTANT);
                            ActivityCompat.requestPermissions(GetLocation.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATION_REQUEST_CONSTANT);
                            //Assign the current location to the client using the FusedLocationApi
                            currentLoc = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                        }
                        // If permission was granted
                        else {
                            //Assign the current location to the client using the FusedLocationApi
                            currentLoc = LocationServices.FusedLocationApi.getLastLocation(apiClient);

                        }
                        break;
                    // Otherwise if the case is SETTINGS_CHANGE_UNAVAILABLE
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(GetLocation.this, "Settings unavailable", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        ImageButton im = findViewById(R.id.im);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    //This method is called when the button is pressed
    private void takePicture() {
        //Create an intent to the system to capture an image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Send the intent and wait for a result which will be identified by the constant identifier
        //REQUEST_CAMERA
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    //This method is called when this activity is opened again after some call to other place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // requestCode is the identifier that you give
        // resultCode is a number returned by the system like result ok for each identifier
        switch (requestCode) {
            //This case is after the call for status.startResolutionForResult which opens another
            //Activity and after some result is required it open this Activity(This case happens
            // when the request didn't fulfill the location settings request)
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Location enabled");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location needs to be enabled", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            //This case is after the call for startActivityForResult() for the Camera from the system
            //which the result was identified by the constant identifier REQUEST_CAMERA
            case REQUEST_CAMERA:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // First we need to make sure the current location isn't null
                        if (currentLoc != null) {
                            // Create a thumbnail and take it from the data(result) passed from the
                            //intent
                            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                            //Take the LatLang position from our currentLoc
                            position = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
                            //Add a marker to the map for our current location/position and add a
                            //thumbnail/picture to that particular marker
                            mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(thumbnail)));
                        }
                }
        }
    }

    // This method is called when the map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setOnMyLocationButtonClickListener(this);
//
//        checkPermission();
    }

    /*
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

    //    // This method handles the location button
    //    @Override
    //    public boolean onMyLocationButtonClick() {
    //        mMap.setTrafficEnabled(true);
    //       //If you male it return false, the location button will take you to your current location
    //        return false;
    //    }
    */
    private void buildClient() {
        // Build the client with LocationServices API and with call backs when the connection is
        // successful or it has failed
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        apiClient.connect();
    }

    // This method is called when the client is connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, "Connected");
    }

    // This method is called when the connected client has suspended
    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection Suspended");
    }

    // This method is called when the client has failed to connect
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Connection Failed " + connectionResult.getErrorMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the client connection when the App has been destroyed after checking if the connection
        // exists
        if (apiClient.isConnected()) {
            apiClient.disconnect();
        }
    }
}
