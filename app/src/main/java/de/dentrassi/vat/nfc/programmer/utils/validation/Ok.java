package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

public final class Ok implements Result {

    public static final Result INSTANCE = new Ok();

    private Ok() {
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void apply(@NonNull final TextInputLayout layout) {
        Result.reset(layout);
    }

    public static Result of() {
        return INSTANCE;
    }
}
