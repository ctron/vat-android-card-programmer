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
}
