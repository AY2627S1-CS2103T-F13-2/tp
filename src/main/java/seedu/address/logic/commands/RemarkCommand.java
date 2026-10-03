package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/**
 * Changes the remark of an existing person in the address book.
 */
public class RemarkCommand extends Command {
    public static final String COMMAND_WORD = "remark";
    private final Index index;
    private final Remark remark;
    /** Creates a command to add a remark to a person. */
    public RemarkCommand(Index index, Remark remark) {
        this.index = requireNonNull(index);
        this.remark = requireNonNull(remark);
    }



    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> displayedPeople = model.getFilteredPersonList();

        if (index.getZeroBased() >= displayedPeople.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person original = displayedPeople.get(index.getZeroBased());
        Person updated = new Person(
                original.getName(),
                original.getPhone(),
                original.getEmail(),
                original.getAddress(),
                remark,
                original.getTags());

        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult("Remark updated for " + Messages.format(updated));
    }
}
