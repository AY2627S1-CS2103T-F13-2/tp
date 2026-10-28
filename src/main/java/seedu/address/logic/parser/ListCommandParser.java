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
import seedu.address.model.tag.Tag;

/** Parses plain lists, summary lists, and optional repeated subject and tag filters. */
public class ListCommandParser implements Parser<ListCommand> {
    public static final String MESSAGE_INVALID_PARAMETERS = "Invalid command format! Use list, list -summary, "
                + "list [s/SUBJECT]... [t/TAG]..., list -archived, or list -subjects. "
                + "Use -archived and -subjects on their own.";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");
    private static final Pattern EXCLUSIVE_FLAG_PATTERN = Pattern.compile("(?:^|\\s)-(?:archived|subjects)(?=\\s|$)");
    // Recognise a complete option before another option or at the end, not text inside a filter value.
    private static final Pattern SUMMARY_FLAG_PATTERN = Pattern.compile(
            "(?<!\\S)-summary(?=\\s+(?:[a-zA-Z][a-zA-Z0-9_-]*/|-summary(?:\\s|$))|\\s*$)\\s*");

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
        if (trimmed.equals(ListCommand.SUBJECTS_FLAG)) {
            return ListCommand.forSubjectGroups();
        }
        if (EXCLUSIVE_FLAG_PATTERN.matcher(trimmed).find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Matcher summaryFlag = SUMMARY_FLAG_PATTERN.matcher(trimmed);
        boolean isSummary = summaryFlag.find();
        if (summaryFlag.find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        String filterArgs = summaryFlag.replaceFirst(" ").trim();
        if (filterArgs.isEmpty()) {
            return new ListCommand(isSummary);
        }

        return parseFilters(filterArgs, isSummary);
    }

    /** Validates both categories before constructing their combined filter. */
    private ListCommand parseFilters(String filterArgs, boolean isSummary) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(filterArgs);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            if (!PREFIX_SUBJECT.toString().equals(prefix) && !PREFIX_TAG.toString().equals(prefix)) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
        }
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + filterArgs, PREFIX_SUBJECT, PREFIX_TAG);
        if (!values.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Set<String> subjectNames = ParserUtil.parseSubjectNames(values.getAllValues(PREFIX_SUBJECT));
        Set<Tag> tags = ParserUtil.parseTags(values.getAllValues(PREFIX_TAG));
        return tags.isEmpty() ? new ListCommand(subjectNames, isSummary)
                : new ListCommand(new PersonHasAnyTagPredicate(tags), subjectNames, isSummary);
    }
}
