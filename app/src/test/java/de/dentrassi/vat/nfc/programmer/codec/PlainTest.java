package de.dentrassi.vat.nfc.programmer.codec;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import de.dentrassi.vat.nfc.programmer.model.CardId;

public class PlainTest {

    @Test
    public void testOk() throws Exception {
        final CardId id = Plain.decode("0123450123".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(12345, id.getMemberId());
        Assert.assertEquals(123, id.getCardNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() throws Exception {
        final CardId id = Plain.decode(new byte[16]);
        Assert.assertEquals(12345, id.getMemberId());
        Assert.assertEquals(123, id.getCardNumber());
    }

    @Test
    public void encode() {
        final byte[] data = Plain.encode(CardId.of(123, 12));

        Assert.assertEquals(16, data.length);

        Assert.assertArrayEquals(data, new byte[]{
                (byte) '0',
                (byte) '0',
                (byte) '0',
                (byte) '1',
                (byte) '2',
                (byte) '3',
                (byte) '0',
                (byte) '0',
                (byte) '1',
                (byte) '2',
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
        });
    }
}
