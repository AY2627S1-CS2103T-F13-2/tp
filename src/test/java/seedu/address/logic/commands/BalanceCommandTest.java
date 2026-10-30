package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void chargeAboveMaximum_throwsCommandException() throws Exception {
        Model model = modelWithStudent();
        Assert.assertThrows(CommandException.class, ChargeCommand.MESSAGE_BALANCE_TOO_LARGE, () ->
                new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("999950.01")).execute(model));
        assertEquals("50", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());

        new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("999950")).execute(model);
        assertEquals(OutstandingAmount.MAXIMUM, model.getFilteredPersonList().get(0).getOutstandingAmount());
    }

    @Test
    public void paidResultingInDuplicate_throwsCommandException() {
        Model model = modelWithStudentAndMatchingBalance("20.00");
        Assert.assertThrows(CommandException.class, PaidCommand.MESSAGE_DUPLICATE_PERSON, () ->
                new PaidCommand(INDEX_FIRST_PERSON, new OutstandingAmount("30.00")).execute(model));
        assertEquals("50", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void paidZeroBalance_doesNotThrowDuplicateException() throws Exception {
        Model model = modelWithStudent("0.00");
        new PaidCommand(INDEX_FIRST_PERSON, null).execute(model);
        assertEquals("0", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void chargeResultingInDuplicate_throwsCommandException() {
        Model model = modelWithStudentAndMatchingBalance("62.50");
        Assert.assertThrows(CommandException.class, ChargeCommand.MESSAGE_DUPLICATE_PERSON, () ->
                new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("12.50")).execute(model));
        assertEquals("50", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void chargeZeroAmount_doesNotThrowDuplicateException() throws Exception {
        Model model = modelWithStudent();
        new ChargeCommand(INDEX_FIRST_PERSON, OutstandingAmount.ZERO).execute(model);
        assertEquals("50", model.getFilteredPersonList().get(0).getOutstandingAmount().toPlainString());
    }

    @Test
    public void paid_invalidIndex_throwsCommandException() {
        Model model = modelWithStudent();
        PaidCommand command = new PaidCommand(seedu.address.commons.core.index.Index.fromOneBased(2), null);
        Assert.assertThrows(CommandException.class, PaidCommand.MESSAGE_CONTACT_NOT_FOUND, () ->
                command.execute(model));
    }

    @Test
    public void charge_invalidIndex_throwsCommandException() {
        Model model = modelWithStudent();
        ChargeCommand command = new ChargeCommand(seedu.address.commons.core.index.Index.fromOneBased(2),
                new OutstandingAmount("1"));
        Assert.assertThrows(CommandException.class, ChargeCommand.MESSAGE_CONTACT_NOT_FOUND, () ->
                command.execute(model));
    }

    @Test
    public void commandEqualityAndString_areSupported() {
        OutstandingAmount amount = new OutstandingAmount("5");
        PaidCommand paid = new PaidCommand(INDEX_FIRST_PERSON, amount);
        ChargeCommand charge = new ChargeCommand(INDEX_FIRST_PERSON, amount);
        assertTrue(paid.equals(new PaidCommand(INDEX_FIRST_PERSON, amount)));
        assertFalse(paid.equals(new PaidCommand(seedu.address.commons.core.index.Index.fromOneBased(2), amount)));
        assertFalse(paid.equals(new PaidCommand(INDEX_FIRST_PERSON, null)));
        assertFalse(paid.equals(charge));
        assertTrue(charge.equals(new ChargeCommand(INDEX_FIRST_PERSON, amount)));
        assertFalse(charge.equals(new ChargeCommand(seedu.address.commons.core.index.Index.fromOneBased(2), amount)));
        assertFalse(charge.equals(new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("6"))));
        assertFalse(charge.equals(paid));
        assertFalse(paid.equals(null));
        assertFalse(charge.equals(null));
        assertTrue(paid.toString().contains("index"));
        assertTrue(charge.toString().contains("amount"));
    }

    private Model modelWithStudent() {
        return modelWithStudent(student.getOutstandingAmount().toPlainString());
    }

    private Model modelWithStudent(String outstandingAmount) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder(student).withOutstandingAmount(outstandingAmount).build());
        return new ModelManager(addressBook, new UserPrefs());
    }

    private Model modelWithStudentAndMatchingBalance(String matchingBalance) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        addressBook.addPerson(new PersonBuilder(student).withOutstandingAmount(matchingBalance).build());
        return new ModelManager(addressBook, new UserPrefs());
    }
}
