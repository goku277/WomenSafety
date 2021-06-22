package com.dhanmani.mysafetyapplication.Custom;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;


import com.dhanmani.mysafetyapplication.Database.ContactsDb;
import com.dhanmani.mysafetyapplication.Database.latlong;
import com.dhanmani.mysafetyapplication.GoogleMap.NearbyPlacesOfCurrentLocation;
import com.dhanmani.mysafetyapplication.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


// Its a custom alert dialog to enhance the beauty of my application...

//  Here in this custom alert dialog class I have used cameras, and setting up of user's profile etc...

public class ProfileDialog extends DialogFragment {

    CircleImageView cig;

    EditText name, mobile;

    Button location, contacts;

    Uri imageUri;

    TextView checkLocation;

    ArrayList<String> ContactNumber;
    ArrayList<String> ContactName;

    Set<String> ContactsSet;

    ContactsDb cdb;

    LocationManager locationManager;
    // String latitude, longitude;

    private static final int CONTACT_PERMISSION_CODE= 1;
    private static final int CONTACT_PICK_CODE= 2;


    public static final int CAMERA_PERMISSION_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int IMAGE_PICK_CODE = 2;

    public static final int REQUEST_CODE_PERMISSION_RESULT = 5;

    private ProfileCreateListener listener;

    double latitude, longitude;

    String lat="", lon="", newLat="latitude: ", newLong="longitude: ";

    latlong ltlg;

    boolean isManual= false, isAutomatic= false;

