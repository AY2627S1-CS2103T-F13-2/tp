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
                CsvReader.read("\uFEFFName,Phone\r\nAmy,1\r\n"));
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
    public void read_noFinalLineBreak_keepsLastRow() throws Exception {
        assertEquals(List.of(List.of("a", "quoted")), CsvReader.read("a,\"quoted\""));
        assertEquals(List.of(List.of("a", "")), CsvReader.read("a,"));
    }

    @Test
    public void read_carriageReturnOnly_endsRow() throws Exception {
        assertEquals(List.of(List.of("a"), List.of("b")), CsvReader.read("a\rb\r"));
    }

    @Test
    public void read_emptyText_returnsNoRows() throws Exception {
        assertEquals(List.of(), CsvReader.read(""));
        assertEquals(List.of(), CsvReader.read("\uFEFF"));
    }

    @Test
    public void read_unclosedQuote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_UNCLOSED_QUOTE, 2), () ->
                CsvReader.read("a,b\n\"never closed,c\n"));
    }

    @Test
    public void read_quoteInsideUnquotedValue_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_STRAY_QUOTE, 2), () ->
                CsvReader.read("Notes\nNeeds \"revision\"\n"));
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_STRAY_QUOTE, 1), () ->
                CsvReader.read("a,b\"c"));
    }

    @Test
    public void read_textAfterClosingQuote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_TEXT_AFTER_QUOTE, 2), () ->
                CsvReader.read("Notes\n\"Needs\" revision\n"));
        assertThrows(IllegalValueException.class, String.format(CsvReader.MESSAGE_TEXT_AFTER_QUOTE, 1), () ->
                CsvReader.read("\"a\" ,b"));
    }

    @Test
    public void read_quotedValueNextToSeparators_accepted() throws Exception {
        assertEquals(List.of(List.of("", "a", "b\"c"), List.of("d")), CsvReader.read("\"\",\"a\",\"b\"\"c\"\r\n\"d\""));
    }

    @Test
    public void read_exporterOutput_returnsSameValues() throws Exception {
        String value = "Weak at \"comprehension\", needs practice";
        String exported = "Notes\n\"" + value.replace("\"", "\"\"") + "\"\n";
        assertEquals(List.of(List.of("Notes"), List.of(value)), CsvReader.read(exported));
    }
}
