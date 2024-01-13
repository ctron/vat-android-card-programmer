package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

public interface Result {
    void apply(final @NonNull TextInputLayout layout);
}
