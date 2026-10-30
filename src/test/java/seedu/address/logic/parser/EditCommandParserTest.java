package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.OUTSTANDING_AMOUNT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_OUTSTANDING_AMOUNT_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_internationalNames_success() {
        for (String name : new String[] {"Anne-Marie", "O'Connor", "O’Connor", "José", "Jose\u0301", "张伟"}) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(name).build();
            assertParseSuccess(parser, "1 n/" + name, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_notesSpecified_preservesFreeTextAndUrls() {
        String notes = "Revise  fractions, graphs & 中文! See https://example.com";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNotes(notes).build();
        assertParseSuccess(parser, "1 notes/  " + notes + "  ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_blankNotes_clearsNote() {
        EditCommand expected = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withNotes("").build());
        assertParseSuccess(parser, "1 notes/", expected);
        assertParseSuccess(parser, "1 notes/   ", expected);
    }

    @Test
    public void parse_repeatedNotes_failure() {
        for (String args : new String[] {"1 notes/One notes/Two", "1 notes/ notes/", "1 notes/Same notes/Same",
            "1 t/A-Level notes/One notes/"}) {
            assertParseFailure(parser, args, EditCommandParser.MESSAGE_DUPLICATE_FIELDS);
        }
    }

    @Test
    public void parse_notesBeforeOtherFields_recognizesFieldBoundaries() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNotes("Revise fractions")
                .withSubjects("Physics").withTags("a-level").withPhone(VALID_PHONE_AMY).build();
        assertParseSuccess(parser, "1 notes/Revise fractions s/Physics day/MON time/09:00"
                        + " t/A-Level p/" + VALID_PHONE_AMY,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseFailure(parser, "1 notes/Revise fractions x/value", EditCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 NOTES/Revise fractions", EditCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_subjectsSpecified_replacesScheduledSet() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withSubjects("Physics", "H2 Chemistry").build();
        assertParseSuccess(parser, "1 s/ Physics day/MON time/09:00"
                        + " s/H2 Chemistry day/MON time/09:00",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 s/Physics day/MON time/09:00 t/A-Level"
                        + " s/H2 Chemistry day/MON time/09:00 p/" + VALID_PHONE_AMY,
                new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder(descriptor)
                        .withTags("a-level").withPhone(VALID_PHONE_AMY).build()));
        assertParseFailure(parser, "1 s/Physics day/MON time/09:00"
                        + " s/PHYSICS day/TUE time/10:00",
                ParserUtil.MESSAGE_DUPLICATE_SUBJECT);
    }

    @Test
    public void parse_blankSubjects_failure() {
        for (String args : new String[] {"1 s/ day/MON time/09:00", "1 s/   day/MON time/09:00",
            "1 s/Maths day/MON time/09:00 s/ day/TUE time/10:00"}) {
            assertParseFailure(parser, args, "Subject must not be blank.");
        }
    }

    @Test
    public void parse_lessonDayOrTimeWithoutSubject_failure() {
        assertParseFailure(parser, "1 day/MON", ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE);
        assertParseFailure(parser, "1 time/09:00", ParserUtil.MESSAGE_INVALID_SUBJECT_SCHEDULE);
    }

    @Test
    public void parse_subjectValidationBoundaries_matchAddRules() {
        for (String value : new String[] {"A", "A".repeat(50), "H2 Chemistry", "Combined-Science"}) {
            assertParseSuccess(parser, "1 s/  " + value + "  day/MON time/09:00",
                    new EditCommand(INDEX_FIRST_PERSON,
                    new EditPersonDescriptorBuilder().withSubjects(value).build()));
        }
        for (String value : new String[] {"A".repeat(51), "---", "Maths,Physics", "中文"}) {
            assertParseFailure(parser, "1 t/A-Level s/" + value + " day/MON time/09:00",
                    Subject.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "1 S/Maths", EditCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_tagsSpecified_replacesNormalizedTags() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags("a-level", "needs revision").build();
        assertParseSuccess(parser, "1 t/ A-Level  t/Needs revision t/a-level",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 t/needs revision p/" + VALID_PHONE_AMY + " t/A-Level",
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder(descriptor).withPhone(VALID_PHONE_AMY).build()));
    }

    @Test
    public void parse_singleBlankTag_clearsTags() {
        EditCommand expected = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withTags().build());
        assertParseSuccess(parser, "1 t/", expected);
        assertParseSuccess(parser, "1 t/   ", expected);
    }

    @Test
    public void parse_invalidOrMixedBlankTags_failure() {
        for (String args : new String[] {"1 t/A-Level t/", "1 t/ t/A-Level", "1 t/ t/", "1 t/Maths,Physics",
            "1 t/中文", "1 p/91234567 t/bad!"}) {
            assertParseFailure(parser, args, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, VALID_NAME_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "", EditCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0" + NAME_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 some random string", EditCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY + PHONE_DESC_BOB
                + ADDRESS_DESC_AMY + START_DATE_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB
                + " s/Physics day/MON time/09:00 t/A-Level notes/Revise fractions";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_AMY)
                .withStartDate(VALID_START_DATE_BOB)
                .withOutstandingAmount(VALID_OUTSTANDING_AMOUNT_BOB)
                .withSubjects("Physics").withTags("a-level").withNotes("Revise fractions")
                .build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_eachFieldSpecifiedIndividually_success() {
        assertParseSuccess(parser, "1" + NAME_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build()));
        assertParseSuccess(parser, "1" + PHONE_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build()));
        assertParseSuccess(parser, "1" + ADDRESS_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build()));
        assertParseSuccess(parser, "1" + START_DATE_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withStartDate(VALID_START_DATE_AMY).build()));
    }

    @Test
    public void parse_invalidFieldValues_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_START_DATE_DESC, StartDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_startDateSpecified_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withStartDate(VALID_START_DATE_BOB).build();
        assertParseSuccess(parser, INDEX_FIRST_PERSON.getOneBased() + START_DATE_DESC_BOB,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidStartDate_failure() {
        assertParseFailure(parser, INDEX_FIRST_PERSON.getOneBased() + INVALID_START_DATE_DESC,
                StartDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_outstandingAmountSpecified_success() {
        String[] amounts = {"0", "600", "20.50"};
        for (String amount : amounts) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withOutstandingAmount(amount).build();
            assertParseSuccess(parser, "1 o/  " + amount + "  ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_invalidOutstandingAmount_failure() {
        String[] amounts = {"-1", "20.123", "abc", "1 2", ""};
        for (String amount : amounts) {
            assertParseFailure(parser, "1 o/" + amount, OutstandingAmount.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidIndexes_failure() {
        String[] invalidIndexes = {"", " ", "-1", "0", "1.5", "abc", "1 2", "+1", "2147483648"};
        for (String invalidIndex : invalidIndexes) {
            assertParseFailure(parser, invalidIndex + PHONE_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_unknownOrUppercasePrefix_failure() {
        String[] arguments = {"1 x/value", "1 a/Main Street x/value", "1 x/value" + PHONE_DESC_AMY,
            "1 P/91234567", "1 a/Main Street N/Amy", "1 O/600", "1 e/student@example.com", "1 T/friend"};
        for (String argument : arguments) {
            assertParseFailure(parser, argument, EditCommandParser.MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_eachSingleValuedFieldRepeated_failure() {
        String[] repeatedFields = {
            "1 n/Amy n/Bob",
            "1 p/11111111 p/22222222",
            "1 a/First Street a/Second Street",
            "1 d/10-Aug d/19-Sep",
            "1 o/20 o/600"
        };
        for (String arguments : repeatedFields) {
            assertParseFailure(parser, arguments, EditCommandParser.MESSAGE_DUPLICATE_FIELDS);
        }
    }

    @Test
    public void parse_trimmedIndexAndValues_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Block 1, #02/03")
                .withPhone(VALID_PHONE_AMY)
                .build();
        assertParseSuccess(parser, "  1  a/  Block 1, #02/03   p/  " + VALID_PHONE_AMY + "  ",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }
}
