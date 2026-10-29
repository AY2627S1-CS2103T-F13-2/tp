package seedu.address.logic.parser;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses archive command arguments. */
public class ArchiveCommandParser implements Parser<ArchiveCommand> {
    @Override
    public ArchiveCommand parse(String args) throws ParseException {
        String value = args.trim();
        if (value.isEmpty() || value.matches(".*\\s+.*")) {
            throw new ParseException(ArchiveCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }
        try {
            return new ArchiveCommand(ParserUtil.parseIndex(value));
        } catch (ParseException pe) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX, pe);
        }
    }
}
