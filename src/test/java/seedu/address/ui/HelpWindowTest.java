package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Verifies the native help screen and its guide and close actions. */
public class HelpWindowTest extends FxTestSupport {
    @Test
    public void parseCommandFormats_readsAlternativesAndUpdatedSyntax() {
        String guide = String.join("\r\n",
                "# Guide with a `code example` outside a format line",
                "Format: `sort FIELD/DIRECTION [FIELD/DIRECTION]...`, `sort`, or `sort reset`",
                "Format: `clear confirm`",
                "Format: `list [-summary] [s/SUBJECT]... [t/TAG]...`",
                "Format: `archive INDEX`");
        assertEquals(List.of("sort FIELD/DIRECTION [FIELD/DIRECTION]...", "sort", "sort reset",
                "clear confirm", "list [-summary] [s/SUBJECT]... [t/TAG]...", "archive INDEX"),
                HelpWindow.parseCommandFormats(guide));
    }

    @Test
    public void parseCommandFormats_withoutFormats_rejectsEmptyHelp() {
        assertThrows(IllegalStateException.class, () -> HelpWindow.parseCommandFormats("# Guide"));
    }

    @Test
    public void commandFormats_loadFromBundledGuideAndHaveFullTooltips() throws Exception {
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow(new Stage(), url -> { });
            ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#commandScroll");
            VBox rows = (VBox) scroll.getContent();
            assertFalse(rows.getChildren().isEmpty());
            assertTrue(rows.getChildren().stream().map(Label.class::cast)
                    .anyMatch(row -> row.getText().startsWith("sort")));
            rows.getChildren().forEach(node -> {
                Label row = (Label) node;
                assertFalse(row.getText().isBlank());
                assertFalse(row.isWrapText());
                assertEquals(row.getText(), row.getTooltip().getText());
            });
        });
    }

    @Test
    public void guideLink_opensFullGuideInBrowser() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> openedUrl = new AtomicReference<>();
            HelpWindow window = new HelpWindow(new Stage(), openedUrl::set);
            Hyperlink guideLink = (Hyperlink) window.getRoot().getScene().lookup("#guideLink");
            guideLink.fire();
            assertEquals(HelpWindow.USERGUIDE_URL, openedUrl.get());
        });
    }

    @Test
    public void focus_minimisedWindow_restoresWindow() throws Exception {
        runOnFxThread(() -> {
            // Not shown, so the iconified state does not depend on the OS window manager
            HelpWindow window = new HelpWindow(new Stage(), url -> { });
            window.getRoot().setIconified(true);
            window.focus();
            assertFalse(window.getRoot().isIconified());
        });
    }

    @Test
    public void closeButton_closesStageAndHelpCanBeOpenedAgain() throws Exception {
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow(new Stage(), url -> { });
            window.show();
            window.getRoot().getScene().getRoot().applyCss();
            window.getRoot().getScene().getRoot().layout();
            Button close = (Button) window.getRoot().getScene().lookup("#closeButton");
            ImageView icon = (ImageView) close.getGraphic();
            assertFalse(icon.getImage().isError());
            assertEquals(39.32, icon.getFitWidth());
            assertEquals(39.32, icon.getFitHeight());
            assertTrue(close.isCancelButton());
            ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#commandScroll");
            assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
            close.fire();
            assertFalse(window.isShowing());
            window.show();
            assertTrue(window.isShowing());
        });
    }
}
