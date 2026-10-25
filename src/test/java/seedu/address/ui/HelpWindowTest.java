package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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
    public void commandFormats_matchUserGuideAndHaveFullTooltips() throws Exception {
        Pattern code = Pattern.compile("`([^`]+)`");
        List<String> formats = Files.readAllLines(Path.of("docs/UserGuide.md")).stream()
                .filter(line -> line.startsWith("Format: "))
                .flatMap(line -> code.matcher(line).results().map(match -> match.group(1)))
                .toList();
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow(new Stage(), url -> { });
            ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#commandScroll");
            VBox rows = (VBox) scroll.getContent();
            assertEquals(formats, rows.getChildren().stream().map(node -> ((Label) node).getText()).toList());
            rows.getChildren().forEach(node -> {
                Label row = (Label) node;
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
    public void closeButton_closesStageAndHelpCanBeOpenedAgain() throws Exception {
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow();
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
