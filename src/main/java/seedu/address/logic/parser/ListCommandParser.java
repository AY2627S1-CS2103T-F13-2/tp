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
     * @throws ParseException if any arguments are given, since the list command takes none
     */
    public ListCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        return new ListCommand();
    }

}
