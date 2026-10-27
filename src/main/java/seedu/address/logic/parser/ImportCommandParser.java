package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ImportCommand object.
 */
public class ImportCommandParser implements Parser<ImportCommand> {

    public static final String MESSAGE_INVALID_PATH = "%1$s is not a valid file path.";

    /** A path wrapped in a pair of double quotes, such as {@code "C:\My Files\students.csv"}. */
    private static final String QUOTED_PATH_PATTERN = "\".*\"";

    /**
     * Parses the given {@code String} of arguments in the context of the ImportCommand
     * and returns an ImportCommand object for execution.
     * The file path may contain spaces and may be wrapped in double quotes, as Windows copies paths that way.
     * A final word {@code confirm} confirms the import.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public ImportCommand parse(String args) throws ParseException {
        String pathText = args.trim();
        boolean isConfirmed = pathText.endsWith(" " + ImportCommand.CONFIRM_WORD);
        if (isConfirmed) {
            pathText = pathText.substring(0, pathText.length() - ImportCommand.CONFIRM_WORD.length()).trim();
        }
        if (pathText.matches(QUOTED_PATH_PATTERN)) {
            pathText = pathText.substring(1, pathText.length() - 1).trim();
        }
        if (pathText.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
        }

        try {
            return new ImportCommand(Path.of(pathText), isConfirmed);
        } catch (InvalidPathException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_PATH, pathText), e);
        }
    }
}
