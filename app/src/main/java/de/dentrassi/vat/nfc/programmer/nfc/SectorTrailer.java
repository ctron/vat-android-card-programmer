package de.dentrassi.vat.nfc.programmer.nfc;

import androidx.annotation.NonNull;

import java.util.Objects;

public class SectorTrailer {
    private final @NonNull Key a;
    private final @NonNull AccessBits accessBits;
    private final @NonNull Key b;

    private SectorTrailer(final @NonNull Key a,
                          final @NonNull AccessBits accessBits,
                          final @NonNull Key b) {
        this.a = Objects.requireNonNull(a);
        this.accessBits = Objects.requireNonNull(accessBits);
        this.b = Objects.requireNonNull(b);
    }

    public static @NonNull SectorTrailer of(final Keys keys, final AccessBits accessBits) {
        return new SectorTrailer(keys.getA(), accessBits.copy(), keys.getB());
    }

    public static @NonNull SectorTrailer defaultTrailer() {
        return new SectorTrailer(Key.defaultKey(), new AccessBits(), Key.defaultKey());
    }

    public @NonNull byte[] encode() {
        final byte[] result = new byte[16];

        System.arraycopy(
                this.a.getKey(), 0,
                result, 0,
                6
        );
        System.arraycopy(
                this.accessBits.getRawBits(), 0,
                result, 6,
                4
        );
        System.arraycopy(
                this.b.getKey(), 0,
                result, 10,
                6
        );

        return result;
    }
}

