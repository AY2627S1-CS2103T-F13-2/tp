package seedu.address.commons.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/** Formats address-book contacts as CSV text. */
public final class CsvExporter {

    public static final String HEADER = "Name,Phone,Address,Start Date,Outstanding Amount,Subjects,Tags,Notes,"
            + "Archived";

    private CsvExporter() {
        // Prevent instantiation.
    }

    /** Returns the supplied contacts formatted as CSV text. */
    public static String format(List<Person> persons) {
        String rows = persons.stream()
                .map(CsvExporter::formatPerson)
                .collect(Collectors.joining("\n"));
        return rows.isEmpty() ? HEADER + "\n" : HEADER + "\n" + rows + "\n";
    }

    private static String formatPerson(Person person) {
        return Stream.of(
                person.getName().toString(),
                person.getPhone().toString(),
                person.getAddress().toString(),
                person.getStartDate().toString(),
                person.getOutstandingAmount().toPlainString(),
                person.getSubjects().stream().map(Object::toString).collect(Collectors.joining("; ")),
                person.getTags().stream().map(Tag::toString).map(value -> value.substring(1, value.length() - 1))
                        .sorted().collect(Collectors.joining("; ")),
                person.getNotes().toString(),
                Boolean.toString(person.isArchived()))
                .map(CsvExporter::escape)
                .collect(Collectors.joining(","));
    }

    private static String escape(String value) {
        boolean containsSpecialCharacter = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        return containsSpecialCharacter
                ? "\"" + value.replace("\"", "\"\"") + "\""
                : value;
    }
}
