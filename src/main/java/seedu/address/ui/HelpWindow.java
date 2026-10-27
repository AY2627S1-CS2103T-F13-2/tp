package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL =
            "https://ay2627s1-cs2103t-f13-2.github.io/tp/UserGuide.html";
    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final Consumer<String> openBrowser;

    @FXML
    private VBox commandFormats;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     * @param openBrowser Browser action supplied by the running application.
     */
    public HelpWindow(Stage root, Consumer<String> openBrowser) {
        super(FXML, root);
        this.openBrowser = requireNonNull(openBrowser);
        Font.loadFont(HelpWindow.class.getResource("/fonts/pokemon_pixel_font.ttf").toExternalForm(), 24);
        Font.loadFont(HelpWindow.class.getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 24);
        for (String format : readCommandFormats()) {
            Label row = new Label(format);
            row.getStyleClass().add("command-format");
            row.setMinWidth(0);
            row.setMaxWidth(Double.MAX_VALUE);
            Tooltip tooltip = new Tooltip(format);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(640);
            row.setTooltip(tooltip);
            commandFormats.getChildren().add(row);
        }
    }

    private static List<String> readCommandFormats() {
        try (var guide = requireNonNull(HelpWindow.class.getResourceAsStream("/docs/UserGuide.md"),
                "Bundled User Guide is missing")) {
            return parseCommandFormats(new String(guide.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read the bundled User Guide", e);
        }
    }

    /** Reads the inline-code formats from each Format line in the User Guide. */
    static List<String> parseCommandFormats(String guide) {
        Pattern code = Pattern.compile("`([^`]+)`");
        List<String> formats = guide.lines()
                .filter(line -> line.startsWith("Format: "))
                .flatMap(line -> code.matcher(line).results().map(match -> match.group(1)))
                .toList();
        if (formats.isEmpty()) {
            throw new IllegalStateException("The User Guide contains no command formats");
        }
        return formats;
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Opens the full user guide in the default browser.
     */
    @FXML
    private void openUserGuide() {
        openBrowser.accept(USERGUIDE_URL);
    }

    @FXML
    private void closeWindow() {
        getRoot().close();
    }
}
