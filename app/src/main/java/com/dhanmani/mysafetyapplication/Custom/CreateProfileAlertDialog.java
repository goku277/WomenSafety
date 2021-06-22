package com.dhanmani.mysafetyapplication.Custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.dhanmani.mysafetyapplication.R;

public class CreateProfileAlertDialog extends AppCompatDialogFragment {
    ButtonUpdateListener listener;
    Button create, check , showcontacts;

    boolean createClicked= false, checkClicked= false, showContacts= false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.setprofile_alert_dialog_layout, null);

        profileDialog.setView(view)
                .setTitle("Create profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        create= (Button) view.findViewById(R.id.create_profile_id);
        check= (Button) view.findViewById(R.id.check_profile_id);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createClicked= true;
                checkClicked= false;
                listener.applyButtonsField(createClicked, checkClicked);
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkClicked= true;
                createClicked= false;
                showContacts= false;
                listener.applyButtonsField(createClicked, checkClicked);
            }
        });

        return profileDialog.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ButtonUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        }
    }

    public interface ButtonUpdateListener {
        public void applyButtonsField(boolean createClicked, boolean checkClicked);
    }
}