package de.dentrassi.vat.nfc.programmer.nfc;

import java.util.Objects;

public class Keys {
    private final Key a;
    private final Key b;

    public Keys(Key a, Key b) {
        this.a = a;
        this.b = b;
    }

    public Key getA() {
        return this.a;
    }

    public Key getB() {
        return this.b;
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
}
