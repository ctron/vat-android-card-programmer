package de.dentrassi.vat.nfc.programmer;

import android.nfc.Tag;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class TagAction<T> extends BlockingAction<T> {
    protected final Tag tag;

    protected TagAction(@NonNull Tag tag, @NonNull final BiConsumer<T, Exception> handler) {
        super(handler);
        this.tag = tag;
    }

    /**
     * Get the tag as a certain technology, or fail.
     *
     * @param getter    The getter
     * @param otherwise The message to throw.
     * @param <U>       The actual type
     * @return The tag specific instance.
     */
    @NonNull
    protected <U> U getTagAs(Function<Tag, U> getter, String otherwise) {
        final U tag = getter.apply(this.tag);
        if (tag != null) {
            return tag;
        }
        throw new IllegalArgumentException(String.format("Tag not supported: %s", otherwise));
    }

}
