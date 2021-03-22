package com.example.studentregistration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.studentregistration.utils.ConversionHelpers;
import com.example.studentregistration.utils.Validation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class UnitsTabFragment extends Fragment {
    public static final int MAXIMUM_UNITS = 5;

    private static final String TAG = "UnitsRegistration";
    private static final String UNITS_COLLECTION = "units";
    private static final String USERS_COLLECTION = "users";
    private static final String REGISTRATION_NUMBER = "registration_number";
    private static final String UNITS = "units";

    private final HashMap<Integer, String> selectedUnits = new HashMap<>();

    private final Integer[] checkBoxIds = new Integer[]{
            R.id.checkbox_artificial_intelligence,
            R.id.checkbox_compiler_construction,
            R.id.checkbox_theory_of_computing,
            R.id.checkbox_computer_security,
            R.id.checkbox_mobile_programming,
            R.id.checkbox_discrete_mathematics,
            R.id.checkbox_data_structures
    };

    private TextInputLayout registrationInputLayout;
    private TextView unitsErrorTextView;
    private Button registerUnitsButton;
    private HashMap<String, Object> userData;
    private FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_units_tab, container, false);

        // Instantiate onClick listener for checkboxes
        setCheckBoxesOnClickListeners(view);
        // Instantiate onClick listener for register units button
        setRegisterUnitsOnClickListener(view);
        // Instantiate views
        registrationInputLayout = view.findViewById(R.id.registration_number);
        unitsErrorTextView = view.findViewById(R.id.units_error);
        registerUnitsButton = view.findViewById(R.id.register_units);
        // Instantiate FirebaseFirestore database
        db = FirebaseFirestore.getInstance();

        return view;
    }

    private void setCheckBoxesOnClickListeners(View view) {
        for(Integer checkBoxId : checkBoxIds) {
            CheckBox currentCheckBox = view.findViewById(checkBoxId);
            currentCheckBox.setOnClickListener(this::onCheckBoxClicked);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onCheckBoxClicked(View view) {
        // Is the view checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which CheckBox is clicked
        switch (view.getId()) {
            case R.id.checkbox_artificial_intelligence:
                addOrRemoveUnit(R.id.checkbox_artificial_intelligence, R.string.artificial_intelligence, checked);
                break;
            case R.id.checkbox_compiler_construction:
                addOrRemoveUnit(R.id.checkbox_compiler_construction, R.string.compiler_construction, checked);
                break;
            case R.id.checkbox_theory_of_computing:
                addOrRemoveUnit(R.id.checkbox_theory_of_computing, R.string.theory_of_computing, checked);
                break;
            case R.id.checkbox_computer_security:
                addOrRemoveUnit(R.id.checkbox_computer_security, R.string.computer_security, checked);
                break;
            case R.id.checkbox_mobile_programming:
                addOrRemoveUnit(R.id.checkbox_mobile_programming, R.string.mobile_programming, checked);
                break;
            case R.id.checkbox_discrete_mathematics:
                addOrRemoveUnit(R.id.checkbox_discrete_mathematics, R.string.discrete_maths, checked);
                break;
            case R.id.checkbox_data_structures:
                addOrRemoveUnit(R.id.checkbox_data_structures, R.string.data_structures, checked);
                break;
            default:
                break;
        }
    }

    private void addOrRemoveUnit(int unitId, int unitNameId, boolean checked) {

        String unitName = getString(unitNameId);

        if (checked) {
            if (selectedUnits.size() < MAXIMUM_UNITS) {
                selectedUnits.put(unitId, unitName);
            } else {
                Toast.makeText(Objects.requireNonNull(getView()).getContext(), "Reached maximum number of units", Toast.LENGTH_SHORT).show();
                // Deselect the selected checkbox
                CheckBox selectedCheckBox = Objects.requireNonNull(getActivity()).findViewById(unitId);
                Objects.requireNonNull(selectedCheckBox).setChecked(false);
            }
        } else {
            selectedUnits.remove(unitId);
        }
    }

    private void setRegisterUnitsOnClickListener(View view) {
        view.findViewById(R.id.register_units).setOnClickListener(this::onRegisterUnitsClicked);
    }

    public void onRegisterUnitsClicked(View view) {
        // Read user data from inputs
        userData = readUserData();
        // Check for errors in the user data
        HashMap<String, String> errors = validateUserData(userData);
        // Update the UI to show errors
        updateUIWithErrors(errors);

        if (Validation.isUserDataValid(errors)) {
           authenticateUserAndRegisterUnits();
        }
    }


    private HashMap<String, Object> readUserData() {
        HashMap<String, Object> capturedData = new HashMap<>();

        capturedData.put(REGISTRATION_NUMBER, getStringFromInputLayout(registrationInputLayout));
        capturedData.put(UNITS, selectedUnits.values());

        return capturedData;
    }

    private String getStringFromInputLayout(TextInputLayout targetInputLayout) {
        return Objects.requireNonNull(targetInputLayout.getEditText()).getText().toString();
    }

    private HashMap<String, String> validateUserData(HashMap<String, Object> userData) {
        HashMap<String, String> errors = new HashMap<>();

        // Check errors for each of the fields in the userData
        String registrationNumberError = Validation.validateString((String) userData.get(REGISTRATION_NUMBER));
        String unitsError = Validation.validateUnits((Collection<String>) Objects.requireNonNull(userData.get(UNITS)));
        // Set errors for each field
        errors.put(REGISTRATION_NUMBER, registrationNumberError);
        errors.put(UNITS, unitsError);

        return errors;
    }

    private void updateUIWithErrors(HashMap<String, String> errors) {
        // Set errors for the registration number
        registrationInputLayout.setError(errors.get(REGISTRATION_NUMBER));
        // Set errors for the units
        if(errors.get(UNITS) != null) {
            unitsErrorTextView.setText(errors.get(UNITS));
            unitsErrorTextView.setVisibility(View.VISIBLE);
        } else {
            unitsErrorTextView.setText(null);
            unitsErrorTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void authenticateUserAndRegisterUnits() {
        // Show loading progress bar and deactivate inputs
        updateUILoadingState(true);
        // Send request to fetch user document with the given document id(registration_number)
        db.collection(USERS_COLLECTION)
                .document(ConversionHelpers.regToHex((String) userData.get(REGISTRATION_NUMBER)))
                .get()
                .addOnCompleteListener(this::onFetchUserComplete);
    }

    private void onFetchUserComplete(Task<DocumentSnapshot> task) {
        String message;

        if (task.isSuccessful()) {
            DocumentSnapshot userDocument = task.getResult();
            if (userDocument.exists()) {
                makeRegisterUnitsRequest();
            } else {
                message = "Please register first! Or check your registration number!";
                finalizeRequest(message);
            }
        } else {
            message = "Try again later! Or check your connection!";
            finalizeRequest(message);
        }
    }


    private void makeRegisterUnitsRequest() {
        String registrationNumber = (String) userData.get(REGISTRATION_NUMBER);
        List units = new ArrayList<>(selectedUnits.values());

        // Prepare data to send to firebasefirestore
        HashMap<String, Object> unitsData= new HashMap<>();
        unitsData.put(REGISTRATION_NUMBER, registrationNumber.trim());
        unitsData.put(UNITS, units);

        // Send request to save units into a document with the given id(registration_number)
       db.collection(UNITS_COLLECTION)
               .document(ConversionHelpers.regToHex((String) userData.get(REGISTRATION_NUMBER)))
               .set(unitsData)
               .addOnCompleteListener(this::onRegisterUnitsComplete);
    }

    private void onRegisterUnitsComplete(Task<Void> task) {
        String message;

        if (task.isSuccessful()) {
            message = "Successfully registered ";
        } else {
            message = "Could not register units!";
        }
        finalizeRequest(message);
    }

    private void finalizeRequest(String message) {
        Toast.makeText(getView().getContext(), message, Toast.LENGTH_SHORT).show();
        updateUILoadingState(false);
    }

    private void updateUILoadingState(boolean isLoading) {
        // Deactivate or activate text input for registration number
        registrationInputLayout.getEditText().setEnabled(!isLoading);
        // Deactivate or activate checkboxes for the units
        for(int checkBoxId : checkBoxIds) {
            CheckBox currentCheckBox = getView().findViewById(checkBoxId);
            currentCheckBox.setEnabled(!isLoading);
        }
        // Deactivate or activate the send button
        registerUnitsButton.setEnabled(!isLoading);
    }

}