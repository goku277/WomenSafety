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
import androidx.fragment.app.DialogFragment;

import com.dhanmani.mysafetyapplication.R;


public class UpdateDialog extends DialogFragment {
    UpdateListener listener;
    Button update_profile, update_contacts;

    boolean updateProfileClicked= false, updateContactsClicked= false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_dialog_layout, null);

        profileDialog.setView(view)
                .setTitle("Create profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        update_profile= (Button) view.findViewById(R.id.update_profile_btn_id);
        update_contacts= (Button) view.findViewById(R.id.update_contacts_id);

        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileClicked= true;
                updateContactsClicked= false;
                listener.applyUpdatesField(updateProfileClicked, updateContactsClicked);
            }
        });

        update_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateContactsClicked= true;
                updateProfileClicked= false;
                listener.applyUpdatesField(updateProfileClicked, updateContactsClicked);
            }
        });

        return profileDialog.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (UpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        }
    }

    public interface UpdateListener {
        public void applyUpdatesField(boolean updateProfileClicked, boolean updateContactsClicked);
    }
}