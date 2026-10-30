package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
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
import seedu.address.logic.commands.EditCommand;
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
    private StorageManager storage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_deletePreviewAndCancellation_doNotSave() throws Exception {
        model.addPerson(AMY);
        logic.execute("delete 1");
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
        logic.execute("list");
        assertThrows(CommandException.class, DeleteCommand.MESSAGE_NO_PREVIEW, () -> logic.execute("delete confirm"));
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_interveningCommands_cancelDeletePreview() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        for (String command : List.of("sort n/desc", "find Bob", "list -archived", "list",
                "delete 99", "unknown", "delete 1 confirm", "delete confirm extra")) {
            logic.execute("list");
            logic.execute("delete 1");
            try {
                logic.execute(command);
            } catch (CommandException | ParseException expected) {
                // Failed commands also cancel a pending deletion.
            }
            assertThrows(CommandException.class, DeleteCommand.MESSAGE_NO_PREVIEW, () ->
                    logic.execute("delete confirm"));
            assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void execute_confirmDelete_persistsAndCannotRepeatOrSurviveRestart() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        storage.saveAddressBook(model.getAddressBook());
        String savedBefore = Files.readString(storage.getAddressBookFilePath());
        logic.execute("delete 1");
        assertEquals(savedBefore, Files.readString(storage.getAddressBookFilePath()));
        Logic restartedLogic = new LogicManager(model, storage);
        assertThrows(CommandException.class, () -> restartedLogic.execute("delete confirm"));
        assertEquals(List.of(AMY, BOB), storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(AMY)),
                logic.execute("delete confirm").getFeedbackToUser());
        assertEquals(List.of(BOB), storage.readAddressBook().orElseThrow().getPersonList());
        assertCommandException("delete confirm", DeleteCommand.MESSAGE_NO_PREVIEW);
    }

    @Test
    public void execute_confirmDeleteSaveFailure_restoresStudentAndAllowsRetry() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw failure;
                }
            };
            Logic failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            failingLogic.execute("find Amy");
            failingLogic.execute("delete 1");
            assertThrows(CommandException.class, () -> failingLogic.execute("delete confirm"));
            assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList());
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertEquals("find Amy", model.getLastViewCommand());
            assertThrows(CommandException.class, DeleteCommand.MESSAGE_NO_PREVIEW, () ->
                    failingLogic.execute("delete confirm"));
        }
        logic.execute("delete 1");
        logic.execute("delete confirm");
        assertEquals(List.of(BOB), storage.readAddressBook().orElseThrow().getPersonList());
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
    public void execute_listAndFindCommands_savedAsLastView() throws Exception {
        logic.execute("list -archived");
        assertEquals("list -archived", model.getLastViewCommand());

        logic.execute("  find Alice  ");
        assertEquals("find Alice", model.getLastViewCommand());
    }

    @Test
    public void execute_commandThatChangesFilter_clearsLastView() throws Exception {
        model.addPerson(AMY);
        logic.execute("find Amy");
        logic.execute("archive 1");
        assertEquals("", model.getLastViewCommand());
    }

    @Test
    public void execute_editAfterFind_nextChargeTargetsSameStudent() throws Exception {
        Person zed = new PersonBuilder(AMY).withName("Zed Test").withOutstandingAmount("10").build();
        Person amy = new PersonBuilder(BOB).withName("Amy Test").withOutstandingAmount("10").build();
        model.addPerson(zed);
        model.addPerson(amy);
        logic.execute("sort n/asc");
        logic.execute("find Zed");
        logic.execute("edit 1 notes/Updated");
        assertEquals(List.of(new PersonBuilder(zed).withNotes("Updated").build()), logic.getFilteredPersonList());
        assertEquals("find Zed", logic.getLastViewCommand());
        logic.execute("charge 1 5");
        assertEquals("15", logic.getFilteredPersonList().getFirst().getOutstandingAmount().toPlainString());
        assertEquals(amy, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void reopen_afterFilteredEdits_restoresRecomputedViewAndSort() throws Exception {
        for (String view : List.of("find Amy", "list s/Maths t/Exam", "list -archived")) {
            model = new ModelManager();
            logic = new LogicManager(model, storage);
            model.addPerson(new PersonBuilder(AMY).withSubjects("Maths").withTags("Exam").build());
            model.addPerson(new PersonBuilder(BOB).withSubjects("Maths").withTags("Exam").build());
            if (view.equals("list -archived")) {
                logic.execute("archive 1");
            }
            logic.execute("sort n/asc");
            logic.execute(view);
            logic.execute("edit 1 notes/Updated");
            assertEquals(view, logic.getLastViewCommand());
            if (view.equals("find Amy")) {
                assertEquals("Edited contact: Zed Test" + EditCommand.MESSAGE_INDEXES_CHANGED,
                        logic.execute("edit 1 n/Zed Test").getFeedbackToUser());
            } else if (view.startsWith("list s/")) {
                assertEquals("Edited contact: Zed Test" + EditCommand.MESSAGE_INDEXES_CHANGED,
                        logic.execute("edit 1 n/Zed Test").getFeedbackToUser());
                assertEquals("Zed Test", logic.getFilteredPersonList().getLast().getName().fullName);
                assertEquals("Edited contact: " + BOB.getName() + EditCommand.MESSAGE_INDEXES_CHANGED,
                        logic.execute("edit 1 t/Other").getFeedbackToUser());
                assertEquals(1, logic.getFilteredPersonList().size());
                assertEquals("Edited contact: Zed Test" + EditCommand.MESSAGE_INDEXES_CHANGED,
                        logic.execute("edit 1 s/Physics day/MON time/09:00").getFeedbackToUser());
            }
            if (!view.equals("list -archived")) {
                assertEquals(List.of(), logic.getFilteredPersonList());
                assertThrows(CommandException.class, () -> logic.execute("charge 1 5"));
            }
            List<Person> shown = List.copyOf(logic.getFilteredPersonList());
            storage.saveUserPrefs(model.getUserPrefs());
            Model reopened = new ModelManager(storage.readAddressBook().orElseThrow(),
                    storage.readUserPrefs().orElseThrow());
            Logic reopenedLogic = new LogicManager(reopened, storage);
            reopenedLogic.execute(reopenedLogic.getLastSortCommand());
            reopenedLogic.execute(reopenedLogic.getLastViewCommand());
            assertEquals(shown, reopenedLogic.getFilteredPersonList());
            assertEquals(model.getSortOrder(), reopened.getSortOrder());
            assertEquals(view, reopenedLogic.getLastViewCommand());
        }
    }

    @Test
    public void execute_commandLeavingSubjectGroups_clearsLastView() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withSubjects("Maths").build());
        logic.execute("list -subjects");
        logic.execute("delete 1");
        logic.execute("delete confirm");
        assertEquals("", model.getLastViewCommand());
    }

    @Test
    public void execute_commandKeepingFilter_keepsLastView() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("find Amy");
        logic.execute("edit 1 n/" + VALID_NAME_AMY);
        logic.execute("delete 1");
        logic.execute("delete confirm");
        assertEquals("find Amy", model.getLastViewCommand());
    }

    @Test
    public void reopen_afterFindAndDelete_showsSameStudents() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("find Amy");
        logic.execute("delete 1");
        logic.execute("delete confirm");
        List<Person> shownBeforeClosing = List.copyOf(model.getFilteredPersonList());

        // Reopen with the same data and preferences, then run the saved view again as MainWindow does on startup.
        Model reopenedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        Logic reopenedLogic = new LogicManager(reopenedModel, storage);
        reopenedLogic.execute(reopenedLogic.getLastViewCommand());
        assertEquals(shownBeforeClosing, reopenedLogic.getFilteredPersonList());
    }

    @Test
    public void execute_helpExitAndFailedCommands_keepLastView() throws Exception {
        logic.execute("list -subjects");
        logic.execute("help");
        logic.execute("exit");
        assertThrows(CommandException.class, () -> logic.execute("delete 9"));
        assertThrows(ParseException.class, () -> logic.execute("list -unknown"));
        assertEquals("list -subjects", model.getLastViewCommand());
    }

    @Test
    public void execute_listAndFind_doNotSaveAddressBook() throws Exception {
        logic.execute("list");
        logic.execute("find Alice");
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_sortCommands_saveLastSortAndKeepView() throws Exception {
        logic.execute("list -subjects");
        logic.execute(" sort o/desc n/asc ");
        assertEquals("sort o/desc n/asc", model.getLastSortCommand());
        assertEquals("list -subjects", model.getLastViewCommand());

        logic.execute("sort");
        assertEquals("sort o/desc n/asc", model.getLastSortCommand());

        assertThrows(ParseException.class, () -> logic.execute("sort n/up"));
        assertEquals("sort o/desc n/asc", model.getLastSortCommand());

        logic.execute("sort reset");
        assertEquals("", model.getLastSortCommand());
        assertEquals("list -subjects", model.getLastViewCommand());
    }

    @Test
    public void getLastSortCommand_savedSortOrOther_onlySortReturned() {
        assertEquals("", logic.getLastSortCommand());
        model.setLastSortCommand("sort slot/asc n/asc");
        assertEquals("sort slot/asc n/asc", logic.getLastSortCommand());
        for (String savedCommand : new String[] {"list", "clear", "sort n/up", "not a command"}) {
            model.setLastSortCommand(savedCommand);
            assertEquals("", logic.getLastSortCommand());
        }
    }

    @Test
    public void getLastViewCommand_savedListOrFind_returned() {
        assertEquals("", logic.getLastViewCommand());
        model.setLastViewCommand("list t/Sec 4");
        assertEquals("list t/Sec 4", logic.getLastViewCommand());
        model.setLastViewCommand("find Alice");
        assertEquals("find Alice", logic.getLastViewCommand());
    }

    @Test
    public void getLastViewCommand_otherOrInvalidCommand_ignored() {
        for (String savedCommand : new String[] {"clear", "delete 1", "sort n/asc", "list -unknown", "not a command"}) {
            model.setLastViewCommand(savedCommand);
            assertEquals("", logic.getLastViewCommand());
        }
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
        logic.execute("delete confirm");
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
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        Person updated = new PersonBuilder(original).withPhone("91234567").build();
        List<Person> displayedUpdates = new ArrayList<>();
        logic.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> {
            while (change.next()) {
                displayedUpdates.addAll(change.getAddedSubList());
            }
        });

        logic.execute("edit 1 p/91234567");

        assertEquals(List.of(updated), displayedUpdates.stream().distinct().toList());
        assertEquals(List.of(updated), logic.getFilteredPersonList());
        JsonAddressBookStorage savedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(updated), savedStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_invalidEdit_preservesSavedData() throws Exception {
        Person original = new PersonBuilder(AMY).withOutstandingAmount("600").build();
        model.addPerson(original);
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        new JsonAddressBookStorage(savedPath).saveAddressBook(model.getAddressBook());
        String originalJson = Files.readString(savedPath);
        String invalidCommand = "edit 1 p/91234567 o/-1";

        assertThrows(ParseException.class, OutstandingAmount.MESSAGE_CONSTRAINTS, () -> logic.execute(invalidCommand));

        assertEquals(List.of(original), logic.getFilteredPersonList());
        assertEquals(originalJson, Files.readString(savedPath));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage())
                + LogicManager.MESSAGE_CHANGES_UNDONE);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage())
                + LogicManager.MESSAGE_CHANGES_UNDONE);
    }

    @Test
    public void execute_retryAfterFailedSave_chargesOnce() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withOutstandingAmount("10").build());
        boolean[] isSaveFailing = {true};
        logic = new LogicManager(model, storageWithSave(addressBook -> {
            if (isSaveFailing[0]) {
                throw DUMMY_IO_EXCEPTION;
            }
        }));

        assertThrows(CommandException.class, () -> logic.execute("charge 1 5"));
        assertEquals(new OutstandingAmount("10"), model.getFilteredPersonList().get(0).getOutstandingAmount());

        isSaveFailing[0] = false;
        logic.execute("charge 1 5");
        assertEquals(new OutstandingAmount("15"), model.getFilteredPersonList().get(0).getOutstandingAmount());
    }

    @Test
    public void execute_commandsThatOnlyShowInformation_doNotSave() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withSubjects("Maths").build());
        logic = new LogicManager(model, storageWithSave(addressBook -> {
            throw DUMMY_IO_EXCEPTION;
        }));

        // Saving would fail, and would overwrite a data file that could not be loaded
        logic.execute("help");
        logic.execute("view 1");
        logic.execute("exit");
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

    /** Functional interface for a stubbed save that may throw. */
    private interface SaveAction {
        void save(ReadOnlyAddressBook addressBook) throws IOException;
    }

    private StorageManager storageWithSave(SaveAction saveAction) {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("StubAddressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        saveAction.save(addressBook);
                    }
                };
        return new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("StubUserPrefs.json")));
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

        // Triggers the saveAddressBook method by executing an add command, which is undone when saving fails
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + ADDRESS_DESC_AMY + START_DATE_DESC_AMY + " s/Maths day/MON time/09:00";
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, new ModelManager());
    }
}
