package de.dentrassi.vat.nfc.programmer.utils.validation;

import android.content.res.ColorStateList;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import de.dentrassi.vat.nfc.programmer.R;

public final class Warning implements Result {

    private final String message;

    private Warning(String message) {
        this.message = message;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void apply(@NonNull final TextInputLayout layout) {
        Result.reset(layout);

        var resources = layout.getContext().getResources();

        var warning = ColorStateList.valueOf(resources.getColor(R.color.warning_color, null));

        layout.setHelperText(this.message);
        // FIXME: need to figure out non-focused color state
        layout.setBoxStrokeColor(resources.getColor(R.color.warning_color, null));
        layout.setHelperTextColor(warning);
        layout.setHintTextColor(warning);
    }

    public static Result of(@NonNull final String message) {
        return new Warning(message);
    }
}
