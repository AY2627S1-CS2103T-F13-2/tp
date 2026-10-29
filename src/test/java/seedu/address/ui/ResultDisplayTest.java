package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ResultDisplayTest extends FxTestSupport {
    private ResultDisplay display;
    private ScrollPane normal;
    private ScrollPane expanded;

    @BeforeEach
    void setUp() throws Exception {
        runOnFxThread(() -> {
            Font.loadFont(getClass().getResource("/fonts/pokemon_pixel_font.ttf").toExternalForm(), 24);
            display = new ResultDisplay();
            VBox root = new VBox(display.getRoot());
            root.getStyleClass().add("tuteedex");
            Scene scene = new Scene(root, 726, 180);
            scene.getStylesheets().add(getClass().getResource("/view/TuteeDex.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            normal = (ScrollPane) root.lookup("#resultDisplay");
            expanded = (ScrollPane) root.lookup("#expandedResultDisplay");
            root.applyCss();
            root.layout();
        });
    }

    @Test
    public void feedback_shortThenLongThenShort_switchesDisplayWithoutLosingText() throws Exception {
        runOnFxThread(() -> {
            display.setFeedbackToUser("Done");
            layout(726);
            assertTrue(normal.isVisible());
            assertFalse(expanded.isVisible());
            assertNull(normal.getTooltip());

            String message = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".repeat(4);
            display.setFeedbackToUser(message);
            layout(726);
            assertFalse(normal.isVisible());
            assertTrue(expanded.isVisible());
            Label wrapped = (Label) expanded.getContent();
            assertEquals(message, wrapped.getText());
            assertTrue(wrapped.isWrapText());
            assertEquals(29.24, wrapped.getFont().getSize(), 0.001);
            assertEquals(68, expanded.getHeight(), 1);
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, expanded.getHbarPolicy());
            assertTrue(wrapped.getWidth() <= expanded.getViewportBounds().getWidth());
            assertTrue(wrapped.getHeight() <= expanded.getViewportBounds().getHeight());
            assertFalse(expanded.lookup(".scroll-bar:vertical").isVisible());

            display.setFeedbackToUser("Done again");
            layout(726);
            assertTrue(normal.isVisible());
            assertFalse(expanded.isVisible());
        });
    }

    @Test
    public void feedback_beyondTwoLines_scrollsVerticallyAndResetsForNextResult() throws Exception {
        runOnFxThread(() -> {
            String message = "Student details 中文 😀\n".repeat(100);
            display.setFeedbackToUser(message);
            layout(726);
            assertEquals(message.replaceAll("\\R", " | "), ((Label) expanded.getContent()).getText());
            assertNull(expanded.getTooltip());
            assertTrue(expanded.getContent().getBoundsInLocal().getHeight()
                    > expanded.getViewportBounds().getHeight());
            ScrollBar vertical = (ScrollBar) expanded.lookup(".scroll-bar:vertical");
            assertTrue(vertical.isVisible());
            double initialY = expanded.getContent().localToScene(0, 0).getY();
            expanded.setVvalue(1);
            layout(726);
            assertTrue(expanded.getContent().localToScene(0, 0).getY() < initialY);

            display.setFeedbackToUser("Next result ".repeat(100));
            layout(726);
            assertEquals(0, expanded.getVvalue());
            display.showSummary(2, 1);
            layout(726);
            assertFalse(normal.isVisible());
            assertFalse(expanded.isVisible());
            assertEquals(62, display.getRoot().getHeight(), 0.001);
        });
    }

    @Test
    public void feedback_resize_usesAvailableWidthInsteadOfFixedCharacterCount() throws Exception {
        runOnFxThread(() -> {
            display.setFeedbackToUser("W".repeat(60));
            layout(726);
            assertTrue(expanded.isVisible());
            layout(1400);
            assertTrue(normal.isVisible());
            assertFalse(expanded.isVisible());
            layout(600);
            assertTrue(expanded.isVisible());
            assertFalse(normal.isVisible());
        });
    }

    private void layout(double width) {
        display.getRoot().applyCss();
        display.getRoot().resize(width, display.getRoot().prefHeight(width));
        display.getRoot().applyCss();
        display.getRoot().layout();
    }
}
