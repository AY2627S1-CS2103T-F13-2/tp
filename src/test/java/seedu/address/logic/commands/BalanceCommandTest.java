package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.testutil.Assert;
import seedu.address.testutil.PersonBuilder;

public class BalanceCommandTest {
    private final Person student = new PersonBuilder().withOutstandingAmount("50.00").build();

    @Test
    public void paidWithoutAmount_clearsBalance() throws Exception {
        Model model = modelWithStudent();
        new PaidCommand(INDEX_FIRST_PERSON, null).execute(model);
        assertEquals("0", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void paidWithAmount_reducesBalance() throws Exception {
        Model model = modelWithStudent();
        new PaidCommand(INDEX_FIRST_PERSON, new OutstandingAmount("20.50"))
                .execute(model);
        assertEquals("29.5", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void paidExceedingBalance_throwsCommandException() {
        Model model = modelWithStudent();
        Assert.assertThrows(
                CommandException.class, PaidCommand.MESSAGE_PAYMENT_EXCEEDS_BALANCE, () -> {
                    new PaidCommand(INDEX_FIRST_PERSON, new OutstandingAmount("50.01")).execute(model);
                });
        assertEquals("50", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void charge_addsToBalance() throws Exception {
        Model model = modelWithStudent();
        new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("12.50"))
                .execute(model);
        assertEquals("62.5", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    private Model modelWithStudent() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        return new ModelManager(addressBook, new UserPrefs());
    }
}
