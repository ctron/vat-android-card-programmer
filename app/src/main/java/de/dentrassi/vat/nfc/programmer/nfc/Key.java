package de.dentrassi.vat.nfc.programmer.nfc;

import android.nfc.tech.MifareClassic;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import org.jetbrains.annotations.NotNull;

public class Key {

    private final byte[] key;

    private Key(@NonNull byte[] key) {
        this.key = key;
    }

    public @NonNull byte[] getKey() {
        return key.clone();
    }

    /**
     * Create a new key from the provided material.
     *
     * @param key the key data
     * @return the new instance
     * @throws IllegalArgumentException if the key doesn't hava length of 6 bytes
     */
    public static @NonNull Key fromData(@NotNull byte[] key) {
        final byte[] newKey = key.clone();

        if (newKey.length != 6) {
            throw new IllegalArgumentException("Key must have a length of 6 bytes");
        }

        return new Key(newKey);
    }

    /**
     * Parse a hex encoded key.
     *
     * @param key Hex encoded key
     * @return the key
     * @throws RuntimeException if anything goes wrong
     */
    public static @NonNull Key fromString(final String key) {
        return fromData(BaseEncoding.base16().decode(key));
    }

    public static @NonNull Key nfcForum() {
        return fromData(MifareClassic.KEY_NFC_FORUM);
    }
}
