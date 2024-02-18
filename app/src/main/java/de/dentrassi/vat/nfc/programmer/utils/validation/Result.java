package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Supplier;

public interface Result {
    void apply(final @NonNull TextInputLayout layout);

    static Result runWith(final @NonNull TextInputLayout layout, @NonNull final Supplier<Result> supplier) {
        var result = supplier.get();
        if (result == null) {
            result = Ok.of();
        }

        result.apply(layout);

        return result;
    }
}
