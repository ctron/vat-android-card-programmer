package de.dentrassi.vat.nfc.programmer.nfc;

import java.io.IOException;

public class AuthenticationFailedException extends IOException {
    public AuthenticationFailedException() {
        super("Authentication failed");
    }
}
