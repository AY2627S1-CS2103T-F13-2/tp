package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.Messages;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label subjects;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, null);
    }

    /** Creates a card focused on one lesson, or the whole profile when subject is null. */
    public PersonCard(Person person, int displayedIndex, Subject subject) {
        super(FXML);
        this.person = person;
        id.setText(String.format(java.util.Locale.ROOT, "%03d", displayedIndex));
        name.setText(abbreviate(person.getName().fullName, 31));
        subjects.setText(abbreviate(subject == null ? Messages.formatSubjects(person) : subject.value, 14));
        String profileDetails = Messages.format(person) + "; Tags: " + Messages.formatTags(person)
                + "; Notes: " + person.getNotes();
        Tooltip profileTooltip = new Tooltip(profileDetails);
        profileTooltip.setWrapText(true);
        profileTooltip.setMaxWidth(420);
        name.setTooltip(profileTooltip);
        subjects.setTooltip(new Tooltip("Subjects: " + (subject == null
                ? Messages.formatSubjects(person) : subject.toString())));
        Tooltip.install(cardPane, profileTooltip);
        cardPane.setAccessibleText(displayedIndex + ". " + profileDetails);
    }

    /** Replaces the final character slot with two dots when the design's limit is exceeded. */
    static String abbreviate(String text, int limit) {
        int length = text.codePointCount(0, text.length());
        return length <= limit ? text : text.substring(0, text.offsetByCodePoints(0, limit - 1)) + "..";
    }
}
