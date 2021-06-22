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


public class EmergencyDialog extends DialogFragment {
    EmergencyListener listener;
    Button emergency;

    boolean emergencyClicked= false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder profileDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.emergency_dialog_layout, null);

        profileDialog.setView(view)
                .setTitle("Create profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        emergency= (Button) view.findViewById(R.id.emergency_btn_id);


        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emergencyClicked= true;
                listener.applyEmergencyField(emergencyClicked);
            }
        });

        return profileDialog.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (EmergencyListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement this Doctor_profile_Listener");
        }
    }

    public interface EmergencyListener {
        public void applyEmergencyField(boolean emergencyClicked);
    }
}
