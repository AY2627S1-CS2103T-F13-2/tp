package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Age;
import seedu.address.model.person.Education;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Note;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;

public class StudentProfileParserUtilTest {
    @Test
    public void parseSubjects_normalizedSet_preservesCaseAndInternalSpaces() throws Exception {
        assertEquals(Set.of(new Subject("Maths"), new Subject("H2  Physics")),
                StudentProfileParserUtil.parseSubjects(List.of(" Maths ", "H2  Physics", "Maths")));
        assertThrows(ParseException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                StudentProfileParserUtil.parseSubjects(List.of()));
        for (String invalid : List.of("", " ", "Maths, Physics", "Biology!", "Maths\nPhysics")) {
            assertThrows(ParseException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                    StudentProfileParserUtil.parseSubjects(List.of("Maths", invalid)));
        }
    }

    @Test
    public void parseStartDate_checksFormatOnly() throws Exception {
        assertEquals(new StartDate("10-Aug"), StudentProfileParserUtil.parseStartDate(" 10-Aug "));
        assertEquals("99-Abc", StudentProfileParserUtil.parseStartDate("99-Abc").value);
        for (String invalid : List.of("", "1-Aug", "10-Aug-2026", "10/08", "10-August")) {
            assertThrows(ParseException.class, StartDate.MESSAGE_CONSTRAINTS, () ->
                    StudentProfileParserUtil.parseStartDate(invalid));
        }
    }

    @Test
    public void parseAmount_normalizesWithoutRounding() throws Exception {
        assertEquals(OutstandingAmount.ZERO, StudentProfileParserUtil.parseOutstandingAmount(" "));
        assertEquals(new OutstandingAmount("20"), StudentProfileParserUtil.parseOutstandingAmount(" 20.00 "));
        assertEquals(new OutstandingAmount("20").hashCode(), new OutstandingAmount("20.0").hashCode());
        assertEquals("12345678901234567890.12",
                StudentProfileParserUtil.parseOutstandingAmount("12345678901234567890.12").toString());
        for (String invalid : List.of("-1", "1.001", "NaN", "1e2", "+1", "1 0", ".5")) {
            assertThrows(ParseException.class, OutstandingAmount.MESSAGE_CONSTRAINTS, () ->
                    StudentProfileParserUtil.parseOutstandingAmount(invalid));
        }
    }

    @Test
    public void parseOptionalFields_supportsAbsenceAndBoundaries() throws Exception {
        assertEquals("", StudentProfileParserUtil.parseAge(" ").value);
        assertEquals("1", StudentProfileParserUtil.parseAge("1").value);
        assertEquals("120", StudentProfileParserUtil.parseAge("120").value);
        for (String invalid : List.of("0", "-1", "121", "1.5", "old", "999999999999")) {
            assertThrows(ParseException.class, Age.MESSAGE_CONSTRAINTS, () ->
                    StudentProfileParserUtil.parseAge(invalid));
        }
        assertEquals("", StudentProfileParserUtil.parseGuardianContact("").value);
        assertEquals("01234567", StudentProfileParserUtil.parseGuardianContact(" 01234567 ").value);
        for (String invalid : List.of("1234567", "123456789", "+6591234567", "9123 4567", "Guardian")) {
            assertThrows(ParseException.class, GuardianContact.MESSAGE_CONSTRAINTS, () ->
                    StudentProfileParserUtil.parseGuardianContact(invalid));
        }
    }

    @Test
    public void parseText_preservesPrintableCharacters() throws Exception {
        assertEquals(new Education("A-Level"), StudentProfileParserUtil.parseEducation(" A-Level "));
        assertEquals(new Note("Bring  worksheets #2!"), StudentProfileParserUtil.parseNote(" Bring  worksheets #2! "));
        assertEquals("", StudentProfileParserUtil.parseEducation(" ").value);
        assertEquals("", StudentProfileParserUtil.parseNote(" ").value);
        assertThrows(ParseException.class, () -> StudentProfileParserUtil.parseNote("two\nlines"));
        assertThrows(ParseException.class, () -> StudentProfileParserUtil.parseEducation("two\twords"));
        assertThrows(NullPointerException.class, () -> StudentProfileParserUtil.parseNote(null));
    }
}
