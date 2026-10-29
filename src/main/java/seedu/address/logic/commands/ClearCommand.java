package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Deletes every student after the user confirms, as there is no undo.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRM_WORD = "confirm";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes every student, including archived students. Run it once to see how many students "
            + "would be deleted, then add '" + CONFIRM_WORD + "' to delete them.\n"
            + "Parameters: [" + CONFIRM_WORD + "]\n"
            + "Example: " + COMMAND_WORD + " " + CONFIRM_WORD;
    public static final String MESSAGE_CONFIRM = "This will permanently delete all %1$d student(s), including "
            + "archived students. There is no undo. Type '" + COMMAND_WORD + " " + CONFIRM_WORD + "' to continue.";
    public static final String MESSAGE_SUCCESS = "Deleted all %1$d student(s) from TuteeDex.";
    public static final String MESSAGE_NO_STUDENTS = "There are no students to delete.";

    private final boolean isConfirmed;

    /**
     * Creates a command that deletes every student if {@code isConfirmed}, or otherwise only asks for confirmation.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int studentCount = model.getAddressBook().getPersonList().size();
        if (studentCount == 0) {
            return new CommandResult(MESSAGE_NO_STUDENTS, false, false, CommandResult.ListPresentation.UNCHANGED);
        }
        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRM, studentCount), false, false,
                    CommandResult.ListPresentation.UNCHANGED);
        }
        model.setAddressBook(new AddressBook());
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentCount));
    }

    @Override
    public boolean requiresAddressBookSave() {
        return isConfirmed;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClearCommand otherCommand && isConfirmed == otherCommand.isConfirmed;
    }
}
