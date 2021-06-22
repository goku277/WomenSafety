package com.dhanmani.mysafetyapplication.Profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dhanmani.mysafetyapplication.Custom.ProfileDialog;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfile extends AppCompatActivity {

    CircleImageView cig;

    EditText name, mobile;

    Button location;

    Uri imageUri;

    TextView checkLocation;


    public static final int CAMERA_PERMISSION_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int IMAGE_PICK_CODE = 2;

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private ProfileDialog.ProfileCreateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        requestPermission();

        name = (EditText) findViewById(R.id.input_name_id);
        //  specialization= (EditText) view.findViewById(R.id.specialization_inputfield_id);
        mobile = (EditText) findViewById(R.id.input_mobile_id);

        cig = (CircleImageView) findViewById(R.id.profile_pic_upload_id);

        checkLocation = (TextView) findViewById(R.id.set_your_home_location_id);

        location = (Button) findViewById(R.id.set_home_town_location_btn_id);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateProfile.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getGeoCurrentLocation();
                }
            }
        });

        cig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder photo = new AlertDialog.Builder(CreateProfile.this);
                photo.setTitle("Use appropriate actions");
                photo.setMessage("Upload or Click your profile photo!\n\n");
                photo.setPositiveButton("Click photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            askCameraPermission();
                        }
                    }
                });
                photo.setNeutralButton("Upload photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                            } else {
                                pickFromGallery();
                            }
                        } else {
                            pickFromGallery();
                        }
                    }
                });
                AlertDialog a1 = photo.create();
                a1.show();
            }
        });
    }

    public void requestPermission() {
        String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CALL_PHONE};
        ActivityCompat.requestPermissions(CreateProfile.this, permissions, 200);
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission required to click photo!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission required to upload photo!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGeoCurrentLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Permission denied by user!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            //  circleImageView.setImageBitmap(image);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), image, "Title", null);
            imageUri = Uri.parse(path);
            cig.setImageURI(imageUri);
        }
        if (requestCode == IMAGE_PICK_CODE) {
            try {
                cig.setImageURI(data.getData());
                imageUri = data.getData();
                cig.setImageURI(imageUri);
            } catch (Exception e) {
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getGeoCurrentLocation() {
        requestPermission();
        Toast.makeText(this, "Entered in this method...", Toast.LENGTH_SHORT).show();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(CreateProfile.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(CreateProfile.this)
                                .removeLocationUpdates(this);

                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;

                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();

                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            checkLocation.setText(
                                    String.format("Latitude: %s\nLongitude: %s", latitude, longitude)
                            );
                            System.out.println("Latitude is: " + latitude);
                            System.out.println("Longitude is: " + longitude);
                            Toast.makeText(CreateProfile.this, "Latitude is: " + latitude + " and Longitude is: " + longitude, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Looper.getMainLooper());
    }
}