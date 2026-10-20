package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Archives a student identified by exact, case-insensitive name. */
public class ArchiveCommand extends Command {
    public static final String COMMAND_WORD = "archive";
    public static final String MESSAGE_USAGE = "Usage: archive s/STUDENT_NAME";
    public static final String MESSAGE_NOT_FOUND = "Student cannot be found.";
    public static final String MESSAGE_ALREADY = "Student already archived.";
    private final String name;

    /** Creates an archive command for the given student name. */
    public ArchiveCommand(String name) {
        this.name = name;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        for (Person person : model.getAddressBook().getPersonList()) {
            if (person.getName().fullName.equalsIgnoreCase(name)) {
                if (person.isArchived()) {
                    throw new CommandException(MESSAGE_ALREADY);
                }
                model.setPerson(person, person.withArchived(true));
                model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
                return new CommandResult("Student archived.");
            }
        }
        throw new CommandException(MESSAGE_NOT_FOUND);
    }

    @Override public boolean equals(Object other) {
        return other instanceof ArchiveCommand c && name.equals(c.name);
    }
}
