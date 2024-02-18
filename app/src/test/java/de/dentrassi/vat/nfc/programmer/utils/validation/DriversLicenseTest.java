package de.dentrassi.vat.nfc.programmer.utils.validation;

import org.junit.Assert;
import org.junit.Test;

public class DriversLicenseTest {
    @Test
    public void testOk() {
        Assert.assertTrue(DriversLicense.isValidGermanLicenseNumber("B072RRE2I55"));
    }

    @Test
    public void testNotOkEmpty() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber(""));
    }

    @Test
    public void testNotOkWrong() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber("B072RRE2I35"));
    }

    @Test
    public void testNotOkWrongIncrement() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber("B072RRE2I3a"));
    }

    @Test
    public void testNotOkWrongIncrement2() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber("B072RRE2I30"));
    }

    @Test
    public void testNotOkRandomString() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber("Führerscheinnummer"));
    }

    @Test
    public void testNotOkRandomChars() {
        Assert.assertFalse(DriversLicense.isValidGermanLicenseNumber("P[]2@a,.<>?"));
    }
}
