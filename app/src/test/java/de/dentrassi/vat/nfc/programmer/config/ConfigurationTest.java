package de.dentrassi.vat.nfc.programmer.config;

import org.junit.Assert;
import org.junit.Test;

import de.dentrassi.vat.nfc.programmer.nfc.Key;

public class ConfigurationTest {

    @Test
    public void example1() {
        final Configuration config = Configuration.load(ConfigurationTest.class.getResourceAsStream("example1.json"));
        Assert.assertNotNull(config.getKeys());
        Assert.assertNotNull(config.getKeys().get("VAT"));
        
        Assert.assertEquals(Key.defaultKey(), config.getKeys().get("VAT").getA());
        Assert.assertEquals(Key.fromString("AABBCCDDEEFF"), config.getKeys().get("VAT").getB());
    }

}
