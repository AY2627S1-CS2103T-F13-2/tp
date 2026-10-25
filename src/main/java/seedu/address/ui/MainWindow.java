package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
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
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;
    private boolean subjectGroupsShown;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     * The data file path parameter is retained for compatibility with existing callers.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        Font.loadFont(MainWindow.class.getResource("/fonts/pokemon_pixel_font.ttf").toExternalForm(), 24);
        Font.loadFont(MainWindow.class.getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 24);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
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
    }

    /** Displays the current list's total and number of students with no balance due. */
    private void showListSummary() {
        int paid = (int) logic.getFilteredPersonList().stream()
                .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count();
        resultDisplay.showSummary(logic.getFilteredPersonList().size(), paid);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
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
            boolean isListCommand = commandText.strip().split("\\s+", 2)[0].equals(ListCommand.COMMAND_WORD);
            refreshPersonList(commandResult.getListPresentation(), isListCommand);

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
            LessonListPanel lessonListPanel = new LessonListPanel(logic.getFilteredPersonList(), logic.getLessonRows());
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
