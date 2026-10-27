package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;

/** Lists all students or students matching a subject or tag filter. */
public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";
    public static final String SUMMARY_FLAG = "-summary";
    public static final String ARCHIVED_FLAG = "-archived";
    public static final String SUBJECTS_FLAG = "-subjects";
    public static final String MESSAGE_NO_ARCHIVED_CONTACTS = "No archived contacts.";
    public static final String MESSAGE_EMPTY = "No contacts stored. Use the add command to add one.";
    public static final String MESSAGE_SINGULAR = "Listed 1 contact.";
    public static final String MESSAGE_PLURAL_FORMAT = "Listed %1$d contacts.";
    public static final String MESSAGE_NO_SUBJECT_MATCHES = "No contacts match the specified subjects.";
    public static final String MESSAGE_NO_SUBJECT_GROUPS = "No subjects found for the active contacts.";
    public static final String MESSAGE_NO_MATCHES = "No contacts match the specified tags.";
    public static final String MESSAGE_SUMMARY_FORMAT = "%-24s%24s";

    private final Predicate<Person> predicate;
    private final Set<String> subjectNames;
    private final boolean isSummary;
    private final boolean isArchivedOnly;
    private final boolean isSubjectList;

    public ListCommand() {
        this(PREDICATE_SHOW_ALL_PERSONS, Set.of(), false, false, false);
    }

    public ListCommand(boolean isSummary) {
        this(PREDICATE_SHOW_ALL_PERSONS, Set.of(), isSummary, false, false);
    }

    /** Lists contacts with any of the specified subjects. */
    public ListCommand(Set<String> subjectNames) {
        this(subjectNames, false);
    }

    /** Lists contacts with any of the specified subjects, with optional summary output. */
    public ListCommand(Set<String> subjectNames, boolean isSummary) {
        this(PREDICATE_SHOW_ALL_PERSONS, subjectNames, isSummary);
    }

    public ListCommand(Predicate<Person> predicate) {
        this(predicate, false);
    }

    /** Creates a filtered list command with optional summary output. */
    public ListCommand(Predicate<Person> predicate, boolean isSummary) {
        this(predicate, Set.of(), isSummary);
    }

    private ListCommand(Predicate<Person> predicate, Set<String> subjectNames, boolean isSummary) {
        this(predicate, subjectNames, isSummary, false);
    }

    private ListCommand(Predicate<Person> predicate, Set<String> subjectNames, boolean isSummary,
            boolean isArchivedOnly) {
        this(predicate, subjectNames, isSummary, isArchivedOnly, false);
    }

    private ListCommand(Predicate<Person> predicate, Set<String> subjectNames, boolean isSummary,
            boolean isArchivedOnly, boolean isSubjectList) {
        this.predicate = requireNonNull(predicate);
        this.subjectNames = requireNonNull(subjectNames).stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.isSummary = isSummary;
        this.isArchivedOnly = isArchivedOnly;
        this.isSubjectList = isSubjectList;
    }

    /** Lists only archived students, without active filters or summary options. */
    public static ListCommand forArchivedStudents() {
        return new ListCommand(PREDICATE_SHOW_ALL_PERSONS, Set.of(), false, true);
    }

    /** Lists active students grouped by subject. */
    public static ListCommand forSubjectGroups() {
        return new ListCommand(PREDICATE_SHOW_ACTIVE_PERSONS, Set.of(), false, false, true);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Predicate<Person> filter = subjectNames.isEmpty()
                ? predicate : person -> person.getSubjects().stream()
                        .anyMatch(subject -> subjectNames.stream().anyMatch(subject::hasName));
        Predicate<Person> visibility = isArchivedOnly ? Person::isArchived : PREDICATE_SHOW_ACTIVE_PERSONS;
        model.updateFilteredPersonList(visibility.and(filter));
        int count = model.getFilteredPersonList().size();
        if (isArchivedOnly && count == 0) {
            return result(MESSAGE_NO_ARCHIVED_CONTACTS);
        }
        if (isSummary) {
            int paidStudents = (int) model.getFilteredPersonList().stream()
                    .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count();
            return result(String.format(MESSAGE_SUMMARY_FORMAT,
                    "Total: " + count, "Paid: " + paidStudents));
        }
        if (isSubjectList && count == 0) {
            return result(MESSAGE_EMPTY);
        }
        boolean hasSubjects = model.getFilteredPersonList().stream()
                .anyMatch(person -> !person.getSubjects().isEmpty());
        if (isSubjectList && !hasSubjects) {
            return result(MESSAGE_NO_SUBJECT_GROUPS);
        }
        if (count == 0 && !subjectNames.isEmpty()) {
            return result(MESSAGE_NO_SUBJECT_MATCHES);
        }
        if (count == 0 && predicate != PREDICATE_SHOW_ALL_PERSONS) {
            return result(MESSAGE_NO_MATCHES);
        }
        return result(getMessageForContactCount(count));
    }

    private CommandResult result(String feedback) {
        return new CommandResult(feedback, false, false, isSubjectList
                ? CommandResult.ListPresentation.SUBJECT_GROUPS : CommandResult.ListPresentation.CONTACTS);
    }

    public static String getMessageForContactCount(int count) {
        if (count == 0) {
            return MESSAGE_EMPTY;
        }
        if (count == 1) {
            return MESSAGE_SINGULAR;
        }
        return String.format(MESSAGE_PLURAL_FORMAT, count);
    }

    // Startup replays list/find, so they must not overwrite a data file that failed to load.
    @Override
    public boolean requiresAddressBookSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && predicate.equals(otherCommand.predicate)
                && subjectNames.equals(otherCommand.subjectNames)
                && isSummary == otherCommand.isSummary
                && isArchivedOnly == otherCommand.isArchivedOnly
                && isSubjectList == otherCommand.isSubjectList;
    }
}
