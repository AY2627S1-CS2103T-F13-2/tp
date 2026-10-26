package seedu.address.logic;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the current session's ordered sorting criteria. */
    SortOrder getSortOrder();

    /** Returns one representative lesson per student in the current display order. */
    List<LessonRow> getStudentRows();

    /** Returns all sorted lessons for the grouped display and their shared student profiles. */
    List<LessonRow> getLessonRows();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns the list or find command that set the displayed list when the app was last closed,
     * or an empty string if the normal list should be shown.
     */
    String getLastViewCommand();

    /**
     * Returns the sort command that set the display order when the app was last closed,
     * or an empty string if the default order should be used.
     */
    String getLastSortCommand();
}
