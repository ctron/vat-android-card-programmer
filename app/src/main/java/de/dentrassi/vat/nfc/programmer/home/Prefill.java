package de.dentrassi.vat.nfc.programmer.home;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Pre-fill information for Home fragment
 */
public class Prefill {

    @NonNull
    private final String memberId;
    @NonNull
    private final String holderName;
    @NonNull
    private final String holderId;
    @NonNull
    private final String holderIdType;

    private Prefill(
            @NonNull final String memberId,
            @NonNull final String holderName,
            @NonNull final String holderId,
            @NonNull final String holderIdType
    ) {
        this.memberId = memberId;
        this.holderName = holderName;
        this.holderId = holderId;
        this.holderIdType = holderIdType;
    }

    @NonNull
    public String getMemberId() {
        return this.memberId;
    }

    @NonNull
    public String getHolderName() {
        return this.holderName;
    }

    @NonNull
    public String getHolderId() {
        return this.holderId;
    }

    @NonNull
    public String getHolderIdType() {
        return this.holderIdType;
    }

    public static Prefill of(final Uri uri) {
        final var memberId = getOrEmpty(uri, "mid");
        final var holderName = getOrEmpty(uri, "hn");
        final var holderId = getOrEmpty(uri, "hid");
        final var holderIdType = getOrEmpty(uri, "hidt");

        return new Prefill(memberId, holderName, holderId, holderIdType);
    }

    private static @NonNull String getOrEmpty(final Uri uri, final String key) {
        var value = uri.getQueryParameter(key);
        return Objects.requireNonNullElse(value, "");
    }

}
