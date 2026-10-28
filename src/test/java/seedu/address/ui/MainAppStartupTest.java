package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.MainApp;

/** Exercises the real startup wiring without reading or writing the user's data. */
public class MainAppStartupTest {
    @TempDir
    public Path testFolder;

    @Test
    public void startup_helpAndExit_useApplicationServicesAndSavePreferences() throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        // Append child startup coverage to the same report as the parent test worker.
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .map(argument -> argument.replace("append=false", "append=true"))
                .map(argument -> Pattern.compile("(?<=destfile=)[^,]+").matcher(argument).replaceAll(
                    match -> Matcher.quoteReplacement(Path.of(match.group()).toAbsolutePath().toString())))
                .forEach(command::add);
        command.addAll(List.of("-ea", "-cp", System.getProperty("test.runtimeClasspath",
                System.getProperty("java.class.path")), MainAppStartupTest.class.getName()));
        Path log = testFolder.resolve("startup.log");
        Process process = new ProcessBuilder(command).directory(testFolder.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(45, TimeUnit.SECONDS), "Application startup did not finish");
            assertEquals(0, process.exitValue(), Files.readString(log));
            assertTrue(Files.isRegularFile(testFolder.resolve("preferences.json")));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly().waitFor();
            }
        }
    }

    /** Entry point for the isolated application process. */
    public static void main(String[] args) {
        Application.launch(StartupApp.class, args);
    }

    /** Runs the normal application lifecycle and checks its help and exit controls. */
    public static class StartupApp extends MainApp {
        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            assertTrue(primaryStage.isShowing());
            assertEquals("TuteeDex", primaryStage.getTitle());
            TextField input = (TextField) primaryStage.getScene().lookup("#commandTextField");
            input.setText("help");
            input.fireEvent(new ActionEvent());
            Stage help = Window.getWindows().stream().filter(Stage.class::isInstance).map(Stage.class::cast)
                    .filter(stage -> stage.getTitle().equals("Help")).findFirst().orElseThrow();
            ScrollPane scroll = (ScrollPane) help.getScene().lookup("#commandScroll");
            VBox rows = (VBox) scroll.getContent();
            assertTrue(rows.getChildren().stream().map(Label.class::cast)
                    .anyMatch(row -> row.getText().startsWith("sort")));
            input.setText("help");
            input.fireEvent(new ActionEvent());
            assertEquals(2L, Window.getWindows().stream().filter(Stage.class::isInstance).count());
            Button closeHelp = (Button) help.getScene().lookup("#closeButton");
            closeHelp.fire();
            assertFalse(help.isShowing());
            input.setText("help");
            input.fireEvent(new ActionEvent());
            assertTrue(help.isShowing());
            input.setText("exit");
            input.fireEvent(new ActionEvent());
            assertFalse(primaryStage.isShowing());
            assertFalse(help.isShowing());
            Platform.exit();
        }
    }
}
