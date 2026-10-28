package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;

/** Records a full or partial payment for a displayed student. */
public class PaidCommand extends Command {
    public static final String COMMAND_WORD = "paid";
    public static final String MESSAGE_USAGE = "Usage: paid INDEX [AMOUNT]";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "No matching contact found at the specified index.";
    public static final String MESSAGE_PAYMENT_EXCEEDS_BALANCE = "Payment cannot exceed the outstanding balance.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact already exists in TuteeDex.";
    public static final String MESSAGE_SUCCESS = "%1$s's outstanding balance is now %2$s.";

    private final Index index;
    private final OutstandingAmount amount;

    /** Creates a payment command; a null amount means payment in full. */
    public PaidCommand(Index index, OutstandingAmount amount) {
        this.index = requireNonNull(index);
        this.amount = amount;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> people = model.getFilteredPersonList();
        if (index.getZeroBased() >= people.size()) {
            throw new CommandException(MESSAGE_CONTACT_NOT_FOUND);
        }
        Person person = people.get(index.getZeroBased());
        OutstandingAmount updatedAmount = amount == null ? OutstandingAmount.ZERO : person.getOutstandingAmount();
        if (amount != null) {
            if (amount.isGreaterThan(person.getOutstandingAmount())) {
                throw new CommandException(MESSAGE_PAYMENT_EXCEEDS_BALANCE);
            }
            updatedAmount = person.getOutstandingAmount().subtract(amount);
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
        return other instanceof PaidCommand command && index.equals(command.index)
                && java.util.Objects.equals(amount, command.amount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("amount", amount).toString();
    }
}
