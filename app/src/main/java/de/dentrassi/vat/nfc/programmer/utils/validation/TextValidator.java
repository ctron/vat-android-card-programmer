package de.dentrassi.vat.nfc.programmer.utils.validation;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

public abstract class TextValidator implements TextWatcher {

    private final @NonNull TextInputLayout layout;

    public TextValidator(@NonNull final TextInputLayout layout) {
        this.layout = layout;

        if (this.layout.getEditText() != null) {
            performValidate(this.layout.getEditText().getText().toString());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(final Editable s) {
        performValidate(s.toString());
    }

    private void performValidate(@NonNull final String text) {
        Result result = validate(text);
        if (result == null) {
            result = Ok.of();
        }
        result.apply(this.layout);
    }

    protected abstract @Nullable Result validate(String value);
}
