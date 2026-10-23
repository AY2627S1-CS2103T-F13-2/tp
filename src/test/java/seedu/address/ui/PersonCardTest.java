package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest extends FxTestSupport {

    @Test
    public void constructor_displaysSubjectsAndLegacyFallback() throws Exception {
        runOnFxThread(() -> {
            assertSubjectsText(ALICE, "Not specified");
            assertSubjectsText(new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build(),
                    "Maths (Mon 09..");
            assertEquals("1234567890123456789012345678901",
                    PersonCard.abbreviate("1234567890123456789012345678901", 31));
            assertEquals("123456789012345678901234567890..",
                    PersonCard.abbreviate("12345678901234567890123456789012", 31));
            assertSummaryAndFeedback();
        });
    }

    private void assertSubjectsText(Person person, String expectedText) {
        PersonCard card = new PersonCard(person, 1);
        Label subjects = (Label) card.getRoot().lookup("#subjects");
        assertNotNull(subjects);
        assertEquals(expectedText, subjects.getText());
    }

    @Test
    public void constructor_tagsAndLongNotes_preservesDetailsInWrappingTooltip() throws Exception {
        runOnFxThread(() -> {
            PersonCard emptyCard = new PersonCard(ALICE, 1);
            assertProfileDetails(emptyCard, "Not specified", "");

            String longNote = "Revise fractions & 中文! " + "Practise algebra. ".repeat(25);
            Person student = new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry")
                    .withTags("Needs revision", "A-Level").withNotes(longNote).build();
            PersonCard card = new PersonCard(student, 1);
            assertProfileDetails(card, "a-level, needs revision", longNote.trim());
            Label subjects = (Label) card.getRoot().lookup("#subjects");
            assertEquals("Maths (Mon 09..", subjects.getText());
            assertEquals("Subjects: Maths (Mon 09:00), H2 Chemistry (Mon 09:00)",
                    subjects.getTooltip().getText());
            assertEquals(4, card.getRoot().getChildrenUnmodifiable().size(), "Keep the compact row layout");

            Person edited = new PersonBuilder(student).withTags("Updated").withNotes("Updated note").build();
            assertProfileDetails(new PersonCard(edited, 1), "updated", "Updated note");
        });
    }

    private void assertProfileDetails(PersonCard card, String expectedTags, String expectedNotes) {
        Label name = (Label) card.getRoot().lookup("#name");
        Tooltip tooltip = name.getTooltip();
        assertNotNull(tooltip);
        assertTrue(tooltip.getText().contains("Tags: " + expectedTags + "; Notes: " + expectedNotes));
        assertTrue(tooltip.isWrapText());
        assertEquals(420, tooltip.getMaxWidth());
        assertEquals("1. " + tooltip.getText(), card.getRoot().getAccessibleText());
    }

    private void assertSummaryAndFeedback() {
        ResultDisplay display = new ResultDisplay();
        display.showSummary(7, 6);
        assertEquals("7", ((Label) display.getRoot().lookup("#totalCount")).getText());
        assertEquals("6", ((Label) display.getRoot().lookup("#paidCount")).getText());
        ScrollPane feedback = (ScrollPane) display.getRoot().lookup("#resultDisplay");
        assertFalse(feedback.isVisible());
        display.setFeedbackToUser("Name: Alice\nPhone: 94351253");
        assertTrue(feedback.isVisible());
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, feedback.getVbarPolicy());
        Label feedbackLabel = (Label) feedback.getContent();
        assertEquals("Name: Alice | Phone: 94351253", feedbackLabel.getText());
        display.showSummary(0, 0);
        assertFalse(feedback.isVisible());
        assertEquals("0", ((Label) display.getRoot().lookup("#totalCount")).getText());
    }
}
