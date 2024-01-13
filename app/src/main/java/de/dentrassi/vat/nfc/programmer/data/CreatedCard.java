package de.dentrassi.vat.nfc.programmer.data;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Objects;

import de.dentrassi.vat.nfc.programmer.model.AdditionalInformation;
import de.dentrassi.vat.nfc.programmer.model.CardId;

public final class CreatedCard {
    private final @NonNull CardId id;
    private final @NonNull AdditionalInformation additional;
    private final @NonNull ZonedDateTime timestamp;

    private CreatedCard(
            @NonNull final CardId id,
            @NonNull final AdditionalInformation additional,
            @NonNull final ZonedDateTime timestamp) {
        this.id = Objects.requireNonNull(id);
        this.additional = Objects.requireNonNull(additional);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public @NonNull CardId getId() {
        return this.id;
    }

    @NonNull
    public AdditionalInformation getAdditional() {
        return this.additional;
    }

    @NonNull
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    public static CreatedCard of(
            @NonNull final CardId id,
            @NonNull final AdditionalInformation additional,
            @NonNull final ZonedDateTime timestamp) {
        return new CreatedCard(id, additional, timestamp);
    }
}
