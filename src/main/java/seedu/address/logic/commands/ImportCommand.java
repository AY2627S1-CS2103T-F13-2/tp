package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.CsvExporter;
import seedu.address.commons.util.CsvReader;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CsvPersonParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Imports students from a CSV file in the format written by the export command.
 * Without confirmation, the command only checks the file and previews the result.
 * With confirmation, it adds every new student, or none of them if any row is invalid.
 */
public class ImportCommand extends Command {

    public static final String COMMAND_WORD = "import";
    public static final String CONFIRM_WORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Checks a CSV file in the format written by "
            + "export and shows which students would be imported. Add " + CONFIRM_WORD + " at the end to import "
            + "them.\n"
            + "Parameters: FILE_PATH [" + CONFIRM_WORD + "]\n"
            + "Example: " + COMMAND_WORD + " contacts.csv " + CONFIRM_WORD;

    public static final String MESSAGE_FILE_NOT_FOUND = "Could not find the file %1$s.";
    public static final String MESSAGE_NOT_CSV = "Only .csv files can be imported.";
    public static final String MESSAGE_READ_FAILURE = "Could not read %1$s as UTF-8 text.";
    public static final String MESSAGE_NO_STUDENTS = "%1$s has no student rows to import.";
    public static final String MESSAGE_WRONG_HEADER = "Row 1 must be the header written by export:\n"
            + CsvExporter.HEADER;
    public static final String MESSAGE_ERRORS = "Nothing was imported. Fix these rows in %1$s and try again:";
    public static final String MESSAGE_ROW_ERROR = "Row %1$d: %2$s";
    public static final String MESSAGE_DUPLICATE_IN_FILE = "Same student as row %1$d, but with different details.";
    public static final String MESSAGE_CONFLICT = "%1$s is already in TuteeDex with different subjects, tags, "
            + "notes or archive status. Use edit to change the stored student, or remove this row.";
    public static final String MESSAGE_MORE_ERRORS = "...and %1$d more.";
    public static final String MESSAGE_NOTHING_NEW = "No new students to import. All %1$d student(s) in %2$s "
            + "are already in TuteeDex.";
    public static final String MESSAGE_PREVIEW = "Ready to import %1$d student(s) from %2$s: %3$s.";
    public static final String MESSAGE_SKIPPED = "%1$d student(s) already in TuteeDex will be skipped: %2$s.";
    public static final String MESSAGE_CONFIRM_HINT = "Nothing has been imported yet. To import them, enter: "
            + COMMAND_WORD + " %1$s " + CONFIRM_WORD;
    public static final String MESSAGE_SUCCESS = "Imported %1$d student(s) from %2$s.";

    static final int MAX_ITEMS_SHOWN = 10;

    private final Path filePath;
    private final boolean isConfirmed;

    /**
     * Creates an ImportCommand for the CSV file at {@code filePath}.
     * The file is only previewed unless {@code isConfirmed} is true.
     */
    public ImportCommand(Path filePath, boolean isConfirmed) {
        requireNonNull(filePath);
        this.filePath = filePath;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<List<String>> rows = readRows();
        if (rows.isEmpty() || !isHeader(rows.get(0))) {
            throw new CommandException(MESSAGE_WRONG_HEADER);
        }

        List<String> errors = new ArrayList<>();
        List<Person> studentsInFile = new ArrayList<>();
        List<Integer> rowNumbersInFile = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            int rowNumber = i + 1;
            if (CsvReader.isBlankRow(rows.get(i))) {
                continue;
            }
            try {
                Person student = CsvPersonParser.parsePerson(rows.get(i));
                int earlierIndex = findSameStudent(student, studentsInFile);
                if (earlierIndex < 0) {
                    studentsInFile.add(student);
                    rowNumbersInFile.add(rowNumber);
                } else if (!studentsInFile.get(earlierIndex).equals(student)) {
                    errors.add(String.format(MESSAGE_ROW_ERROR, rowNumber,
                            String.format(MESSAGE_DUPLICATE_IN_FILE, rowNumbersInFile.get(earlierIndex))));
                }
                // An exact copy of an earlier row is skipped.
            } catch (ParseException e) {
                errors.add(String.format(MESSAGE_ROW_ERROR, rowNumber, e.getMessage()));
            }
        }

