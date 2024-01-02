package de.dentrassi.vat.nfc.programmer.list;

import androidx.annotation.NonNull;

import java.util.Objects;

import de.dentrassi.vat.nfc.programmer.data.CardId;

public class CreatedCard {
    private final String uid;
    private final CardId id;

    public CreatedCard(@NonNull final String uid, @NonNull final CardId id) {
        this.uid = Objects.requireNonNull(uid);
        this.id = Objects.requireNonNull(id);
    }

    public @NonNull CardId getId() {
        return this.id;
    }

    public @NonNull String getUid() {
        return this.uid;
    }
}
