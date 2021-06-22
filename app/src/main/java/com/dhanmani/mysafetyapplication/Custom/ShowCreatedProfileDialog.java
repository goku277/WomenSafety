package com.dhanmani.mysafetyapplication.Custom;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.dhanmani.mysafetyapplication.Database.Profile;
import com.dhanmani.mysafetyapplication.R;

import de.hdodenhof.circleimageview.CircleImageView;

// This is a custom alert dialog class which displays the profile of the specific registered user...

public class ShowCreatedProfileDialog extends AppCompatDialogFragment {

    CircleImageView cig;

    TextView name, mobile;

    Button location;

    Uri imageUri;

    Profile pf;

    TextView checkLocation;

    LocationManager locationManager;
    String latitude, longitude;

    private ProfileDialog.ProfileCreateListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        pf= new Profile(getActivity());

        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.showcreatedprofilelayout, null);

        profileDialog.setView(view)
                .setTitle("Check profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String Name = name.getText().toString().trim();
                        // String Specialization= specialization.getText().toString().trim();
                        String Mobile = mobile.getText().toString().trim();

                        //   getGeoCurrentLocation();

                        String getHomeAddress = checkLocation.getText().toString().trim();

                        listener.applyProfileCreateFields(Name, Mobile, imageUri, getHomeAddress);
                    }
                });

        name = (TextView) view.findViewById(R.id.input_name_id);
        //  specialization= (EditText) view.findViewById(R.id.specialization_inputfield_id);
        mobile = (TextView) view.findViewById(R.id.input_mobile_id);

        cig = (CircleImageView) view.findViewById(R.id.profile_pic_upload_id);

        checkLocation = (TextView) view.findViewById(R.id.set_your_home_location_id);


        SQLiteDatabase db= pf.getWritableDatabase();

        String query = "select * from profile";
        Cursor c1 = db.rawQuery(query, null);

        if (c1!= null && c1.getCount() > 0) {
            if (c1.moveToFirst()) {
                name.setText(c1.getString(0));
                mobile.setText(c1.getString(1));
                checkLocation.setText(c1.getString(3));

                Glide.with(getActivity()).load(c1.getString(2)).centerCrop().into(cig);
            }
        }

        return profileDialog.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ProfileDialog.ProfileCreateListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        };
    }

    public interface ProfileCreateListener {
        public void applyShowProfileCreateFields(String name1, String mobile1, Uri imageUri, String homeLocation);
    }
}
