package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest extends FxTestSupport {

    @Test
    public void constructor_displaysSubjectsAndLegacyFallback() throws Exception {
        runOnFxThread(() -> {
            assertSubjectsText(ALICE, "Not specified");
            assertSubjectsText(new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build(),
                    "Maths, H2 Che..");
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
