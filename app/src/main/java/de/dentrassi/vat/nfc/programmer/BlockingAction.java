package de.dentrassi.vat.nfc.programmer;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

public abstract class BlockingAction<T> {
    private final BiConsumer<T, Exception> handler;

    protected BlockingAction(@NonNull final BiConsumer<T, Exception> handler) {
        this.handler = handler;
    }

    protected abstract T process() throws Exception;

    public void run() {
        new Thread(() -> {
            try {
                final T result = BlockingAction.this.process();
                BlockingAction.this.complete(result, null);
            } catch (final Exception e) {
                BlockingAction.this.complete(null, e);
            }
        }).start();
    }

    private void complete(final T result, final Exception ex) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> this.handler.accept(result, ex));
    }
}
