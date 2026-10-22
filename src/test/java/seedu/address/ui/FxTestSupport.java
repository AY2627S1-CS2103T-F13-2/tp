package seedu.address.ui;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import javafx.application.Platform;
import javafx.stage.Window;

/** Shares one JavaFX toolkit across UI tests and keeps assertions on its application thread. */
abstract class FxTestSupport {
    private static boolean toolkitStarted;

    @BeforeAll
    static synchronized void initializeToolkit() throws Exception {
        if (toolkitStarted) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        if (!ready.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX toolkit did not start");
        }
        toolkitStarted = true;
    }

    @AfterEach
    void closeWindows() throws Exception {
        runOnFxThread(() -> new ArrayList<>(Window.getWindows()).forEach(Window::hide));
    }

    /** Waits for the action to finish and propagates assertion failures to JUnit. */
    static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try {
            task.get(20, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new AssertionError("JavaFX test action failed", e.getCause());
        }
    }
}
