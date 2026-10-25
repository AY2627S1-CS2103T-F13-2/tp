package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest extends FxTestSupport {
    private MainWindow window;
    private InMemoryLogic logic;
    private Parent root;
    private TextField input;

    @BeforeEach
    void setUp() throws Exception {
        runOnFxThread(() -> {
            logic = new InMemoryLogic();
            window = new MainWindow(new Stage(), logic, Path.of("test-addressbook.json"));
            window.show();
            window.fillInnerParts();
            root = window.getPrimaryStage().getScene().getRoot();
            input = (TextField) root.lookup("#commandTextField");
        });
    }

    @Test
    public void startup_mixedBalances_showsTotalAndPaidCounts() throws Exception {
        runOnFxThread(() -> {
            assertSummary("2", "1");
            assertNotNull(window.getPersonListPanel());
        });
    }

    @Test
    public void startup_buttonIcons_loadsMatchingImageResources() throws Exception {
        runOnFxThread(() -> {
            root.applyCss();
            root.layout();
            assertButtonIcon("findButton", "/images/Search.png");
            assertButtonIcon("exitButton", "/images/close.png");
            Button exitButton = (Button) root.lookup("#exitButton");
            ImageView icon = (ImageView) exitButton.getGraphic();
            assertEquals(39.32, icon.getFitWidth());
            assertEquals(39.32, icon.getFitHeight());
            assertFalse(icon.isSmooth());
            assertEquals(Color.TRANSPARENT, exitButton.getBackground().getFills().get(0).getFill());
        });
    }

    @Test
    public void startup_commandPrompt_showsHintAtHalfOpacity() throws Exception {
        runOnFxThread(() -> {
            root.applyCss();
            root.layout();
            assertEquals("", input.getText(), "The prompt must not become command input");
            assertEquals("Enter command here...", input.getPromptText());
            Text prompt = input.lookupAll(".text").stream()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .filter(text -> input.getPromptText().equals(text.getText()))
                    .findFirst().orElseThrow(() -> new AssertionError("Rendered command prompt is missing"));
            assertTrue(prompt.isVisible());
            assertEquals(Color.color(1, 1, 1, 0.5), prompt.getFill());
        });
    }

    @Test
    public void findButton_blankInput_focusesAndPreparesCommand() throws Exception {
        runOnFxThread(() -> {
            Button findButton = (Button) root.lookup("#findButton");
            findButton.fire();
            assertEquals("find ", input.getText());
            assertEquals(5, input.getCaretPosition());
            assertSame(input, window.getPrimaryStage().getScene().getFocusOwner());
            assertEquals(2, logic.getFilteredPersonList().size());
        });
    }

    @Test
    public void list_afterFind_restoresSummaryForAllStudents() throws Exception {
        runOnFxThread(() -> {
            enterCommand("find Alice");
            assertEquals(1, logic.getFilteredPersonList().size());
            assertTrue(feedback().isVisible());
            enterCommand("  list  ");
            assertSummary("2", "1");
            assertEquals("", input.getText());
            enterCommand("list -summary");
            assertSummary("2", "1");
        });
    }

    @Test
    public void list_subjects_showsGroupedEmptyStateForSubjectlessStudents() throws Exception {
        runOnFxThread(() -> {
            enterCommand("list -subjects");
            root.applyCss();
            root.layout();
            assertNotNull(root.lookup(".subject-group-empty"));
            assertEquals("No subjects found for the active contacts.", ((Label) root.lookup(
                    ".subject-group-empty")).getText());
        });
    }

    @Test
    public void groupedList_plainListFindAndFilters_restoreContactPresentation() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            enterCommand("find Nobody");
            enterCommand("list -subjects");
            assertNotNull(root.lookup(".subject-group-panel"));
            assertSummary("2", "1");
            enterCommand("list");
            assertTrue(root.lookup(".subject-group-panel") == null);
            assertSummary("2", "1");

            enterCommand("list -subjects");
            enterCommand("find Alice");
            assertTrue(root.lookup(".subject-group-panel") == null);
            assertEquals(1, logic.getFilteredPersonList().size());
            enterCommand("list -subjects");
            enterCommand("list s/Maths");
            assertTrue(root.lookup(".subject-group-panel") == null);
            assertSummary("1", "1");
            enterCommand("list -subjects");
            enterCommand("list t/Needs revision");
            assertTrue(root.lookup(".subject-group-panel") == null);
            assertSummary("1", "0");
        });
    }

    @Test
    public void groupedList_readOnlyAndFailedCommands_preservePanelAndScroll() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            for (int index = 0; index < 20; index++) {
                logic.model.addPerson(new PersonBuilder(ALICE).withName("Extra Student " + (char) ('A' + index))
                        .withSubjects("Maths").build());
            }
            enterCommand("list -subjects");
            ScrollPane groups = (ScrollPane) root.lookup(".subject-group-panel");
            groups.setVvalue(0.5);
            enterCommand("view 2");
            assertSame(groups, root.lookup(".subject-group-panel"));
            assertEquals(0.5, groups.getVvalue());
            assertTrue(feedbackText().startsWith("Name: Benson Meier"));
            enterCommand("help");
            assertSame(groups, root.lookup(".subject-group-panel"));
            for (String command : new String[] {"unknown", "delete 99"}) {
                enterCommand(command);
                assertSame(groups, root.lookup(".subject-group-panel"));
                assertEquals(0.5, groups.getVvalue());
                assertEquals(command, input.getText());
                assertEquals(22, logic.getFilteredPersonList().size());
            }
        });
    }

    @Test
    public void contactList_viewAndFailedCommands_preserveSelection() throws Exception {
        runOnFxThread(() -> {
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            list.getSelectionModel().select(1);
            enterCommand("view 2");
            assertSame(list, root.lookup("#personListView"));
            assertEquals(1, list.getSelectionModel().getSelectedIndex());
            enterCommand("delete 99");
            assertSame(list, root.lookup("#personListView"));
            assertEquals(1, list.getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    public void groupedList_emptyAndAllArchived_showsEmptyPanelAndZeroCounts() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            for (Person person : List.copyOf(logic.getFilteredPersonList())) {
                logic.model.setPerson(person, person.withArchived(true));
            }
            enterCommand("list -subjects");
            assertNotNull(root.lookup(".subject-group-empty"));
            assertTrue(root.lookup(".subject-group-heading") == null);
            assertSummary("0", "0");
            enterCommand("list -archived");
            assertSummary("2", "1");
            for (Person person : List.copyOf(logic.getFilteredPersonList())) {
                logic.model.deletePerson(person);
            }
            enterCommand("list -subjects");
            assertNotNull(root.lookup(".subject-group-empty"));
            assertSummary("0", "0");
        });
    }

    @Test
    public void groupedList_mixedSubjectlessAndArchivedContacts_countsActiveContactsOnly() throws Exception {
        runOnFxThread(() -> {
            Person benson = logic.getFilteredPersonList().get(1);
            logic.model.setPerson(benson, new PersonBuilder(benson).withSubjects("Physics").build());
            logic.model.addPerson(new PersonBuilder(ALICE).withName("Archived Student")
                    .withSubjects("English").build().withArchived(true));
            enterCommand("list -subjects");
            assertSummary("2", "1");
            assertEquals(1, root.lookupAll(".subject-group-heading").size());
            assertEquals("Physics", ((Label) root.lookup(".subject-group-heading")).getText());
            assertEquals("002", ((Label) root.lookup(".subject-group-panel").lookup("#id")).getText());
        });
    }

    private void addScheduledSubjects() {
        Person alice = logic.getFilteredPersonList().get(0);
        Person benson = logic.getFilteredPersonList().get(1);
        logic.model.setPerson(alice, new PersonBuilder(alice).withSubjects("Maths").build());
        logic.model.setPerson(benson, new PersonBuilder(benson).withSubjects("Physics")
                .withTags("Needs revision").build());
    }

    @Test
    public void list_emptyAddressBook_showsZeroCounts() throws Exception {
        runOnFxThread(() -> {
            logic.model.deletePerson(logic.getFilteredPersonList().get(0));
            logic.model.deletePerson(logic.getFilteredPersonList().get(0));
            enterCommand("list");
            assertSummary("0", "0");
        });
    }

    @Test
    public void view_existingStudent_showsScrollableFeedback() throws Exception {
        runOnFxThread(() -> {
            enterCommand("view 1");
            assertTrue(feedback().isVisible());
            assertFalse(root.lookup("#summary").isVisible());
            assertTrue(feedbackText().startsWith("Name: Alice Pauline | Phone:"));
            assertTrue(feedback().getTooltip().getText().contains("\nPhone:"));
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, feedback().getVbarPolicy());
            assertEquals("", input.getText());
        });
    }

    @Test
    public void view_tagsAndNotes_preservesUpstreamProfileDetails() throws Exception {
        runOnFxThread(() -> {
            Person original = logic.getFilteredPersonList().get(0);
            Person edited = new PersonBuilder(original).withTags("Needs revision", "A-Level")
                    .withNotes("Revise fractions & 中文!").build();
            logic.model.setPerson(original, edited);
            enterCommand("view 1");
            assertTrue(feedbackText().contains("Tags: a-level, needs revision | Notes: Revise fractions & 中文!"));
            assertTrue(feedback().getTooltip().getText().contains("\nTags: a-level, needs revision\nNotes:"));
        });
    }

    @Test
    public void failedCommands_showErrorsAndPreserveInput() throws Exception {
        runOnFxThread(() -> {
            enterCommand("unknown");
            assertTrue(feedback().isVisible());
            assertEquals(Messages.MESSAGE_UNKNOWN_COMMAND, feedbackText());
            assertEquals("unknown", input.getText());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            enterCommand("delete 99");
            assertEquals(DeleteCommand.MESSAGE_CONTACT_NOT_FOUND, feedbackText());
            assertEquals("delete 99", input.getText());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            enterCommand("list");
            assertSummary("2", "1");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void exitButton_closesWindowAndStoresGuiSettings() throws Exception {
        runOnFxThread(() -> {
            Stage stage = window.getPrimaryStage();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Button exitButton = (Button) root.lookup("#exitButton");
            exitButton.fire();
            assertFalse(stage.isShowing());
            assertEquals(width, logic.getGuiSettings().getWindowWidth());
            assertEquals(height, logic.getGuiSettings().getWindowHeight());
            assertNotNull(logic.getGuiSettings().getWindowCoordinates());
        });
    }

    private void enterCommand(String text) {
        input.setText(text);
        input.fireEvent(new ActionEvent());
        root.applyCss();
        root.layout();
    }

    private void assertButtonIcon(String buttonId, String resourcePath) {
        Button button = (Button) root.lookup("#" + buttonId);
        assertNotNull(button);
        ImageView icon = assertInstanceOf(ImageView.class, button.getGraphic());
        Image image = icon.getImage();
        assertNotNull(image);
        assertFalse(image.isError(), "Button icon must decode successfully");
        assertEquals(getClass().getResource(resourcePath).toExternalForm(), image.getUrl());
        assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
        assertTrue(icon.isPreserveRatio());
        assertTrue(icon.getBoundsInLocal().getWidth() > 0 && icon.getBoundsInLocal().getHeight() > 0);
    }

    private ScrollPane feedback() {
        return (ScrollPane) root.lookup("#resultDisplay");
    }

    private String feedbackText() {
        return ((Label) feedback().getContent()).getText();
    }

    private void assertSummary(String total, String paid) {
        assertTrue(root.lookup("#summary").isVisible());
        assertFalse(feedback().isVisible());
        assertEquals(total, ((Label) root.lookup("#totalCount")).getText());
        assertEquals(paid, ((Label) root.lookup("#paidCount")).getText());
    }

    /** Executes real parsers and commands against an isolated model, with no storage writes. */
    private static class InMemoryLogic implements Logic {
        private final ModelManager model = new ModelManager();
        private final AddressBookParser parser = new AddressBookParser();

        InMemoryLogic() {
            model.addPerson(new PersonBuilder(ALICE).withOutstandingAmount("0").build());
            model.addPerson(new PersonBuilder(BENSON).withOutstandingAmount("12.50").build());
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            return parser.parseCommand(commandText).execute(model);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return model.getFilteredPersonList();
        }

        @Override
        public GuiSettings getGuiSettings() {
            return model.getGuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            model.setGuiSettings(guiSettings);
        }
    }
}
