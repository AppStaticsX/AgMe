package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

/**
 * CustomDialog is a utility class that provides a reusable custom dialog
 * with a Lottie animation and a message TextView.
 */
public class CustomDialog {
    // Dialog instance to display the custom dialog
    private final Dialog dialog;
    // LottieAnimationView for displaying animations
    private final LottieAnimationView animationView;
    // TextView to display a message
    private final TextView messageTextView;

    /**
     * Constructor that initializes the custom dialog with the provided context.
     *
     * @param context Context used to create the dialog and inflate its layout
     */
    public CustomDialog(Context context) {
        dialog = new Dialog(context);
        // Inflate the custom dialog layout
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null);
        dialog.setContentView(view);
        dialog.setCancelable(true); // Allow the dialog to be dismissible

        // Initialize views from the inflated layout
        animationView = view.findViewById(R.id.lottie_animation);
        messageTextView = view.findViewById(R.id.dialog_message);
    }

    /**
     * Sets the message to be displayed in the dialog.
     *
     * @param message The message text to display
     */
    public void setMessage(String message) {
        messageTextView.setText(message);
    }

    /**
     * Sets the animation file for the LottieAnimationView.
     *
     * @param animationFile The name of the animation file (from assets folder)
     */
    public void setAnimation(String animationFile) {
        animationView.setAnimation(animationFile);
    }

    /**
     * Displays the dialog to the user.
     */
    public void show() {
        dialog.show();
    }
}
