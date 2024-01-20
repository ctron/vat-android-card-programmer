package de.dentrassi.vat.nfc.programmer.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdditionalInformation {
    private final String name;
    private final String id;
    private final IdType idType;

    private AdditionalInformation(
            @NotNull final String name,
            @NotNull final String id,
            @NotNull final IdType idType
    ) {
        this.name = Objects.requireNonNull(name);
        this.id = Objects.requireNonNull(id);
        this.idType = Objects.requireNonNull(idType);
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public IdType getIdType() {
        return this.idType;
    }

    public static AdditionalInformation of(
            @NotNull final String name,
            @NotNull String id,
            @NotNull final IdType idType) {

        if (!id.isEmpty() && idType == IdType.None) {
            id = "";
        }

        return new AdditionalInformation(name, id, idType);
    }


    public static AdditionalInformation ofDefault() {
        return new AdditionalInformation("", "", IdType.None);
    }

}
