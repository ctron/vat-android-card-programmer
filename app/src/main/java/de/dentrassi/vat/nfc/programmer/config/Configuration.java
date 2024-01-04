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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import de.dentrassi.vat.nfc.programmer.nfc.Key;
import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class Configuration {

    private static final String TAG = "Configuration";

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

    private final Map<String, Keys> keys = new HashMap<>();

    public Configuration() {
    }

    public Map<String, Keys> getKeys() {
        return this.keys;
    }

    public static Configuration load(final Path path) {
        try (final Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(in);
        } catch (final Exception e) {
            Log.w(TAG, "Failed to load config", e);
            return new Configuration();
        }
    }

    public static Configuration load(final Reader in) {
        try {
            return newGson().fromJson(in, Configuration.class);
        } catch (final Exception e) {
            return new Configuration();
        }
    }

    public static Configuration load(final InputStream in) {
        return load(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public void store(final Path path) {
        try (final Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            newGson()
                    .toJson(this, out);
        } catch (final Exception e) {
            Log.w(TAG, "Failed to store configuration", e);
        }
    }

    protected static @NonNull Gson newGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Key.class, new KeyAdapter())
                .create();
    }
}
