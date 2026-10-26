package seedu.address.ui;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;

/** Displays active students grouped under the subjects they take. */
public class SubjectGroupPanel {
    private final ScrollPane root;

    /** Creates a grouped subject view from the currently displayed students. */
    public SubjectGroupPanel(ObservableList<Person> persons) {
        this(SubjectGrouping.group(persons));
    }

    /** Creates subject groups using the model's sorted lessons and shared student indexes. */
    public SubjectGroupPanel(List<Person> persons, List<LessonRow> orderedLessons) {
        this(SubjectGrouping.group(persons, orderedLessons));
    }

    private SubjectGroupPanel(List<SubjectGrouping.Group> groupedSubjects) {
        VBox groups = new VBox(12);
        groups.setFillWidth(true);
        for (SubjectGrouping.Group group : groupedSubjects) {
            VBox entries = new VBox(4);
            for (SubjectGrouping.Entry entry : group.entries()) {
                PersonCard card = new PersonCard(entry.person(), entry.displayedIndex(), entry.subject());
                Label lessonSlot = new Label(entry.subject().getLessonSlot().toString());
                lessonSlot.getStyleClass().add("subject-lesson-slot");
                HBox row = new HBox(8, card.getRoot(), lessonSlot);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("subject-student-row");
                entries.getChildren().add(row);
            }
            Label heading = new Label(group.displayName());
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

}
