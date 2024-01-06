package de.dentrassi.vat.nfc.programmer.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class ConfigurationTest {

    @Test
    public void example1() throws Exception {
        final Configuration config = ConfigurationStore.load(new File("../config/example1.json").toPath());
        Assert.assertNotNull(config.getOrganizations());
        Assert.assertNotNull(config.getOrganizations().get("VAT"));

        final Keys keys = config.getKeysFor("VAT");
        Assert.assertNotNull(keys);

        Assert.assertEquals(Key.fromString("FFEEDDCCBBAA"), keys.getA());
        Assert.assertEquals(Key.fromString("AABBCCDDEEFF"), keys.getB());
    }

}
