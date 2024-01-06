package de.dentrassi.vat.nfc.programmer.nfc;

import android.nfc.tech.MifareClassic;

import androidx.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

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
        Objects.requireNonNull(key, "Key must not be null");

        final byte[] newKey = key.clone();

        if (newKey.length != 6) {
            throw new IllegalArgumentException("Key must have a length of 6 bytes");
        }

        return new Key(newKey);
    }

    @NonNull
    @Override
    public String toString() {
        return BaseEncoding.base16().encode(this.key);
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

    public static @NonNull Key defaultKey() {
        return fromData(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    }

    public static @NonNull Key applicationDirectory() {
        return fromData(new byte[]{(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5});
    }

    public static @NonNull Key emptyKey() {
        return fromData(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key1 = (Key) o;
        return Arrays.equals(key, key1.key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }
}
