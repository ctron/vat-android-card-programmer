package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.function.BooleanConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class FormValidator {
    private final Supplier<Result> validateGlobal;
    private final BooleanConsumer applyCanSubmit;
    private final List<BooleanSupplier> suppliers = new ArrayList<>();

    public FormValidator(@NonNull final Supplier<Result> validateGlobal, @NonNull final BooleanConsumer applyCanSubmit) {
        this.validateGlobal = validateGlobal;
        this.applyCanSubmit = applyCanSubmit;
    }

    public void validate() {
        boolean valid = true;
        for (var supplier : this.suppliers) {
            if (!supplier.getAsBoolean()) {
                valid = false;
            }
        }

        var result = this.validateGlobal.get();
        if (result == null) {
            result = Ok.of();
        }
        if (result.isBlocking()) {
            valid = false;
        }

        this.applyCanSubmit.accept(valid);
    }

    /**
     * Contribute to the validation state with a boolean.
     * <p>
     * If the boolean is {@code true}, then the validation is ok for this contribution.
     *
     * @param supplier The supplier contributing to the state
     */

    public void contribute(@NonNull final BooleanSupplier supplier) {
        this.suppliers.add(supplier);
    }

    public void reset() {
        this.suppliers.clear();
    }
}
