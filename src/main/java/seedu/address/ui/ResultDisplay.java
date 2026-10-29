package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

    @FXML
    private ScrollPane resultDisplay;
    @FXML
    private Label feedbackText;
    @FXML
    private ScrollPane expandedResultDisplay;
    @FXML
    private Label wrappedFeedbackText;

    @FXML
    private GridPane summary;
    @FXML
    private Label totalCount;
    @FXML
    private Label paidCount;

    /** Creates a result display that adapts to the available width. */
    public ResultDisplay() {
        super(FXML);
        getRoot().widthProperty().addListener((observable, oldValue, newValue) -> updateDisplay());
        feedbackText.fontProperty().addListener((observable, oldValue, newValue) -> updateDisplay());
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        summary.setVisible(false);
        summary.setManaged(false);
        feedbackText.setText(feedbackToUser.replaceAll("\\R", " | "));
        wrappedFeedbackText.setText(feedbackText.getText());
        Tooltip tooltip = new Tooltip(feedbackToUser);
        resultDisplay.setTooltip(tooltip);
        expandedResultDisplay.setTooltip(tooltip);
        expandedResultDisplay.setVvalue(0);
        updateDisplay();
    }

    /** Measures the normal display so resizing or switching fonts cannot leave overflowing text hidden. */
    private void updateDisplay() {
        if (summary.isVisible()) {
            return;
        }
        Insets margin = StackPane.getMargin(resultDisplay);
        double availableWidth = getRoot().getWidth() - margin.getLeft() - margin.getRight();
        boolean expanded = availableWidth > 0 && feedbackText.prefWidth(-1) > availableWidth;
        resultDisplay.setVisible(!expanded);
        resultDisplay.setManaged(!expanded);
        expandedResultDisplay.setVisible(expanded);
        expandedResultDisplay.setManaged(expanded);
        getRoot().pseudoClassStateChanged(EXPANDED, expanded);
    }

    /** Shows counts for the currently listed students instead of command feedback. */
    public void showSummary(int total, int paid) {
        totalCount.setText(Integer.toString(total));
        paidCount.setText(Integer.toString(paid));
        resultDisplay.setVisible(false);
        resultDisplay.setManaged(false);
        expandedResultDisplay.setVisible(false);
        expandedResultDisplay.setManaged(false);
        getRoot().pseudoClassStateChanged(EXPANDED, false);
        summary.setVisible(true);
        summary.setManaged(true);
    }

}
