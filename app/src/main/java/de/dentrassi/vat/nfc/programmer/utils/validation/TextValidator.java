package de.dentrassi.vat.nfc.programmer.utils.validation;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

public abstract class TextValidator implements TextWatcher {

    private final @NonNull TextInputLayout layout;
    private final @NonNull FormValidator validator;

    private Result lastResult = Ok.of();

    public TextValidator(@NonNull final TextInputLayout layout, @NonNull final FormValidator validator) {
        this.layout = layout;
        this.validator = validator;
        validator.contribute(() -> lastResult instanceof Ok);

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
        this.lastResult = Result.runWith(this.layout, () -> validate(text));
        this.lastResult.apply(this.layout);

        // trigger validation update
        this.validator.validate();
    }

    protected abstract @Nullable Result validate(String value);
}
