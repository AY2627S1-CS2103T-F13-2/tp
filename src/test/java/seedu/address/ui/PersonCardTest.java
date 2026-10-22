package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @Test
    public void constructor_displaysProfileFieldsAndWrapsLongNotes() throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
            try {
                assertSubjectsText(ALICE, "Subjects: Not specified");
                assertSubjectsText(new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build(),
                        "Subjects: Maths, H2 Chemistry");
                PersonCard emptyCard = new PersonCard(ALICE, 1);
                assertLabel(emptyCard, "tags", "Tags: Not specified");
                assertLabel(emptyCard, "notes", "Notes: ");

                String longNote = "Revise  fractions & 中文! " + "Practise algebra. ".repeat(25);
                Person student = new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry")
                        .withTags("Needs revision", "A-Level").withNotes(longNote).build();
                PersonCard card = new PersonCard(student, 1);
                assertLabel(card, "tags", "Tags: a-level, needs revision");
                assertLabel(card, "notes", "Notes: " + longNote.trim());
                StackPane container = new StackPane(card.getRoot());
                new Scene(container, 320, 800);
                container.resize(320, 800);
                container.applyCss();
                container.layout();
                Label notes = (Label) card.getRoot().lookup("#notes");
                assertTrue(notes.isWrapText());
                assertTrue(notes.getHeight() > 30, "Long notes should wrap onto multiple lines");
                assertTrue(notes.getWidth() <= 320, "Notes should fit the contact card width");

                Person edited = new PersonBuilder(student).withTags("Updated").withNotes("Updated note").build();
                PersonCard refreshed = new PersonCard(edited, 1);
                assertLabel(refreshed, "tags", "Tags: updated");
                assertLabel(refreshed, "notes", "Notes: Updated note");
            } catch (Throwable exception) {
                failure.set(exception);
            } finally {
                completed.countDown();
            }
        });
        try {
            assertTrue(completed.await(10, TimeUnit.SECONDS));
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        } finally {
            Platform.exit();
        }
    }

    private void assertSubjectsText(Person person, String expectedText) {
        PersonCard card = new PersonCard(person, 1);
        assertLabel(card, "subjects", expectedText);
    }

    private void assertLabel(PersonCard card, String id, String expectedText) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label);
        assertEquals(expectedText, label.getText());
    }
}
