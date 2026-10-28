package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, DeleteCommand.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand,
                ListCommand.getMessageForContactCount(model.getFilteredPersonList().size()), model);
    }

    @Test
    public void execute_addWithZeroOutstandingAmount_success() throws Exception {
        Person expectedPerson = new PersonBuilder().withName("Zero Balance").withPhone("81234567")
                .withAddress("1 Test Street").withStartDate("14-Oct").withOutstandingAmount("0")
                .withSubjects("Maths").build();

        CommandResult result = logic.execute(
                "add n/Zero Balance p/81234567 a/1 Test Street d/14-Oct o/0"
                        + " s/Maths day/MON time/09:00");

        assertEquals("Zero Balance's profile has been added to TuteeDex!", result.getFeedbackToUser());
        assertEquals(List.of(expectedPerson), logic.getFilteredPersonList());
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(expectedPerson), savedStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_addSubjects_subjectsPersist() throws Exception {
        logic.execute("add n/Subject Student p/81234567 a/1 Test Street d/14-Oct"
                + " s/Maths day/MON time/09:00 s/H2 Chemistry day/TUE time/15:30");
        logic.execute("edit 1 p/87654321");

        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Person saved = savedStorage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals(new PersonBuilder().withName("Subject Student").withPhone("87654321")
                .withAddress("1 Test Street").withStartDate("14-Oct")
                .withScheduledSubjects(
                        new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0))),
                        new Subject("H2 Chemistry", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(15, 30))))
                .build(), saved);
        assertEquals("Name: Subject Student\nPhone: 87654321\nAddress: 1 Test Street\nStart date: 14-Oct"
                + "\nOutstanding amount: SGD 0.00"
                + "\nSubjects: Maths (Mon 09:00), H2 Chemistry (Tue 15:30)"
                + "\nTags: Not specified\nNotes: ",
                logic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_subjectFilter_preservesDisplayedIndices() throws Exception {
        logic.execute("add n/Anna Tutor p/81234567 a/1 Test Street d/14-Oct"
                + " s/Maths day/MON time/09:00");
        logic.execute("add n/Ben Tutor p/81234568 a/2 Test Street d/14-Oct"
                + " s/Physics day/MON time/09:00");
        logic.execute("add n/Cara Tutor p/81234569 a/3 Test Street d/14-Oct"
                + " s/maths day/MON time/09:00 s/Chemistry day/TUE time/10:00");

        logic.execute("find Ben");
        assertEquals("Listed 2 contacts.",
                logic.execute("list s/MATHS s/maths s/Chemistry").getFeedbackToUser());
        assertEquals(List.of("Anna Tutor", "Cara Tutor"), logic.getFilteredPersonList().stream()
                .map(person -> person.getName().toString()).toList());
        assertThrows(ParseException.class, "Subject must not be blank.", () -> logic.execute("list s/"));
        assertEquals(2, logic.getFilteredPersonList().size());
        assertEquals("Name: Cara Tutor", logic.execute("view 2").getFeedbackToUser().split("\\n")[0]);

        logic.execute("delete 2");
        assertEquals("Listed 2 contacts.", logic.execute("list").getFeedbackToUser());
        assertEquals(List.of("Anna Tutor", "Ben Tutor"), logic.getFilteredPersonList().stream()
                .map(person -> person.getName().toString()).toList());
    }

    @Test
    public void execute_addWithTags_savesAndReloadsNormalizedTags() throws Exception {
        Person student = new PersonBuilder(AMY).withSubjects("Maths").build();
        Person expectedPerson = new PersonBuilder(student).withTags("a-level", "needs revision").build();

        CommandResult result = logic.execute(PersonUtil.getAddCommand(student)
                + "t/A-Level t/Needs revision t/a-level");

        assertEquals("Amy Bee's profile has been added to TuteeDex!", result.getFeedbackToUser());
        assertEquals(List.of(expectedPerson), logic.getFilteredPersonList());
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloadedModel = new ModelManager(savedStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(expectedPerson), reloadedModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addWithInvalidTags_preservesSavedDataAndFilter() throws Exception {
        Person existing = new PersonBuilder(AMY).withSubjects("Maths").withTags("a-level").build();
        Person bobWithSubject = new PersonBuilder(BOB).withSubjects("Physics").build();
        logic.execute(PersonUtil.getAddCommand(existing));
        logic.execute(PersonUtil.getAddCommand(bobWithSubject));
        logic.execute("find Amy");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String originalJson = Files.readString(savedPath);

        for (String tagInput : List.of("t/", "t/A-Level t/", "t/invalid!")) {
            String invalidCommand = PersonUtil.getAddCommand(new PersonBuilder().withSubjects("Maths").build())
                    + tagInput;
            assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> logic.execute(invalidCommand));
            assertEquals(List.of(existing, bobWithSubject), model.getAddressBook().getPersonList());
            assertEquals(List.of(existing), logic.getFilteredPersonList());
            assertEquals(originalJson, Files.readString(savedPath));
        }
    }

    @Test
    public void execute_addWithDifferentTags_preservesSavedDataOnDuplicate() throws Exception {
        Person existing = new PersonBuilder(AMY).withSubjects("Maths").withTags("a-level").build();
        Person bobWithSubject = new PersonBuilder(BOB).withSubjects("Physics").build();
        logic.execute(PersonUtil.getAddCommand(existing));
        logic.execute(PersonUtil.getAddCommand(bobWithSubject));
        logic.execute("find Amy");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String originalJson = Files.readString(savedPath);
        String duplicateCommand = PersonUtil.getAddCommand(new PersonBuilder(AMY).withSubjects("Maths")
                .withTags("needs revision").build());

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                logic.execute(duplicateCommand));

        assertEquals(List.of(existing, bobWithSubject), model.getAddressBook().getPersonList());
        assertEquals(List.of(existing), logic.getFilteredPersonList());
        assertEquals(originalJson, Files.readString(savedPath));
    }

    @Test
    public void execute_editAllFields_savesAndReloadsUpdatedDetails() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        Person updated = new PersonBuilder(AMY).withName("Amy Tan").withPhone("91234567")
                .withAddress("Block 9, Clementi Road").withStartDate("20-Oct").withOutstandingAmount("20.50").build();

        CommandResult result = logic.execute(
                "edit 1 n/Amy Tan p/91234567 a/Block 9, Clementi Road d/20-Oct o/20.50");

        assertEquals("Edited contact: Amy Tan", result.getFeedbackToUser());
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook savedData = savedStorage.readAddressBook().orElseThrow();
        assertEquals(List.of(updated, BOB), savedData.getPersonList());
        Model reloadedModel = new ModelManager(savedData, new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloadedModel, new StorageManager(savedStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        assertEquals("Name: Amy Tan\nPhone: 91234567\nAddress: Block 9, Clementi Road\nStart date: 20-Oct"
                + "\nOutstanding amount: SGD 20.50\nSubjects: Not specified\nTags: Not specified\nNotes: ",
                reloadedLogic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_partialEdit_savesPreservedFieldsAndNotifiesDisplayedList() throws Exception {
        Person original = new PersonBuilder(AMY).withOutstandingAmount("600").build();
        model.addPerson(original);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);
        Person updated = new PersonBuilder(original).withPhone("91234567").build();
        List<Person> displayedUpdates = new ArrayList<>();
        logic.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> {
            while (change.next()) {
                displayedUpdates.addAll(change.getAddedSubList());
            }
        });

        logic.execute("edit 1 p/91234567");

        assertEquals(List.of(updated), displayedUpdates);
        assertEquals(List.of(updated), logic.getFilteredPersonList());
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(updated), savedStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_invalidEdit_preservesSavedData() throws Exception {
        Person original = new PersonBuilder(AMY).withOutstandingAmount("600").build();
        model.addPerson(original);
        logic.execute("list");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String originalJson = Files.readString(savedPath);
        String invalidCommand = "edit 1 p/91234567 o/-1";

        assertThrows(ParseException.class, OutstandingAmount.MESSAGE_CONSTRAINTS, () -> logic.execute(invalidCommand));

        assertEquals(List.of(original), logic.getFilteredPersonList());
        assertEquals(originalJson, Files.readString(savedPath));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + ADDRESS_DESC_AMY + START_DATE_DESC_AMY + " s/Maths day/MON time/09:00";
        Person expectedPerson = new PersonBuilder(AMY).withSubjects("Maths").build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
