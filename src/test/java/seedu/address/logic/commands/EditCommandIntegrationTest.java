package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests editing through the command parser and model together.
 */
public class EditCommandIntegrationTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_partialEditFilteredList_updatesDisplayedContact() throws Exception {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person updated = new PersonBuilder(target).withPhone("91234567").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        Command command = parser.parseCommand("edit 1 p/91234567");

        assertCommandSuccess(command, model, "Edited contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_trimmedUnchangedValue_preservesFilteredList() throws Exception {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        Person target = model.getFilteredPersonList().get(0);

        Command command = parser.parseCommand("edit 1 p/  " + target.getPhone().value + "  ");

        assertCommandSuccess(command, model, "No changes made to contact: " + target.getName(), expectedModel);
    }

    @Test
    public void execute_invalidInput_preservesDataAndFilteredList() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalDisplayedList = new ArrayList<>(model.getFilteredPersonList());
        String[] invalidCommands = {
            "edit 1 n/New Name p/invalid",
            "edit 1 a/New address x/unknown",
            "edit 1 p/91234567 p/87654321",
            "edit 1",
            "edit 0 p/91234567"
        };

        for (String commandText : invalidCommands) {
            assertThrows(ParseException.class, () -> parser.parseCommand(commandText).execute(model));
            assertEquals(originalData, model.getAddressBook());
            assertEquals(originalDisplayedList, model.getFilteredPersonList());
        }
    }
}
