package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.function.Predicate;

import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;

/** Lists students matching a filter, or all students when no filter is supplied. */
public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";
    public static final String SUMMARY_FLAG = "-summary";
    public static final String MESSAGE_EMPTY = "No contacts stored. Use the add command to add one.";
    public static final String MESSAGE_SINGULAR = "Listed 1 contact.";
    public static final String MESSAGE_PLURAL_FORMAT = "Listed %1$d contacts.";
    public static final String MESSAGE_NO_MATCHES = "No contacts match the specified tags.";
    public static final String MESSAGE_SUMMARY_FORMAT = "%-24s%24s";

    private final Predicate<Person> predicate;
    private final boolean isSummary;

    public ListCommand() { this(PREDICATE_SHOW_ALL_PERSONS, false); }
    public ListCommand(boolean isSummary) { this(PREDICATE_SHOW_ALL_PERSONS, isSummary); }
    public ListCommand(Predicate<Person> predicate) { this(predicate, false); }

    /** Creates a filtered list command with optional summary output. */
    public ListCommand(Predicate<Person> predicate, boolean isSummary) {
        this.predicate = requireNonNull(predicate);
        this.isSummary = isSummary;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int count = model.getFilteredPersonList().size();
        if (!isSummary) {
            return new CommandResult(count == 0 && predicate != PREDICATE_SHOW_ALL_PERSONS
                    ? MESSAGE_NO_MATCHES : getMessageForContactCount(count));
        }
        int paidStudents = (int) model.getFilteredPersonList().stream()
                .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO)).count();
        return new CommandResult(String.format(MESSAGE_SUMMARY_FORMAT,
                "Total: " + count, "Paid: " + paidStudents));
    }

    public static String getMessageForContactCount(int count) {
        if (count == 0) return MESSAGE_EMPTY;
        if (count == 1) return MESSAGE_SINGULAR;
        return String.format(MESSAGE_PLURAL_FORMAT, count);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListCommand otherCommand
                && predicate.equals(otherCommand.predicate) && isSummary == otherCommand.isSummary;
    }
}
