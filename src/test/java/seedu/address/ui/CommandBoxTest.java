package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest extends FxTestSupport {
    @Test
    public void prepareFind_blankInput_focusesAndPrefillsWithoutExecuting() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> executed = new AtomicReference<>();
            CommandBox box = new CommandBox(command -> {
                executed.set(command);
                return new CommandResult("Success");
            });
            TextField input = inputOf(box);
            Stage stage = new Stage();
            stage.setScene(new Scene(box.getRoot()));
            stage.show();

            box.prepareFind();
            assertEquals("find ", input.getText());
            assertEquals(5, input.getCaretPosition());
            assertSame(input, stage.getScene().getFocusOwner());
            input.setText("  \t");
            box.prepareFind();
            assertEquals("find ", input.getText());
            assertNull(executed.get());
        });
    }

    @Test
    public void prepareFind_unfinishedInput_preservesTextAndMovesCaretToEnd() throws Exception {
        runOnFxThread(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Success"));
            TextField input = inputOf(box);
            input.setText("edit 1 n/Alice");
            input.positionCaret(0);
            box.prepareFind();
            assertEquals("edit 1 n/Alice", input.getText());
            assertEquals(input.getLength(), input.getCaretPosition());
        });
    }

    @Test
    public void submit_emptyInput_doesNotExecute() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> executed = new AtomicReference<>();
            CommandBox box = new CommandBox(command -> {
                executed.set(command);
                return new CommandResult("Success");
            });
            inputOf(box).fireEvent(new ActionEvent());
            assertNull(executed.get());
        });
    }

    @Test
    public void submit_success_passesCommandAndClearsInput() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> executed = new AtomicReference<>();
            CommandBox box = new CommandBox(command -> {
                executed.set(command);
                return new CommandResult("Success");
            });
            TextField input = inputOf(box);
            input.setText("find Alice");
            input.fireEvent(new ActionEvent());
            assertEquals("find Alice", executed.get());
            assertEquals("", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void submit_commandException_preservesInputAndClearsErrorWhenEdited() throws Exception {
        runOnFxThread(() -> assertFailure(command -> {
            throw new CommandException("Cannot execute command");
        }));
    }

    @Test
    public void submit_parseException_preservesInputAndClearsErrorWhenEdited() throws Exception {
        runOnFxThread(() -> assertFailure(command -> {
            throw new ParseException("Invalid command");
        }));
    }

    private TextField inputOf(CommandBox box) {
        return (TextField) box.getRoot().lookup("#commandTextField");
    }

    private void assertFailure(CommandBox.CommandExecutor executor) {
        CommandBox box = new CommandBox(executor);
        TextField input = inputOf(box);
        input.setText("invalid command");
        input.fireEvent(new ActionEvent());
        input.fireEvent(new ActionEvent());
        assertEquals("invalid command", input.getText());
        assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        assertEquals(1L, input.getStyleClass().stream().filter(CommandBox.ERROR_STYLE_CLASS::equals).count());
        input.setText("corrected command");
        assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
    }
}
