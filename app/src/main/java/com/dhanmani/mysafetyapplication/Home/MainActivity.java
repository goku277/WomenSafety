package com.dhanmani.mysafetyapplication.Home;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dhanmani.mysafetyapplication.Credential.Signin;
import com.dhanmani.mysafetyapplication.Custom.ContactsUpdateDialog;
import com.dhanmani.mysafetyapplication.Custom.CreateProfileAlertDialog;
import com.dhanmani.mysafetyapplication.Custom.EmergencyDialog;
import com.dhanmani.mysafetyapplication.Custom.ProfileDialog;
import com.dhanmani.mysafetyapplication.Custom.SOS;
import com.dhanmani.mysafetyapplication.Custom.ShowCreatedProfileDialog;
import com.dhanmani.mysafetyapplication.Custom.UpdateDialog;
import com.dhanmani.mysafetyapplication.Database.ContactsDb;
import com.dhanmani.mysafetyapplication.Database.Profile;
import com.dhanmani.mysafetyapplication.R;
import com.dhanmani.mysafetyapplication.Service.AccelorometerService;
import com.dhanmani.mysafetyapplication.Service.Constants1;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ProfileDialog.ProfileCreateListener, View.OnClickListener, ShowCreatedProfileDialog.ProfileCreateListener, SOS.SOSListener, ContactsUpdateDialog.ContactsUpdateDialogListener, CreateProfileAlertDialog.ButtonUpdateListener, UpdateDialog.UpdateListener, EmergencyDialog.EmergencyListener {

    private static final int PROXIMITY_RADIUS = 25;
    private static final String TAG = "MainActivity";
    ImageView createprofile, updateprofile, deleteprofile, emmergency, create_ten_contacts_img, check_ten_contacts_img;


    TextView create, update, delete, emmergency_1, create_ten_contacts_text, check_ten_concats;

    FirebaseStorage storage;
    StorageReference storageReference;

    String uriPath= "";

    Profile pf;

    ContactsDb cdb;

    private static final int CONTACT_PERMISSION_CODE= 1;
    private static final int CONTACT_PICK_CODE= 2;

    ArrayList<String> ContactNumber;
    ArrayList<String> ContactName;

    Set<String> ContactsSet;

    private GoogleMap map;
    UiSettings mapSettings;



    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, 200);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }

        createprofile = (ImageView) findViewById(R.id.user_img_id);
        create= (TextView) findViewById(R.id.user_text_id);

        storage= FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        pf= new Profile(MainActivity.this);

        cdb= new ContactsDb(MainActivity.this);

        // cdb.delete();

        ContactNumber= new ArrayList<>();
        ContactName= new ArrayList<>();

        ContactsSet = new LinkedHashSet<>();


        updateprofile= (ImageView) findViewById(R.id.update_img_id);
        update= (TextView) findViewById(R.id.updateprofile_text_id);

        deleteprofile= (ImageView) findViewById(R.id.delete_img_id);
        delete= (TextView) findViewById(R.id.delete_text_id);

        emmergency= (ImageView) findViewById(R.id.emmergency_img_id);
        emmergency_1= (TextView) findViewById(R.id.emmergency_text_id);

        createprofile.setOnClickListener(this);
        create.setOnClickListener(this);

        updateprofile.setOnClickListener(this);
        update.setOnClickListener(this);

        deleteprofile.setOnClickListener(this);
        delete.setOnClickListener(this);

        emmergency.setOnClickListener(this);
        emmergency_1.setOnClickListener(this);

        create_ten_contacts_img= (ImageView) findViewById(R.id.input_ten_contacts_id);
        create_ten_contacts_text= (TextView) findViewById(R.id.input_ten_contacts_text_id);

        create_ten_contacts_img.setOnClickListener(this);
        create_ten_contacts_text.setOnClickListener(this);

        check_ten_contacts_img= (ImageView) findViewById(R.id.check_contacts_img_id);
        check_ten_concats= (TextView) findViewById(R.id.check_contacts_text_id);

        check_ten_contacts_img.setOnClickListener(this);
        check_ten_concats.setOnClickListener(this);

        InitiateFallDetected();
    }

    private void InitiateFallDetected() {
        startAccelorometer();
    }

    private void startAccelorometer() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(MainActivity.this, AccelorometerService.class);
            intent.setAction(Constants1.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(MainActivity.this, "Accelorometer service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAccelorometer() {
        System.out.println("stopAcceleration initiated...");
       // if (isLocationServiceRunning()) {
            Intent intent = new Intent(MainActivity.this, AccelorometerService.class);
            intent.setAction(Constants1.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(MainActivity.this, "Accelerometer Service Stopped", Toast.LENGTH_SHORT).show();
      //  }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (AccelorometerService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, Menu.NONE,"Logout");
        menu.add(0, Menu.FIRST+1, Menu.NONE, "Stop Accelerometer Service");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Signin.class));
                finish();
                break;
            case Menu.FIRST+1:
                stopAccelorometer();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void applyProfileCreateFields(String name1, String mobile1, Uri imageUri, String homeLocation) {
        System.out.println("From MainActivity applyProfileCreateFields(): " + name1 + " " + mobile1 + " " + homeLocation);
        System.out.println("imageUri==null? " + imageUri==null);
        saveProfileData(name1, mobile1, imageUri, homeLocation);
    }

    private void saveProfileData(final String name1, final String mobile1, Uri imageUri, final String homeLocation) {
        if (imageUri != null) {
            final StorageReference ref = storageReference.child("Profile Pics/" + UUID.randomUUID().toString());

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Please wait...");
            pd.show();

            try {

                ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "Profile pic uploaded successfully", Toast.LENGTH_SHORT).show();

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(MainActivity.this, "Uploaded Image url: " + uri+"", Toast.LENGTH_SHORT).show();
                                uriPath= uri + "";
                                System.out.println("uriPath is: " + uriPath);

                                SQLiteDatabase db= pf.getWritableDatabase();

                                String query = "select * from profile";
                                Cursor c1 = db.rawQuery(query, null);

                                if (c1!= null && c1.getCount() > 0) {
                                    Toast.makeText(MainActivity.this, "User cannot create multiple profiles!", Toast.LENGTH_SHORT).show();
                                }

                                else {
                                    pf.insertData(name1,mobile1, uriPath, homeLocation);
                                    Toast.makeText(MainActivity.this, "Data successfully saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "Error while uploading profile pic", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Uploaded: " + progress + "%");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_img_id:
                createProfile();
                break;
            case R.id.user_text_id:
                createProfile();
                break;
            case R.id.update_img_id:
                update();
                break;
            case R.id.updateprofile_text_id:
                update();
                break;
            case R.id.delete_img_id:
                delete();
                break;
            case R.id.delete_text_id:
                delete();
                break;
            case R.id.emmergency_img_id:
                emmergency();
                break;
            case R.id.emmergency_text_id:
                emmergency();
                break;
            case R.id.input_ten_contacts_id:
                inputTenContacts();
                displayStoredContactsAndUpload();
                break;
            case R.id.input_ten_contacts_text_id:
                inputTenContacts();
                displayStoredContactsAndUpload();
                break;
            case R.id.check_contacts_img_id:
                checkTenContacts();
                break;
            case R.id.check_contacts_text_id:
                checkTenContacts();
                break;
        }
    }

    private void checkTenContacts() {
        SQLiteDatabase db= cdb.getWritableDatabase();
        String query = "select * from contact";
        Cursor c1 = db.rawQuery(query, null);
        StringBuilder sb1= new StringBuilder();
        if (c1!= null && c1.getCount() > 0) {
            if (c1.moveToFirst()) {
                do {
                    ContactsSet.add(c1.getString(0) + "\t" + c1.getString(1) + "\n\n\n\n");
                } while (c1.moveToNext());
            }
        }

        String str= ContactsSet + "";

        str= str.replace("[","").replace("]","").replace(",","").trim();

        AlertDialog.Builder a100= new AlertDialog.Builder(MainActivity.this);

        a100.setMessage(str);

        a100.setTitle("Check Contacts");

        a100.setIcon(R.drawable.contact_icon);

        a100.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        a100.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog a111= a100.create();
        a111.show();
    }

    private void inputTenContacts() {
        Toast.makeText(this, "Clicked on inputTenContacts()", Toast.LENGTH_SHORT).show();
        if (checkContactPermission()) {
            pickContactIntent();
            recreate();
        } else {
            requestContactPermission();
        }
    }

    private void delete() {
        pf.delete();
        ContactsSet.clear();
        ContactName.clear();
        ContactNumber.clear();
        Toast.makeText(this, "Data deleted Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void update() {
        openUpdate();
    }

    private void openUpdate() {
        UpdateDialog upd= new UpdateDialog();
        upd.show(getSupportFragmentManager(), "Update");
    }

    private void contactsUpdate() {
        AlertDialog.Builder a11= new AlertDialog.Builder(MainActivity.this);
        a11.setTitle("Update contacts");
        a11.setMessage("Choose appropriate action");
        a11.setPositiveButton("Delete all contacts", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cdb.delete();
                recreate();
            }
        });
        a11.setNegativeButton("Update a single contact", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openContactsUpdateDialog();
            }
        });
        AlertDialog a1= a11.create();
        a1.show();
    }

    private void openContactsUpdateDialog() {
        ContactsUpdateDialog cud= new ContactsUpdateDialog();
        cud.show(getSupportFragmentManager(), "Update Contacts");
    }

    private void profileUpdate() {
        pf.delete();
       // recreate();
        createProfile();
    }

    private void displayStoredContactsAndUpload() {
        System.out.println("From displayStoredContactsAndUpload() Contact Name: " + ContactName + " And Contact Number: " + ContactNumber);
        Toast.makeText(this, "Contact Name: " + ContactName + " Contact Number: " + ContactNumber, Toast.LENGTH_SHORT).show();
        SQLiteDatabase db= cdb.getWritableDatabase();
        String query = "select * from contact";
        Cursor c1 = db.rawQuery(query, null);
        if (!ContactName.isEmpty() && !ContactNumber.isEmpty()) {
            for (int i = 0, i1 = 0; (i < ContactName.size() && i1 < ContactNumber.size()); i++, i1++) {
                cdb.insertData(ContactName.get(i), ContactNumber.get(i1));
            }
        }
    }

    private void emmergency() {
        openEmergency();
    }

    private void openEmergency() {
        EmergencyDialog ed= new EmergencyDialog();
        ed.show(getSupportFragmentManager(), "Emergency");
    }

    private void buttonPress() {
        openSOSDialog();
    }

    private void openSOSDialog() {
        SOS sd= new SOS();
        sd.show(getSupportFragmentManager(), "SOS Message");
    }

    private boolean checkContactPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)== (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestContactPermission() {
        String permission[]= {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE);
    }

    private void pickContactIntent() {
        Intent intent= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                pickContactIntent();
            }
            else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK) {
            if (requestCode == CONTACT_PICK_CODE) {
                Cursor c1, c2;
                Uri uri= data.getData();
                c1= getContentResolver().query(uri, null, null, null, null);
                if (c1.moveToFirst()) {
                    String contactId= c1.getString(c1.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName= c1.getString(c1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String idResults= c1.getString(c1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold= Integer.parseInt(idResults);
                    ContactName.add(contactName);
                    if (idResultHold==1) {
                        c2= getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = " + contactId, null, null);
                        while (c2.moveToNext()) {
                            String concatNumber= c2.getString(c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            ContactNumber.add(concatNumber);
                        }
                        displayStoredContactsAndUpload();
                        c2.close();
                    }
                    c1.close();
                }
            }
        }
    }

    private void createProfile() {
        showOptions();
    }

    private void showOptions() {
        CreateProfileAlertDialog cpd= new CreateProfileAlertDialog();
        cpd.show(getSupportFragmentManager(), "Profile");
    }

    private void checkProfile() {
        SQLiteDatabase db= pf.getWritableDatabase();

        String query = "select * from profile";
        Cursor c1 = db.rawQuery(query, null);

        if (c1.getCount() == 0) {
            Toast.makeText(this, "Create profile first!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (c1!=null && c1.getCount() > 0) {
                if (c1.moveToFirst()) {
                    openShowProfileDialog();
                }
            }
        }
    }

    private void openShowProfileDialog() {
        ShowCreatedProfileDialog spd= new ShowCreatedProfileDialog();
        spd.show(getSupportFragmentManager(), "Show Created Profile");
    }

    private void openDialog() {
        ProfileDialog pd= new ProfileDialog();
        pd.show(getSupportFragmentManager(), "Profile Creation");
    }

    @Override
    public void applyShowProfileCreateFields(String name1, String mobile1, Uri imageUri, String homeLocation) {

    }

    @Override
    public void SOSMessageFields(String message) {

    }

    @Override
    public void applyUpdateContactsFields(String newContactNumber1, String name11) {
        System.out.println("From applyUpdateContactsFields() name11 is: " + name11);
        String newContactNumber= newContactNumber1, oldName= name11;
        cdb.delete(oldName);
        cdb.insertData(oldName, newContactNumber);
        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();
        recreate();
    }

    @Override
    public void applyButtonsField(boolean createClicked, boolean checkClicked) {
        if (createClicked) {
            openDialog();
            createClicked= false;
        }
        else if (checkClicked) {
            checkProfile();
            checkClicked= false;
        }
        else {
            SQLiteDatabase db= cdb.getWritableDatabase();
            String query = "select * from contact";
            Cursor c1 = db.rawQuery(query, null);
            StringBuilder sb1= new StringBuilder();
            if (c1!= null && c1.getCount() > 0) {
                if (c1.moveToFirst()) {
                    do {
                        ContactsSet.add(c1.getString(0) + "\t" + c1.getString(1) + "\n\n\n\n");
                    } while (c1.moveToNext());
                }
            }

            String str= ContactsSet + "";

            str= str.replace("[","").replace("]","").replace(",","").trim();

            AlertDialog.Builder a100= new AlertDialog.Builder(MainActivity.this);

            a100.setMessage(str);

            a100.setTitle("Check Contacts");

            a100.setIcon(R.drawable.contact_icon);

            a100.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            a100.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog a111= a100.create();
            a111.show();
        }
    }

    @Override
    public void applyUpdatesField(boolean updateProfileClicked, boolean updateContactsClicked) {
        if (updateProfileClicked) {
            profileUpdate();
        }
        else {
            contactsUpdate();
        }
    }

    @Override
    public void applyEmergencyField(boolean emergencyClicked) {
        if (emergencyClicked) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            buttonPress();
        }
    }
}