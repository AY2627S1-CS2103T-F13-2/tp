package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class CsvPersonParserTest {

    private static final List<String> VALID_ROW = List.of("Bob Choo", "22222222", "Blk 2, #02-03", "19-Sep",
            "120.5", "Physics (Tue 10:00); H2 Chemistry (Wed 11:30)", "a-level; needs revision",
            "Weak at \"moles\", needs practice", "true");

    @Test
    public void parsePerson_validRow_returnsMatchingPerson() throws Exception {
        Person expected = new PersonBuilder().withName("Bob Choo").withPhone("22222222")
                .withAddress("Blk 2, #02-03").withStartDate("19-Sep").withOutstandingAmount("120.5")
                .withScheduledSubjects(subject("Physics", DayOfWeek.TUESDAY, 10, 0),
                        subject("H2 Chemistry", DayOfWeek.WEDNESDAY, 11, 30))
                .withTags("a-level", "needs revision").withNotes("Weak at \"moles\", needs practice")
                .build().withArchived(true);
        Person actual = CsvPersonParser.parsePerson(VALID_ROW);
        assertEquals(expected, actual);
        assertEquals(List.of("Physics", "H2 Chemistry"),
                actual.getSubjects().stream().map(subject -> subject.value).toList());
    }

    @Test
    public void parsePerson_optionalValuesBlank_usesDefaults() throws Exception {
        Person person = CsvPersonParser.parsePerson(withColumn(withColumn(withColumn(VALID_ROW, 4, ""), 6, " "),
                7, ""));
        assertEquals(OutstandingAmount.ZERO, person.getOutstandingAmount());
        assertTrue(person.getTags().isEmpty());
        assertEquals("", person.getNotes().value);
    }

    @Test
    public void parsePerson_apostropheNotFollowedByFormulaPrefix_preservesApostrophe() throws Exception {
        Person person = CsvPersonParser.parsePerson(withColumn(withColumn(VALID_ROW, 2, "'plain address"),
                7, "'plain note"));

        assertEquals("'plain address", person.getAddress().value);
        assertEquals("'plain note", person.getNotes().value);
    }

    @Test
    public void parsePerson_wrongColumnCount_throwsParseException() {
        assertThrows(ParseException.class, String.format(CsvPersonParser.MESSAGE_WRONG_COLUMN_COUNT, 8), () ->
                CsvPersonParser.parsePerson(VALID_ROW.subList(0, 8)));
    }

    @Test
    public void parsePerson_invalidField_throwsSameMessageAsAdd() {
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () ->
                CsvPersonParser.parsePerson(withColumn(VALID_ROW, 0, "Bob 2")));
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () ->
                CsvPersonParser.parsePerson(withColumn(VALID_ROW, 1, "123")));
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                CsvPersonParser.parsePerson(withColumn(VALID_ROW, 6, "a-level; A_Level")));
        assertThrows(ParseException.class, CsvPersonParser.MESSAGE_INVALID_ARCHIVED, () ->
                CsvPersonParser.parsePerson(withColumn(VALID_ROW, 8, "yes")));
    }

    @Test
    public void parseSubjects_validFormats_success() throws Exception {
        Set<Subject> subjects = CsvPersonParser.parseSubjects(" maths(mon 09:00) ;  Physics ( TUE  23:59 ) ");
        assertEquals(List.of(subject("maths", DayOfWeek.MONDAY, 9, 0), subject("Physics", DayOfWeek.TUESDAY, 23, 59)),
                new ArrayList<>(subjects));
    }

    @Test
    public void parseSubjects_repeatedSpaces_normalizesNamesAndRejectsCollisions() throws Exception {
        assertEquals(Set.of(subject("H2 Maths", DayOfWeek.MONDAY, 9, 0)),
                CsvPersonParser.parseSubjects("H2   Maths (Mon 09:00)"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_DUPLICATE_SUBJECT, () ->
                CsvPersonParser.parseSubjects("H2 Maths (Mon 09:00); h2  Maths (Tue 10:00)"));
    }

    @Test
    public void parseSubjects_invalidFormats_throwParseException() {
        assertThrows(ParseException.class, CsvPersonParser.MESSAGE_MISSING_SUBJECT, () ->
                CsvPersonParser.parseSubjects(" "));
        assertThrows(ParseException.class, CsvPersonParser.MESSAGE_INVALID_SUBJECTS, () ->
                CsvPersonParser.parseSubjects("Maths"));
        assertThrows(ParseException.class, CsvPersonParser.MESSAGE_INVALID_SUBJECTS, () ->
                CsvPersonParser.parseSubjects("Maths (Mon 09:00);"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_LESSON_DAY, () ->
                CsvPersonParser.parseSubjects("Maths (Monday 09:00)"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_LESSON_TIME, () ->
                CsvPersonParser.parseSubjects("Maths (Mon 9:00)"));
        assertThrows(ParseException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                CsvPersonParser.parseSubjects("C++ (Mon 09:00)"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_DUPLICATE_SUBJECT, () ->
                CsvPersonParser.parseSubjects("Maths (Mon 09:00); MATHS (Tue 10:00)"));
    }

    @Test
    public void parseArchived_acceptsOnlyTrueOrFalse() throws Exception {
        assertTrue(CsvPersonParser.parseArchived(" TRUE "));
        assertFalse(CsvPersonParser.parseArchived("false"));
        assertThrows(ParseException.class, () -> CsvPersonParser.parseArchived(""));
        assertThrows(ParseException.class, () -> CsvPersonParser.parseArchived("1"));
    }

    private static Subject subject(String name, DayOfWeek day, int hour, int minute) {
        return new Subject(name, new LessonSlot(day, LocalTime.of(hour, minute)));
    }

    private static List<String> withColumn(List<String> row, int column, String value) {
        List<String> copy = new ArrayList<>(row);
        copy.set(column, value);
        return copy;
    }
}
