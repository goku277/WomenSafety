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
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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

import java.util.LinkedHashSet;
import java.util.Set;

public class AccelorometerService extends Service implements SensorEventListener {
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

    Set<String> aList = new LinkedHashSet<>();

    public boolean isStopped= false;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            cdb = new ContactsDb(AccelorometerService.this);

            smg = new SOSMessage(AccelorometerService.this);

            pf = new Profile(AccelorometerService.this);

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
            }
        }
    };

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
        builder.setContentTitle("Accelorometer Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelID, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by AccelorometerSevice");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(2000);
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
        startForeground(Constants1.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationServoice() {
        System.out.println("Clicked on AccelerometerService stopLocationService:");
        isStopped= true;
        aList.clear();
        System.out.println("isStopped is: " + isStopped);
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
            SM = (SensorManager) getSystemService(SENSOR_SERVICE);
            mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SM.registerListener((SensorEventListener) AccelorometerService.this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
            // here u should make your service foreground so it will keep working even if app closed
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onSensorChanged(SensorEvent event) {
      //  System.out.println("From AccelorometerService LocationService onSensorChangedInitiated:");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
              //  Log.d("mySensor", "Acceleration is " + acceleration + "m/s^2");
                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(getApplicationContext(), "FALL DETECTED",
                            Toast.LENGTH_LONG).show();
                    String message = "Hi Fall Detected please save me its emergency! My current location is:" + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                    SQLiteDatabase db = cdb.getWritableDatabase();
                    String query = "select * from contact";
                    Cursor c1 = db.rawQuery(query, null);

                    if (c1.getCount() > 0) {
                        for (String s : aList) {
                            SmsManager mySmsManager = SmsManager.getDefault();
                            s = s.replace("+91", "").replace(" ", "").trim();
                            System.out.println("From AccelorometerService Location Service message is: " + message);
                            System.out.println("From AccelorometerService Location Service mobile number is: " + s);
                            mySmsManager.sendTextMessage(s, null, message, null, null);
                        }
                    }
                }

             //   System.out.println("From Accelerometer Service latitude: " + latitude + "\tlongitude: " + longitude);
              //  System.out.println("From Accelerometer Service accelaration is: " + acceleration);

             //   System.out.println("isStopped is: " + isStopped);
                if (isStopped) {
                    aList.clear();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}