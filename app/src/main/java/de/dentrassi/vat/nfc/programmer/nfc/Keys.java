package de.dentrassi.vat.nfc.programmer.nfc;

public class Keys {
    private final Key a;
    private final Key b;

    public Keys(Key a, Key b) {
        this.a = a;
        this.b = b;
    }

    public Key getA() {
        return a;
    }

    public Key getB() {
        return b;
    }

    public static Keys defaultKeys() {
        // FIXME: remove this key and make it configurable
        return new Keys(Key.nfcForum(), Key.fromString("AABBCCDDEEFF"));
    }
}
