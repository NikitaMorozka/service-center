package ru.servicecenter.client.controller;

import javafx.scene.Node;
import javafx.scene.control.Label;
import ru.servicecenter.client.api.ApiException;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.UiMessages;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class BaseViewController {

    private final AtomicInteger loadGeneration = new AtomicInteger(0);
    private Label messageLabel;

    protected void bindMessageLabel(Label label) {
        this.messageLabel = label;
    }

    protected void showError(String message) {
        UiMessages.showError(messageLabel, message);
    }

    protected void showInfo(String message) {
        UiMessages.showInfo(messageLabel, message);
    }

    protected void showSuccess(String message) {
        UiMessages.showSuccess(messageLabel, message);
    }

    protected void clearMessage() {
        UiMessages.clear(messageLabel);
    }

    protected static String errorText(Throwable error) {
        return UiMessages.errorText(error);
    }

    protected static boolean isAccessDenied(Throwable error) {
        if (!(error instanceof ApiException)) {
            return false;
        }
        String msg = error.getMessage();
        return msg != null && (msg.toLowerCase().contains("запрещ") || msg.toLowerCase().contains("forbidden"));
    }

    protected void showErrorUnlessAccessDenied(Throwable error) {
        if (!isAccessDenied(error)) {
            showError(errorText(error));
        }
    }

    protected int nextLoadGeneration() {
        return loadGeneration.incrementAndGet();
    }

    protected boolean isLatestLoad(int generation) {
        return loadGeneration.get() == generation;
    }

    protected <T> void runAsync(Callable<T> task, Consumer<T> onSuccess) {
        int generation = nextLoadGeneration();
        setBusy(true);
        FxTasks.run(
                task,
                result -> {
                    if (isLatestLoad(generation)) {
                        setBusy(false);
                        onSuccess.accept(result);
                    }
                },
                error -> {
                    if (isLatestLoad(generation)) {
                        setBusy(false);
                        showError(errorText(error));
                    }
                }
        );
    }

    protected void runAsync(Callable<Void> task, Runnable onSuccess) {
        runAsync(task, ignored -> onSuccess.run());
    }

    protected void setBusy(boolean busy) {
        for (Node node : busyNodes()) {
            node.setDisable(busy);
        }
    }

    protected Node[] busyNodes() {
        return new Node[0];
    }
}
