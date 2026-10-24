package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

/** Tests the grouped subject presentation and its empty state. */
public class SubjectGroupPanelTest extends FxTestSupport {
    @Test
    public void constructor_groupsSubjectsAndShowsLessonSlots() throws Exception {
        Person first = new PersonBuilder(ALICE).withName("Alice Student")
                .withScheduledSubjects(new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY,
                        LocalTime.of(9, 0))))
                .build();
        Person second = new PersonBuilder(ALICE).withName("Bob Student")
                .withScheduledSubjects(new Subject("maths", new LessonSlot(DayOfWeek.TUESDAY,
                        LocalTime.of(15, 30))))
                .build();

        runOnFxThread(() -> {
            SubjectGroupPanel panel = new SubjectGroupPanel(FXCollections.observableArrayList(first, second));
            ScrollPane root = (ScrollPane) panel.getRoot();
            VBox groups = (VBox) root.getContent();
            VBox section = (VBox) groups.getChildren().get(0);
            Label heading = (Label) section.getChildren().get(0);
            VBox entries = (VBox) section.getChildren().get(1);

            assertEquals("Maths", heading.getText());
            assertEquals(2, entries.getChildren().size());
            assertTrue(entries.getChildren().stream().allMatch(node -> node.getStyleClass()
                    .contains("subject-student-row")));
        });
    }

    @Test
    public void constructor_withoutSubjects_showsEmptyMessage() throws Exception {
        runOnFxThread(() -> {
            SubjectGroupPanel panel = new SubjectGroupPanel(
                    FXCollections.observableArrayList(new PersonBuilder(ALICE).build()));
            ScrollPane root = (ScrollPane) panel.getRoot();

            VBox groups = (VBox) root.getContent();
            Label message = (Label) groups.getChildren().get(0);
            assertEquals("No subjects found for the active contacts.", message.getText());
        });
    }
}
