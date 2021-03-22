package com.example.studentregistration.utils;

import android.text.TextUtils;
import android.util.Patterns;

import com.example.studentregistration.UnitsTabFragment;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Validation {

    public static boolean isUserDataValid(HashMap<String, String> userDataErrors) {
        return Collections.frequency(userDataErrors.values(), null) == userDataErrors.size();
    }

    public static String validateString(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Cannot be empty!";
        }

        return null;
    }

    public static String validateUnits(Collection<String> targetCollection) {
        if (targetCollection.isEmpty() || targetCollection.size() < UnitsTabFragment.MAXIMUM_UNITS) {
            return "Please select at least five units!";
        }

        return null;
    }
}
