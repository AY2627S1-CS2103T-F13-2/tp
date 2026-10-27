package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final List<String> history = new ArrayList<>();
    // Equals history.size() while the user is typing a new command rather than viewing an old one.
    private int historyIndex = 0;
    private String draft = "";

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryKey);
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        addToHistory(commandText);
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /** Records an entered command, skipping it if it repeats the previous one. */
    private void addToHistory(String commandText) {
        if (history.isEmpty() || !history.get(history.size() - 1).equals(commandText)) {
            history.add(commandText);
        }
        historyIndex = history.size();
        draft = "";
    }

    /** Shows the previous command on Up and the next one on Down. */
    private void handleHistoryKey(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            event.consume();
            if (historyIndex == 0) {
                return;
            }
            if (historyIndex == history.size()) {
                draft = commandTextField.getText();
            }
            historyIndex--;
            showInInput(history.get(historyIndex));
        } else if (event.getCode() == KeyCode.DOWN) {
            event.consume();
            if (historyIndex == history.size()) {
                return;
            }
            historyIndex++;
            showInInput(historyIndex == history.size() ? draft : history.get(historyIndex));
        }
    }

    private void showInInput(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /** Converts the current input into a find command without executing it. */
    public void prepareFind() {
        String query = commandTextField.getText().strip();
        if (query.isEmpty() || query.equals("find")) {
            commandTextField.setText("find ");
        } else if (query.startsWith("find ")) {
            commandTextField.setText(query);
        } else {
            commandTextField.setText("find " + query);
        }
        commandTextField.requestFocus();
        commandTextField.positionCaret(commandTextField.getLength());
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
