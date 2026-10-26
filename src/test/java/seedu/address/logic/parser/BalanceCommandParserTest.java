package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.commands.PaidCommand;
import seedu.address.model.person.OutstandingAmount;

public class BalanceCommandParserTest {
    private final PaidCommandParser paidParser = new PaidCommandParser();
    private final ChargeCommandParser chargeParser = new ChargeCommandParser();

    @Test
    public void parsePaid_validInputs_success() {
        assertParseSuccess(paidParser, "1", new PaidCommand(INDEX_FIRST_PERSON, null));
        assertParseSuccess(paidParser, "1 20.50",
                new PaidCommand(INDEX_FIRST_PERSON, new OutstandingAmount("20.50")));
    }

    @Test
    public void parseCharge_validInput_success() {
        assertParseSuccess(chargeParser, "1 20.50",
                new ChargeCommand(INDEX_FIRST_PERSON, new OutstandingAmount("20.50")));
    }

    @Test
    public void parse_invalidInputs_failure() {
        assertParseFailure(paidParser, "", String.format(BalanceCommand.MESSAGE_INVALID_FORMAT,
                PaidCommand.MESSAGE_USAGE));
        assertParseFailure(paidParser, "1 2 3",
                String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, PaidCommand.MESSAGE_USAGE));
        assertParseFailure(chargeParser, "1", String.format(BalanceCommand.MESSAGE_INVALID_FORMAT,
                ChargeCommand.MESSAGE_USAGE));
        assertParseFailure(chargeParser, "1 0", "Amount must be greater than 0.");
        assertParseFailure(chargeParser, "1 -1", OutstandingAmount.MESSAGE_CONSTRAINTS);
        assertParseFailure(paidParser, "0", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(paidParser, "1 1 1",
                String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, PaidCommand.MESSAGE_USAGE));
        assertParseFailure(chargeParser, "1 1 1",
                String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, ChargeCommand.MESSAGE_USAGE));
    }
}
