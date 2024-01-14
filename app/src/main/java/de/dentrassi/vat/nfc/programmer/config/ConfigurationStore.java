package de.dentrassi.vat.nfc.programmer.config;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.dentrassi.vat.nfc.programmer.nfc.Key;

public final class ConfigurationStore {

    private static final String TAG = ConfigurationStore.class.getName();

    private ConfigurationStore() {
    }

    private static final class KeyAdapter implements JsonSerializer<Key>, JsonDeserializer<Key> {

        @Override
        public Key deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Key.fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(Key src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    public static Configuration load(@NonNull final Path path) throws Exception {
        try (final Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(in);
        }
    }

    public static Configuration load(@NonNull final Reader in) throws Exception {
        return newGson().fromJson(in, Configuration.class);
    }

    public static Configuration load(@NonNull final InputStream in) throws Exception {
        return load(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public static void store(@NonNull final Configuration configuration, @NonNull final Path path) {
        try (final Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            newGson()
                    .toJson(configuration, out);
        } catch (final Exception e) {
            Log.w(TAG, "Failed to store configuration", e);
        }
    }

    private static @NonNull Gson newGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Key.class, new KeyAdapter())
                .create();
    }

    /**
     * Derive a key from a password.
     *
     * @param password the password to derive the key from.
     * @return the derived key.
     * @throws Exception if anything goes wrong
     */
    static @NonNull SecretKeySpec createKeyFromPassword(final String password, final byte[] salt) throws Exception {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    public static class Encrypted {
        public byte[] iv;
        public byte[] data;
        public byte[] salt;

        public Encrypted(@NonNull byte[] salt, @NonNull byte[] iv, @NonNull byte[] data) {
            this.salt = Objects.requireNonNull(salt);
            this.iv = Objects.requireNonNull(iv);
            this.data = Objects.requireNonNull(data);
        }
    }

    public static @NonNull Encrypted encrypt(final @NonNull byte[] data, final @NonNull String password) throws Exception {
        byte[] salt = "JustForTesting".getBytes(StandardCharsets.UTF_8);
        final SecretKey key = createKeyFromPassword(password, salt);
        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        final AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        return new Encrypted(salt, iv, cipher.doFinal(data));
    }

    public static @NonNull byte[] decrypt(final @NonNull Encrypted encrypted, final @NonNull String password) throws Exception {
        final SecretKey key = createKeyFromPassword(password, encrypted.salt);
        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(encrypted.iv));
        return cipher.doFinal(encrypted.data);
    }

    public static @NonNull byte[] decryptFromOpenSsl(final @NonNull String input, final @NonNull String password) throws Exception {

        // need the mime decoder for newlines
        final ByteBuffer data = ByteBuffer.wrap(Base64.getMimeDecoder().decode(input));

        // skip 8 bytes (Salted__)
        data.get(new byte[8]);

        // get salt
        final byte[] salt = new byte[8];
        data.get(salt);

        // decode data

        final byte[] decodedData = new byte[data.remaining()];
        data.get(decodedData);

        final int keylen = 32;
        final int ivlen = 16;

        final byte[] keyAndIv = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256")
                .generateSecret(new PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        65536,
                        (keylen + ivlen) * 8))
                .getEncoded();

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(keyAndIv, 0, keylen, "AES"),
                new IvParameterSpec(keyAndIv, keylen, ivlen)
        );

        return cipher.doFinal(decodedData);
    }
}
