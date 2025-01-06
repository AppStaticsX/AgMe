package com.appstaticsx.app.agmeapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.content.Context;

public class PasswordStrengthChecker {

    // Method to evaluate the password strength
    public static void getPasswordStrength(String password, EditText passwordField) {
        int score = 0;
        StringBuilder message = new StringBuilder("Password must include:\n");

        // Check password length
        if (password.length() >= 8) {
            score++;
        } else {
            message.append("- At least 8 characters\n");
        }

        // Check for uppercase letters
        if (password.matches(".*[A-Z].*")) {
            score++;
        } else {
            message.append("- At least one uppercase letter\n");
        }

        // Check for lowercase letters
        if (password.matches(".*[a-z].*")) {
            score++;
        } else {
            message.append("- At least one lowercase letter\n");
        }

        // Check for digits
        if (password.matches(".*\\d.*")) {
            score++;
        } else {
            message.append("- At least one digit\n");
        }

        // Check for special characters
        if (password.matches(".*[@#$%^&+=!].*")) {
            score++;
        } else {
            message.append("- At least one special character (@#$%^&+=!)\n");
        }

        // Show the message inside the EditText if any criteria are missing
        if (score < 5) {
            passwordField.setError(message.toString()); // Set error message inside EditText
        } else {
            passwordField.setError(null); // Clear error message if password is strong
        }
    }

    // Set the password strength watcher for the EditText
    public static void setPasswordStrengthWatcher(final EditText passwordField, final Context context) {
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String password = charSequence.toString();
                getPasswordStrength(password, passwordField); // Check password strength
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed here
            }
        });
    }
}
