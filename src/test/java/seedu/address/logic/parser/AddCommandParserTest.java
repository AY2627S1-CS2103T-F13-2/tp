package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_OUTSTANDING_AMOUNT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.OUTSTANDING_AMOUNT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
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
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String SUBJECT = " s/Maths";
    private final AddCommandParser parser = new AddCommandParser();

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
    public void parse_repeatedSubjects_caseInsensitiveDuplicatesCombined() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                        + " s/Maths s/maths s/H2 Chemistry",
                new AddCommand(new PersonBuilder(BOB).withSubjects("Maths", "H2 Chemistry").build()));
    }

    @Test
    public void parse_blankSubjectAmongValidSubjects_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                + " s/Maths s/", "Subject must not be blank.");
    }

    @Test
    public void parse_commaSeparatedSubjects_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB
                + " s/Maths, Physics", seedu.address.model.person.Subject.MESSAGE_CONSTRAINTS);
    }
}
