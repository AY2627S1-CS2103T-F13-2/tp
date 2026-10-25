package seedu.address.ui;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL =
            "https://ay2627s1-cs2103t-f13-2.github.io/tp/UserGuide.html";
    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private static final List<String> COMMAND_FORMATS = List.of(
            "help",
            "add n/NAME a/ADDRESS p/PHONE_NUMBER d/START_DATE s/SUBJECT day/DAY time/TIME"
                    + " [s/SUBJECT day/DAY time/TIME]... [o/OUTSTANDING_AMOUNT] [t/TAG]... [notes/NOTE]",
            "view INDEX",
            "export",
            "import FILE_PATH [confirm]",
            "list [-summary] [s/SUBJECT]...",
            "list [-summary] [t/TAG]...",
            "list -archived",
            "list -subjects",
            "edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [d/START_DATE] [o/OUTSTANDING_AMOUNT]"
                    + " [s/SUBJECT day/DAY time/TIME]... [t/TAG]... [notes/NOTE]",
            "find KEYWORD [MORE_KEYWORDS]",
            "delete INDEX",
            "archive s/STUDENT_NAME",
            "unarchive s/STUDENT_NAME",
            "clear",
            "exit");

    private final Consumer<String> openBrowser;

    @FXML
    private VBox commandFormats;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, new MainApp().getHostServices()::showDocument);
    }

    /** Creates the help window with the browser action used by its guide link. */
    HelpWindow(Stage root, Consumer<String> openBrowser) {
        super(FXML, root);
        this.openBrowser = openBrowser;
        Font.loadFont(HelpWindow.class.getResource("/fonts/pokemon_pixel_font.ttf").toExternalForm(), 24);
        Font.loadFont(HelpWindow.class.getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 24);
        for (String format : COMMAND_FORMATS) {
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

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
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
