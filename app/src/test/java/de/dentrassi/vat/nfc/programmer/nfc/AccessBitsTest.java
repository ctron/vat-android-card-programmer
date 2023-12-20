package de.dentrassi.vat.nfc.programmer.nfc;

import org.junit.Test;

public class AccessBitsTest {

    @Test
    public void example1() {
        final AccessBits bits = AccessBits.fromString("FF078069");
        System.out.format("Bits: %s%n", bits);
    }

}
