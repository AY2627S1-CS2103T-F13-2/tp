package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.StartDate;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allRequiredFieldsPresent_success() {
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + START_DATE_DESC_BOB,
                new AddCommand(BOB));
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
}
