package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String SUMMARY_FLAG = "-summary";

    public static final String MESSAGE_EMPTY = "No contacts stored. Use the add command to add one.";
    public static final String MESSAGE_SINGULAR = "Listed 1 contact.";
    public static final String MESSAGE_PLURAL_FORMAT = "Listed %1$d contacts.";
    public static final String MESSAGE_SUMMARY_FORMAT = "%-24s%24s";

    private final boolean isSummary;

    public ListCommand() {
        this(false);
    }

    public ListCommand(boolean isSummary) {
        this.isSummary = isSummary;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        if (!isSummary) {
            return new CommandResult(getMessageForContactCount(model.getFilteredPersonList().size()));
        }
        int totalStudents = model.getFilteredPersonList().size();
        int paidStudents = (int) model.getFilteredPersonList().stream()
                .filter(person -> person.getOutstandingAmount().equals(OutstandingAmount.ZERO))
                .count();
        return new CommandResult(String.format(MESSAGE_SUMMARY_FORMAT,
                "Total: " + totalStudents, "Paid: " + paidStudents));
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
                && isSummary == otherListCommand.isSummary);
    }
}
