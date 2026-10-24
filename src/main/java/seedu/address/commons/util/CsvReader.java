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

    private static final char BYTE_ORDER_MARK = '﻿';

    private CsvReader() {
        // Prevent instantiation.
    }

    /**
     * Returns the rows in {@code text}, each as a list of fields, in the same order as a spreadsheet shows them.
     * A blank line is returned as a row with one empty field, so row numbers still match the spreadsheet.
     * A line break inside a quoted field stays in that field. A byte order mark at the start is ignored,
     * lines may end with LF or CRLF, and a line break at the very end does not add a row.
     *
     * @throws IllegalValueException if a quoted field is not closed.
     */
    public static List<List<String>> read(String text) throws IllegalValueException {
        requireNonNull(text);
        String content = !text.isEmpty() && text.charAt(0) == BYTE_ORDER_MARK ? text.substring(1) : text;

        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean isInQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (isInQuotes) {
                if (c == '"' && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else if (c == '"') {
                    isInQuotes = false;
                } else {
                    currentField.append(c);
                }
            } else if (c == '"') {
                isInQuotes = true;
            } else if (c == ',') {
                currentRow.add(currentField.toString());
                currentField.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                currentRow.add(currentField.toString());
                currentField.setLength(0);
                rows.add(currentRow);
                currentRow = new ArrayList<>();
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
