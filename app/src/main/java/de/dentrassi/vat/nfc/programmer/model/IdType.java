package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// NOTE: Must be aligned with the string array in `id_types`.
public enum IdType {
    None,
    CardNumber,
    DriversLicense,
    Other;

    public static IdType fromOrdinal(int ordinal) {
        return IdType.values()[ordinal];
    }

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case None:
                return "none";
            case CardNumber:
                return "card_number";
            case DriversLicense:
                return "drivers_license";
            default:
            case Other:
                return "other";
        }
    }

    public static @NonNull IdType fromString(@Nullable final String value) {
        if (value == null) {
            return IdType.Other;
        }

        switch (value.toLowerCase()) {
            case "":
            case "none":
                return IdType.None;
            case "card_number":
                return IdType.CardNumber;
            case "drivers_license":
                return IdType.DriversLicense;
            default:
                return IdType.Other;
        }
    }
}
