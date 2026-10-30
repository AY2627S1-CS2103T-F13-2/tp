package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult.ListPresentation;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.sort.SortOrder;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises mixed subject/tag filtering with real command parsing, sorting, and storage. */
public class ListMixedFiltersIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private List<Person> stored;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Amy Bee").withSubjects("Maths", "Physics")
                .withTags("a-level", "needs revision").build());
        model.addPerson(new PersonBuilder().withName("Bob Choo").withSubjects("Physics")
                .withTags("needs revision").withOutstandingAmount("20").build());
        model.addPerson(new PersonBuilder().withName("Cara Dee").withSubjects("Maths").withTags("other").build());
        model.addPerson(new PersonBuilder().withName("Dora Lee").withSubjects("Chemistry")
                .withTags("a-level").build());
        model.addPerson(new PersonBuilder().withName("Evan Goh").withSubjects("Maths").withTags().build());
        model.addPerson(new PersonBuilder().withName("Finn Tan").withSubjects().withTags("a-level").build());
        model.addPerson(new PersonBuilder().withName("Gina Lim").withSubjects("Maths")
                .withTags("a-level").build().withArchived(true));
        stored = List.copyOf(model.getAddressBook().getPersonList());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_mixedFilters_matchesAnyWithinEachCategoryAndBothCategories() throws Exception {
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("list s/Maths s/Physics t/A-Level t/Needs revision",
                "list t/A-LEVEL s/physics t/needs revision s/MATHS t/a-level s/Maths")) {
            logic.execute("find Nobody");
            assertEquals("Listed 2 contacts.", logic.execute(command).getFeedbackToUser());
            assertEquals(stored.subList(0, 2), model.getFilteredPersonList());
        }
        assertEquals(stored, storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("list");
        assertEquals(stored.subList(0, 6), model.getFilteredPersonList());
    }

    @Test
    public void execute_completeCaseInsensitiveMatches_returnsSingleStudent() throws Exception {
        assertEquals(ListCommand.MESSAGE_SINGULAR,
                logic.execute("list t/A-LEVEL s/mAtHs").getFeedbackToUser());
        assertEquals(List.of(stored.getFirst()), model.getFilteredPersonList());
        logic.execute("list t/revision s/Physics");
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("list t/a-level s/Math");
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_summaryAndNoMatches_countsOnlyIntersection() throws Exception {
        assertEquals(String.format(ListCommand.MESSAGE_SUMMARY_FORMAT, "Total: 2", "Paid: 1"),
                logic.execute("list -summary s/Maths s/Physics t/A-Level t/Needs revision").getFeedbackToUser());
        assertEquals("No contacts match the specified subjects and tags.",
                logic.execute("list s/Chemistry t/Needs revision").getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(String.format(ListCommand.MESSAGE_SUMMARY_FORMAT, "Total: 0", "Paid: 0"),
                logic.execute("list -summary t/Needs revision s/Chemistry").getFeedbackToUser());
    }

    @Test
    public void execute_mixedFilterAfterGrouping_preservesSortAndUsesDisplayedIndexes() throws Exception {
        logic.execute("sort n/desc");
        SortOrder order = model.getSortOrder();
        logic.execute("list -subjects");
        assertEquals(ListPresentation.CONTACTS, logic.execute(
                "list s/Maths s/Physics t/A-Level t/Needs revision").getListPresentation());
        assertEquals(List.of(stored.get(1), stored.get(0)), model.getFilteredPersonList());
        assertEquals(order, model.getSortOrder());
        assertTrue(logic.execute("view 1").getFeedbackToUser().startsWith("Name: Bob Choo\n"));
        logic.execute("delete 1");
        logic.execute("delete confirm");
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().stream()
                .noneMatch(person -> person.getName().fullName.equals("Bob Choo")));
        assertTrue(model.getAddressBook().getPersonList().contains(stored.get(6)));
    }

    @Test
    public void execute_invalidMixedFilters_preservesCurrentViewSortAndSavedProfiles() throws Exception {
        logic.execute("sort n/desc");
        logic.execute("find Amy");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        SortOrder order = model.getSortOrder();
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("list s/ t/a-level", "list s/Maths t/", "list s/Maths t/bad_tag",
                "list t/a-level s/---", "list t/a-level S/Maths", "list s/Maths t/a-level x/value",
                "list extra s/Maths t/a-level", "list -archived s/Maths t/a-level",
                "list s/Maths t/a-level -subjects", "list -summary -summary s/Maths t/a-level")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(order, model.getSortOrder());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }
}
