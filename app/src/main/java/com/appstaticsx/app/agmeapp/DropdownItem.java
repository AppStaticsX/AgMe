package com.appstaticsx.app.agmeapp;

import androidx.annotation.NonNull;

public class DropdownItem {
    private final String text;
    private final int iconResId;

    public DropdownItem(String text, int iconResId) {
        this.text = text;
        this.iconResId = iconResId;
    }

    public String getText() {
        return text;
    }

    public int getIconResId() {
        return iconResId;
    }

    @NonNull
    @Override
    public String toString() {
        return text; // Ensures the correct text is displayed in the AutoCompleteTextView
    }
}

