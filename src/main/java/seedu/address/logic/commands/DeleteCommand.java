package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = "Usage: delete INDEX, then delete confirm";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_INDEX = "INDEX must be a positive integer! (e.g., 1, 2, 3, ...)";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No matching contact found at the specified index.";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    public static final String MESSAGE_CONFIRM = "Permanently delete this student? %1$s\n"
            + "There is no undo. Type 'delete confirm' next to continue, or any other command to cancel.";
    public static final String MESSAGE_NO_PREVIEW = "No pending deletion. Run delete INDEX to preview a student first.";

    private final Index targetIndex;
    private final Person confirmedPerson;

    /** Creates a preview for the student at the displayed index. */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.confirmedPerson = null;
    }

    /** Creates a confirmation request; LogicManager binds it to the preceding preview. */
    public DeleteCommand() {
        targetIndex = null;
        confirmedPerson = null;
    }

    private DeleteCommand(Person person) {
        targetIndex = null;
        confirmedPerson = requireNonNull(person);
    }

    /** Returns whether this command confirms a preceding preview. */
    public boolean isConfirmation() {
        return targetIndex == null;
    }

    /** Captures the exact student immediately after a successful preview. */
    public DeleteCommand confirmed(Model model) {
        return new DeleteCommand(model.getFilteredPersonList().get(targetIndex.getZeroBased()));
    }

    @Override
    public boolean requiresAddressBookSave() {
        return confirmedPerson != null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (isConfirmation()) {
            if (confirmedPerson == null || !model.getFilteredPersonList().contains(confirmedPerson)) {
                throw new CommandException(MESSAGE_NO_PREVIEW);
            }
            model.deletePerson(confirmedPerson);
            return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(confirmedPerson)));
        }
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_CONTACT_NOT_FOUND);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        return new CommandResult(String.format(MESSAGE_CONFIRM, Messages.format(personToDelete)),
                CommandResult.ListPresentation.UNCHANGED);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeleteCommand.targetIndex)
                && Objects.equals(confirmedPerson, otherDeleteCommand.confirmedPerson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
