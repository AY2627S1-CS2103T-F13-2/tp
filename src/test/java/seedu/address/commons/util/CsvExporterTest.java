package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void format_emptyList_returnsHeaderOnly() {
        assertEquals("Name,Phone,Address,Start Date,Outstanding Amount,Subjects,Tags,Notes,Archived\n",
                CsvExporter.format(List.of()));
    }

    @Test
    public void format_formulaPrefixesInAddressAndNotes_exportsAsPlainText() {
        for (String prefix : List.of("=", "+", "-", "@")) {
            Person person = new PersonBuilder().withAddress(prefix + "address")
                    .withNotes(prefix + "note").build();

            String exportedRow = CsvExporter.format(List.of(person)).lines().skip(1).findFirst().orElseThrow();

            assertTrue(exportedRow.contains("'" + prefix + "address"));
            assertTrue(exportedRow.contains("'" + prefix + "note"));
        }
    }
}
