package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.exceptions.DuplicatePersonException;

public class StudentDetailsTest {
    @Test
    public void constructor_requiresSubjectsAndStartDate() {
        assertThrows(IllegalArgumentException.class, () -> new StudentDetails(Set.of(), new StartDate("10-Aug")));
        assertThrows(NullPointerException.class, () -> new StudentDetails(Set.of(new Subject("Maths")), null));
        Set<Subject> subjects = new HashSet<>(Set.of(new Subject("Maths")));
        StudentDetails details = new StudentDetails(subjects, new StartDate("10-Aug"));
        subjects.clear();
        assertEquals(Set.of(new Subject("Maths")), details.getSubjects());
        assertThrows(UnsupportedOperationException.class, () -> details.getSubjects().clear());
    }

    @Test
    public void equals_normalizedValuesAndSubjectOrder_match() {
        StudentDetails first = details(Set.of(new Subject(" Maths "), new Subject("Physics")), "20");
        StudentDetails second = details(Set.of(new Subject("Physics"), new Subject("Maths")), "20.00");
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(student(first).isSamePerson(student(second)));
        assertEquals(new StudentDetails(Set.of(new Subject("Maths")), new StartDate("10-Aug")),
                details(Set.of(new Subject("Maths")), ""));
    }

    @Test
    public void equals_everyStudentFieldParticipates() {
        StudentDetails original = details(Set.of(new Subject("Maths")), "0");
        List<StudentDetails> variants = List.of(
                details(Set.of(new Subject("Physics")), "0"),
                new StudentDetails(original.getSubjects(), new StartDate("11-Aug")),
                details(original.getSubjects(), "20"),
                changedOptional("21", "", "", ""),
                changedOptional("", "A-Level", "", ""),
                changedOptional("", "", "91234567", ""),
                changedOptional("", "", "", "Bring notes"));
        for (StudentDetails variant : variants) {
            assertFalse(student(original).isSamePerson(student(variant)));
        }
        assertFalse(original.equals(null));
        assertFalse(original.equals("Maths"));
    }

    @Test
    public void uniqueness_allowsSameNameButRejectsExactDuplicates() {
        UniquePersonList persons = new UniquePersonList();
        Person first = student(details(Set.of(new Subject("Maths")), "0"));
        Person second = student(details(Set.of(new Subject("Maths")), "20"));
        persons.add(first);
        persons.add(second);
        assertEquals(2, persons.asUnmodifiableObservableList().size());
        assertThrows(DuplicatePersonException.class, () -> persons.setPerson(first, second));
        assertEquals(List.of(first, second), persons.asUnmodifiableObservableList());
        assertThrows(DuplicatePersonException.class, () ->
                persons.add(student(first.getStudentDetails().orElseThrow())));
    }

    @Test
    public void legacyEdit_preservesStudentDetails() throws Exception {
        Person original = student(changedOptional("21", "A-Level", "91234567", "Bring worksheets"));
        AddressBook book = new AddressBook();
        book.addPerson(original);
        Model model = new ModelManager(book, new UserPrefs());
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPhone(new Phone("87654321"));
        new EditCommand(Index.fromOneBased(1), descriptor).execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(original.getStudentDetails(), edited.getStudentDetails());
        assertTrue(edited.getEmail().isEmpty());
        assertEquals(new Phone("87654321"), edited.getPhone());
    }

    @Test
    public void studentConstructor_rejectsLegacyOnlyValues() {
        StudentDetails details = details(Set.of(new Subject("Maths")), "0");
        assertThrows(IllegalArgumentException.class, () ->
                new Person(new Name("Peter2"), new Phone("91234567"), new Address("Clementi"), details));
        assertThrows(IllegalArgumentException.class, () ->
                new Person(new Name("Peter"), new Phone("123"), new Address("Clementi"), details));
    }

    @Test
    public void legacyEdit_invalidStudentValue_reportsErrorWithoutMutation() {
        Person original = student(details(Set.of(new Subject("Maths")), "0"));
        AddressBook book = new AddressBook();
        book.addPerson(original);
        Model model = new ModelManager(book, new UserPrefs());
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPhone(new Phone("123"));
        EditCommand command = new EditCommand(Index.fromOneBased(1), descriptor);
        assertThrows(CommandException.class, Phone.STUDENT_MESSAGE_CONSTRAINTS, () -> command.execute(model));
        assertEquals(List.of(original), model.getAddressBook().getPersonList());
    }

    private StudentDetails details(Set<Subject> subjects, String amount) {
        return new StudentDetails(subjects, new StartDate("10-Aug"), new OutstandingAmount(amount),
                new Age(""), new Education(""), new GuardianContact(""), new Note(""));
    }

    private StudentDetails changedOptional(String age, String education, String guardian, String note) {
        return new StudentDetails(Set.of(new Subject("Maths")), new StartDate("10-Aug"), OutstandingAmount.ZERO,
                new Age(age), new Education(education), new GuardianContact(guardian), new Note(note));
    }

    private Person student(StudentDetails details) {
        return new Person(new Name(" Peter Parker "), new Phone("91234567"), new Address(" Clementi "), details);
    }
}
