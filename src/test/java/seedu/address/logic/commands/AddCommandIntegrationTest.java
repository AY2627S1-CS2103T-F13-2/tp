package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, validPerson.getName()),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_sameProfileWithDifferentOutstandingAmount_success() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person personWithDifferentAmount = new PersonBuilder(personInList).withOutstandingAmount("20").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(personWithDifferentAmount);

        assertCommandSuccess(new AddCommand(personWithDifferentAmount), model,
                String.format(AddCommand.MESSAGE_SUCCESS, personWithDifferentAmount.getName()), expectedModel);
    }

    @Test
    public void execute_equivalentOutstandingAmount_throwsCommandException() {
        Person personWithZero = model.getAddressBook().getPersonList().get(0);
        Person personWithEquivalentZero = new PersonBuilder(personWithZero).withOutstandingAmount("0.00").build();

        assertCommandFailure(new AddCommand(personWithEquivalentZero), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

}
