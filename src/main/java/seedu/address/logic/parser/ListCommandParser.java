package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Subject;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    public static final String MESSAGE_INVALID_PARAMETERS =
            "Invalid command format! Use list or list s/SUBJECT [s/SUBJECT]...";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * @throws ParseException if arguments are not repeated subject values
     */
    public ListCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new ListCommand();
        }
        Matcher matcher = PREFIX_PATTERN.matcher(args.trim());
        while (matcher.find()) {
            if (!PREFIX_SUBJECT.toString().equals(matcher.group(1))) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
        }
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT);
        if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_SUBJECT).isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Set<Subject> subjects = ParserUtil.parseSubjects(values.getAllValues(PREFIX_SUBJECT));
        return new ListCommand(subjects);
    }

}
