package seedu.address.ui;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.ListPresentation;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.sort.SortOrder;

/**
 * The Main Window. Provides the basic application layout containing
 * a date/time header and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    public static final String MESSAGE_LAST_VIEW_RESTORED =
            "Showing the same view as when you last closed TuteeDex. Type list to see all students.";

    private static final String FXML = "MainWindow.fxml";
    private static final DateTimeFormatter HEADER_DATE = DateTimeFormatter.ofPattern("dd  -  MM", Locale.ENGLISH);
    private static final DateTimeFormatter HEADER_TIME = DateTimeFormatter.ofPattern("HH  :  mm", Locale.ENGLISH);
    /** Height of the window title bar that must be visible for the saved position to be reused. */
    private static final double TITLE_BAR_HEIGHT = 30;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private final Clock clock;
    private final Timeline headerClock;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;
    private boolean subjectGroupsShown;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private Label headerDate;

    @FXML
    private Label headerTime;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     * The data file path parameter is retained for compatibility with existing callers.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, Consumer<String> openBrowser) {
        this(primaryStage, logic, dataFilePath, openBrowser, Clock.systemDefaultZone());
    }

    /** Creates a window with a supplied clock for its local date and time display. */
    MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, Consumer<String> openBrowser, Clock clock) {
        super(FXML, primaryStage);

        Font.loadFont(MainWindow.class.getResource("/fonts/pokemon_pixel_font.ttf").toExternalForm(), 24);
        Font.loadFont(MainWindow.class.getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 24);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.clock = clock;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow(new Stage(), openBrowser);
        headerClock = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateHeaderClock()));
        headerClock.setCycleCount(Animation.INDEFINITE);
        updateHeaderClock();
        primaryStage.showingProperty().addListener((observable, wasShowing, isShowing) -> {
            if (isShowing) {
                updateHeaderClock();
                headerClock.play();
            } else {
                headerClock.stop();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        KeyCombination helpKey = KeyCombination.valueOf("F1");
        // Handle F1 before text controls can consume it.
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (helpKey.match(event)) {
                handleHelp();
                event.consume();
            }
        });
    }

    private void updateHeaderClock() {
        LocalDateTime now = LocalDateTime.now(clock);
        headerDate.setText(now.format(HEADER_DATE));
        headerTime.setText(now.format(HEADER_TIME));
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        refreshPersonList(ListPresentation.CONTACTS, false);

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        showListSummary();
        restoreLastView();
    }

    /** Shows the sort order and the list or find view that were on screen when the app was last closed. */
    private void restoreLastView() {
        // Sort first, so the list or find view that follows is shown in the saved order.
        restoreCommand(logic.getLastSortCommand());
        String lastViewCommand = logic.getLastViewCommand();
        CommandResult viewResult = restoreCommand(lastViewCommand);
        // A restored list shows the student counts instead, so only a restored find explains why it is filtered.
        if (viewResult != null && !isListCommand(lastViewCommand)) {
            resultDisplay.setFeedbackToUser(MESSAGE_LAST_VIEW_RESTORED + "\n" + viewResult.getFeedbackToUser());
        }
    }

    /** Runs a saved command and returns its result, or null if nothing was saved or it failed. */
    private CommandResult restoreCommand(String savedCommand) {
        if (savedCommand.isEmpty()) {
            return null;
        }
        logger.info("Restoring last view: " + savedCommand);
        try {
            return executeCommand(savedCommand);
        } catch (CommandException | ParseException e) {
            // executeCommand has already shown the error, and the current list stays on screen.
            logger.warning("Could not restore last view: " + e.getMessage());
            return null;
        }
    }

    /** Displays the current list's total and number of students with no balance due. */
    private void showListSummary() {
        int paid = (int) logic.getFilteredPersonList().stream()
                .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count();
        resultDisplay.showSummary(logic.getFilteredPersonList().size(), paid);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     * The saved position is only used if the window's title bar would be on a connected screen,
     * so the user can always see and drag the window.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            double x = guiSettings.getWindowCoordinates().getX();
            double y = guiSettings.getWindowCoordinates().getY();
            double titleBarMiddleX = x + guiSettings.getWindowWidth() / 2;
            boolean isTitleBarOnScreen = Screen.getScreens().stream()
                    .anyMatch(screen -> screen.getVisualBounds().contains(titleBarMiddleX, y, 1, TITLE_BAR_HEIGHT));
            if (isTitleBarOnScreen) {
                primaryStage.setX(x);
                primaryStage.setY(y);
            }
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    /** Focuses the command input for finding a student. */
    @FXML
    private void handleFind() {
        commandBox.prepareFind();
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            refreshPersonList(commandResult.getListPresentation(), isListCommand(commandText));

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    private boolean isListCommand(String commandText) {
        return commandText.strip().split("\\s+", 2)[0].equals(ListCommand.COMMAND_WORD);
    }

    /** Refreshes the central list using the presentation mode requested by the command. */
    private void refreshPersonList(ListPresentation presentation, boolean updateSummary) {
        if (presentation == ListPresentation.UNCHANGED) {
            return;
        }
        if (presentation == ListPresentation.SUBJECT_GROUPS) {
            subjectGroupsShown = true;
        } else if (presentation == ListPresentation.CONTACTS) {
            subjectGroupsShown = false;
        }
        if (subjectGroupsShown) {
            SubjectGroupPanel subjectGroupPanel = logic.getSortOrder().equals(SortOrder.NONE)
                    ? new SubjectGroupPanel(logic.getFilteredPersonList())
                    : new SubjectGroupPanel(logic.getFilteredPersonList(), logic.getLessonRows());
            personListPanelPlaceholder.getChildren().setAll(subjectGroupPanel.getRoot());
        } else {
            refreshUngroupedList();
        }
        if (updateSummary) {
            showListSummary();
        }
    }

    private void refreshUngroupedList() {
        if (logic.getSortOrder().hasLessonSlot()) {
            LessonListPanel lessonListPanel = new LessonListPanel(logic.getStudentRows());
            personListPanelPlaceholder.getChildren().setAll(lessonListPanel.getRoot());
            personListPanel = null;
        } else {
            if (personListPanel == null) {
                personListPanel = new PersonListPanel(logic.getFilteredPersonList());
            }
            if (!personListPanelPlaceholder.getChildren().contains(personListPanel.getRoot())) {
                personListPanelPlaceholder.getChildren().setAll(personListPanel.getRoot());
            }
        }
    }
}
