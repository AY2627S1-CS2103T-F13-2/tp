package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new ListCommand object.
 */
public class ListCommandParser implements Parser<ListCommand> {

    public static final String MESSAGE_INVALID_PARAMETERS =
            "Invalid command format! Use list or list t/TAG [t/TAG]...";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");

    /**
     * Parses the given {@code String} of arguments in the context of the ListCommand
     * and returns a ListCommand object for execution.
     * @throws ParseException if arguments are not valid repeated tag values
     */
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new ListCommand();
        }
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            if (!PREFIX_TAG.toString().equals(matcher.group(1))) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
        }
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_TAG);
        if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_TAG).isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Set<Tag> tags = ParserUtil.parseTags(values.getAllValues(PREFIX_TAG));
        return new ListCommand(new PersonHasAnyTagPredicate(tags));
    }

}
