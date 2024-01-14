package de.dentrassi.vat.nfc.programmer.config;

import com.google.common.io.BaseEncoding;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

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

    @Test
    public void encrypted() throws Exception {
        final ConfigurationStore.Encrypted encrypted = ConfigurationStore.encrypt("Hello World!".getBytes(StandardCharsets.UTF_8), "foobar");
        final byte[] decrypted = ConfigurationStore.decrypt(encrypted, "foobar");

        Assert.assertEquals("Hello World!", StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(decrypted)).toString());
    }

    @Test
    public void decryptFromOpenssl() throws Exception {
        // created by: echo -n "Hello World!" | openssl aes-256-cbc -pbkdf2 -iter 65536 -a -k "foobar"
        final byte[] decrypted = ConfigurationStore.decryptFromOpenSsl("U2FsdGVkX1++fsegL5Na0jFd5fXjb+xXhxISJT35ZkQ=", "foobar");

        Assert.assertEquals("Hello World!", StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(decrypted)).toString());
    }

    @Test
    public void secretKey() throws Exception {
        final SecretKey key = ConfigurationStore.createKeyFromPassword("foobar", BaseEncoding.base16().decode("F076DA319E405AEA"));
        Assert.assertEquals("A51BDF8521AE8FC64E35A34CDCDB22813F654FEDEDDE657FCFFE08906CF074DC", BaseEncoding.base16().encode(key.getEncoded()));
    }

    @Test
    public void actual() throws Exception {
        final String original = "U2FsdGVkX1+kMBwcdE2etvIfCs1ppFWTFaEI/5m05UzRQPiEqObLrNrXHoHvJhdU\n" +
                "8uHIBLtYx+hV0pBCsG8UBJP2ZEh6NaG33ISkUyy4EjhTB1QhHfa8+8LnIl8TboYl\n" +
                "pRldtysfENYcbwXoFvbEHCRJsLlPkqp5GDqIpw6aiMHQCcCEj2FWppJY/IEEXJIS\n" +
                "w3SFKfqcaBkBhMm7WAsU3iAg8vpEscM84ZjffFytdnJXQ+BgnIsxBPQOGf3t/CCx\n";
        final byte[] blob = original.getBytes(StandardCharsets.UTF_8);
        final String password = "foobar";

        // from import

        final String data = new String(blob, StandardCharsets.UTF_8);
        final Configuration configuration = ConfigurationStore.load(new ByteArrayInputStream(ConfigurationStore.decryptFromOpenSsl(data, password)));


    }

}