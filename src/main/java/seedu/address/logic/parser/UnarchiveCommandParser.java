package seedu.address.logic.parser;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses unarchive command arguments. */
public class UnarchiveCommandParser implements Parser<UnarchiveCommand> {
    @Override
    public UnarchiveCommand parse(String args) throws ParseException {
        String value = args.trim();
        if (value.isEmpty() || value.matches(".*\\s+.*")) {
            throw new ParseException(UnarchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }
        try {
            return new UnarchiveCommand(ParserUtil.parseIndex(value));
        } catch (ParseException pe) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX, pe);
        }
    }
}
