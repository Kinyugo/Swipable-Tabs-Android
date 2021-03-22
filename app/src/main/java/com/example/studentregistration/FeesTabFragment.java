package com.example.studentregistration;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studentregistration.ui.PayFeesDialogFragment;
import com.example.studentregistration.utils.ConversionHelpers;
import com.example.studentregistration.utils.Validation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class FeesTabFragment extends Fragment {

    private static final String TAG = "FeesTabFragment";

    private static final String REGISTRATION_NUMBER = "REGISTRATION_NUMBER";
    private static final String FEES = "FEES";
    private static final int REQUIRED_FEES = 50_000;

    private TextInputLayout registrationNumberInputLayout;
    private TextInputLayout feesInputLayout;
    private Button sendButton;
    private ProgressBar progressBar;

    private HashMap<String, String> userData;

    private FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_fees_tab, container, false);

        db = FirebaseFirestore.getInstance();

        registrationNumberInputLayout = view.findViewById(R.id.registration_number);
        feesInputLayout = view.findViewById(R.id.fees_paid);
        sendButton = view.findViewById(R.id.send);
        progressBar = view.findViewById(R.id.progress_bar);

        sendButton.setOnClickListener(this::onSend);

        return view;
    }

    private void onSend(View view) {
        // Read data from inputs
        userData = readUserData();

        HashMap<String, String> errors = validateUserData(userData);
        // Show errors
        updateUIWithErrors(errors);

        if (Validation.isUserDataValid(errors)) {
            if (Integer.parseInt(userData.get(FEES)) < REQUIRED_FEES) {
                remindUserToPayFullFees();
            } else {
                authenticateUserAndPayFees();
            }
        }
    }

    public void remindUserToPayFullFees() {
      Toast.makeText(getView().getContext(), "Please remember to pay full fee amount of 50,000!", Toast.LENGTH_LONG).show();
      authenticateUserAndPayFees();
    }

    private void authenticateUserAndPayFees() {
        updateUILoadingState(true);
        db.collection("users")
                .document(ConversionHelpers.regToHex(userData.get(REGISTRATION_NUMBER)))
                .get()
                .addOnCompleteListener(this::onFetchUser);
    }

    private void onFetchUser(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                payFees();
            } else {
                Toast.makeText(getView().getContext(), "Please register first! Or check your registration number!", Toast.LENGTH_LONG).show();
                updateUILoadingState(false);
            }
        } else {
            Toast.makeText(getView().getContext(), "Try again later! Or check your connection!", Toast.LENGTH_LONG).show();
            updateUILoadingState(false);
        }
    }

    private void payFees() {
        DocumentReference feesReference = db.collection("fees")
                .document(ConversionHelpers.regToHex(userData.get(REGISTRATION_NUMBER)));

        Double paidFees = Double.parseDouble(userData.get(FEES));
        FieldValue incrementBy = FieldValue.increment(paidFees);

        HashMap<String, Object> feesData = new HashMap<>();
        feesData.put("registration_number", userData.get(REGISTRATION_NUMBER).trim());
        feesData.put("fees", incrementBy);

        feesReference
                .update(feesData)
                .addOnCompleteListener(this::onPayFeesComplete);
    }

    private void onPayFeesComplete(Task<Void> voidTask) {
        if (voidTask.isSuccessful()) {
            Toast.makeText(getView().getContext(), "Successfully paid fees!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getView().getContext(), "Could not pay fees! Try again later!", Toast.LENGTH_SHORT).show();
        }

        updateUILoadingState(false);
    }

    private HashMap<String, String> readUserData() {
        HashMap<String, String> userData = new HashMap<>();

        userData.put(REGISTRATION_NUMBER, getStringFromInputLayout(registrationNumberInputLayout));
        userData.put(FEES, getStringFromInputLayout(feesInputLayout));

        return userData;
    }

    private String getStringFromInputLayout(TextInputLayout targetInputLayout) {
        return targetInputLayout.getEditText().getText().toString();
    }

    private HashMap<String, String> validateUserData(HashMap<String, String> userData) {

        HashMap<String, String> errors = new HashMap<>();

        for (HashMap.Entry userDataEntry : userData.entrySet()) {
            String key = (String) userDataEntry.getKey();
            String value = (String) userDataEntry.getValue();

            // Holds the current error message
            String errorMsg;
            errorMsg = Validation.validateString(value);
            errors.put(key, errorMsg);
        }

        return errors;
    }

    private void updateUIWithErrors(HashMap<String, String> userDataErrors) {
        for (HashMap.Entry userDataErrorsEntry : userDataErrors.entrySet()) {
            String key = (String) userDataErrorsEntry.getKey();
            String value = (String) userDataErrorsEntry.getValue();

            if (REGISTRATION_NUMBER.equals(key)) {
                registrationNumberInputLayout.setError(value);
            } else if (FEES.equals(key)) {
                feesInputLayout.setError(value);
            }
        }
    }

    private void updateUILoadingState(boolean isLoading) {
        // Hide or display the ProgressBar based on the loading state
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }


        // Activate or deactivate inputs based on the loading stated
        registrationNumberInputLayout.getEditText().setEnabled(!isLoading);
        feesInputLayout.getEditText().setEnabled(!isLoading);

        sendButton.setEnabled(!isLoading);
    }
}