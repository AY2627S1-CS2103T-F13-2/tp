package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Set;

import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Subject;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String SUMMARY_FLAG = "-summary";

    public static final String MESSAGE_EMPTY = "No contacts stored. Use the add command to add one.";
    public static final String MESSAGE_SINGULAR = "Listed 1 contact.";
    public static final String MESSAGE_PLURAL_FORMAT = "Listed %1$d contacts.";
    public static final String MESSAGE_NO_SUBJECT_MATCHES = "No contacts match the specified subjects.";
    public static final String MESSAGE_SUMMARY_FORMAT = "%-24s%24s";

    private final boolean isSummary;
    private final Set<Subject> subjects;

    public ListCommand() {
        this(false, Set.of());
    }

    public ListCommand(boolean isSummary) {
        this(isSummary, Set.of());
    }

    /** Lists contacts with any of the specified subjects. */
    public ListCommand(Set<Subject> subjects) {
        this(false, subjects);
    }

    private ListCommand(boolean isSummary, Set<Subject> subjects) {
        requireNonNull(subjects);
        this.isSummary = isSummary;
        this.subjects = Set.copyOf(subjects);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(subjects.isEmpty() ? PREDICATE_SHOW_ALL_PERSONS
                : person -> person.getSubjects().stream().anyMatch(subjects::contains));
        if (isSummary) {
            int totalStudents = model.getFilteredPersonList().size();
            int paidStudents = (int) model.getFilteredPersonList().stream()
                    .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO))
                    .count();
            return new CommandResult(String.format(MESSAGE_SUMMARY_FORMAT,
                    "Total: " + totalStudents, "Paid: " + paidStudents));
        }
        if (!subjects.isEmpty() && model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_NO_SUBJECT_MATCHES);
        }
        return new CommandResult(getMessageForContactCount(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the status message to show for a list of {@code count} contacts.
     */
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
        return other == this || (other instanceof ListCommand otherListCommand
                && isSummary == otherListCommand.isSummary && subjects.equals(otherListCommand.subjects));
    }
}
