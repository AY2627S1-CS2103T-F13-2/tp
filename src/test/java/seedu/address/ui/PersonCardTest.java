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
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @Test
    public void constructor_displaysSubjectsAndLegacyFallback() throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
            try {
                assertSubjectsText(ALICE, "Subjects: Not specified");
                assertSubjectsText(new PersonBuilder(ALICE).withSubjects("Maths", "H2 Chemistry").build(),
                        "Subjects: Maths, H2 Chemistry");
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
        Label subjects = (Label) card.getRoot().lookup("#subjects");
        assertNotNull(subjects);
        assertEquals(expectedText, subjects.getText());
    }
}
