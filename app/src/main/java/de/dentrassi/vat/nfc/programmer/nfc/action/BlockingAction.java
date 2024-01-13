package de.dentrassi.vat.nfc.programmer.nfc.action;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.BiConsumer;

public abstract class BlockingAction<T> {

    private static final String TAG = BlockingAction.class.getName();

    private final BiConsumer<T, Exception> handler;

    protected BlockingAction(@NonNull final BiConsumer<T, Exception> handler) {
        this.handler = handler;
    }

    protected abstract @Nullable T process() throws Exception;

    public void run() {
        Log.d(TAG, "Spawning operation: " + this);

        new Thread(() -> {
            Log.d(TAG, "Running operation: " + this);
            try {
                final T result = BlockingAction.this.process();
                BlockingAction.this.complete(result, null);
            } catch (final Exception e) {
                BlockingAction.this.complete(null, e);
            }
        }).start();
    }

    private void complete(final T result, final Exception ex) {
        Log.d(TAG, "Operation completed: " + this);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> this.handler.accept(result, ex));
    }
}
