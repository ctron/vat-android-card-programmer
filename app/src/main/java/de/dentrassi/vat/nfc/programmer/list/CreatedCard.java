package de.dentrassi.vat.nfc.programmer.list;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Objects;

import de.dentrassi.vat.nfc.programmer.data.CardId;

public class CreatedCard {
    private final @NonNull String uid;
    private final @NonNull CardId id;
    private final @NonNull ZonedDateTime timestamp;

    public CreatedCard(@NonNull final String uid,
                       @NonNull final CardId id,
                       @NonNull final ZonedDateTime timestamp) {
        this.uid = Objects.requireNonNull(uid);
        this.id = Objects.requireNonNull(id);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public @NonNull CardId getId() {
        return this.id;
    }

    public @NonNull String getUid() {
        return this.uid;
    }

    @NonNull
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }
}
