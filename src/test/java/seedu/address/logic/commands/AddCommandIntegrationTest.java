package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;
    private final AddressBookParser parser = new AddressBookParser();

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
    public void execute_newPersonAfterArchive_keepsArchivedPersonHidden() throws Exception {
        Person toArchive = model.getFilteredPersonList().getFirst();
        new ArchiveCommand(toArchive.getName().fullName).execute(model);
        Person archived = toArchive.withArchived(true);
        List<Person> activePersons = new ArrayList<>(model.getFilteredPersonList());
        Person newPerson = new PersonBuilder().build();
        activePersons.add(newPerson);

        CommandResult result = new AddCommand(newPerson).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, newPerson.getName()), result.getFeedbackToUser());
        assertEquals(activePersons, model.getFilteredPersonList());
        assertEquals(archived, model.getAddressBook().getPersonList().getFirst());
        ListCommand.forArchivedStudents().execute(model);
        assertEquals(List.of(archived), model.getFilteredPersonList());
    }

    @Test
    public void execute_newPersonFromArchivedList_restoresActiveList() throws Exception {
        Person toArchive = model.getFilteredPersonList().getFirst();
        new ArchiveCommand(toArchive.getName().fullName).execute(model);
        List<Person> activePersons = new ArrayList<>(model.getFilteredPersonList());
        ListCommand.forArchivedStudents().execute(model);
        Person newPerson = new PersonBuilder().build();
        activePersons.add(newPerson);

        new AddCommand(newPerson).execute(model);

        assertEquals(activePersons, model.getFilteredPersonList());
        assertEquals(toArchive.withArchived(true), model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_addWithRepeatedTags_storesNormalizedTags() throws Exception {
        Person validPerson = new PersonBuilder().withSubjects("Maths").build();
        Person taggedPerson = new PersonBuilder(validPerson).withTags("a-level", "needs revision").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(taggedPerson);
        Command command = parser.parseCommand(PersonUtil.getAddCommand(validPerson)
                + "t/A-Level t/Needs revision t/a-level");

        assertCommandSuccess(command, model, String.format(AddCommand.MESSAGE_SUCCESS, taggedPerson.getName()),
                expectedModel);
    }

    @Test
    public void execute_addWithDifferentTags_rejectsDuplicateProfile() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person existing = new PersonBuilder(original).withTags("A-Level").build();
        model.setPerson(original, existing);
        Person duplicate = new PersonBuilder(original).withSubjects("Maths")
                .withTags("Needs revision").build();
        Command command = parser.parseCommand(PersonUtil.getAddCommand(duplicate));

        assertCommandFailure(command, model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_addWithDifferentSubjects_rejectsDuplicateProfile() {
        Person existing = model.getFilteredPersonList().get(0);
        Person duplicate = new PersonBuilder(existing).withSubjects("Maths").build();

        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_addWithInvalidTag_preservesDataAndFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalDisplayedList = new ArrayList<>(model.getFilteredPersonList());
        String addCommand = PersonUtil.getAddCommand(new PersonBuilder().withSubjects("Maths").build());

        for (String tagInput : List.of("t/", "t/A-Level t/", "t/ t/A-Level", "t/invalid!")) {
            String commandText = addCommand + tagInput;
            assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                    parser.parseCommand(commandText).execute(model));
            assertEquals(originalData, model.getAddressBook());
            assertEquals(originalDisplayedList, model.getFilteredPersonList());
        }
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
