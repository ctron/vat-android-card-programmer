package de.dentrassi.vat.nfc.programmer.nfc;

import android.nfc.Tag;

import androidx.annotation.NonNull;

public interface TagFragment {
    /**
     * Handle a discovered tag.
     *
     * @param tag The tag discovered.
     * @return {@code true} if the tag was handled, {@code false} otherwise.
     */
    boolean tagDiscovered(@NonNull Tag tag);
}
