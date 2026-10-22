package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/** Parses plain lists, summary lists, and repeated subject or tag filters. */
public class ListCommandParser implements Parser<ListCommand> {
    public static final String MESSAGE_INVALID_PARAMETERS = "Invalid command format! Use list, list -summary, "
            + "list s/SUBJECT [s/SUBJECT]..., list t/TAG [t/TAG]..., or list -archived. "
            + "Use -archived on its own.";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");
    private static final Pattern ARCHIVED_FLAG_PATTERN = Pattern.compile("(?:^|\\s)-archived(?:\\s|$)");

    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            return new ListCommand();
        }
        if (trimmed.equals(ListCommand.ARCHIVED_FLAG)) {
            return ListCommand.forArchivedStudents();
        }
        if (ARCHIVED_FLAG_PATTERN.matcher(trimmed).find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        boolean isSummary = trimmed.equals(ListCommand.SUMMARY_FLAG);
        String filterArgs = isSummary ? "" : trimmed;
        if (filterArgs.startsWith(ListCommand.SUMMARY_FLAG + " ")) {
            isSummary = true;
            filterArgs = filterArgs.substring(ListCommand.SUMMARY_FLAG.length()).trim();
        }
        if (filterArgs.contains(ListCommand.SUMMARY_FLAG)) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        if (filterArgs.isEmpty()) {
            return new ListCommand(isSummary);
        }

        String filterPrefix = null;
        Matcher matcher = PREFIX_PATTERN.matcher(filterArgs);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            if (!PREFIX_SUBJECT.toString().equals(prefix) && !PREFIX_TAG.toString().equals(prefix)) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            if (filterPrefix != null && !filterPrefix.equals(prefix)) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            filterPrefix = prefix;
        }
        if (PREFIX_SUBJECT.toString().equals(filterPrefix)) {
            ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + filterArgs, PREFIX_SUBJECT);
            if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_SUBJECT).isEmpty()) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            Set<Subject> subjects = ParserUtil.parseSubjects(values.getAllValues(PREFIX_SUBJECT));
            return new ListCommand(subjects, isSummary);
        }
        if (PREFIX_TAG.toString().equals(filterPrefix)) {
            ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + filterArgs, PREFIX_TAG);
            if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_TAG).isEmpty()) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            Set<Tag> tags = ParserUtil.parseTags(values.getAllValues(PREFIX_TAG));
            return new ListCommand(new PersonHasAnyTagPredicate(tags), isSummary);
        }
        throw new ParseException(MESSAGE_INVALID_PARAMETERS);
    }
}
