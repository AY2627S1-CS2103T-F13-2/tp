package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;

public class CsvReaderTest {

    @Test
    public void read_plainFields_splitsRowsAndFields() throws Exception {
        assertEquals(List.of(List.of("a", "b", "c"), List.of("1", "", "3")), CsvReader.read("a,b,c\n1,,3\n"));
    }

    @Test
    public void read_quotedFields_keepsCommasQuotesAndLineBreaks() throws Exception {
        String text = "\"Blk 1, #01-02\",\"Said \"\"hi\"\"\",\"line one\r\nline two\"\r\nnext,row";
        assertEquals(List.of(List.of("Blk 1, #01-02", "Said \"hi\"", "line one\r\nline two"),
                List.of("next", "row")), CsvReader.read(text));
    }

    @Test
    public void read_crlfAndByteOrderMark_ignored() throws Exception {
        assertEquals(List.of(List.of("Name", "Phone"), List.of("Amy", "1")),
                CsvReader.read("﻿Name,Phone\r\nAmy,1\r\n"));
    }

    @Test
    public void read_blankLines_keptAsBlankRows() throws Exception {
        List<List<String>> rows = CsvReader.read("a\n\nb");
        assertEquals(3, rows.size());
        assertTrue(CsvReader.isBlankRow(rows.get(1)));
        assertFalse(CsvReader.isBlankRow(rows.get(0)));
        assertFalse(CsvReader.isBlankRow(List.of("", "")));
    }

    @Test
    public void read_emptyText_returnsNoRows() throws Exception {
        assertEquals(List.of(), CsvReader.read(""));
        assertEquals(List.of(), CsvReader.read("﻿"));
    }

    @Test
    public void read_unclosedQuote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_UNCLOSED_QUOTE, 2), () ->
                CsvReader.read("a,b\n\"never closed,c\n"));
    }

    @Test
    public void read_exporterOutput_returnsSameValues() throws Exception {
        String value = "Weak at \"comprehension\", needs practice";
        String exported = "Notes\n\"" + value.replace("\"", "\"\"") + "\"\n";
        assertEquals(List.of(List.of("Notes"), List.of(value)), CsvReader.read(exported));
    }
}
