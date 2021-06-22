package com.dhanmani.mysafetyapplication.Custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhanmani.mysafetyapplication.R;


public class ContactsUpdateDialog extends DialogFragment {

    ContactsUpdateDialogListener listener;
    EditText oldName, newContactNumber;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.updatecontactsinputlayout, null);

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
                        String NewContactNumber = newContactNumber.getText().toString().trim();
                        String OldName = oldName.getText().toString().trim();
                        newContactNumber.setText("");
                        oldName.setText("");
                        listener.applyUpdateContactsFields(NewContactNumber, OldName);

                    }
                });

        newContactNumber = (EditText) view.findViewById(R.id.new_contact_number_id);
        oldName = (EditText) view.findViewById(R.id.old_contact_name_id);
        return profileDialog.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ContactsUpdateDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        }
        ;
    }

    public interface ContactsUpdateDialogListener {
        public void applyUpdateContactsFields(String number1, String name11);
    }
}