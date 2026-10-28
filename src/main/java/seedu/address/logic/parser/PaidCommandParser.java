package seedu.address.logic.parser;

import seedu.address.logic.commands.PaidCommand;
import seedu.address.logic.parser.BalanceCommandParser.ParsedBalanceArguments;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code paid} command arguments. */
public class PaidCommandParser implements Parser<PaidCommand> {
    @Override
    public PaidCommand parse(String args) throws ParseException {
        ParsedBalanceArguments parsed = BalanceCommandParser.parse(args, PaidCommand.MESSAGE_USAGE, false);
        return new PaidCommand(parsed.index(), parsed.amount());
    }
}
