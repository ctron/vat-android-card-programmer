package de.dentrassi.vat.nfc.programmer.nfc;

import com.google.common.io.BaseEncoding;

import org.junit.Assert;
import org.junit.Test;

public class SectorTrailerTest {
    /**
     * Ensure the default sector trailer matches a certain outcome.
     * <p>
     * Key A and B must be the default key
     */
    @Test
    public void defaultTest() {
        Assert.assertEquals("FFFFFFFFFFFFFF078069FFFFFFFFFFFF", BaseEncoding.base16().encode(SectorTrailer.defaultTrailer().encode()));
    }
}
