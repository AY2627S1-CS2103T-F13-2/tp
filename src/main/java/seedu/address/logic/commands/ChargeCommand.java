package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;

/** Adds a positive charge to a displayed student's outstanding balance. */
public class ChargeCommand extends Command {
    public static final String COMMAND_WORD = "charge";
    public static final String MESSAGE_USAGE = "Usage: charge INDEX AMOUNT";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No matching contact found at the specified index.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact already exists in TuteeDex.";
    public static final String MESSAGE_SUCCESS = "%1$s's outstanding balance is now %2$s.";
    public static final String MESSAGE_BALANCE_TOO_LARGE =
            "This charge would make the balance more than SGD 1,000,000.00, which is not allowed.";

    private final Index index;
    private final OutstandingAmount amount;

    /** Creates a charge command for the specified positive amount. */
    public ChargeCommand(Index index, OutstandingAmount amount) {
        this.index = requireNonNull(index);
        this.amount = requireNonNull(amount);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> people = model.getFilteredPersonList();
        if (index.getZeroBased() >= people.size()) {
            throw new CommandException(MESSAGE_CONTACT_NOT_FOUND);
        }
        Person person = people.get(index.getZeroBased());
        OutstandingAmount updatedAmount = person.getOutstandingAmount().add(amount);
        if (updatedAmount.isGreaterThan(OutstandingAmount.MAXIMUM)) {
            throw new CommandException(MESSAGE_BALANCE_TOO_LARGE);
        }
        Person updated = new Person(person.getName(), person.getPhone(), person.getAddress(), person.getStartDate(),
                updatedAmount, person.getTags(), person.getNotes(), person.getSubjects(), person.isArchived());
        if (!person.isSamePerson(updated) && model.hasPerson(updated)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
        model.setPerson(person, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), updatedAmount.toCurrencyString()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ChargeCommand command && index.equals(command.index) && amount.equals(command.amount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("amount", amount).toString();
    }
}
