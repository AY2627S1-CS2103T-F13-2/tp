package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "12345678";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";
    private static final String VALID_OUTSTANDING_AMOUNT = "20.50";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseName_internationalNames_preservesSpelling() throws Exception {
        for (String name : new String[] {"Anne-Marie", "O'Connor", "O’Connor", "José", "Jose\u0301", "张伟"}) {
            assertEquals(name, ParserUtil.parseName(name).fullName);
        }
    }

    @Test
    public void parseStartDate_invalidCalendarDate_throwsParseException() {
        for (String invalid : List.of("31-Feb", "30-Feb", "31-Apr", "00-Jan", "32-Jan", "99-Abc", "10-Abc",
                "1-Aug", "10-Aug-2026", "")) {
            assertThrows(ParseException.class, StartDate.MESSAGE_CONSTRAINTS, () ->
                    ParserUtil.parseStartDate(invalid));
        }
    }

    @Test
    public void parseStartDate_validCalendarDate_preservesCaseAndTrimsWhitespace() throws Exception {
        for (String valid : List.of("01-Jan", "29-Feb", "28-Feb", "30-Apr", "31-Dec", "10-aUG")) {
            assertEquals(new StartDate(valid), ParserUtil.parseStartDate(" " + valid + " "));
        }
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStartDate(null));
    }

    @Test
    public void parseSubjectName_trimsAndCollapsesOrdinarySpaces() throws Exception {
        assertEquals("H2 Maths", ParserUtil.parseSubjectName("  H2  Maths  "));
        assertThrows(ParseException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseSubjectName("H2\tMaths"));
    }

    @Test
    public void parseSubjectNames_spaceAndCaseVariants_combinesEquivalentFilters() throws Exception {
        assertEquals(Set.of("H2 Maths"), ParserUtil.parseSubjectNames(List.of(" H2  Maths ", "h2 Maths")));
    }

    @Test
    public void parseScheduledSubjects_normalizedDuplicateNames_rejectsEvenForDifferentSlots() {
        for (String day : List.of("MON", "TUE")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_DUPLICATE_SUBJECT, () ->
                    ParserUtil.parseScheduledSubjects(List.of("H2 Maths", "h2  Maths"),
                            List.of("MON", day), List.of("09:00", "09:00")));
        }
    }

    @Test
    public void parseSubjectName_blankValue_throwsParseException() {
        assertThrows(ParseException.class, "Subject must not be blank.", () -> ParserUtil.parseSubjectName("   "));
    }

    @Test
    public void parseLessonDay_validAbbreviations_caseInsensitive() throws Exception {
        assertEquals(DayOfWeek.MONDAY, ParserUtil.parseLessonDay(" mon "));
        assertEquals(DayOfWeek.TUESDAY, ParserUtil.parseLessonDay("TUE"));
        assertEquals(DayOfWeek.WEDNESDAY, ParserUtil.parseLessonDay("WED"));
        assertEquals(DayOfWeek.THURSDAY, ParserUtil.parseLessonDay("THU"));
        assertEquals(DayOfWeek.FRIDAY, ParserUtil.parseLessonDay("FRI"));
        assertEquals(DayOfWeek.SATURDAY, ParserUtil.parseLessonDay("SAT"));
        assertEquals(DayOfWeek.SUNDAY, ParserUtil.parseLessonDay("SUN"));
    }

    @Test
    public void parseLessonDay_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_LESSON_DAY, () ->
                ParserUtil.parseLessonDay("Monday"));
    }

    @Test
    public void parseLessonTime_requiresStrict24HourFormat() throws Exception {
        assertEquals(LocalTime.of(0, 0), ParserUtil.parseLessonTime("00:00"));
        assertEquals(LocalTime.of(23, 59), ParserUtil.parseLessonTime(" 23:59 "));
        for (String invalid : List.of("9:00", "24:00", "12:60", "noon")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_LESSON_TIME, () ->
                    ParserUtil.parseLessonTime(invalid));
        }
    }

    @Test
    public void parseScheduledSubjects_validTriplets_preservesOrder() throws Exception {
        Set<Subject> parsed = ParserUtil.parseScheduledSubjects(
                List.of("Maths", "Physics"), List.of("MON", "fri"), List.of("09:00", "15:30"));
        assertEquals(List.of(
                new Subject("Maths", new LessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0))),
                new Subject("Physics", new LessonSlot(DayOfWeek.FRIDAY, LocalTime.of(15, 30)))),
                List.copyOf(parsed));
    }

    @Test
    public void parseScheduledSubjects_missingOrDuplicateTriplet_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE, () ->
                ParserUtil.parseScheduledSubjects(List.of(), List.of(), List.of()));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE, () ->
                ParserUtil.parseScheduledSubjects(List.of("Maths"), List.of(), List.of("09:00")));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE, () ->
                ParserUtil.parseScheduledSubjects(List.of("Maths"), List.of("MON"), List.of()));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_DUPLICATE_SUBJECT, () ->
                ParserUtil.parseScheduledSubjects(List.of("Maths", "maths"),
                        List.of("MON", "TUE"), List.of("09:00", "10:00")));
    }

    @Test
    public void verifySubjectScheduleOrder_missingOrStandaloneComponents_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE, () ->
                ParserUtil.verifySubjectScheduleOrder("s/Maths day/MON t/A-Level"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE, () ->
                ParserUtil.verifySubjectScheduleOrder("time/09:00"));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseOutstandingAmount_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseOutstandingAmount(null));
    }

    @Test
    public void parseOutstandingAmount_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseOutstandingAmount("-1"));
    }

    @Test
    public void parseOutstandingAmount_validValue_returnsNormalizedAmount() throws Exception {
        OutstandingAmount expectedAmount = new OutstandingAmount(VALID_OUTSTANDING_AMOUNT);
        assertEquals(expectedAmount, ParserUtil.parseOutstandingAmount("  " + VALID_OUTSTANDING_AMOUNT + "  "));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
}
