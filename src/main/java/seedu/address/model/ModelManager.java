package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortComparators;
import seedu.address.model.sort.SortOrder;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final Clock clock;
    private final Map<Person, Integer> storedPositions = new HashMap<>();
    private SortOrder sortOrder = SortOrder.NONE;
    private LocalDateTime sortReference;
    private boolean commandInProgress;
    private LocalDateTime commandReference;
    private List<LessonRow> studentRows;
    private List<LessonRow> lessonRows;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, Clock.systemDefaultZone());
    }

    /**
     * Creates a model whose local lesson time is defined by the supplied clock's zone.
     * Production uses the system-default zone; tests can deliberately supply another zone.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, Clock clock) {
        requireAllNonNull(addressBook, userPrefs, clock);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        this.clock = clock;
        updateStoredPositions();
        // Update positions before transformation listeners compare a new or edited profile.
        this.addressBook.getPersonList().addListener((ListChangeListener<Person>) change -> {
            updateStoredPositions();
            invalidateRows();
        });
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        filteredPersons.setPredicate(PREDICATE_SHOW_ACTIVE_PERSONS);
        sortedPersons = new SortedList<>(filteredPersons);
        sortReference = LocalDateTime.now(clock);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public String getLastViewCommand() {
        return userPrefs.getLastViewCommand();
    }

    @Override
    public void setLastViewCommand(String lastViewCommand) {
        requireNonNull(lastViewCommand);
        userPrefs.setLastViewCommand(lastViewCommand);
    }

    @Override
    public String getLastSortCommand() {
        return userPrefs.getLastSortCommand();
    }

    @Override
    public void setLastSortCommand(String lastSortCommand) {
        requireNonNull(lastSortCommand);
        userPrefs.setLastSortCommand(lastSortCommand);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        refreshSorting();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
        refreshSorting();
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
        refreshSorting();
    }

    @Override
    public void beginCommand() {
        assert !commandInProgress : "Command scopes must not be nested.";
        commandInProgress = true;
        commandReference = null;
    }

    @Override
    public void endCommand() {
        assert commandInProgress : "A command scope must be started before it ends.";
        commandInProgress = false;
        commandReference = null;
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(SortOrder order) {
        sortOrder = requireNonNull(order);
        refreshSorting();
    }

    @Override
    public List<LessonRow> getStudentRows() {
        if (studentRows == null) {
            studentRows = sortedPersons.stream()
                    .map(person -> SortComparators.representativeLesson(person, sortOrder, sortReference)).toList();
        }
        return studentRows;
    }

    @Override
    public List<LessonRow> getLessonRows() {
        if (lessonRows != null) {
            return lessonRows;
        }
        List<LessonRow> rows = new ArrayList<>();
        for (Person person : sortedPersons) {
            if (person.getSubjects().isEmpty()) {
                rows.add(new LessonRow(person, null));
            } else {
                person.getSubjects().forEach(subject -> rows.add(new LessonRow(person, subject)));
            }
        }
        rows.sort(SortComparators.lessons(sortOrder, sortReference)
                .thenComparingInt(row -> storedPositions.get(row.person())));
        lessonRows = List.copyOf(rows);
        return lessonRows;
    }

    private void refreshSorting() {
        sortReference = captureSortReference();
        invalidateRows();
        sortedPersons.setComparator(sortOrder.equals(SortOrder.NONE) ? null
                : SortComparators.students(sortOrder, sortReference)
                .thenComparingInt(storedPositions::get));
    }

    private LocalDateTime captureSortReference() {
        if (!commandInProgress) {
            return LocalDateTime.now(clock);
        }
        if (commandReference == null) {
            commandReference = LocalDateTime.now(clock);
        }
        return commandReference;
    }

    private void invalidateRows() {
        studentRows = null;
        lessonRows = null;
    }

    private void updateStoredPositions() {
        storedPositions.clear();
        List<Person> storedStudents = addressBook.getPersonList();
        for (int index = 0; index < storedStudents.size(); index++) {
            storedPositions.put(storedStudents.get(index), index);
        }
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
        refreshSorting();
    }

    @Override
    public Predicate<? super Person> getFilteredPersonListPredicate() {
        return filteredPersons.getPredicate();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && sortedPersons.equals(otherModelManager.sortedPersons)
                && sortOrder.equals(otherModelManager.sortOrder);
    }

}
