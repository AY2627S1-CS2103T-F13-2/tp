package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

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
}
