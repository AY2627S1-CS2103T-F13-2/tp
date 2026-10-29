package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_OUTSTANDING_AMOUNT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.OUTSTANDING_AMOUNT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OUTSTANDING_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.StartDate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String SUBJECT = " s/Maths day/MON time/09:00";
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_notesPresent_preservesFreeText() {
        String notes = "Needs  revision: fractions, algebra & 中文!";
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + SUBJECT + " notes/  " + notes + "  ",
                new AddCommand(new PersonBuilder(BOB).withSubjects("Maths").withNotes(notes).build()));
    }

    @Test
    public void parse_notesOmittedOrBlank_defaultsToEmpty() {
        String requiredFields = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + SUBJECT;
        for (String optionalNotes : new String[] {"", " notes/", " notes/   "}) {
            assertParseSuccess(parser, requiredFields + optionalNotes,
                    new AddCommand(new PersonBuilder(BOB).withSubjects("Maths").withNotes("").build()));
        }
    }

    @Test
    public void parse_notesBeforeOtherFields_success() {
        assertParseSuccess(parser, " notes/Revise fractions" + NAME_DESC_BOB + PHONE_DESC_BOB
                        + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20")
                        .withSubjects("Maths").withNotes("Revise fractions").build()));
    }

    @Test
    public void parse_repeatedNotes_failure() {
        String requiredFields = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + SUBJECT;
        for (String repeatedNotes : new String[] {" notes/One notes/Two", " notes/ notes/",
            " notes/Same notes/Same"}) {
            assertParseFailure(parser, requiredFields + repeatedNotes,
                    Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTES));
        }
    }

    @Test
    public void parse_allRequiredFieldsPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withSubjects("Maths").build()));
    }

    @Test
    public void parse_outstandingAmountPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + OUTSTANDING_AMOUNT_DESC_BOB + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20").withSubjects("Maths").build()));
    }

    @Test
    public void parse_zeroOutstandingAmountPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " o/0" + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("0").withSubjects("Maths").build()));
    }

    @Test
    public void parse_outstandingAmountOmitted_defaultsToZero() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("0").withSubjects("Maths").build()));
    }

    @Test
    public void parse_repeatedTags_normalizesAndCombines() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + SUBJECT + " t/ A-Level  t/Needs revision t/a-level",
                new AddCommand(new PersonBuilder(BOB).withSubjects("Maths")
                        .withTags("a-level", "needs revision").build()));
    }

    @Test
    public void parse_tagsInterleavedWithOtherFields_success() {
        assertParseSuccess(parser, " t/A-Level" + NAME_DESC_BOB + " t/Needs revision" + START_DATE_DESC_BOB
                        + ADDRESS_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB + PHONE_DESC_BOB + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20")
                        .withSubjects("Maths").withTags("a-level", "needs revision").build()));
    }

    @Test
    public void parse_invalidOrBlankTag_failure() {
        String[] tags = {" t/", " t/   ", " t/ t/A-Level", " t/A-Level t/", " t/invalid!", " t/A_Level"};
        for (String tagInput : tags) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                    + SUBJECT + tagInput, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingRequiredField_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStartDate_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                        + INVALID_START_DATE_DESC + SUBJECT,
                StartDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_impossibleStartDate_failure() {
        for (String invalid : new String[] {"31-Feb", "31-Apr", "00-Jan", "99-Abc"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                    + " d/" + invalid + SUBJECT, StartDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_leapDay_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + " d/29-Feb" + SUBJECT,
                new AddCommand(new PersonBuilder(BOB).withStartDate("29-Feb").withSubjects("Maths").build()));
    }

    @Test
    public void parse_invalidLessonTime_failure() {
        for (String invalid : new String[] {"24:00", "12:60", "-1:00", "9:00"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                    + " s/Maths day/MON time/" + invalid, ParserUtil.MESSAGE_INVALID_LESSON_TIME);
        }
    }

    @Test
    public void parse_repeatedStartDate_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                        + START_DATE_DESC_BOB + START_DATE_DESC_BOB + SUBJECT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_START_DATE));
    }

    @Test
    public void parse_invalidOutstandingAmount_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + INVALID_OUTSTANDING_AMOUNT_DESC + SUBJECT,
                OutstandingAmount.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedOutstandingAmount_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + OUTSTANDING_AMOUNT_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB + SUBJECT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_OUTSTANDING_AMOUNT));
    }

    @Test
    public void parse_missingSubject_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB,
                "At least one subject must be supplied.");
    }

    @Test
    public void parse_repeatedSubjects_caseInsensitiveDuplicatesRejected() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " s/Maths day/MON time/09:00 s/maths day/TUE time/10:00",
                ParserUtil.MESSAGE_DUPLICATE_SUBJECT);
    }

    @Test
    public void parse_blankSubjectAmongValidSubjects_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                + " s/Maths day/MON time/09:00 s/ day/TUE time/10:00", "Subject must not be blank.");
    }

    @Test
    public void parse_commaSeparatedSubjects_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                + " s/Maths, Physics day/MON time/09:00",
                seedu.address.model.person.Subject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingOrMisorderedLessonFields_failure() {
        String fields = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB;
        for (String subjects : new String[] {" s/Maths", " s/Maths day/MON", " day/MON time/09:00 s/Maths",
            " s/Maths time/09:00 day/MON", " s/Maths day/Monday time/09:00", " s/Maths day/MON time/9:00"}) {
            assertParseFailure(parser, fields + subjects,
                    subjects.contains("Monday") ? ParserUtil.MESSAGE_INVALID_LESSON_DAY
                            : subjects.endsWith("9:00") ? ParserUtil.MESSAGE_INVALID_LESSON_TIME
                            : ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE);
        }
    }
}
