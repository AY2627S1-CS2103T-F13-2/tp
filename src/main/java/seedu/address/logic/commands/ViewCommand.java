package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays the full profile of the student at a displayed index.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays the full profile of the student at the given index in the displayed list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No matching contact found at the specified index.";

    private final Index targetIndex;

    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_CONTACT_NOT_FOUND);
        }

        Person personToDisplay = lastShownList.get(targetIndex.getZeroBased());
        return new CommandResult(String.format(
                "Name: %s\nPhone: %s\nAddress: %s\nStart date: %s\nOutstanding amount: %s"
                        + "\nSubjects: %s\nTags: %s\nNotes: %s",
                personToDisplay.getName(), personToDisplay.getPhone(), personToDisplay.getAddress(),
                personToDisplay.getStartDate(), personToDisplay.getOutstandingAmount().toCurrencyString(),
                Messages.formatSubjects(personToDisplay), Messages.formatTags(personToDisplay),
                personToDisplay.getNotes()), CommandResult.ListPresentation.UNCHANGED);
    }

    // Only shows information, so it must not overwrite a data file that failed to load.
    @Override
    public boolean requiresAddressBookSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }
        return targetIndex.equals(otherViewCommand.targetIndex);
    }
}
