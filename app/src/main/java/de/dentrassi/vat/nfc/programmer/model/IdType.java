package de.dentrassi.vat.nfc.programmer.model;

import android.content.Context;
import android.text.Editable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.dentrassi.vat.nfc.programmer.R;

// NOTE: Must be aligned with the string array in `id_types`.
public enum IdType {
    None,
    CardNumber,
    DriversLicense,
    Other;

    private static final String TAG = IdType.class.getSimpleName();

    /**
     * Get the ID type from a localized text.
     * <p>
     * This is required as the input control with the dropdown doesn't return an index, but a
     * translated text from the UI only.
     *
     * @param context       the context to use for retrieving the UI resource.
     * @param localizedText the localized text, from the UI control
     * @return the parsed type, or {@link IdType#Other} if it couldn't be matched.
     */
    public static @NonNull IdType fromLocalizedText(final @Nullable Context context, final @Nullable Editable localizedText) {
        if (context == null || localizedText == null) {
            return IdType.Other;
        }

        final String localizedString = localizedText.toString();

        final String[] values = context.getResources().getStringArray(R.array.id_types);
        for (int i = 0; i < Math.min(values.length, IdType.values().length); i++) {
            if (values[i].equals(localizedString)) {
                return IdType.values()[i];
            }
        }

        return IdType.Other;
    }

    public @NonNull String toLocalizedText(final @NonNull Context context) {
        final String[] values = context.getResources().getStringArray(R.array.id_types);
        try {
            return values[this.ordinal()];
        } catch (final Exception e) {
            Log.w(TAG, String.format("Unable to get label for: %s", this), e);
            return "";
        }
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
