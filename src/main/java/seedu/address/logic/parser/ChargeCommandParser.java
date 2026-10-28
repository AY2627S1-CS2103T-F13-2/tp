package seedu.address.logic.parser;

import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.parser.BalanceCommandParser.ParsedBalanceArguments;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code charge} command arguments. */
public class ChargeCommandParser implements Parser<ChargeCommand> {
    @Override
    public ChargeCommand parse(String args) throws ParseException {
        ParsedBalanceArguments parsed = BalanceCommandParser.parse(args, ChargeCommand.MESSAGE_USAGE, true);
        return new ChargeCommand(parsed.index(), parsed.amount());
    }
}
