package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

public final class Error implements Result {

    private final String message;

    private Error(String message) {
        this.message = message;
    }

    @Override
    public void apply(@NonNull final TextInputLayout layout) {
        layout.setErrorEnabled(true);
        layout.setError(this.message);
    }

    public static Result of(@NonNull final String message) {
        return new Error(message);
    }
}
