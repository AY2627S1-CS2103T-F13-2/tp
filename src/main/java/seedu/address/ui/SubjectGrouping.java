package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.sort.LessonRow;

/** Builds ordered subject groups independently of JavaFX rendering. */
final class SubjectGrouping {
    private SubjectGrouping() {}

    /** Groups the supplied contacts by subject, preserving the first subject spelling. */
    static List<Group> group(List<Person> persons) {
        return group(persons, Comparator.comparing(entry -> entry.person().getName().fullName,
                String.CASE_INSENSITIVE_ORDER));
    }

    /** Preserves the model's lesson order within each subject without changing shared student indexes. */
    static List<Group> group(List<Person> persons, List<LessonRow> orderedLessons) {
        Map<LessonRow, Integer> positions = new HashMap<>();
        for (int index = 0; index < orderedLessons.size(); index++) {
            positions.put(orderedLessons.get(index), index);
        }
        return group(persons, Comparator.comparingInt(entry -> {
            Integer position = positions.get(new LessonRow(entry.person(), entry.subject()));
            assert position != null : "Grouped entries must belong to the ordered lesson snapshot.";
            return position;
        }));
    }

    private static List<Group> group(List<Person> persons, Comparator<Entry> order) {
        Map<String, List<Entry>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int displayedIndex = 0;
        for (Person person : persons) {
            displayedIndex++;
            for (Subject subject : person.getSubjects()) {
                groups.computeIfAbsent(subject.value, unused -> new ArrayList<>())
                        .add(new Entry(person, subject, displayedIndex));
            }
        }
        List<Group> result = new ArrayList<>();
        groups.forEach((name, entries) -> {
            entries.sort(order);
            result.add(new Group(name, entries));
        });
        return List.copyOf(result);
    }

    /** A subject heading and its immutable, ordered entries. */
    record Group(String displayName, List<Entry> entries) {
        Group {
            entries = List.copyOf(entries);
        }
    }

    /** One student's own lesson within a subject group. */
    record Entry(Person person, Subject subject, int displayedIndex) {}
}