        List<Person> newStudents = new ArrayList<>();
        List<Person> existingStudents = new ArrayList<>();
        for (int i = 0; i < studentsInFile.size(); i++) {
            Person student = studentsInFile.get(i);
            Person storedStudent = findSameStudent(model, student);
            if (storedStudent == null) {
                newStudents.add(student);
            } else if (storedStudent.equals(student)) {
                // An exact copy of a stored student is skipped, so the same file can be imported again safely.
                existingStudents.add(student);
            } else {
                errors.add(String.format(MESSAGE_ROW_ERROR, rowNumbersInFile.get(i),
                        String.format(MESSAGE_CONFLICT, student.getName())));
            }
        }

        if (!errors.isEmpty()) {
            throw new CommandException(formatErrors(errors));
        }
        if (studentsInFile.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_STUDENTS, filePath));
        }
        if (newStudents.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NOTHING_NEW, existingStudents.size(), filePath));
        }

        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_PREVIEW, newStudents.size(), filePath,
                    formatNames(newStudents)) + formatSkipped(existingStudents) + "\n"
                    + String.format(MESSAGE_CONFIRM_HINT, filePath));
        }

        // Add every student to a copy first, so the address book changes only once and all at once.
        AddressBook updatedAddressBook = new AddressBook(model.getAddressBook());
        newStudents.forEach(updatedAddressBook::addPerson);
        model.setAddressBook(updatedAddressBook);
        model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, newStudents.size(), filePath)
                + formatSkipped(existingStudents));
    }

    /**
     * Reads the file and splits it into CSV rows.
     *
     * @throws CommandException if the file is missing, not a CSV file, unreadable or badly quoted.
     */
    private List<List<String>> readRows() throws CommandException {
        if (!Files.isRegularFile(filePath)) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, filePath.toAbsolutePath()));
        }
        if (!filePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new CommandException(MESSAGE_NOT_CSV);
        }

        String text;
        try {
            text = Files.readString(filePath);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_READ_FAILURE, filePath), e);
        }

        try {
            return CsvReader.read(text);
        } catch (IllegalValueException e) {
            throw new CommandException(formatErrors(List.of(e.getMessage())));
        }
    }

    private static boolean isHeader(List<String> row) {
        String header = row.stream().map(String::trim).collect(Collectors.joining(","));
        return header.equalsIgnoreCase(CsvExporter.HEADER);
    }

    /**
     * Returns the stored student with the same identity as {@code student}, or null if there is none.
     */
    private static Person findSameStudent(Model model, Person student) {
        return model.getAddressBook().getPersonList().stream()
                .filter(storedStudent -> storedStudent.isSamePerson(student))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the position of an earlier student in the file with the same identity, or -1 if there is none.
     */
    private static int findSameStudent(Person student, List<Person> earlierStudents) {
        for (int i = 0; i < earlierStudents.size(); i++) {
            if (earlierStudents.get(i).isSamePerson(student)) {
                return i;
            }
        }
        return -1;
    }

    private String formatErrors(List<String> errors) {
        StringBuilder message = new StringBuilder(String.format(MESSAGE_ERRORS, filePath));
        errors.stream().limit(MAX_ITEMS_SHOWN).forEach(error -> message.append("\n").append(error));
        if (errors.size() > MAX_ITEMS_SHOWN) {
            message.append("\n").append(String.format(MESSAGE_MORE_ERRORS, errors.size() - MAX_ITEMS_SHOWN));
        }
        return message.toString();
    }

    private static String formatSkipped(List<Person> existingStudents) {
        if (existingStudents.isEmpty()) {
            return "";
        }
        return "\n" + String.format(MESSAGE_SKIPPED, existingStudents.size(), formatNames(existingStudents));
    }

    /**
     * Returns up to {@code MAX_ITEMS_SHOWN} names separated by commas, followed by how many more there are.
     */
    static String formatNames(List<Person> students) {
        String names = students.stream().limit(MAX_ITEMS_SHOWN)
                .map(student -> student.getName().fullName)
                .collect(Collectors.joining(", "));
        if (students.size() > MAX_ITEMS_SHOWN) {
            names += " and " + (students.size() - MAX_ITEMS_SHOWN) + " more";
        }
        return names;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ImportCommand otherCommand)) {
            return false;
        }

        return filePath.equals(otherCommand.filePath)
                && isConfirmed == otherCommand.isConfirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filePath", filePath)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
