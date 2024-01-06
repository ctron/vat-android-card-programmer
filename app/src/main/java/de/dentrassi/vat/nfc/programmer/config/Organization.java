package de.dentrassi.vat.nfc.programmer.config;

import de.dentrassi.vat.nfc.programmer.nfc.Keys;

public class Organization {
    private Keys keys = Keys.defaultKeys();

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public Keys getKeys() {
        return keys;
    }
}
