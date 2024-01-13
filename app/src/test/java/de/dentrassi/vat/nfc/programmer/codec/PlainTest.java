package de.dentrassi.vat.nfc.programmer.codec;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import de.dentrassi.vat.nfc.programmer.model.CardId;

public class PlainTest {

    @Test
    public void testOk() throws Exception {
        final CardId id = Plain.decode(
                new byte[]{0, 0, 0, 123, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 0}
        );

        Assert.assertEquals(123, id.getMemberId());
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7}, id.getUid());
    }

    @Test
    public void testNull() throws Exception {
        final CardId id = Plain.decode(new byte[16]);

        Assert.assertEquals(0, id.getMemberId());
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0}, id.getUid());
    }

    @Test
    public void encodeDecode() throws Exception {
        final UUID uid = UUID.fromString("a9ec45d7-b56c-47a4-943e-3af3a27eedaf");
        final byte[] data = Plain.encode(CardId.of(999999, new byte[]{1, 2, 3, 4, 5, 6, 7}));

        Assert.assertEquals(16, data.length);

        Assert.assertArrayEquals(new byte[]{
                (byte) 0x00,
                (byte) 0x0F,
                (byte) 0x42,
                (byte) 0x3F,
                (byte) 1,
                (byte) 2,
                (byte) 3,
                (byte) 4,
                (byte) 5,
                (byte) 6,
                (byte) 7,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
        }, data);

        final CardId result = Plain.decode(data);
        Assert.assertEquals(999999, result.getMemberId());

    }
}
