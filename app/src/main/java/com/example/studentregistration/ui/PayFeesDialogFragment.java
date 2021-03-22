package com.example.studentregistration.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.studentregistration.R;

public class PayFeesDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    private PayFeesDialogListener listener;

    public interface PayFeesDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the PayFeesDialogListener so we can send events to the host
            listener = (PayFeesDialogListener) context;
        } catch(ClassCastException e) {
            // The activity does not implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement PayFeesDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_pay_fees)
                .setPositiveButton(R.string.proceed_pay_fees, (dialog, which) -> {
                    // Send the positive button event to the host activity
                    listener.onDialogPositiveClick(PayFeesDialogFragment.this);
                })
                .setNegativeButton(R.string.cancel_pay_fees, (dialog, which) -> {
                    // Send the negative button event to the host activity
                    listener.onDialogNegativeClick(PayFeesDialogFragment.this);
                });

        return builder.create();
    }
}
