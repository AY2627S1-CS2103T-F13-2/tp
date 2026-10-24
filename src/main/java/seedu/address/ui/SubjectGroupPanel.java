package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/** Displays active students grouped under the subjects they take. */
public class SubjectGroupPanel {
    private final ScrollPane root;

    /** Creates a grouped subject view from the currently displayed students. */
    public SubjectGroupPanel(ObservableList<Person> persons) {
        VBox groups = new VBox(12);
        groups.setFillWidth(true);
        Map<String, SubjectGroup> groupedSubjects = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Person person : persons) {
            for (Subject subject : person.getSubjects()) {
                String key = subject.value.toLowerCase(java.util.Locale.ROOT);
                groupedSubjects.computeIfAbsent(key, unused -> new SubjectGroup(subject.value))
                        .entries.add(new SubjectEntry(person, subject));
            }
        }

        for (SubjectGroup group : groupedSubjects.values()) {
            group.entries.sort(Comparator.comparing(entry -> entry.person.getName().fullName,
                    String.CASE_INSENSITIVE_ORDER));
            VBox entries = new VBox(4);
            for (SubjectEntry entry : group.entries) {
                int displayedIndex = persons.indexOf(entry.person) + 1;
                PersonCard card = new PersonCard(entry.person, displayedIndex);
                Label lessonSlot = new Label(entry.subject.getLessonSlot().toString());
                lessonSlot.getStyleClass().add("subject-lesson-slot");
                HBox row = new HBox(8, card.getRoot(), lessonSlot);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("subject-student-row");
                entries.getChildren().add(row);
            }
            Label heading = new Label(group.displayName);
            heading.getStyleClass().add("subject-group-heading");
            VBox section = new VBox(4, heading, entries);
            section.getStyleClass().add("subject-group");
            groups.getChildren().add(section);
        }

        if (groupedSubjects.isEmpty()) {
            Label emptyMessage = new Label("No subjects found for the active contacts.");
            emptyMessage.getStyleClass().add("subject-group-empty");
            groups.getChildren().add(emptyMessage);
        }

        root = new ScrollPane(groups);
        root.setFitToWidth(true);
        root.setFitToHeight(true);
        root.getStyleClass().add("subject-group-panel");
    }

    public Node getRoot() {
        return root;
    }

    private static class SubjectGroup {
        private final String displayName;
        private final List<SubjectEntry> entries = new ArrayList<>();

        SubjectGroup(String displayName) {
            this.displayName = displayName;
        }
    }

    private static class SubjectEntry {
        private final Person person;
        private final Subject subject;

        SubjectEntry(Person person, Subject subject) {
            this.person = person;
            this.subject = subject;
        }
    }
}
