package com.appstaticsx.app.agmeapp;

// CustomDialog.java
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;
import com.tomer.fadingtextview.FadingTextView;

public class ProcessingCustomDialog {
    private final Dialog dialog;
    private final LottieAnimationView animationView;
    private final FadingTextView messageTextView;

    public ProcessingCustomDialog(Context context) {
        dialog = new Dialog(context);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.processing_custom_dialog_layout, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        // Make the dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        animationView = view.findViewById(R.id.lottie_animation);
        messageTextView = view.findViewById(R.id.dialog_message);
    }

    public void setMessage(String[] message) {
        messageTextView.setTexts(message);
    }

    public void setAnimation(String animationFile) {
        animationView.setAnimation(animationFile);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}

