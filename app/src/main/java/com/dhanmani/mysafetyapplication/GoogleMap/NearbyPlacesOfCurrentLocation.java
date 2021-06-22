package com.dhanmani.mysafetyapplication.GoogleMap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.dhanmani.mysafetyapplication.Database.latlong;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NearbyPlacesOfCurrentLocation extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    double currentLatitude, currentLongitude;
    Location myLocation;

    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    String getPlace = "", correspondPlace = "";

    double lat, lon;

    DecimalFormat df = new DecimalFormat("#.##");

    latlong ltlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places_of_current_location);

        ltlg= new latlong(NearbyPlacesOfCurrentLocation.this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpGClient();

        System.out.println("From onCreate() lat is: " + lat + "\tlon: " + lon);
    }

    private void setUpGClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
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

        LatLng sydney = new LatLng( 28.38, 77.12);

        mMap.setOnMapClickListener(this);

        //  System.out.println("From onMapReady() lat is: " + lat + "\tlon: " + lon);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermission();
    }

    private void checkPermission() {
        int permissionLocation = ContextCompat.checkSelfPermission(NearbyPlacesOfCurrentLocation.this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> permissions = new ArrayList<>();

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {
            getMyLocation();
        }
    }

    private void getMyLocation() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(NearbyPlacesOfCurrentLocation.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    //   locationRequest.setInterval(3000);
                    //   locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
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
                                            .checkSelfPermission(NearbyPlacesOfCurrentLocation.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {


                                        myLocation = LocationServices.FusedLocationApi
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
                                        status.startResolutionForResult(NearbyPlacesOfCurrentLocation.this,
                                                REQUEST_CHECK_SETTINGS_GPS);


                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }


                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    });

                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

        } else {
            checkPermission();
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
        myLocation = location;
        if (myLocation != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        lat = latLng.latitude;
        lon = latLng.longitude;

        System.out.println("From NearbyPlacesOfCurrentLocation onMapClick() lat: " + lat + "\tlon: " + lon);

        if (lat!=0.0 && lon!=0.0) {
            AlertDialog.Builder a11= new AlertDialog.Builder(NearbyPlacesOfCurrentLocation.this);
            a11.setTitle("Confirm your home location");
            a11.setMessage("Do you want to set this location as your home location");
            a11.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String query= "select * from latlng";
                    SQLiteDatabase db= ltlg.getWritableDatabase();
                    Cursor c1= db.rawQuery(query,null);

                    if (c1.getCount() > 0) {
                        ltlg.delete();
                        ltlg.insertData(lat+"", lon+"");
                    }
                    else {
                        ltlg.insertData(lat + "", lon + "");
                    }

                    String query1= "select * from latlng";
                    SQLiteDatabase db1= ltlg.getWritableDatabase();
                    Cursor c11= db1.rawQuery(query1,null);

                    if (c11!=null && c11.getCount() > 0) {
                        if (c11.moveToFirst()) {
                            System.out.println("Latitude is: " + c11.getString(0) + "\tLongitude is: " + c11.getString(1));
                        }
                    }
                }
            });
            a11.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog a1= a11.create();
            a1.show();
        }
    }
}