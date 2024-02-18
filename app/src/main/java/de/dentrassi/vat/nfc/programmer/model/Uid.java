package de.dentrassi.vat.nfc.programmer.model;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable UID of a tag/chip.
 */
public class Uid {
    private final byte[] uid;

    private Uid(byte[] uid) {
        this.uid = uid;
    }

    public static Uid of(@NonNull final byte[] uid) {
        Objects.requireNonNull(uid);
        if (uid.length < 4 || uid.length > 7) {
            throw new IllegalArgumentException(String.format("UID must be between 4 and 7 bytes (both inclusive), but has a length of: %s", uid.length));
        }
        return new Uid(uid.clone());
    }

    public byte[] getUid() {
        return uid.clone();
    }

    /**
     * Check if a UID is empty, all zeroes.
     *
     * @return {@code true} if the UID is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        for (byte b : this.uid) {
            if (b > 0) {
                return false;
            }
        }

        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uid uid1 = (Uid) o;
        return Arrays.equals(uid, uid1.uid);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uid", uid)
                .toString();
    }

    public String toHex() {
        return BaseEncoding.base16().encode(this.uid);
    }
}
