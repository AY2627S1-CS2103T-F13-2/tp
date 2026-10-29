package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.sort.LessonRow;
import seedu.address.model.sort.SortOrder;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest extends FxTestSupport {
    private MainWindow window;
    private InMemoryLogic logic;
    private Parent root;
    private TextField input;
    private AtomicReference<String> openedUrl;

    @BeforeEach
    void setUp() throws Exception {
        runOnFxThread(() -> {
            logic = new InMemoryLogic();
            openedUrl = new AtomicReference<>();
            window = new MainWindow(new Stage(), logic, Path.of("test-addressbook.json"), openedUrl::set);
            window.show();
            window.fillInnerParts();
            root = window.getPrimaryStage().getScene().getRoot();
            input = (TextField) root.lookup("#commandTextField");
        });
    }

    @Test
    public void helpGuideLink_usesSuppliedBrowserAction() throws Exception {
        runOnFxThread(() -> {
            window.handleHelp();
            Stage helpStage = Window.getWindows().stream()
                    .filter(Stage.class::isInstance).map(Stage.class::cast)
                    .filter(stage -> stage.getTitle().equals("Help")).findFirst().orElseThrow();
            Hyperlink guideLink = (Hyperlink) helpStage.getScene().lookup("#guideLink");
            guideLink.fire();
            assertEquals(HelpWindow.USERGUIDE_URL, openedUrl.get());
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
    public void groupedList_successfulEdit_reusesContactPanelWithUpdatedCards() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            Person alice = logic.getFilteredPersonList().get(0);
            Person benson = logic.getFilteredPersonList().get(1);
            Person updated = new PersonBuilder(alice).withName("Alice Updated").withPhone("87654321")
                    .withSubjects("Chemistry").withNotes("Updated note").build();
            PersonListPanel contactPanel = window.getPersonListPanel();
            enterCommand("list -subjects");
            assertNotNull(root.lookup(".subject-group-panel"));

            enterCommand("edit 1 n/Alice Updated p/87654321 s/Chemistry day/MON time/09:00 notes/Updated note");

            assertNull(root.lookup(".subject-group-panel"));
            assertSame(contactPanel, window.getPersonListPanel());
            assertContactCards(List.of(updated, benson));
            enterCommand("view 1");
            assertTrue(feedbackText().startsWith("Name: Alice Updated | Phone: 87654321"));
            assertTrue(feedbackText().contains("Subjects: Chemistry (Mon 09:00)"));
            assertTrue(feedbackText().endsWith("Notes: Updated note"));
        });
    }

    @Test
    public void groupedList_successfulDelete_reindexesRemainingCardsAndCommandTargets() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            Person alice = logic.getFilteredPersonList().get(0);
            Person cara = new PersonBuilder(ALICE).withName("Cara Student").withSubjects("English").build();
            logic.model.addPerson(cara);
            PersonListPanel contactPanel = window.getPersonListPanel();
            enterCommand("list -subjects");
            assertNotNull(root.lookup(".subject-group-panel"));

            enterCommand("delete 2");

            assertNull(root.lookup(".subject-group-panel"));
            assertSame(contactPanel, window.getPersonListPanel());
            assertContactCards(List.of(alice, cara));
            enterCommand("view 2");
            assertTrue(feedbackText().startsWith("Name: Cara Student | Phone:"));
            enterCommand("delete 2");
            assertContactCards(List.of(alice));
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
            enterCommand("sort");
            assertSame(groups, root.lookup(".subject-group-panel"));
            for (String command : new String[] {"unknown", "delete 99", "sort n/up"}) {
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

    @Test
    public void startup_savedGroupedView_showsSubjectGroups() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            reopenWithSavedView("", "list -subjects");
            assertNotNull(root.lookup(".subject-group-panel"));
            assertSummary("2", "1");
        });
    }

    @Test
    public void startup_savedSortAndView_showsViewInSavedOrder() throws Exception {
        runOnFxThread(() -> {
            addScheduledSubjects();
            reopenWithSavedView("sort o/desc", "list -subjects");
            // Benson owes more, so the saved descending order puts Benson before Alice.
            assertEquals(List.of(BENSON.getName(), ALICE.getName()),
                    logic.getFilteredPersonList().stream().map(Person::getName).toList());
            assertNotNull(root.lookup(".subject-group-panel"));
        });
    }

    @Test
    public void startup_savedFindView_showsMatchingStudents() throws Exception {
        runOnFxThread(() -> {
            reopenWithSavedView("", "find Alice");
            assertEquals(List.of(ALICE.getName()),
                    logic.getFilteredPersonList().stream().map(Person::getName).toList());
            assertTrue(root.lookup(".subject-group-panel") == null);
            assertEquals(MainWindow.MESSAGE_LAST_VIEW_RESTORED + " | " + FindCommand.MESSAGE_SINGULAR,
                    feedbackText());
        });
    }

    @Test
    public void startup_savedFindWithNoMatches_explainsEmptyList() throws Exception {
        runOnFxThread(() -> {
            reopenWithSavedView("", "find Zed");
            assertTrue(logic.getFilteredPersonList().isEmpty());
            assertEquals(MainWindow.MESSAGE_LAST_VIEW_RESTORED + " | " + FindCommand.MESSAGE_NO_MATCHES,
                    feedbackText());
        });
    }

    /** Opens a new window, as on the next launch, with the sort and view commands saved by the last session. */
    private void reopenWithSavedView(String lastSortCommand, String lastViewCommand) {
        window.getPrimaryStage().hide();
        logic.lastSortCommand = lastSortCommand;
        logic.lastViewCommand = lastViewCommand;
        window = new MainWindow(new Stage(), logic, Path.of("test-addressbook.json"), openedUrl::set);
        window.show();
        window.fillInnerParts();
        root = window.getPrimaryStage().getScene().getRoot();
        input = (TextField) root.lookup("#commandTextField");
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

    @Test
    public void startup_savedPositionOffScreen_ignoresSavedPosition() throws Exception {
        runOnFxThread(() -> {
            window.getPrimaryStage().hide();
            logic.setGuiSettings(new GuiSettings(740, 600, -100000, -100000));
            window = new MainWindow(new Stage(), logic, Path.of("test-addressbook.json"), openedUrl::set);
            // Unset (NaN) coordinates let JavaFX centre the window on the primary screen
            assertTrue(Double.isNaN(window.getPrimaryStage().getX()));
            assertTrue(Double.isNaN(window.getPrimaryStage().getY()));
        });
    }

    private void enterCommand(String text) {
        input.setText(text);
        input.fireEvent(new ActionEvent());
        root.applyCss();
        root.layout();
    }

    private void assertContactCards(List<Person> expected) {
        ListView<?> view = (ListView<?>) root.lookup("#personListView");
        assertEquals(expected, view.getItems());
        for (int index = 0; index < expected.size(); index++) {
            int cellIndex = index;
            Person person = expected.get(index);
            ListCell<?> cell = view.lookupAll(".list-cell").stream()
                    .filter(ListCell.class::isInstance).map(node -> (ListCell<?>) node)
                    .filter(candidate -> !candidate.isEmpty() && candidate.getIndex() == cellIndex)
                    .findFirst().orElseThrow();
            assertEquals(String.format("%03d", index + 1), ((Label) cell.getGraphic().lookup("#id")).getText());
            assertEquals(person.getName().fullName, ((Label) cell.getGraphic().lookup("#name")).getText());
            Label subjects = (Label) cell.getGraphic().lookup("#subjects");
            assertEquals("Subjects: " + Messages.formatSubjects(person), subjects.getTooltip().getText());
            assertEquals((index + 1) + ". " + Messages.format(person) + "; Tags: " + Messages.formatTags(person)
                    + "; Notes: " + person.getNotes(), cell.getAccessibleText());
        }
    }

    @Test
    public void sort_studentRows_haveNaturalIndexesAndKeepSummaryCountsUnique() throws Exception {
        runOnFxThread(() -> {
            Person original = logic.getFilteredPersonList().getFirst();
            Person updated = new PersonBuilder(original).withScheduledSubjects(
                    new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0))),
                    new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(15, 0)))).build();
            logic.model.setPerson(original, updated);
            enterCommand("sort slot/asc");
            root.applyCss();
            root.layout();
            assertNotNull(root.lookup(".lesson-list-panel"));
            assertEquals(2, root.lookupAll(".lesson-row").size());
            assertEquals(List.of("001", "002"), root.lookupAll("#id").stream().map(Label.class::cast)
                    .map(Label::getText).sorted().toList());
            enterCommand("list -summary");
            assertSummary("2", "1");
            assertNotNull(root.lookup(".lesson-list-panel"));
            enterCommand("sort reset");
            assertNotNull(window.getPersonListPanel());
            assertNull(root.lookup(".lesson-list-panel"));
        });
    }

    @Test
    public void sort_queryAndInvalidInput_preserveTheSelectedSort() throws Exception {
        runOnFxThread(() -> {
            enterCommand("sort n/desc");
            SortOrder order = logic.getSortOrder();
            enterCommand("sort");
            assertEquals(order, logic.getSortOrder());
            enterCommand("sort n/up");
            assertEquals(order, logic.getSortOrder());
            assertTrue(feedbackText().contains("Sorting direction must be asc or desc."));
        });
    }

    @Test
    public void groupedSort_amountAndReset_preserveGroupsAndUseDefaultOrderOnReset() throws Exception {
        runOnFxThread(() -> {
            addTwoLessonsPerStudent();
            enterCommand("list -subjects");
            enterCommand("sort o/desc n/asc");
            assertEquals(List.of("Benson Meier", "Alice Pauline"), groupedNames("Maths"));
            assertEquals(List.of("Benson Meier", "Alice Pauline"), groupedNames("Physics"));
            assertEquals(List.of("001", "002"), groupedRows("Maths").stream()
                    .map(row -> ((Label) row.lookup("#id")).getText()).toList());
            ScrollPane groups = (ScrollPane) root.lookup(".subject-group-panel");
            enterCommand("sort");
            assertSame(groups, root.lookup(".subject-group-panel"));
            enterCommand("sort reset");
            assertEquals(SortOrder.NONE, logic.getSortOrder());
            assertEquals(List.of("Alice Pauline", "Benson Meier"), groupedNames("Maths"));
            enterCommand("list -subjects");
            assertSummary("2", "1");
        });
    }

    @Test
    public void groupedSort_slotsAndListTransitions_keepTheChosenOrder() throws Exception {
        runOnFxThread(() -> {
            addTwoLessonsPerStudent();
            enterCommand("sort slot/asc");
            assertNotNull(root.lookup(".lesson-list-panel"));
            enterCommand("list -subjects");
            assertEquals(List.of("Benson Meier", "Alice Pauline"), groupedNames("Maths"));
            assertEquals(List.of("Alice Pauline", "Benson Meier"), groupedNames("Physics"));
            assertSummary("2", "1");
            enterCommand("sort slot/desc");
            assertEquals(List.of("Alice Pauline", "Benson Meier"), groupedNames("Maths"));
            assertEquals(List.of("Benson Meier", "Alice Pauline"), groupedNames("Physics"));
            enterCommand("list");
            assertNull(root.lookup(".subject-group-panel"));
            assertEquals(2, root.lookupAll(".lesson-row").size());
            assertSummary("2", "1");
            enterCommand("list -subjects");
            assertEquals(List.of("Benson Meier", "Alice Pauline"), groupedNames("Physics"));
            enterCommand("sort reset");
            assertNotNull(root.lookup(".subject-group-panel"));
            enterCommand("list");
            assertNotNull(window.getPersonListPanel());
            assertNull(root.lookup(".lesson-list-panel"));
        });
    }

    @Test
    public void groupedSort_viewEditAndDelete_usePrintedStudentIndexes() throws Exception {
        runOnFxThread(() -> {
            addTwoLessonsPerStudent();
            enterCommand("list -subjects");
            enterCommand("sort slot/asc");
            assertEquals("002", ((Label) groupedRows("Maths").getFirst().lookup("#id")).getText());
            ScrollPane groups = (ScrollPane) root.lookup(".subject-group-panel");
            enterCommand("view 2");
            assertTrue(feedbackText().startsWith("Name: Benson Meier"));
            assertSame(groups, root.lookup(".subject-group-panel"));
            enterCommand("edit 2 notes/Review algebra");
            assertNull(root.lookup(".subject-group-panel"));
            assertEquals("Review algebra", logic.getFilteredPersonList().get(1).getNotes().value);
            enterCommand("list -subjects");
            enterCommand("delete 2");
            assertEquals(1, root.lookupAll(".lesson-row").size());
            enterCommand("list -subjects");
            assertEquals(List.of("Alice Pauline"), groupedNames("Maths"));
            assertEquals(List.of("Alice Pauline"), groupedNames("Physics"));
            assertSummary("1", "1");
        });
    }

    @Test
    public void groupedSort_findAndArchivedFilters_switchPresentationWithoutLosingCriteria() throws Exception {
        runOnFxThread(() -> {
            addTwoLessonsPerStudent();
            enterCommand("archive 2");
            enterCommand("list -subjects");
            enterCommand("sort slot/asc n/desc");
            SortOrder order = logic.getSortOrder();
            assertEquals(List.of("Alice Pauline"), groupedNames("Maths"));
            enterCommand("find Alice");
            assertNotNull(root.lookup(".lesson-list-panel"));
            assertEquals(1, logic.getFilteredPersonList().size());
            assertEquals(order, logic.getSortOrder());
            enterCommand("list -archived");
            assertSummary("1", "0");
            assertTrue(logic.getFilteredPersonList().getFirst().isArchived());
            enterCommand("sort o/desc");
            assertTrue(logic.getFilteredPersonList().getFirst().isArchived());
            assertNull(root.lookup(".subject-group-panel"));
            enterCommand("list -subjects");
            assertEquals(List.of("Alice Pauline"), groupedNames("Physics"));
        });
    }

    @Test
    public void groupedSort_emptyAndSubjectlessLists_keepTheGroupedEmptyState() throws Exception {
        runOnFxThread(() -> {
            enterCommand("list -subjects");
            enterCommand("sort slot/asc");
            assertNotNull(root.lookup(".subject-group-empty"));
            enterCommand("list -subjects");
            assertSummary("2", "1");
            enterCommand("clear confirm");
            enterCommand("list -subjects");
            enterCommand("sort o/desc");
            assertNotNull(root.lookup(".subject-group-empty"));
            assertTrue(logic.getFilteredPersonList().isEmpty());
        });
    }

    @Test
    public void ungroupedSlotSort_directionChanges_keepOneNumberedRowPerStudent() throws Exception {
        runOnFxThread(() -> {
            addTwoLessonsPerStudent();
            enterCommand("sort slot/asc");
            ListView<?> students = (ListView<?>) root.lookup(".lesson-list-panel");
            assertEquals(List.of("Alice Pauline", "Benson Meier"), students.getItems().stream()
                    .map(LessonRow.class::cast).map(row -> row.person().getName().fullName).toList());
            enterCommand("sort slot/desc");
            students = (ListView<?>) root.lookup(".lesson-list-panel");
            assertEquals(List.of("Benson Meier", "Alice Pauline"), students.getItems().stream()
                    .map(LessonRow.class::cast).map(row -> row.person().getName().fullName).toList());
            assertEquals(List.of("001", "002"), root.lookupAll("#id").stream().map(Label.class::cast)
                    .map(Label::getText).sorted().toList());
            enterCommand("view 1");
            assertTrue(feedbackText().startsWith("Name: Benson Meier"));
            enterCommand("edit 1 notes/Check fractions");
            assertEquals("Check fractions", logic.getFilteredPersonList().getFirst().getNotes().value);
            enterCommand("list -subjects");
            assertEquals(4, root.lookupAll(".subject-student-row").size());
            assertEquals(2, root.lookupAll("#id").stream().map(Label.class::cast)
                    .filter(label -> label.getText().equals("001")).count());
            enterCommand("list");
            enterCommand("delete 1");
            assertEquals(1, root.lookupAll(".lesson-row").size());
            assertEquals("Alice Pauline", logic.getFilteredPersonList().getFirst().getName().fullName);
        });
    }

    private void addTwoLessonsPerStudent() {
        Person alice = logic.getFilteredPersonList().get(0);
        Person benson = logic.getFilteredPersonList().get(1);
        logic.model.setPerson(alice, new PersonBuilder(alice).withScheduledSubjects(
                new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0))),
                new Subject("Physics", new LessonSlot(DayOfWeek.THURSDAY, LocalTime.of(15, 0)))).build());
        logic.model.setPerson(benson, new PersonBuilder(benson).withScheduledSubjects(
                new Subject("Maths", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(9, 0))),
                new Subject("Physics", new LessonSlot(DayOfWeek.TUESDAY, LocalTime.of(10, 0)))).build());
    }

    private List<HBox> groupedRows(String subject) {
        ScrollPane panel = (ScrollPane) root.lookup(".subject-group-panel");
        VBox groups = (VBox) panel.getContent();
        VBox section = groups.getChildren().stream().map(VBox.class::cast)
                .filter(group -> ((Label) group.getChildren().get(0)).getText().equals(subject))
                .findFirst().orElseThrow();
        VBox entries = (VBox) section.getChildren().get(1);
        return entries.getChildren().stream().map(HBox.class::cast).toList();
    }

    private List<String> groupedNames(String subject) {
        return groupedRows(subject).stream().map(row -> ((Label) row.lookup("#name")).getText()).toList();
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
        private final ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(),
                Clock.fixed(Instant.parse("2026-10-22T14:00:00Z"), ZoneOffset.UTC));
        private final AddressBookParser parser = new AddressBookParser();
        private String lastViewCommand = "";
        private String lastSortCommand = "";

        InMemoryLogic() {
            model.addPerson(new PersonBuilder(ALICE).withOutstandingAmount("0").build());
            model.addPerson(new PersonBuilder(BENSON).withOutstandingAmount("12.50").build());
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            Command command = parser.parseCommand(commandText);
            model.beginCommand();
            try {
                return command.execute(model);
            } finally {
                model.endCommand();
            }
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return model.getFilteredPersonList();
        }

        @Override
        public SortOrder getSortOrder() {
            return model.getSortOrder();
        }

        @Override
        public List<LessonRow> getStudentRows() {
            return model.getStudentRows();
        }

        @Override
        public List<LessonRow> getLessonRows() {
            return model.getLessonRows();
        }

        @Override
        public GuiSettings getGuiSettings() {
            return model.getGuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            model.setGuiSettings(guiSettings);
        }

        @Override
        public String getLastViewCommand() {
            return lastViewCommand;
        }

        @Override
        public String getLastSortCommand() {
            return lastSortCommand;
        }
    }
}
