package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.logic.Messages;
import seedu.address.model.person.Person;

public class PersonListPanelTest extends FxTestSupport {
    @Test
    public void constructor_observableList_retainsLiveItems() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(ALICE);
            PersonListPanel panel = new PersonListPanel(persons);
            ListView<?> view = (ListView<?>) panel.getRoot().lookup("#personListView");
            assertSame(persons, view.getItems());
            persons.add(BENSON);
            assertEquals(2, view.getItems().size());
            persons.remove(ALICE);
            assertEquals(BENSON, view.getItems().get(0));
        });
    }

    @Test
    public void updateItem_populatedCell_bindsWidthAndExposesDetails() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel.PersonListViewCell cell = newCell();
            cell.updateIndex(0);
            cell.resize(320, 43);
            cell.updateItem(ALICE, false);
            Region card = (Region) cell.getGraphic();
            assertEquals("001", ((Label) card.lookup("#id")).getText());
            assertEquals("1. " + Messages.format(ALICE) + "; Tags: Not specified; Notes: ", cell.getAccessibleText());
            assertEquals(card.getAccessibleText(), cell.getAccessibleText());
            assertTrue(card.prefWidthProperty().isBound());
            assertEquals(310, card.getPrefWidth());
            cell.resize(500, 43);
            assertEquals(490, card.getPrefWidth());
        });
    }

    @Test
    public void updateItem_emptyOrNullCell_clearsGraphicAndAccessibleText() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel.PersonListViewCell cell = newCell();
            cell.updateItem(ALICE, false);
            cell.updateItem(null, true);
            assertCleared(cell);
            cell.updateItem(ALICE, false);
            cell.updateItem(null, false);
            assertCleared(cell);
            cell.updateItem(ALICE, false);
            cell.updateItem(ALICE, true);
            assertCleared(cell);
        });
    }

    @SuppressWarnings("unchecked")
    private PersonListPanel.PersonListViewCell newCell() {
        PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE));
        ListView<Person> view = (ListView<Person>) panel.getRoot().lookup("#personListView");
        return (PersonListPanel.PersonListViewCell) view.getCellFactory().call(view);
    }

    private void assertCleared(PersonListPanel.PersonListViewCell cell) {
        assertNull(cell.getGraphic());
        assertNull(cell.getText());
        assertNull(cell.getAccessibleText());
    }
}
