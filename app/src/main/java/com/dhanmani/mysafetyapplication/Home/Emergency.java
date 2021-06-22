package com.dhanmani.mysafetyapplication.Home;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Emergency extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    double currentLatitude, currentLongitude;
    Location mylocation;

    private final static int REQUEST_SETTINGS_CHECK_GPS= 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS= 0x2;

    double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_list_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_id);
        mapFragment.getMapAsync(this);

        setUPGClient();
    }

    private void setUPGClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,0,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);


        // Add a marker in Sydney and move the camera
        //   LatLng sydney = new LatLng(-34, 151);
        //   mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //   mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermission();
    }

    private void checkPermission() {
        int permissionLocation= ContextCompat.checkSelfPermission(Emergency.this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermission= new ArrayList<>();
        if (permissionLocation!= PackageManager.PERMISSION_GRANTED) {
            listPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if (!listPermission.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermission.toArray(new String[listPermission.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }
        else {
            getMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int permissionLocation= ContextCompat.checkSelfPermission(Emergency.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
        else {
            checkPermission();
        }
    }

    private void getMyLocation(){
        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(Emergency.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(mGoogleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(mGoogleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(Emergency.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {


                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(mGoogleApiClient);


                                    }
                                    break;

                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(Emergency.this,
                                                REQUEST_SETTINGS_CHECK_GPS);


                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;

                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });

                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null) {
            currentLatitude= location.getLatitude();
            currentLongitude= location.getLongitude();

            BitmapDescriptor icon= BitmapDescriptorFactory.fromResource(R.drawable.blue_marker);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLatitude), 15.0f));

            MarkerOptions markerOptions= new MarkerOptions();
            markerOptions.position(new LatLng(currentLatitude, currentLongitude));
            markerOptions.title("You");
            markerOptions.icon(icon);
            mMap.addMarker(markerOptions);

            getNearByHospitals();

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getNearByHospitals() {
        StringBuffer sb1= new StringBuffer("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb1.append("location="+ String.valueOf(currentLatitude)+","+ String.valueOf(currentLongitude));
        sb1.append("&radius=1000");
        sb1.append("&type=hospital");
        sb1.append("&key="+getResources().getString(R.string.google_maps_key));

        String url= sb1.toString();

        Object dataTransfer[]= new Object[2];

        dataTransfer[0]= mMap;
        dataTransfer[1]= url;

        GetNearbyPlacesData getNearbyPlacesData= new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
    }

    @Override
    public void onMapClick(LatLng point) {
        lat = point.latitude;
        lon = point.longitude;

        System.out.println("From Emergency onMapClick() lat is: " + lat + "\tlon is: " + lon);
    }
}