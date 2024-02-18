package de.dentrassi.vat.nfc.programmer.utils;

import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Editables {
    private Editables() {
    }

    public static @NonNull String getText(@Nullable final Editable editable) {
        if (editable == null) {
            return "";
        }

        return editable.toString();
    }
}
