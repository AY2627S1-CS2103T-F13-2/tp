package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

/** Tests subject grouping without starting the JavaFX toolkit. */
public class SubjectGroupingTest {
    @Test
    public void group_reversedInput_ordersAllSubjectsAndStudentsIgnoringCase() {
        Person zed = new PersonBuilder(ALICE).withName("Zed Student").withSubjects("Physics", "Maths").build();
        Person alice = new PersonBuilder(ALICE).withName("alice Student").withSubjects("maths", "English").build();
        List<SubjectGrouping.Group> groups = SubjectGrouping.group(List.of(zed, alice));

        assertEquals(List.of("English", "Maths", "Physics"),
                groups.stream().map(SubjectGrouping.Group::displayName).toList());
        assertEquals(List.of(List.of(alice), List.of(alice, zed), List.of(zed)), groups.stream()
                .map(group -> group.entries().stream().map(SubjectGrouping.Entry::person).toList()).toList());
    }

    @Test
    public void group_sameSubjectDifferentSlots_preservesEachStudentsLesson() {
        Subject monday = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        Subject tuesday = new Subject("maths", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(15, 30)));
        Person bob = new PersonBuilder(ALICE).withName("Bob Student").withScheduledSubjects(tuesday).build();
        Person alice = new PersonBuilder(ALICE).withScheduledSubjects(monday).build();
        List<SubjectGrouping.Group> groups = SubjectGrouping.group(List.of(bob, alice));

        assertEquals(1, groups.size());
        assertEquals(List.of(monday, tuesday), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
    }

