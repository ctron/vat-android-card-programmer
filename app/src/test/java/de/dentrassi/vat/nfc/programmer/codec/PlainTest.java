package de.dentrassi.vat.nfc.programmer.codec;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import de.dentrassi.vat.nfc.programmer.model.CardId;

public class PlainTest {

    @Test
    public void testOk() throws Exception {
        final CardId id = Plain.decode(
                "0123450123".getBytes(StandardCharsets.US_ASCII),
                new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        );

        Assert.assertEquals(12345, id.getMemberId());
        Assert.assertEquals(123, id.getCardNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() throws Exception {
        final CardId id = Plain.decode(new byte[16], new byte[16]);

        Assert.assertEquals(12345, id.getMemberId());
        Assert.assertEquals(123, id.getCardNumber());
    }

    @Test
    public void encode() {
        final UUID uid = UUID.fromString("a9ec45d7-b56c-47a4-943e-3af3a27eedaf");
        final byte[][] data = Plain.encode(CardId.of(123, 12, uid));

        Assert.assertEquals(2, data.length);
        Assert.assertEquals(16, data[0].length);
        Assert.assertEquals(16, data[1].length);

        Assert.assertArrayEquals(data[0], new byte[]{
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

        Assert.assertArrayEquals(data[1], new byte[]{
                (byte) 0xA9,
                (byte) 0xEC,
                (byte) 0x45,
                (byte) 0xD7,
                (byte) 0xB5,
                (byte) 0x6C,
                (byte) 0x47,
                (byte) 0xA4,
                (byte) 0x94,
                (byte) 0x3E,
                (byte) 0x3A,
                (byte) 0xF3,
                (byte) 0xA2,
                (byte) 0x7E,
                (byte) 0xED,
                (byte) 0xAF,
        });

    }
}
