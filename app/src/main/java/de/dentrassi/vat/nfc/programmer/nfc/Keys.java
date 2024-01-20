package de.dentrassi.vat.nfc.programmer.nfc;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Keys {
    private final @NonNull Key a;
    private final @NonNull Key b;

    public Keys(@NonNull final Key a, @NonNull final Key b) {
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
    }

    public @NonNull Key getA() {
        return this.a;
    }

    public @NonNull Key getB() {
        return this.b;
    }

    public static @NonNull Keys defaultKeys() {
        return new Keys(Key.defaultKey(), Key.defaultKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keys keys = (Keys) o;
        return Objects.equals(a, keys.a) && Objects.equals(b, keys.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    public static Keys of(@NonNull final Key a, @NonNull final Key b) {
        return new Keys(a, b);
    }
}
