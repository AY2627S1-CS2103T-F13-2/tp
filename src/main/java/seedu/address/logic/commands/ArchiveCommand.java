package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Archives the student at a displayed index. */
public class ArchiveCommand extends Command {
    public static final String COMMAND_WORD = "archive";
    public static final String MESSAGE_USAGE = "Usage: archive INDEX";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! " + MESSAGE_USAGE;
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No matching contact found at the specified index.";
    public static final String MESSAGE_SUCCESS = "Archived %1$s.";
    public static final String MESSAGE_ALREADY = "%1$s is already archived.";
    private final Index targetIndex;

    /** Creates an archive command for the student at the given displayed index. */
    public ArchiveCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_CONTACT_NOT_FOUND);
        }
        Person person = lastShownList.get(targetIndex.getZeroBased());
        String name = person.getName().fullName;
        if (person.isArchived()) {
            throw new CommandException(String.format(MESSAGE_ALREADY, name));
        }
        model.setPerson(person, person.withArchived(true));
        model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name));
    }

    @Override public boolean equals(Object other) {
        return other instanceof ArchiveCommand c && targetIndex.equals(c.targetIndex);
    }
}
