package seedu.address.logic.parser;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ArchiveCommandParser implements Parser<ArchiveCommand> {
    @Override
    public ArchiveCommand parse(String args) throws ParseException {
        String value = args.trim();
        if (!value.matches("s/[A-Za-z][A-Za-z ]*")) {
            throw new ParseException(ArchiveCommand.MESSAGE_USAGE);
        }
        return new ArchiveCommand(value.substring(2));
    }
}
