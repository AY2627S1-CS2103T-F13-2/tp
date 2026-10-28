package seedu.address.model;

import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;
    Predicate<Person> PREDICATE_SHOW_ACTIVE_PERSONS = person -> !person.isArchived();

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns the saved list or find command that sets the displayed list, or an empty string for the normal list.
     */
    String getLastViewCommand();

    /**
     * Saves {@code lastViewCommand} so the same list can be shown the next time the app opens.
     * An empty string means the normal list.
     */
    void setLastViewCommand(String lastViewCommand);

    /** Returns the saved sort command that sets the display order, or an empty string for the default order. */
    String getLastSortCommand();

    /**
     * Saves {@code lastSortCommand} so the same order can be used the next time the app opens.
     * An empty string means the default order.
     */
    void setLastSortCommand(String lastSortCommand);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Starts one command's lazy sorting-reference scope; queries need not capture a new time. */
    void beginCommand();

    /** Ends the command scope, including when command execution fails. */
    void endCommand();

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the current session's ordered sorting criteria. */
    SortOrder getSortOrder();

    /** Applies sorting without changing profile data or the current filter. */
    void setSortOrder(SortOrder order);

    /** Returns one representative lesson per student in the current unique-student display order. */
    List<LessonRow> getStudentRows();

    /** Returns all sorted lesson rows for subject grouping, referring to the current unique students. */
    List<LessonRow> getLessonRows();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /** Returns the filter applied to the filtered person list, or null if no filter has been applied. */
    Predicate<? super Person> getFilteredPersonListPredicate();
}
