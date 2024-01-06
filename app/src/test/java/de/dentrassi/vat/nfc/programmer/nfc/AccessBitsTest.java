package de.dentrassi.vat.nfc.programmer.nfc;

import org.junit.Assert;
import org.junit.Test;

public class AccessBitsTest {

    @Test
    public void example1() {
        final AccessBits bits = AccessBits.fromString("FF078069");
        System.out.format("Bits: %s%n", bits);
    }

    @Test
    public void defaultBits() {
        final AccessBits bits = AccessBits.fromString("FF078069");
        System.out.format("Bits: %s%n", bits);
    }

    @Test
    public void defaultsAreDefaults() {
        final AccessBits bits = AccessBits.fromString("FF078069");
        Assert.assertEquals(bits, new AccessBits());
        Assert.assertArrayEquals(new AccessBits().getRawBits(), new byte[]{(byte) 0xFF, (byte) 0x07, (byte) 0x80, (byte) 0x69});
    }

}
