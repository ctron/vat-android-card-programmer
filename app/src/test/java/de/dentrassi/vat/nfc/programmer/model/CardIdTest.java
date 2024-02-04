package de.dentrassi.vat.nfc.programmer.model;

import org.junit.Assert;
import org.junit.Test;

public class CardIdTest {

    @Test
    public void testValid() {
        final CardId id = CardId.of(1234, new byte[]{1, 2, 3, 4, 0, 0, 0});
        Assert.assertTrue(id.validateUid(new byte[]{1, 2, 3, 4}));
    }

    @Test
    public void testInvalid() {
        final CardId id = CardId.of(1234, new byte[]{1, 2, 3, 4, 0, 0, 1});
        Assert.assertFalse(id.validateUid(new byte[]{1, 2, 3, 4}));
    }
}
