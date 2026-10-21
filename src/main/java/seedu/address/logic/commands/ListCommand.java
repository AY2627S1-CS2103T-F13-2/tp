package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Set;
import java.util.function.Predicate;

import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/** Lists all students or students matching a subject or tag filter. */
public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";
    public static final String SUMMARY_FLAG = "-summary";
    public static final String ARCHIVED_FLAG = "-archived";
    public static final String MESSAGE_NO_ARCHIVED_CONTACTS = "No archived contacts.";
    public static final String MESSAGE_EMPTY = "No contacts stored. Use the add command to add one.";
    public static final String MESSAGE_SINGULAR = "Listed 1 contact.";
    public static final String MESSAGE_PLURAL_FORMAT = "Listed %1$d contacts.";
    public static final String MESSAGE_NO_SUBJECT_MATCHES = "No contacts match the specified subjects.";
    public static final String MESSAGE_NO_MATCHES = "No contacts match the specified tags.";
    public static final String MESSAGE_SUMMARY_FORMAT = "%-24s%24s";

    private final Predicate<Person> predicate;
    private final Set<Subject> subjects;
    private final boolean isSummary;
    private final boolean archivedOnly;

    public ListCommand() {
        this(PREDICATE_SHOW_ALL_PERSONS, Set.of(), false);
    }

    public ListCommand(boolean isSummary) {
        this(PREDICATE_SHOW_ALL_PERSONS, Set.of(), isSummary);
    }

    /** Lists contacts with any of the specified subjects. */
    public ListCommand(Set<Subject> subjects) {
        this(subjects, false);
    }

    /** Lists contacts with any of the specified subjects, with optional summary output. */
    public ListCommand(Set<Subject> subjects, boolean isSummary) {
        this(PREDICATE_SHOW_ALL_PERSONS, subjects, isSummary);
    }

    public ListCommand(Predicate<Person> predicate) {
        this(predicate, false);
    }

    /** Creates a filtered list command with optional summary output. */
    public ListCommand(Predicate<Person> predicate, boolean isSummary) {
        this(predicate, Set.of(), isSummary);
    }

    private ListCommand(Predicate<Person> predicate, Set<Subject> subjects, boolean isSummary) {
        this(predicate, subjects, isSummary, false);
    }

    private ListCommand(Predicate<Person> predicate, Set<Subject> subjects, boolean isSummary, boolean archivedOnly) {
        this.predicate = requireNonNull(predicate);
        this.subjects = Set.copyOf(requireNonNull(subjects));
        this.isSummary = isSummary;
        this.archivedOnly = archivedOnly;
    }

    /** Lists only archived students, without active filters or summary options. */
    public static ListCommand forArchivedStudents() {
        return new ListCommand(PREDICATE_SHOW_ALL_PERSONS, Set.of(), false, true);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Predicate<Person> filter = subjects.isEmpty()
                ? predicate : person -> person.getSubjects().stream().anyMatch(subjects::contains);
        Predicate<Person> visibility = archivedOnly ? Person::isArchived : PREDICATE_SHOW_ACTIVE_PERSONS;
        model.updateFilteredPersonList(visibility.and(filter));
        int count = model.getFilteredPersonList().size();
        if (archivedOnly && count == 0) {
            return new CommandResult(MESSAGE_NO_ARCHIVED_CONTACTS);
        }
        if (isSummary) {
            int paidStudents = (int) model.getFilteredPersonList().stream()
                    .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count();
            return new CommandResult(String.format(MESSAGE_SUMMARY_FORMAT,
                    "Total: " + count, "Paid: " + paidStudents));
        }
        if (count == 0 && !subjects.isEmpty()) {
            return new CommandResult(MESSAGE_NO_SUBJECT_MATCHES);
        }
        if (count == 0 && predicate != PREDICATE_SHOW_ALL_PERSONS) {
            return new CommandResult(MESSAGE_NO_MATCHES);
        }
        return new CommandResult(getMessageForContactCount(count));
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

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && predicate.equals(otherCommand.predicate)
                && subjects.equals(otherCommand.subjects)
                && isSummary == otherCommand.isSummary
                && archivedOnly == otherCommand.archivedOnly;
    }
}
