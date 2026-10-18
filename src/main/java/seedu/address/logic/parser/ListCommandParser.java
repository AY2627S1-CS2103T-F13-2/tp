package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    public static final String MESSAGE_INVALID_PARAMETERS =
            "Invalid command format! List command should not take any parameters!";

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * @throws ParseException if the arguments are not blank or the summary flag
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new ListCommand();
        }
        if (args.trim().equals(ListCommand.SUMMARY_FLAG)) {
            return new ListCommand(true);
        }
        throw new ParseException(MESSAGE_INVALID_PARAMETERS);
    }

}
