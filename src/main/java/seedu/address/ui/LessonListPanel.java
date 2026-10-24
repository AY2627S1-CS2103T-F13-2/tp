package seedu.address.ui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.sort.LessonRow;

/** Displays sorted lessons while preserving the printed index of each shared student profile. */
public class LessonListPanel {
    private final ListView<LessonRow> root;
    private final List<Person> students;

    /** Creates a lesson snapshot using indexes from the current unique-student list. */
    public LessonListPanel(List<Person> students, List<LessonRow> lessons) {
        this.students = List.copyOf(students);
        root = new ListView<>(FXCollections.observableArrayList(lessons));
        root.getStyleClass().add("lesson-list-panel");
        root.setPlaceholder(new Label("No students to display."));
        root.setCellFactory(list -> new LessonCell());
    }

    public Region getRoot() {
        return root;
    }

    private class LessonCell extends ListCell<LessonRow> {
        @Override
        protected void updateItem(LessonRow lesson, boolean empty) {
            super.updateItem(lesson, empty);
            setText(null);
            setGraphic(null);
            setAccessibleText(null);
            if (!empty && lesson != null) {
                int studentIndex = students.indexOf(lesson.person()) + 1;
                PersonCard card = new PersonCard(lesson.person(), studentIndex, lesson.subject());
                Label slot = new Label(lesson.subject() == null ? "No lesson slot"
                        : lesson.subject().getLessonSlot().toString());
                slot.getStyleClass().add("lesson-slot");
                slot.setMinWidth(Region.USE_PREF_SIZE);
                card.getRoot().setMinWidth(0);
                HBox.setHgrow(card.getRoot(), Priority.ALWAYS);
                HBox row = new HBox(8, card.getRoot(), slot);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("lesson-row");
                row.prefWidthProperty().bind(widthProperty().subtract(20));
                setGraphic(row);
                setAccessibleText(card.getRoot().getAccessibleText() + "; Lesson: " + slot.getText());
            }
        }
    }
}
