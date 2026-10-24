package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Splits CSV text into rows of fields.
 * Reads the format written by {@link CsvExporter}: fields are separated by commas, and a field that contains
 * a comma, double quote or line break is wrapped in double quotes, with inner double quotes doubled.
 */
public final class CsvReader {

    public static final String MESSAGE_UNCLOSED_QUOTE = "Row %1$d has a quoted field that is never closed.";
    public static final String MESSAGE_STRAY_QUOTE = "Row %1$d has a double quote inside a value that is not "
            + "wrapped in double quotes. Wrap the whole value in double quotes and write each \" inside it as \"\".";
    public static final String MESSAGE_TEXT_AFTER_QUOTE = "Row %1$d has text after the closing double quote of "
            + "a value. Put the whole value inside the double quotes.";

    private static final char BYTE_ORDER_MARK = '\uFEFF';

    private CsvReader() {
        // Prevent instantiation.
    }

    /**
     * Returns the rows in {@code text}, each as a list of fields, in the same order as a spreadsheet shows them.
     * A blank line is returned as a row with one empty field, so row numbers still match the spreadsheet.
     * A line break inside a quoted field stays in that field. A byte order mark at the start is ignored,
     * lines may end with LF or CRLF, and a line break at the very end does not add a row.
     *
     * @throws IllegalValueException if a quoted field is not closed, a value that is not wrapped in double quotes
     *     contains one, or text follows the closing double quote of a value.
     */
    public static List<List<String>> read(String text) throws IllegalValueException {
        requireNonNull(text);
        String content = !text.isEmpty() && text.charAt(0) == BYTE_ORDER_MARK ? text.substring(1) : text;

        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean isInQuotes = false;
        boolean isAfterClosingQuote = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (isInQuotes) {
                if (c == '"' && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else if (c == '"') {
                    isInQuotes = false;
                    isAfterClosingQuote = true;
                } else {
                    currentField.append(c);
                }
            } else if (c == ',') {
                currentRow.add(currentField.toString());
                currentField.setLength(0);
                isAfterClosingQuote = false;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                currentRow.add(currentField.toString());
                currentField.setLength(0);
                isAfterClosingQuote = false;
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            } else if (isAfterClosingQuote) {
                // Keeping or dropping this text would quietly change the value, so the row is reported instead.
                throw new IllegalValueException(String.format(MESSAGE_TEXT_AFTER_QUOTE, rows.size() + 1));
            } else if (c == '"' && currentField.length() > 0) {
                throw new IllegalValueException(String.format(MESSAGE_STRAY_QUOTE, rows.size() + 1));
            } else if (c == '"') {
                isInQuotes = true;
            } else {
                currentField.append(c);
            }
        }

        if (isInQuotes) {
            throw new IllegalValueException(String.format(MESSAGE_UNCLOSED_QUOTE, rows.size() + 1));
        }
        boolean hasLastRow = !currentRow.isEmpty() || currentField.length() > 0;
        if (hasLastRow) {
            currentRow.add(currentField.toString());
            rows.add(currentRow);
        }
        return rows;
    }

    /**
     * Returns true if the row came from a blank line.
     */
    public static boolean isBlankRow(List<String> row) {
        return row.size() == 1 && row.get(0).isBlank();
    }
}
