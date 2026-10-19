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
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_notesPresent_preservesFreeText() {
        String notes = "Needs  revision: fractions, algebra & 中文!";
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " notes/  " + notes + "  ",
                new AddCommand(new PersonBuilder(BOB).withNotes(notes).build()));
    }

    @Test
    public void parse_notesOmittedOrBlank_defaultsToEmpty() {
        String requiredFields = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB;
        for (String optionalNotes : new String[] {"", " notes/", " notes/   "}) {
            assertParseSuccess(parser, requiredFields + optionalNotes,
                    new AddCommand(new PersonBuilder(BOB).withNotes("").build()));
        }
    }

    @Test
    public void parse_notesBeforeOtherFields_success() {
        assertParseSuccess(parser, " notes/Revise fractions" + NAME_DESC_BOB + PHONE_DESC_BOB
                        + ADDRESS_DESC_BOB + START_DATE_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20")
                        .withNotes("Revise fractions").build()));
    }

    @Test
    public void parse_repeatedNotes_failure() {
        String requiredFields = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB;
        for (String repeatedNotes : new String[] {" notes/One notes/Two", " notes/ notes/",
            " notes/Same notes/Same"}) {
            assertParseFailure(parser, requiredFields + repeatedNotes,
                    Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTES));
        }
    }

    @Test
    public void parse_allRequiredFieldsPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB,
                new AddCommand(BOB));
    }

    @Test
    public void parse_outstandingAmountPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + OUTSTANDING_AMOUNT_DESC_BOB,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20").build()));
    }

    @Test
    public void parse_zeroOutstandingAmountPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " o/0",
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("0").build()));
    }

    @Test
    public void parse_outstandingAmountOmitted_defaultsToZero() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("0").build()));
    }

    @Test
    public void parse_repeatedTags_normalizesAndCombines() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " t/ A-Level  t/Needs revision t/a-level",
                new AddCommand(new PersonBuilder(BOB).withTags("a-level", "needs revision").build()));
    }

    @Test
    public void parse_tagsInterleavedWithOtherFields_success() {
        assertParseSuccess(parser, " t/A-Level" + NAME_DESC_BOB + " t/Needs revision" + START_DATE_DESC_BOB
                        + ADDRESS_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB + PHONE_DESC_BOB,
                new AddCommand(new PersonBuilder(BOB).withOutstandingAmount("20")
                        .withTags("a-level", "needs revision").build()));
    }

    @Test
    public void parse_invalidOrBlankTag_failure() {
        String[] tags = {" t/", " t/   ", " t/ t/A-Level", " t/A-Level t/", " t/invalid!", " t/A_Level"};
        for (String tagInput : tags) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                    + tagInput, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingRequiredField_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStartDate_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + INVALID_START_DATE_DESC,
                StartDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedStartDate_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                        + START_DATE_DESC_BOB + START_DATE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_START_DATE));
    }

    @Test
    public void parse_invalidOutstandingAmount_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + INVALID_OUTSTANDING_AMOUNT_DESC,
                OutstandingAmount.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedOutstandingAmount_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + OUTSTANDING_AMOUNT_DESC_BOB + OUTSTANDING_AMOUNT_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_OUTSTANDING_AMOUNT));
    }
}
