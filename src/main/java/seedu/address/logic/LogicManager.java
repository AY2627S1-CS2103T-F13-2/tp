package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.ListPresentation;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private DeleteCommand pendingDelete;
    private boolean isShowingSubjectGroups = false;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        DeleteCommand previousPreview = pendingDelete;
        pendingDelete = null;
        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof DeleteCommand deleteCommand && deleteCommand.isConfirmation()) {
            if (previousPreview == null) {
                throw new CommandException(DeleteCommand.MESSAGE_NO_PREVIEW);
            }
            command = previousPreview;
        }
        AddressBook beforeDelete = command instanceof DeleteCommand && command.requiresAddressBookSave()
                ? new AddressBook(model.getAddressBook()) : null;
        Predicate<? super Person> filterBeforeCommand = model.getFilteredPersonListPredicate();
        model.beginCommand();
        try {
            commandResult = command.execute(model);
            if (command instanceof DeleteCommand deleteCommand && !deleteCommand.isConfirmation()) {
                pendingDelete = deleteCommand.confirmed(model);
            }
        } finally {
            model.endCommand();
        }
        if (command.requiresAddressBookSave()) {
            try {
                saveAddressBook();
            } catch (CommandException e) {
                if (beforeDelete != null) {
                    model.setAddressBook(beforeDelete);
                }
                throw e;
            }
        }
        saveLastView(command, commandText, commandResult, filterBeforeCommand);
        return commandResult;
    }

    private void saveAddressBook() throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public SortOrder getSortOrder() {
        return model.getSortOrder();
    }

    @Override
    public List<LessonRow> getStudentRows() {
        return model.getStudentRows();
    }

    @Override
    public List<LessonRow> getLessonRows() {
        return model.getLessonRows();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }

    @Override
    public String getLastViewCommand() {
        String lastViewCommand = model.getLastViewCommand();
        // The preferences file can be edited by hand, so only list and find are run again on startup.
        if (!lastViewCommand.isEmpty() && !isViewCommand(parseSavedCommand(lastViewCommand))) {
            logger.warning("Ignoring saved view that is not a list or find command: " + lastViewCommand);
            return "";
        }
        return lastViewCommand;
    }

    @Override
    public String getLastSortCommand() {
        String lastSortCommand = model.getLastSortCommand();
        if (!lastSortCommand.isEmpty() && !(parseSavedCommand(lastSortCommand) instanceof SortCommand)) {
            logger.warning("Ignoring saved sort order that is not a sort command: " + lastSortCommand);
            return "";
        }
        return lastSortCommand;
    }

    /**
     * Saves the commands that decide which students are listed and in what order, so the same view is shown
     * the next time the app opens. The saved view is cleared only when another command changes what is on screen,
     * by replacing the filter or by switching from subject groups back to contacts.
     */
    private void saveLastView(Command command, String commandText, CommandResult commandResult,
            Predicate<? super Person> filterBeforeCommand) {
        ListPresentation presentation = commandResult.getListPresentation();
        if (command instanceof SortCommand) {
            // A bare sort only reports the order (UNCHANGED); sort reset leaves no order to save.
            if (presentation == ListPresentation.REFRESH) {
                model.setLastSortCommand(model.getSortOrder().equals(SortOrder.NONE) ? "" : commandText.strip());
            }
        } else if (isViewCommand(command)) {
            model.setLastViewCommand(commandText.strip());
        } else if (!commandResult.isExit() && presentation != ListPresentation.UNCHANGED
                && (model.getFilteredPersonListPredicate() != filterBeforeCommand || isShowingSubjectGroups)) {
            // Commands such as delete keep the filter, so the saved view still matches the screen.
            model.setLastViewCommand("");
        }

        if (presentation == ListPresentation.SUBJECT_GROUPS) {
            isShowingSubjectGroups = true;
        } else if (presentation == ListPresentation.CONTACTS) {
            isShowingSubjectGroups = false;
        }
    }

    private boolean isViewCommand(Command command) {
        return command instanceof ListCommand || command instanceof FindCommand;
    }

    /** Returns the command in {@code commandText}, or null if it cannot be parsed. */
    private Command parseSavedCommand(String commandText) {
        try {
            return addressBookParser.parseCommand(commandText);
        } catch (ParseException e) {
            return null;
        }
    }
}
