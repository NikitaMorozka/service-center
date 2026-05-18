package ru.servicecenter.client.util;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Выполнение сетевых и прочих блокирующих операций вне JavaFX Application Thread.
 */
public final class FxTasks {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "service-center-client");
        thread.setDaemon(true);
        return thread;
    });

    private FxTasks() {
    }

    public static <T> void run(
            Callable<T> background,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        EXECUTOR.submit(() -> {
            try {
                T result = background.call();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        });
    }

    public static void runVoid(
            Runnable background,
            Runnable onSuccess,
            Consumer<Throwable> onError
    ) {
        run(() -> {
            background.run();
            return null;
        }, ignored -> onSuccess.run(), onError);
    }
}