    @Override
    public void onStart() {
        super.onStart();

        if (isManual) {

            String query = "select * from latlng";
            SQLiteDatabase db = ltlg.getWritableDatabase();
            Cursor c1 = db.rawQuery(query, null);

            if (c1 != null && c1.getCount() > 0) {
                if (c1.moveToFirst()) {
                    //  newLat += c1.getString(0);
                    //  newLong += c1.getString(1);
                    System.out.println("From ProfileDialog onCreateDialog() latitude: " + c1.getString(0) + "\tlongitude: " + c1.getString(1));

                    checkLocation.setText("latitude: " + c1.getString(0) + "\n\n" + "longitude: " + c1.getString(1));
                }
            }
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        ContactName= new ArrayList<>();
        ContactNumber= new ArrayList<>();
        ContactsSet= new LinkedHashSet<>();

        ltlg= new latlong(getActivity());

        if (isManual) {

            String query = "select * from latlng";
            SQLiteDatabase db = ltlg.getWritableDatabase();
            Cursor c1 = db.rawQuery(query, null);

            if (c1 != null && c1.getCount() > 0) {
                if (c1.moveToFirst()) {
                    //  newLat += c1.getString(0);
                    //  newLong += c1.getString(1);
                    System.out.println("From ProfileDialog onCreateDialog() latitude: " + c1.getString(0) + "\tlongitude: " + c1.getString(1));
                }
            }
        }

        getGeoCurrentLocation();

        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.createprofile, null);

        profileDialog.setView(view)
                .setTitle("Create profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String Name = name.getText().toString().trim();
                        // String Specialization= specialization.getText().toString().trim();
                        String Mobile = mobile.getText().toString().trim();

                        //   getGeoCurrentLocation();

                        String getHomeAddress = checkLocation.getText().toString().trim();

                        if (isManual) {

                            String query= "select * from latlng";
                            SQLiteDatabase db= ltlg.getWritableDatabase();
                            Cursor c1= db.rawQuery(query,null);

                            if (c1!=null && c1.getCount() > 0) {
                                if (c1.moveToFirst()) {
                                    newLat+= c1.getString(0);
                                    newLong+= c1.getString(1);
                                }
                            }

                            System.out.println("newLat is: " + newLat + "\tnewLong: " + newLong);

                            checkLocation.setText(newLat + "\n" + newLong);

                            listener.applyProfileCreateFields(Name, Mobile, imageUri, newLat + "\n" + newLong);
                        }
                        else {
                            listener.applyProfileCreateFields(Name, Mobile, imageUri, getHomeAddress);
                        }
                    }
                });


        if (isManual) {

            String query = "select * from latlng";
            SQLiteDatabase db = ltlg.getWritableDatabase();
            Cursor c1 = db.rawQuery(query, null);

            if (c1 != null && c1.getCount() > 0) {
                if (c1.moveToFirst()) {
                  //  newLat += c1.getString(0);
                  //  newLong += c1.getString(1);
                    System.out.println("From ProfileDialog onCreateDialog() latitude: " + c1.getString(0) + "\tlongitude: " + c1.getString(1));
                }
            }
        }


        name = (EditText) view.findViewById(R.id.input_name_id);
        //  specialization= (EditText) view.findViewById(R.id.specialization_inputfield_id);
        mobile = (EditText) view.findViewById(R.id.input_mobile_id);

        cig = (CircleImageView) view.findViewById(R.id.profile_pic_upload_id);

        checkLocation = (TextView) view.findViewById(R.id.set_your_home_location_id);

        location = (Button) view.findViewById(R.id.set_home_town_location_btn_id);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder a22= new AlertDialog.Builder(getActivity());
                a22.setTitle("Select between the two options provided");
                a22.setMessage("Please choose manual or automatic for setting up tour home location");
                a22.setTitle("Chosse between automatic and manual location searching");
                a22.setMessage("Please select between the provided two options");
                a22.setPositiveButton("Manual", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isManual= true;
                        startActivity(new Intent(getActivity(), NearbyPlacesOfCurrentLocation.class));

                        if (isManual) {

                            String query = "select * from latlng";
                            SQLiteDatabase db = ltlg.getWritableDatabase();
                            Cursor c1 = db.rawQuery(query, null);

                            if (c1 != null && c1.getCount() > 0) {
                                if (c1.moveToFirst()) {
                                      newLat += c1.getString(0);
                                      newLong += c1.getString(1);
                                    System.out.println("From ProfileDialog onCreateDialog() latitude: " + c1.getString(0) + "\tlongitude: " + c1.getString(1));
                                }
                            }
                        }
                    }
                });
                a22.setNegativeButton("Automatic", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isAutomatic= true;
                        isManual= false;
                        getGeoCurrentLocation();
                    }
                });
                AlertDialog a1= a22.create();
                a1.show();
            }
        });

        cig.setOnClickListener(new View.OnClickListener() {
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
                            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
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

        return profileDialog.create();
    }

  /*  private boolean checkContactPermission() {
        boolean result= ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)== (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestContactPermission() {
        String permission[]= {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(getActivity(), permission, CONTACT_PERMISSION_CODE);`
    }

    private void pickContactIntent() {
        Intent intent= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }    */

    private void getGeoCurrentLocation() {
        System.out.println("Entered into getGeoCurrentLocation");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION_RESULT);
        } else {
            getLocation();
          //  check();
        }
    }

    private void check() {
        AlertDialog.Builder a11= new AlertDialog.Builder(getActivity());
        a11.setTitle("Select between the two options provided");
        a11.setMessage("Please choose manual or automatic for setting up tour home location");
        a11.setTitle("Chosse between automatic and manual location searching");
        a11.setMessage("Please select between the provided two options");
        a11.setPositiveButton("Automatic", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getLocation();
            }
        });
        a11.setNegativeButton("Manual", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              //  Emergency e1= new Emergency();
               // startActivity(new Intent(getActivity(), NearbyPlacesOfCurrentLocation.class));
            }
        });
        AlertDialog a1= a11.create();
        a1.show();
    }

    private void getLocation() {

        try {

            System.out.println("Entered into getLocation()");

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);  // REQUEST_CODE_PERMISSION_RESULT
            }
            LocationServices.getFusedLocationProviderClient(getActivity())
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            try {
                                LocationServices.getFusedLocationProviderClient(getActivity())
                                        .removeLocationUpdates(this);
                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    int latestLocationIndex = locationResult.getLocations().size() - 1;

                                    latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                    longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                                    System.out.println("From ProfileDialog latitude is: " + latitude + " longitude: " + longitude);

                                    if (!isManual) {
                                        checkLocation.setText("latitude: " + latitude + " longitude: " + longitude);
                                        Toast.makeText(getActivity(), "Latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }, Looper.getMainLooper());

        } catch (Exception e){}
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        }
        else {
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "NEW PICTURE");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "FROM THE CAMERA");
        imageUri= getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
                else {
                    Toast.makeText(getActivity(), "Permission required to click photo!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                else {
                    Toast.makeText(getActivity(), "Permission required to upload photo!", Toast.LENGTH_SHORT).show();

                    //  pickFromGallery();
                }
            }
        }

        if (requestCode == REQUEST_CODE_PERMISSION_RESULT && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
              //  check();
            }
            else {
                Toast.makeText(getActivity(), "Permission denied by user!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickFromGallery() {
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==getActivity().RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                //  circleImageView.setImageBitmap(image);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = "";
                //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), image, "IMG_" + Calendar.getInstance().getTime(), null);
                Toast.makeText(getActivity(), "path is: " + path, Toast.LENGTH_SHORT).show();
                imageUri= Uri.parse(path);
                cig.setImageURI(imageUri);
            }
        }
        if (requestCode== IMAGE_PICK_CODE) {
            try {
                cig.setImageURI(data.getData());
                imageUri = data.getData();
                cig.setImageURI(imageUri);
            } catch (Exception e){}
        }

        if (requestCode== REQUEST_CODE_PERMISSION_RESULT) {
            getLocation();
        }

      /*  if (resultCode== getActivity().RESULT_OK) {
            if (requestCode == CONTACT_PICK_CODE) {
                Cursor c1, c2;
                Uri uri= data.getData();
                c1= getActivity().getContentResolver().query(uri, null, null, null, null);
                if (c1.moveToFirst()) {
                    String contactId= c1.getString(c1.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName= c1.getString(c1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String idResults= c1.getString(c1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold= Integer.parseInt(idResults);
                    ContactName.add(contactName);
                    if (idResultHold==1) {
                        c2= getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = " + contactId, null, null);
                        while (c2.moveToNext()) {
                            String concatNumber= c2.getString(c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            ContactNumber.add(concatNumber);
                        }
                        c2.close();
                    }
                    c1.close();
                }
            }
        }   */
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ProfileCreateListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        };
    }

    public interface ProfileCreateListener {
        public void applyProfileCreateFields(String name1, String mobile1, Uri imageUri, String homeLocation);
    }
}