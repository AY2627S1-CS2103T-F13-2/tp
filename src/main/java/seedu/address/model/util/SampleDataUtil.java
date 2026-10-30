package seedu.address.model.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"),
                new Address("Sample Block 1, #01-01"), new StartDate("01-Jan"), new OutstandingAmount("120"),
                Set.of(new Tag("Secondary")), new Notes("Practise fractions before the next lesson."),
                Set.of(new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(15, 30))))),
            new Person(new Name("Bernice Yu"), new Phone("99272758"),
                new Address("Sample Block 2, #02-02"), new StartDate("02-Feb"), OutstandingAmount.ZERO,
                Set.of(new Tag("Secondary")), new Notes("Bring the essay draft."),
                Set.of(new Subject("English", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(16, 0))))),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"),
                new Address("Sample Block 3, #03-03"), new StartDate("03-Mar"), new OutstandingAmount("80"),
                Set.of(new Tag("Primary")), Notes.EMPTY,
                Set.of(new Subject("Maths", new LessonSlot(DayOfWeek.WEDNESDAY, LocalTime.of(15, 0))))),
            new Person(new Name("David Li"), new Phone("91031282"),
                new Address("Sample Block 4, #04-04"), new StartDate("04-Apr"), OutstandingAmount.ZERO,
                Set.of(new Tag("Secondary")), Notes.EMPTY,
                Set.of(new Subject("Science", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(17, 0))))),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"),
                new Address("Sample Block 5, #05-05"), new StartDate("05-May"), new OutstandingAmount("160"),
                Set.of(new Tag("Primary")), new Notes("Review vocabulary each week."),
                Set.of(new Subject("English", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(16, 30))))),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"),
                new Address("Sample Block 6, #06-06"), new StartDate("06-Jun"), OutstandingAmount.ZERO,
                Set.of(new Tag("Secondary")), Notes.EMPTY,
                Set.of(new Subject("Science", new LessonSlot(DayOfWeek.SATURDAY, LocalTime.of(10, 0)))))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }
}
