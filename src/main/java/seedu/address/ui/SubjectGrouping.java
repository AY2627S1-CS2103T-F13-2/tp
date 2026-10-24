package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/** Builds ordered subject groups independently of JavaFX rendering. */
final class SubjectGrouping {
    private SubjectGrouping() {}

    /** Groups the supplied contacts by subject, preserving the first subject spelling. */
    static List<Group> group(List<Person> persons) {
        Map<String, List<Entry>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Person person : persons) {
            for (Subject subject : person.getSubjects()) {
                groups.computeIfAbsent(subject.value, unused -> new ArrayList<>())
                        .add(new Entry(person, subject));
            }
        }
        List<Group> result = new ArrayList<>();
        groups.forEach((name, entries) -> {
            entries.sort(Comparator.comparing(entry -> entry.person().getName().fullName,
                    String.CASE_INSENSITIVE_ORDER));
            result.add(new Group(name, entries));
        });
        return List.copyOf(result);
    }

    /** A subject heading and its immutable, alphabetically ordered entries. */
    record Group(String displayName, List<Entry> entries) {
        Group {
            entries = List.copyOf(entries);
        }
    }

    /** One student's own lesson within a subject group. */
    record Entry(Person person, Subject subject) {}
}
