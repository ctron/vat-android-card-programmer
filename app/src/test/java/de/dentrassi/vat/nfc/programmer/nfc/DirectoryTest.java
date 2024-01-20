package de.dentrassi.vat.nfc.programmer.nfc;

import org.junit.Assert;
import org.junit.Test;

public class DirectoryTest {
    @Test
    public void test1() {
        Directory directory = Directory.empty();
        directory.setInfoSector(1);
        directory.setApplication(1, ApplicationIds.VAT);
        byte[][] output = directory.toData();


        Assert.assertArrayEquals(new byte[]{
                (byte) 0x59, 0x01, 0x01, 0x78, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        }, Tools.concatBlocks(output));
    }
}
