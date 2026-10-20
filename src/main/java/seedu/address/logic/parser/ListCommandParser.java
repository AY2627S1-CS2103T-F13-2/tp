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

/** Parses plain lists, summary lists, and repeated tag filters. */
public class ListCommandParser implements Parser<ListCommand> {
    public static final String MESSAGE_INVALID_PARAMETERS =
            "Invalid command format! Use list, list -summary, or list t/TAG [t/TAG]...";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");

    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmed = args.trim();
        if (trimmed.isEmpty()) return new ListCommand();
        boolean isSummary = trimmed.equals(ListCommand.SUMMARY_FLAG);
        String tagArgs = isSummary ? "" : trimmed;
        if (tagArgs.startsWith(ListCommand.SUMMARY_FLAG + " ")) {
            isSummary = true;
            tagArgs = tagArgs.substring(ListCommand.SUMMARY_FLAG.length()).trim();
        }
        if (tagArgs.contains(ListCommand.SUMMARY_FLAG)) throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        if (tagArgs.isEmpty()) return new ListCommand(isSummary);
        Matcher matcher = PREFIX_PATTERN.matcher(tagArgs);
        while (matcher.find()) {
            if (!PREFIX_TAG.toString().equals(matcher.group(1))) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
        }
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + tagArgs, PREFIX_TAG);
        if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_TAG).isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Set<Tag> tags = ParserUtil.parseTags(values.getAllValues(PREFIX_TAG));
        return new ListCommand(new PersonHasAnyTagPredicate(tags), isSummary);
    }
}
