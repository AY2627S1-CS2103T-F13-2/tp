package seedu.address.logic.parser;

import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses unarchive command arguments. */
public class UnarchiveCommandParser implements Parser<UnarchiveCommand> {
    @Override
    public UnarchiveCommand parse(String args) throws ParseException {
        String value = args.trim();
        if (!value.matches("s/[A-Za-z][A-Za-z ]*")) {
            throw new ParseException(UnarchiveCommand.MESSAGE_USAGE);
        }
        return new UnarchiveCommand(value.substring(2));
    }
}
