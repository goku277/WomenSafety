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

public class LocationService extends Service {

    ContactsDb cdb;

    SOSMessage smg;

    Profile pf;

    SmsManager smsManager;

    Context ctx;

    private static final String SMS_SEND_ACTION = "CTS_SMS_SEND_ACTION";
    private static final String SMS_DELIVERY_ACTION = "CTS_SMS_DELIVERY_ACTION";

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";


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

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            cdb = new ContactsDb(LocationService.this);

            smg = new SOSMessage(LocationService.this);

            pf = new Profile(LocationService.this);

            boolean installed = appInstalledOrNot("com.whatsapp");
            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();

                System.out.println("Latitude: " + latitude);
                System.out.println("Longitude: " + longitude);

                SQLiteDatabase db = cdb.getWritableDatabase();
                String query = "select * from contact";
                Cursor c1 = db.rawQuery(query, null);

                Set<String> numberList = new LinkedHashSet<>();

                if (c1 != null && c1.getCount() > 0) {
                    if (c1.moveToFirst()) {
                        do {
                            numberList.add(c1.getString(1));
                        } while (c1.moveToNext());
                    }
                }

                aList.addAll(numberList);

             //   boolean match = check(latitude, longitude, aList);

                //  System.out.println("From Location Service mobile number is: " + aList);

                String message = "Hi please save me its emergency! My current location is:" + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                if (c1.getCount() > 0) {

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

  /*  private boolean check(double latitude, double longitude, ArrayList<String> aList) {
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
        System.out.println("From Location latlon is: " + latlon);
        if (!latlon.isEmpty()) {
            lat = latlon.get(0);
            lon = latlon.get(1);
        }

        System.out.println("From Location lat is: " + lat + "\nFrom Location lon is: " + lon);

        if (!String.valueOf(latitude).trim().substring(0, 5).equals(lat.trim().substring(0, 5)) && !String.valueOf(longitude).trim().substring(0, 5).equals(lon.trim().substring(0, 5))) {
            for (String s : aList) {
                SmsManager mySmsManager = SmsManager.getDefault();
                s = s.replace("+91", "").replace(" ", "").trim();
                //  System.out.println("From Location Service message is: " + message);
                //  System.out.println("From Location Service mobile number is: " + s);
                mySmsManager.sendTextMessage(s, null, "Hello I am going out of my home and my current location is: " + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude, null, null);
            }
        }
        return false;
    }   */

    private boolean appInstalledOrNot(String url) {
        PackageManager packageManager = getPackageManager();
        boolean appInstalled;
        try {
            packageManager.getPackageInfo(url, packageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
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
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(50000);
        locationRequest.setFastestInterval(30000);
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
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationServoice() {
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
         //   SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        //    mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //    SM.registerListener((SensorEventListener) LocationService.this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
            // here u should make your service foreground so it will keep working even if app closed
        }
        return super.onStartCommand(intent, flags, startId);
    }

  /*  public void onSensorChanged(SensorEvent event) {
        System.out.println("From LocationService onSensorChangedInitiated:");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                Log.d("mySensor", "Acceleration is " + acceleration + "m/s^2");
                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(getApplicationContext(), "FALL DETECTED",
                            Toast.LENGTH_LONG).show();
                    System.out.println("From AccelorometerService() FALL DETECTED");
                    String message = "Hi please save me its emergency! My current location is:" + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                    for (String s : aList) {
                        SmsManager mySmsManager = SmsManager.getDefault();
                        s = s.replace("+91", "").replace(" ", "").trim();
                        System.out.println("From Location Service message is: " + message);
                        System.out.println("From Location Service mobile number is: " + s);
                        mySmsManager.sendTextMessage(s, null, message, null, null);
                    }
                }
                System.out.println("From LocationService accelaration is: " + acceleration);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }   */
}