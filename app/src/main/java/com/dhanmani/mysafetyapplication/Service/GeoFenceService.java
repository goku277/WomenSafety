package com.dhanmani.mysafetyapplication.Service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.dhanmani.mysafetyapplication.Database.ContactsDb;
import com.dhanmani.mysafetyapplication.Database.Profile;
import com.dhanmani.mysafetyapplication.Database.SOSMessage;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class GeoFenceService extends Service {

    private static final String SMS_SEND_ACTION = "CTS_SMS_SEND_ACTION";
    private static final String SMS_DELIVERY_ACTION = "CTS_SMS_DELIVERY_ACTION";

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    ContactsDb cdb;

    SOSMessage smg;

    Profile pf;

    SmsManager smsManager;

    Context ctx;

    int count1=0;


    int count = 1;
    private boolean init;
    private Sensor mySensor;
    private SensorManager SM;
    private float x1, x2, x3;
    private static final float ERROR = (float) 7.0;
    private static final float SHAKE_THRESHOLD = 15.00f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private TextView counter;

    double ax, ay, az;

    double latitude;
    double longitude;

    ArrayList<String> aList = new ArrayList<>();

    public boolean isStopped= false;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            cdb = new ContactsDb(GeoFenceService.this);

            smg = new SOSMessage(GeoFenceService.this);

            pf = new Profile(GeoFenceService.this);

            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();

                System.out.println("Latitude: " + latitude);
                System.out.println("Longitude: " + longitude);

                SQLiteDatabase db = cdb.getWritableDatabase();
                String query = "select * from contact";
                Cursor c1 = db.rawQuery(query, null);

                Set<String> numberList = new LinkedHashSet<>();

                if (!isStopped) {

                    if (c1 != null && c1.getCount() > 0) {
                        if (c1.moveToFirst()) {
                            do {
                                numberList.add(c1.getString(1));
                            } while (c1.moveToNext());
                        }
                    }

                    aList.addAll(numberList);
                }
                else {
                    aList.clear();
                }

                boolean match = check(latitude, longitude, aList);

                //  System.out.println("From Location Service mobile number is: " + aList);

                if (match && c1.getCount() > 0) {

                    String message = "I have moved from my location. This is my current location:" + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                    for (String s : numberList) {
                        ctx = getApplicationContext();

                        SmsManager sms = SmsManager.getDefault();
                        ArrayList<String> parts = sms.divideMessage(message);

                        sms.sendMultipartTextMessage(s, null, parts, null, null);

                        Toast.makeText(ctx, "sms sent", Toast.LENGTH_SHORT).show();
                    }
                    locationResult = null;
                    latitude = 0;
                    longitude = 0;
                }
            }
        }
    };

    private boolean check(double latitude, double longitude, ArrayList<String> aList) {
        SQLiteDatabase db = pf.getWritableDatabase();
        String query = "select * from profile";
        Cursor c1 = db.rawQuery(query, null);

        String latLng = "";

        if (c1 != null && c1.getCount() > 0) {
            if (c1.moveToFirst()) {
                do {
                    latLng = c1.getString(3);
                } while (c1.moveToNext());
            }
        }

        System.out.println("From Location latLng is: " + latLng);

        String getLatLng[] = latLng.split("latitude:|longitude:");

        ArrayList<String> latlon = new ArrayList<>();
        String lat = "", lon = "";
        for (String s : getLatLng) {
            if (!s.trim().isEmpty()) {
                latlon.add(s.trim());
            }
        }
        System.out.println("From GeoFenceService Location latlon is: " + latlon);
        if (!latlon.isEmpty()) {
            lat = latlon.get(0);
            lon = latlon.get(1);
        }

        System.out.println("From GeoFenceService Location lat is: " + lat + "\nFrom Location lon is: " + lon);

        System.out.println("String.valueOf(latitude).trim().substring(0, 5): " + String.valueOf(latitude).trim().substring(0, 5) + "\tlat.trim().substring(0, 5): " +
                lat.trim().substring(0, 5) + "\nString.valueOf(longitude).trim().substring(0, 5): " + String.valueOf(longitude).trim().substring(0, 5) + "\t" +
                "lon.trim().substring(0, 5): " + lon.trim().substring(0, 5));

        if (!String.valueOf(latitude).trim().substring(0, 5).equals(lat.trim().substring(0, 5)) && !String.valueOf(longitude).trim().substring(0, 5).equals(lon.trim().substring(0, 5))) {
          //  Toast.makeText(ctx, "Moved away from your home location", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationService() {
        String channelID = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                channelID);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("GeoFence Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by GeoFence Service service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(50000);
        locationRequest.setFastestInterval(25000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).
                requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants2.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationServoice() {
        System.out.println("From GeoFenceService stopLocationService initiated...");
        isStopped= true;
        aList.clear();
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationServoice();
                }
            }
            Toast.makeText(this, "Start Detecting", Toast.LENGTH_LONG).show();
            if (isStopped) {
                aList.clear();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}