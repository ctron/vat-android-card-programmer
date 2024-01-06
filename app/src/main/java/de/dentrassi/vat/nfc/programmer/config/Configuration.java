package de.dentrassi.vat.nfc.programmer.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class Configuration {

    private final Map<String, Organization> organizations = new LinkedHashMap<>();

    public Configuration() {
    }

    public @NonNull Map<String, Organization> getOrganizations() {
        return this.organizations;
    }

    public void store(@NonNull final Path path) {
        ConfigurationStore.store(this, path);
    }

    public @Nullable Keys getKeysFor(@NonNull final String organization) {
        final Organization org = this.organizations.get(organization);
        if (org == null) {
            return null;
        } else {
            return org.getKeys();
        }
    }
}