    @Test
    public void group_spaceVariantsAcrossStudents_sharesHeadingAndPreservesIndividualSlots() {
        Subject monday = new Subject("H2  Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        Subject tuesday = new Subject("h2   maths", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(15, 30)));
        Person alice = new PersonBuilder(ALICE).withScheduledSubjects(monday).build();
        Person bob = new PersonBuilder(ALICE).withName("Bob Student").withScheduledSubjects(tuesday).build();

        List<SubjectGrouping.Group> groups = SubjectGrouping.group(List.of(alice, bob));

        assertEquals(1, groups.size());
        assertEquals("H2 Maths", groups.getFirst().displayName());
        assertEquals(List.of(monday, tuesday), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
    }

    @Test
    public void group_emptyOrSubjectlessContacts_returnsNoGroups() {
        assertEquals(List.of(), SubjectGrouping.group(List.of()));
        assertEquals(List.of(), SubjectGrouping.group(List.of(ALICE)));
    }

    @Test
    public void group_resultsAreImmutableSnapshots() {
        List<SubjectGrouping.Group> groups = SubjectGrouping.group(
                List.of(new PersonBuilder(ALICE).withSubjects("Maths").build()));
        assertThrows(UnsupportedOperationException.class, groups::clear);
        assertThrows(UnsupportedOperationException.class, groups.getFirst().entries()::clear);
    }

    @Test
    public void group_incompleteOrderedSnapshot_reportsTheInternalInvariant() {
        Person amy = new PersonBuilder(ALICE).withSubjects("Maths").build();
        Person zed = new PersonBuilder(ALICE).withName("Zed Student").withSubjects("Maths").build();
        assertThrows(AssertionError.class, () -> SubjectGrouping.group(List.of(amy, zed), List.of()));
    }

    @Test
    public void group_multipleSubjects_keepsCommandIndexesAfterSortingAndSubjectlessContacts() throws Exception {
        Person zed = new PersonBuilder(ALICE).withName("Zed Student").withSubjects("Physics", "Maths").build();
        Person alice = new PersonBuilder(ALICE).withSubjects("Maths").build();
        Person subjectless = new PersonBuilder(ALICE).withName("No Subjects").build();
        Person archived = new PersonBuilder(ALICE).withName("Archived Student")
                .withSubjects("Physics").build().withArchived(true);
        ModelManager model = new ModelManager();
        model.addPerson(archived);
        model.addPerson(subjectless);
        model.addPerson(zed);
        model.addPerson(alice);
        ListCommand.forSubjectGroups().execute(model);
        List<SubjectGrouping.Group> groups = SubjectGrouping.group(model.getFilteredPersonList());

        assertEquals(List.of("Maths", "Physics"),
                groups.stream().map(SubjectGrouping.Group::displayName).toList());
        assertEquals(List.of(3, 2), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::displayedIndex).toList());
        SubjectGrouping.Entry physicsEntry = groups.getLast().entries().getFirst();
        assertEquals(zed, physicsEntry.person());
        assertEquals(2, physicsEntry.displayedIndex());

        AddressBookParser parser = new AddressBookParser();
        assertTrue(parser.parseCommand("view " + physicsEntry.displayedIndex()).execute(model)
                .getFeedbackToUser().startsWith("Name: Zed Student\n"));
        parser.parseCommand("delete " + physicsEntry.displayedIndex()).execute(model);
        assertEquals(List.of(archived, subjectless, alice), model.getAddressBook().getPersonList());
    }

    @Test
    public void group_amountSorting_preservesPriorityAndPrintedStudentIndexes() throws Exception {
        Person amy = new PersonBuilder(ALICE).withSubjects("Physics", "Maths").withOutstandingAmount("2").build();
        Person bob = new PersonBuilder(ALICE).withName("Bob Student").withSubjects("Maths", "Physics")
                .withOutstandingAmount("10").build();
        ModelManager model = new ModelManager();
        model.addPerson(amy);
        model.addPerson(bob);
        model.addPerson(new PersonBuilder(ALICE).withName("No Subjects").withOutstandingAmount("20").build());
        new AddressBookParser().parseCommand("sort o/desc n/asc").execute(model);

        List<SubjectGrouping.Group> groups = SubjectGrouping.group(
                model.getFilteredPersonList(), model.getLessonRows());
        assertEquals(List.of("Maths", "Physics"), groups.stream().map(SubjectGrouping.Group::displayName).toList());
        for (SubjectGrouping.Group group : groups) {
            assertEquals(List.of(bob, amy), group.entries().stream().map(SubjectGrouping.Entry::person).toList());
            assertEquals(List.of(2, 3), group.entries().stream().map(SubjectGrouping.Entry::displayedIndex).toList());
        }
    }

    @Test
    public void group_slotSorting_usesEachSubjectsOwnScheduleInBothDirections() throws Exception {
        Subject amyMaths = new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        Subject amyPhysics = new Subject("Physics", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Subject bobMaths = new Subject("Maths", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        Subject bobPhysics = new Subject("Physics", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(10, 0)));
        Person amy = new PersonBuilder(ALICE).withScheduledSubjects(amyMaths, amyPhysics).build();
        Person bob = new PersonBuilder(ALICE).withName("Bob Student")
                .withScheduledSubjects(bobMaths, bobPhysics).build();
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(),
                Clock.fixed(Instant.parse("2026-10-22T14:00:00Z"), ZoneOffset.UTC));
        model.addPerson(amy);
        model.addPerson(bob);
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("sort slot/asc").execute(model);

        List<SubjectGrouping.Group> groups = SubjectGrouping.group(
                model.getFilteredPersonList(), model.getLessonRows());
        assertEquals(List.of(bobMaths, amyMaths), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
        assertEquals(List.of(amyPhysics, bobPhysics), groups.getLast().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
        assertEquals(List.of(2, 1), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::displayedIndex).toList());

        parser.parseCommand("sort slot/desc").execute(model);
        groups = SubjectGrouping.group(model.getFilteredPersonList(), model.getLessonRows());
        assertEquals(List.of(amyMaths, bobMaths), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
        assertEquals(List.of(bobPhysics, amyPhysics), groups.getLast().entries().stream()
                .map(SubjectGrouping.Entry::subject).toList());
        assertEquals(List.of(amy, bob), model.getAddressBook().getPersonList());
    }

    @Test
    public void group_equalSortKeys_preservesStoredTiesRatherThanAlphabeticalOrder() throws Exception {
        Person zed = new PersonBuilder(ALICE).withName("Zed Student").withSubjects("Maths").build();
        Person amy = new PersonBuilder(ALICE).withSubjects("maths").build();
        ModelManager model = new ModelManager();
        model.addPerson(zed);
        model.addPerson(amy);
        new AddressBookParser().parseCommand("sort o/asc").execute(model);

        List<SubjectGrouping.Group> groups = SubjectGrouping.group(
                model.getFilteredPersonList(), model.getLessonRows());
        assertEquals("Maths", groups.getFirst().displayName());
        assertEquals(List.of(zed, amy), groups.getFirst().entries().stream()
                .map(SubjectGrouping.Entry::person).toList());
        assertEquals(List.of(amy, zed), SubjectGrouping.group(model.getFilteredPersonList()).getFirst().entries()
                .stream().map(SubjectGrouping.Entry::person).toList());
        assertThrows(UnsupportedOperationException.class, groups.getFirst().entries()::clear);
    }

    @Test
    public void group_equalLessonSlots_keepStoredTiesDespiteDifferentRepresentativeLessons() throws Exception {
        Subject maths = new Subject("Maths", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        Subject english = new Subject("English", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)));
        Person amy = new PersonBuilder(ALICE).withScheduledSubjects(maths).build();
        Person zed = new PersonBuilder(ALICE).withName("Zed Student").withScheduledSubjects(maths, english).build();
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(),
                Clock.fixed(Instant.parse("2026-10-22T14:00:00Z"), ZoneOffset.UTC));
        model.addPerson(amy);
        model.addPerson(zed);
        new AddressBookParser().parseCommand("sort slot/asc").execute(model);

        assertEquals(List.of(zed, amy), model.getFilteredPersonList());
        List<SubjectGrouping.Group> groups = SubjectGrouping.group(
                model.getFilteredPersonList(), model.getLessonRows());
        assertEquals("Maths", groups.getLast().displayName());
        assertEquals(List.of(amy, zed), groups.getLast().entries().stream()
                .map(SubjectGrouping.Entry::person).toList());
        assertEquals(List.of(2, 1), groups.getLast().entries().stream()
                .map(SubjectGrouping.Entry::displayedIndex).toList());
    }
}
