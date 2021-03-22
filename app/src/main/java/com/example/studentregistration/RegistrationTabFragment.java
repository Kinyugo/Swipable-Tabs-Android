package com.example.studentregistration;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.studentregistration.utils.ConversionHelpers;
import com.example.studentregistration.utils.Validation;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegistrationTabFragment extends Fragment {

    private static final String TAG = "Registration";

    private static final String STUDENT_NAME = "STUDENT_NAME";
    private static final String REGISTRATION_NUMBER = "REGISTRATION_NUMBER";
    private static final String COURSE_OF_STUDY = "COURSE_OF_STUDY";
    private static final String YEAR_OF_STUDY = "YEAR_OF_STUDY";

    private TextInputLayout studentNameInputLayout;
    private TextInputLayout registrationNumberInputLayout;
    private TextInputLayout courseOfStudyInputLayout;
    private TextInputLayout yearOfStudyInputLayout;
    private ProgressBar progressBar;

    private Button registerButton;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration_tab, container, false);

        db = FirebaseFirestore.getInstance();

        studentNameInputLayout = view.findViewById(R.id.student_name);
        registrationNumberInputLayout = view.findViewById(R.id.registration_number);
        courseOfStudyInputLayout = view.findViewById(R.id.course_of_study);
        yearOfStudyInputLayout = view.findViewById(R.id.year_of_study);

        registerButton = view.findViewById(R.id.register);
        progressBar = view.findViewById(R.id.progress_bar);

        registerButton.setOnClickListener(this::onRegister);

        return view;
    }

    private void onRegister(View view) {
        // Read data from inputs
        HashMap<String, String> userData = readUserData();
        // Get errors
        HashMap<String, String> errors = validateUserData(userData);
        // Show errors
        updateUIWithErrors(errors);

        if (Validation.isUserDataValid(errors)) {
            addUserToDB(userData);
        }
    }

    private HashMap<String, String> readUserData() {
        HashMap<String, String> userData = new HashMap<>();

        userData.put(STUDENT_NAME, getStringFromInputLayout(studentNameInputLayout));
        userData.put(REGISTRATION_NUMBER, getStringFromInputLayout(registrationNumberInputLayout));
        userData.put(COURSE_OF_STUDY, getStringFromInputLayout(courseOfStudyInputLayout));
        userData.put(YEAR_OF_STUDY, getStringFromInputLayout(yearOfStudyInputLayout));

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

            if (STUDENT_NAME.equals(key)) {
                studentNameInputLayout.setError(value);
            } else if (REGISTRATION_NUMBER.equals(key)) {
                registrationNumberInputLayout.setError(value);
            } else if (COURSE_OF_STUDY.equals(key)) {
                courseOfStudyInputLayout.setError(value);
            } else if (YEAR_OF_STUDY.equals(key)) {
                yearOfStudyInputLayout.setError(value);
            }
        }
    }

    private void addUserToDB(HashMap<String, String> userData) {
        updateUILoadingState(true);

        HashMap<String, Object> userDocument = new HashMap<>();
        userDocument.put("registration_number", userData.get(REGISTRATION_NUMBER));
        userDocument.put("student_name", userData.get(STUDENT_NAME));
        userDocument.put("course_of_study", userData.get(COURSE_OF_STUDY));
        userDocument.put("year_of_study", userData.get(YEAR_OF_STUDY));

        HashMap<String, Object> feesDocument = new HashMap<>();
        feesDocument.put("registration_number", userData.get(REGISTRATION_NUMBER));
        feesDocument.put("fees", 0);


        String hexRegistrationNumber = ConversionHelpers.regToHex(userData.get(REGISTRATION_NUMBER));
        if (hexRegistrationNumber == null) {
            Toast.makeText(getView().getContext(), "Registration failed! Notify owner!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add a new user with the user id from auth
        db.collection("users")
                .document(hexRegistrationNumber)
                .set(userDocument)
                .addOnSuccessListener(aVoid -> {
                    db.collection("fees")
                            .document(hexRegistrationNumber)
                            .set(feesDocument)
                            .addOnSuccessListener(feesVoid -> {
                                Toast.makeText(getView().getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                                updateUILoadingState(false);
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "Registration failed with: " + e.getMessage());
                                Toast.makeText(getView().getContext(), "Registration failure!", Toast.LENGTH_SHORT).show();
                                updateUILoadingState(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Registration failed with: " + e.getMessage());
                    Toast.makeText(getView().getContext(), "Registration failure!", Toast.LENGTH_SHORT).show();
                    updateUILoadingState(false);
                });
    }


    private void updateUILoadingState(boolean isLoading) {
        // Hide or display the ProgressBar based on the loading state
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }


        // Activate or deactivate inputs based on the loading stated
        studentNameInputLayout.getEditText().setEnabled(!isLoading);
        registrationNumberInputLayout.getEditText().setEnabled(!isLoading);
        courseOfStudyInputLayout.getEditText().setEnabled(!isLoading);
        yearOfStudyInputLayout.getEditText().setEnabled(!isLoading);

        registerButton.setEnabled(!isLoading);
    }

}