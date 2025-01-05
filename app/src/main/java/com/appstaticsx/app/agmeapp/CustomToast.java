package com.appstaticsx.app.agmeapp;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * CustomToast is a utility class for displaying custom-designed toast messages
 * with an image and styled text.
 */
public class CustomToast {

    // Context for accessing application resources and layout inflator
    private final Context context;

    /**
     * Constructor to initialize the CustomToast class.
     *
     * @param context The context in which the toast will be displayed.
     */
    public CustomToast(Context context) {
        this.context = context;
    }

    /**
     * Displays a custom toast message with an image and styled text.
     *
     * @param message     The message to display in the toast.
     * @param imageResId  The resource ID of the image to display.
     */
    public void show(String message, int imageResId) {
        // Inflate the custom toast layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(
                R.layout.toast_background,
                ((Activity) context).findViewById(R.id.toast_layout_root)
        );

        // Set the image resource for the toast's ImageView
        ImageView image = layout.findViewById(R.id.image);
        image.setImageResource(imageResId);

        // Set the message text and apply custom font, gravity, and color
        TextView text = layout.findViewById(R.id.text);
        text.setText(message);
        text.setTypeface(context.getResources().getFont(R.font.lexend_medium));
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTextColor(context.getResources().getColor(R.color.textColor, null));

        // Create and configure the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
