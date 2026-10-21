package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private ScrollPane resultDisplay;
    @FXML
    private Label feedbackText;

    @FXML
    private GridPane summary;
    @FXML
    private Label totalCount;
    @FXML
    private Label paidCount;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        summary.setVisible(false);
        summary.setManaged(false);
        resultDisplay.setVisible(true);
        resultDisplay.setManaged(true);
        feedbackText.setText(feedbackToUser.replaceAll("\\R", " | "));
        resultDisplay.setTooltip(new Tooltip(feedbackToUser));
        resultDisplay.setHvalue(0);
    }

    /** Shows counts for the currently listed students instead of command feedback. */
    public void showSummary(int total, int paid) {
        totalCount.setText(Integer.toString(total));
        paidCount.setText(Integer.toString(paid));
        resultDisplay.setVisible(false);
        resultDisplay.setManaged(false);
        summary.setVisible(true);
        summary.setManaged(true);
    }

}
