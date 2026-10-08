package seedu.address.logic.parser;

import java.math.BigInteger;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty() || trimmedArgs.matches(".*\\s+.*")) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        if (!trimmedArgs.matches("[0-9]+")) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX);
        }

        BigInteger oneBasedIndex = new BigInteger(trimmedArgs);
        if (oneBasedIndex.signum() == 0) {
            throw new ParseException(DeleteCommand.MESSAGE_INVALID_INDEX);
        }

        // Any larger positive value is necessarily outside a Java list's possible index range.
        int indexValue = oneBasedIndex.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                ? Integer.MAX_VALUE : oneBasedIndex.intValue();
        return new DeleteCommand(Index.fromOneBased(indexValue));
    }

}
