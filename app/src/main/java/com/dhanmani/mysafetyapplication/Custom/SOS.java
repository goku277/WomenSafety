package com.dhanmani.mysafetyapplication.Custom;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.dhanmani.mysafetyapplication.Database.ContactsDb;
import com.dhanmani.mysafetyapplication.Database.GeoFenceDb;
import com.dhanmani.mysafetyapplication.Database.SOSMessage;
import com.dhanmani.mysafetyapplication.R;
import com.dhanmani.mysafetyapplication.Service.Constants2;
import com.dhanmani.mysafetyapplication.Service.GeoFenceService;
import com.dhanmani.mysafetyapplication.Service.LocationService;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SOS extends AppCompatDialogFragment {

    private static final int PERMISSION_REQUEST_SEND_SMS = 0;
    CircleImageView cig;

    SensorManager sm;
    List<Sensor> los;
    Sensor accelorometer;

    Uri imageUri, videoUri;

    ContactsDb cdb;

    SOSMessage smg;

    Constants constants;

    Button stop, photo_video_location, location_periodically, start_geofence, stop_geofence;

    StorageReference storageReference, storageReference11;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    Context ctx;

    private static final String SMS_SEND_ACTION = "CTS_SMS_SEND_ACTION";
    private static final String SMS_DELIVERY_ACTION = "CTS_SMS_DELIVERY_ACTION";

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    public static final int CAMERA_PERMISSION_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int IMAGE_PICK_CODE = 2;

    private static final int VIDEO_REQUEST = 101;

    private static final int REQUEST_CODE_LOCATION_PERMISSISON = 1;

    public static final int REQUEST_CODE_PERMISSION_RESULT = 5;

    private SOSListener listener;

    double latitude, longitude;

    Animation scaleUp, scaleDown;

    GeoFenceDb geodb;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        cdb = new ContactsDb(getActivity());

        smg = new SOSMessage(getActivity());

        ctx = getActivity();

        constants= new Constants();

        geodb= new GeoFenceDb(getActivity());

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);

        storageReference = FirebaseStorage.getInstance().getReference("Videos");
        storageReference11 = FirebaseStorage.getInstance().getReference("Photos");

        checkForVideoPermission();

        requestPermission();

        getGeoCurrentLocation();

        los = new ArrayList<>();

        sm = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);

        los = sm.getSensorList(Sensor.TYPE_ALL);

        accelorometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelorometer != null) {
            System.out.println("Accelorometer has been found in this device!" + accelorometer);
            System.out.println("accelorometer value is: " + accelorometer.getPower());
        }

        // Create a constant to convert nanoseconds to seconds.

        for (int i = 0; i < los.size(); i++) {
            System.out.println(los.get(i).getName());
        }

        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sos_input_layout, null);

        profileDialog.setView(view)
                .setTitle("SOS Message")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (imageUri != null || videoUri != null) {
                            //  gotoWhatsapp();
                        }
                        listener.SOSMessageFields("");
                    }
                });

        scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        start_geofence = view.findViewById(R.id.start_geofence_id);

        start_geofence.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    start_geofence.startAnimation(scaleDown);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    start_geofence.startAnimation(scaleUp);
                  //  getGeoCurrentLocation();
                    startGeoFence();
                    Toast.makeText(getActivity(), "Geo Fence has been initiated", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        start_geofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  if (!isLocationServiceRunning()) {
              //  getGeoCurrentLocation();
              //  startGeoFence();
              //  }
              //  Toast.makeText(getActivity(), "Geo Fence has been initiated", Toast.LENGTH_SHORT).show();
            }
        });

        stop_geofence = view.findViewById(R.id.stop_geofence_id);
        stop_geofence.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    stop_geofence.startAnimation(scaleDown);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    stop_geofence.startAnimation(scaleUp);
                }

                Toast.makeText(getActivity(), "Stopping Geo Fence in few seconds!...", Toast.LENGTH_SHORT).show();
               // stopLocationService();
                stopGeoFenceLocation();


                return true;
            }
        });

        stop_geofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stopping Geo Fence in few seconds!...", Toast.LENGTH_SHORT).show();
              //  stopLocationService();
               // stopGeoFenceService();
            }
        });

        cig = view.findViewById(R.id.cig_id);

        photo_video_location = (Button) view.findViewById(R.id.share_photo_video_location_id);

        photo_video_location.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    photo_video_location.startAnimation(scaleDown);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    photo_video_location.startAnimation(scaleUp);
                }

                AlertDialog.Builder photo = new AlertDialog.Builder(getActivity());
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
              /*  photo.setNeutralButton("Upload photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                            } else {
                                pickFromGallery();
                            }
                        } else {
                            pickFromGallery();
                        }
                    }
                });    */
                photo.setNegativeButton("Click video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkForVideoPermission();
                        openVideo();
                    }
                });
                AlertDialog a1 = photo.create();
                a1.show();

                return true;
            }
        });

        photo_video_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder photo = new AlertDialog.Builder(getActivity());
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
                            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                            } else {
                                pickFromGallery();
                            }
                        } else {
                            pickFromGallery();
                        }
                    }
                });
                photo.setNegativeButton("Click video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkForVideoPermission();
                        openVideo();
                    }
                });
                AlertDialog a1 = photo.create();
                a1.show();
            }
        });

        location_periodically = view.findViewById(R.id.send_location_periodically_id);

        location_periodically.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    location_periodically.startAnimation(scaleDown);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    location_periodically.startAnimation(scaleUp);
                }

                gotoWhatsapp();

                return true;
            }
        });

        location_periodically.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  gotoWhatsapp();
            }
        });

        stop = view.findViewById(R.id.stoplocationsharing_id);

        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    stop.startAnimation(scaleDown);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    stop.startAnimation(scaleUp);
                }

                stopLocationService();

                Toast.makeText(getActivity(), "Stopping location service", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationService();
                Toast.makeText(getActivity(), "Stopping location service", Toast.LENGTH_SHORT).show();
            }
        });
        return profileDialog.create();
    }

    private void startGeoFence() {
        getGeoFenceLocation();
    }

    private void getGeoFenceLocation() {
        startGeoFenceService();
    }

    private void stopGeoFenceLocation() {
        stopGeoFenceService();
    }

    private void gotoWhatsapp() {
        boolean installed = appInstalledOrNot("com.whatsapp");
        getContinuousCurrentLocation();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_SEND_SMS);
            }
        }
    }

    private void getContinuousCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSISON);
        } else {
            startLocationService(false);
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean isGeoFenceServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (GeoFenceService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startGeoFenceService() {
        if (!isGeoFenceServiceRunning()) {
            Intent intent = new Intent(getActivity(), GeoFenceService.class);
            intent.setAction(Constants2.ACTION_START_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "GeoFence service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopGeoFenceService() {
        if (isGeoFenceServiceRunning()) {
            Intent intent = new Intent(getActivity(), GeoFenceService.class);
            intent.setAction(Constants2.ACTION_STOP_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "GeoFence Service Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationService(boolean isGeoFence) {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getActivity(), LocationService.class);
            intent.setAction(com.dhanmani.mysafetyapplication.Service.Constants.ACTION_START_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "Location service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getActivity(), LocationService.class);
            intent.setAction(com.dhanmani.mysafetyapplication.Service.Constants.ACTION_STOP_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "Location Service Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean appInstalledOrNot(String url) {
        PackageManager packageManager = getActivity().getPackageManager();
        boolean appInstalled;
        try {
            packageManager.getPackageInfo(url, packageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }

    private void openVideo() {
        // checkForVideoPermission();
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(videoIntent, VIDEO_REQUEST);
        }
    }

    private void checkForVideoPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
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
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getActivity(), "Permission required to click photo!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openVideo();
                } else {
                    Toast.makeText(getActivity(), "Permission required to record video!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getActivity(), "Permission required to upload photo!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContinuousCurrentLocation();
                }
            }
        }

        if (requestCode == REQUEST_CODE_PERMISSION_RESULT && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //   getLocation();
            } else {
                Toast.makeText(getActivity(), "Permission denied by user!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSISON && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService(false);
            } else {
                Toast.makeText(getActivity(), "Permission denied!", Toast.LENGTH_SHORT).show();
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
            String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), image, "IMG_" + Calendar.getInstance().getTime(), null);
            imageUri = Uri.parse(path);
            cig.setImageURI(imageUri);

            if (imageUri != null) {
                System.out.println("The imageUri is: " + imageUri);
                String uid = UUID.randomUUID().toString();  //
                requestPermission();
                final StorageReference ref = storageReference11.child(String.valueOf(System.currentTimeMillis()) + " " + uid);
                ref.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println("The image uri is: " + uri);
                                        String message = uri.toString();
                                        SQLiteDatabase db = cdb.getWritableDatabase();
                                        String query = "select * from contact";
                                        Cursor c1 = db.rawQuery(query, null);
                                        Set<String> contactSet = new LinkedHashSet<>();
                                        if (c1 != null && c1.getCount() > 0) {
                                            if (c1.moveToFirst()) {
                                                do {
                                                    contactSet.add(c1.getString(1));
                                                } while (c1.moveToNext());
                                            }
                                        }
                                        ArrayList<String> contactList = new ArrayList<>();
                                        contactList.addAll(contactSet);
                                        System.out.println("From SOSDialog contactList: " + contactList);

                                        //  getGeoCurrentLocation();

                                        String geoLoc = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                                        System.out.println("From image latitude is: " + latitude + "\n\n" + "From video longitude is: " + longitude);

                                        for (String s : contactList) {
                                            SmsManager sm = SmsManager.getDefault();

                                            IntentFilter sendIntentFilter = new IntentFilter(SMS_SEND_ACTION);
                                            IntentFilter receiveIntentFilter = new IntentFilter(SMS_DELIVERY_ACTION);

                                            PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_SEND_ACTION), 0);
                                            PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_DELIVERY_ACTION), 0);

                                            BroadcastReceiver messageSentReceiver = new BroadcastReceiver() {
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    switch (getResultCode()) {
                                                        case Activity.RESULT_OK:
                                                            Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                            Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                            Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                            Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                            Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                            };

                                            try {

                                                getActivity().registerReceiver(messageSentReceiver, sendIntentFilter);

                                                BroadcastReceiver messageReceiveReceiver = new BroadcastReceiver() {
                                                    @Override
                                                    public void onReceive(Context arg0, Intent arg1) {
                                                        switch (getResultCode()) {
                                                            case Activity.RESULT_OK:
                                                                Toast.makeText(getActivity(), "SMS Delivered", Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case Activity.RESULT_CANCELED:
                                                                Toast.makeText(getActivity(), "SMS Not Delivered", Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }
                                                    }
                                                };
                                                getActivity().registerReceiver(messageReceiveReceiver, receiveIntentFilter);
                                                ArrayList<String> parts = sm.divideMessage(uri.toString() + "\n\nMy Current location is:  " + geoLoc);
                                                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                                                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                                                for (int i = 0; i < parts.size(); i++) {
                                                    sentIntents.add(PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_SEND_ACTION), 0));
                                                    deliveryIntents.add(PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_DELIVERY_ACTION), 0));
                                                }
                                                sm.sendMultipartTextMessage(s, null, parts, sentIntents, deliveryIntents);
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                });
                            }
                        });   //
            }
        }

        if (requestCode == VIDEO_REQUEST && resultCode == getActivity().RESULT_OK) {
            videoUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            VideoView videoView = new VideoView(getActivity());
            videoView.setVideoURI(data.getData());
            videoView.start();
            builder.setView(videoView).show();

            System.out.println("From SOSDialog videoUri: " + videoUri);

            if (videoUri != null) {
                requestPermission();
                checkForVideoPermission();
                String uid = UUID.randomUUID().toString();
                final StorageReference ref = storageReference.child(String.valueOf(System.currentTimeMillis()) + " " + uid);
                ref.putFile(videoUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println("The video uri is: " + uri);

                                        String message = uri.toString();

                                        SQLiteDatabase db = cdb.getWritableDatabase();
                                        String query = "select * from contact";
                                        Cursor c1 = db.rawQuery(query, null);

                                        Set<String> contactSet = new LinkedHashSet<>();

                                        if (c1 != null && c1.getCount() > 0) {
                                            if (c1.moveToFirst()) {
                                                do {
                                                    contactSet.add(c1.getString(1));
                                                } while (c1.moveToNext());
                                            }
                                        }
                                        ArrayList<String> contactList = new ArrayList<>();
                                        contactList.addAll(contactSet);
                                        System.out.println("From SOSDialog contactList: " + contactList);

                                        //   getGeoCurrentLocation();

                                        String geoLoc = "My current location is\n\n" + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                                        System.out.println("From Video latitude is: " + latitude + "\n\n" + "From video longitude is: " + longitude);

                                        for (String s : contactList) {  //
                                            SmsManager sm = SmsManager.getDefault();

                                            IntentFilter sendIntentFilter = new IntentFilter(SMS_SEND_ACTION);
                                            IntentFilter receiveIntentFilter = new IntentFilter(SMS_DELIVERY_ACTION);

                                            PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_SEND_ACTION), 0);
                                            PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_DELIVERY_ACTION), 0);

                                            BroadcastReceiver messageSentReceiver = new BroadcastReceiver() {
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    switch (getResultCode()) {
                                                        case Activity.RESULT_OK:
                                                            Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                            Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                            Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                            Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                            Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                            };

                                            try {

                                                getActivity().registerReceiver(messageSentReceiver, sendIntentFilter);

                                                BroadcastReceiver messageReceiveReceiver = new BroadcastReceiver() {
                                                    @Override
                                                    public void onReceive(Context arg0, Intent arg1) {
                                                        switch (getResultCode()) {
                                                            case Activity.RESULT_OK:
                                                                Toast.makeText(getActivity(), "SMS Delivered", Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case Activity.RESULT_CANCELED:
                                                                Toast.makeText(getActivity(), "SMS Not Delivered", Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }
                                                    }
                                                };

                                                getActivity().registerReceiver(messageReceiveReceiver, receiveIntentFilter);

                                                ArrayList<String> parts = sm.divideMessage(uri.toString() + "\n\nMy Current location is:  " + geoLoc);

                                                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                                                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

                                                for (int i = 0; i < parts.size(); i++) {
                                                    sentIntents.add(PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_SEND_ACTION), 0));
                                                    deliveryIntents.add(PendingIntent.getBroadcast(ctx, 0, new Intent(SMS_DELIVERY_ACTION), 0));
                                                }
                                                sm.sendMultipartTextMessage(s, null, parts, sentIntents, deliveryIntents);
                                            } catch (Exception e) {
                                            }
                                        }  //
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }

        if (requestCode == REQUEST_CODE_PERMISSION_RESULT) {
            // getLocation();
        }
    }

    private void getGeoCurrentLocation() {
        requestPermission();
        Toast.makeText(getActivity(), "Entered in this method...", Toast.LENGTH_SHORT).show();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }
        LocationServices.getFusedLocationProviderClient(getActivity())
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(getActivity())
                                .removeLocationUpdates(this);

                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;

                            latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();

                            longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            System.out.println("Latitude is: " + latitude);
                            System.out.println("Longitude is: " + longitude);
                            Toast.makeText(getActivity(), "Latitude is: " + latitude + " and Longitude is: " + longitude, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Looper.getMainLooper());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (SOSListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        }
        ;
    }

    public interface SOSListener {
        public void SOSMessageFields(String message);
    }
}