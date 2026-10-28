package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Tests CSV formatting for contacts. */
public class CsvExporterTest {

    @Test
    public void format_contactWithAllFields_quotesAndFormatsFields() {
        Person person = new PersonBuilder().withName("Amy Bee").withAddress("Block 1, #02-03")
                .withSubjects("Maths", "Physics").withTags("z-tag", "a-tag")
                .withNotes("Bring, calculator\".").build().withArchived(true);

        String expected = "Name,Phone,Address,Start Date,Outstanding Amount,Subjects,Tags,Notes,"
                + "Archived\n"
                + "Amy Bee,85355255,\"Block 1, #02-03\",10-Aug,0,Maths (Mon 09:00); Physics (Mon 09:00),"
                + "a-tag; z-tag,"
                + "\"Bring, calculator\"\".\",true\n";

        assertEquals(expected, CsvExporter.format(List.of(person)));
    }

    @Test
    public void format_multilineNotes_quotesLineFeedsAndCarriageReturns() throws Exception {
        String notes = "Line one\n\nLine three\r\nLine five\rLine six";
        Person person = new PersonBuilder().withNotes(notes).build();

        List<List<String>> rows = CsvReader.read(CsvExporter.format(List.of(person)));

        assertEquals(2, rows.size());
        assertEquals(notes, rows.get(1).get(7));
    }

    @Test
    public void format_notesWithEachCsvDelimiter_quotesEverySpecialValue() throws Exception {
        List<String> notesValues = List.of("comma,value", "quote\"value", "line\nvalue", "carriage\rvalue",
                "windows\r\nline");
        List<Person> persons = notesValues.stream().map(notes -> new PersonBuilder().withNotes(notes).build())
                .toList();

        List<List<String>> rows = CsvReader.read(CsvExporter.format(persons));

        assertEquals(notesValues.size() + 1, rows.size());
        for (int i = 0; i < notesValues.size(); i++) {
            assertEquals(notesValues.get(i), rows.get(i + 1).get(7));
        }
    }

    @Test
    public void format_plainFields_doesNotQuoteUnnecessaryFields() throws Exception {
        String notes = "Plain note";
        Person person = new PersonBuilder().withNotes(notes).build();

        List<List<String>> rows = CsvReader.read(CsvExporter.format(List.of(person)));

        assertEquals(notes, rows.get(1).get(7));
    }

    @Test
    public void format_emptyList_returnsHeaderOnly() {
        assertEquals("Name,Phone,Address,Start Date,Outstanding Amount,Subjects,Tags,Notes,Archived\n",
                CsvExporter.format(List.of()));
    }
}
