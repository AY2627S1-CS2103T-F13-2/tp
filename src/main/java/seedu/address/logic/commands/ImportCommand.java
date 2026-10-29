package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PERSONS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
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
            + "notes, outstanding amount or archive status. Use edit to change the stored student, or remove this row.";
    public static final String MESSAGE_MORE_ERRORS = "...and %1$d more.";
    public static final String MESSAGE_NOTHING_NEW = "No new students to import. All %1$d student(s) in %2$s "
            + "are already in TuteeDex.";
    public static final String MESSAGE_PREVIEW = "Ready to import %1$d student(s) from %2$s: %3$s.";
    public static final String MESSAGE_SKIPPED = "%1$d student(s) already in TuteeDex will be skipped: %2$s.";
    public static final String MESSAGE_CONFIRM_HINT = "Nothing has been imported yet. To import them, enter: "
            + COMMAND_WORD + " %1$s " + CONFIRM_WORD;
    public static final String MESSAGE_SUCCESS = "Imported %1$d student(s) from %2$s.";

    public static final String MESSAGE_OUTSIDE_HOME_FOLDER = "For safety, TuteeDex can only import files inside "
            + "the folder it runs from (%1$s). Move the file into that folder and try again.";

    static final int MAX_ITEMS_SHOWN = 10;

    private static final Logger logger = LogsCenter.getLogger(ImportCommand.class);

    private final Path filePath;
    private final boolean isConfirmed;
    private final Path homeFolder;

    /**
     * Creates an ImportCommand for the CSV file at {@code filePath}, relative to the folder TuteeDex runs from.
     * The file is only previewed unless {@code isConfirmed} is true.
     */
    public ImportCommand(Path filePath, boolean isConfirmed) {
        this(filePath, isConfirmed, Path.of(""));
    }

    /**
     * Creates an ImportCommand that only reads files inside {@code homeFolder}.
     */
    ImportCommand(Path filePath, boolean isConfirmed, Path homeFolder) {
        requireNonNull(filePath);
        requireNonNull(homeFolder);
        this.filePath = filePath;
        this.isConfirmed = isConfirmed;
        this.homeFolder = homeFolder.toAbsolutePath().normalize();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<String> errors = new ArrayList<>();
        List<StudentRow> studentRows = parseStudentRows(readRows(), errors);

        List<Person> newStudents = new ArrayList<>();
        List<Person> storedStudents = new ArrayList<>();
        sortByStoredStudents(model, studentRows, newStudents, storedStudents, errors);

        if (!errors.isEmpty()) {
            logger.info(String.format("Rejected import of %s with %d invalid row(s)", filePath, errors.size()));
            throw new CommandException(formatErrors(errors));
        }
        if (studentRows.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_STUDENTS, filePath));
        }
        assert newStudents.size() + storedStudents.size() == studentRows.size();

        if (newStudents.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NOTHING_NEW, storedStudents.size(), filePath));
        }
        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_PREVIEW, newStudents.size(), filePath,
                    formatNames(newStudents)) + formatSkipped(storedStudents) + "\n"
                    + String.format(MESSAGE_CONFIRM_HINT, filePath));
        }

        addAll(model, newStudents);
        logger.info(String.format("Imported %d student(s) from %s", newStudents.size(), filePath));
        return new CommandResult(String.format(MESSAGE_SUCCESS, newStudents.size(), filePath)
                + formatSkipped(storedStudents));
    }

    /**
     * Returns the students in the data rows, each with its row number.
     * Invalid rows, and rows for the same student as an earlier row but with different details, are added to
     * {@code errors}. An exact copy of an earlier row is skipped.
     *
     * @throws CommandException if the first row is not the export header.
     */
    private static List<StudentRow> parseStudentRows(List<List<String>> rows, List<String> errors)
            throws CommandException {
        if (rows.isEmpty() || !isHeader(rows.get(0))) {
            throw new CommandException(MESSAGE_WRONG_HEADER);
        }

        List<StudentRow> studentRows = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            int rowNumber = i + 1;
            if (CsvReader.isBlankRow(rows.get(i))) {
                continue;
            }
            try {
                Person student = CsvPersonParser.parsePerson(rows.get(i));
                StudentRow earlierRow = findSameStudent(student, studentRows);
                if (earlierRow == null) {
                    studentRows.add(new StudentRow(student, rowNumber));
                } else if (!earlierRow.student.equals(student)) {
                    errors.add(String.format(MESSAGE_ROW_ERROR, rowNumber,
                            String.format(MESSAGE_DUPLICATE_IN_FILE, earlierRow.rowNumber)));
                }
            } catch (ParseException e) {
                errors.add(String.format(MESSAGE_ROW_ERROR, rowNumber, e.getMessage()));
            }
        }
        return studentRows;
    }

    /**
     * Puts each student into {@code newStudents} or {@code storedStudents}.
     * A stored student with the same identity but different details is added to {@code errors} instead,
     * because the model allows only one such student.
     */
    private static void sortByStoredStudents(Model model, List<StudentRow> studentRows, List<Person> newStudents,
            List<Person> storedStudents, List<String> errors) {
        for (StudentRow studentRow : studentRows) {
            Person storedStudent = findStoredStudent(model, studentRow.student);
            if (storedStudent == null) {
                newStudents.add(studentRow.student);
            } else if (storedStudent.equals(studentRow.student)) {
                // An exact copy of a stored student is skipped, so the same file can be imported again safely.
                storedStudents.add(studentRow.student);
            } else {
                errors.add(String.format(MESSAGE_ROW_ERROR, studentRow.rowNumber,
                        String.format(MESSAGE_CONFLICT, studentRow.student.getName())));
            }
        }
    }

    /**
     * Adds every student to a copy of the address book first, so the stored data changes only once.
     */
    private static void addAll(Model model, List<Person> newStudents) {
        AddressBook updatedAddressBook = new AddressBook(model.getAddressBook());
        newStudents.forEach(updatedAddressBook::addPerson);
        model.setAddressBook(updatedAddressBook);
        model.updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
    }

    /**
     * Reads the file and splits it into CSV rows.
     *
     * @throws CommandException if the file is outside the home folder, missing, not a CSV file, unreadable
     *     or badly quoted.
     */
    private List<List<String>> readRows() throws CommandException {
        Path resolvedPath = getPathInsideHomeFolder();
        if (!Files.isRegularFile(resolvedPath)) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, resolvedPath));
        }
        if (!resolvedPath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new CommandException(MESSAGE_NOT_CSV);
        }

        String text;
        try {
            if (!resolvedPath.toRealPath().startsWith(homeFolder.toRealPath())) {
                // The path is a link that leads out of the home folder.
                throw new CommandException(String.format(MESSAGE_OUTSIDE_HOME_FOLDER, homeFolder));
            }
            text = Files.readString(resolvedPath);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_READ_FAILURE, filePath), e);
        }

        try {
            return CsvReader.read(text);
        } catch (IllegalValueException e) {
            throw new CommandException(formatErrors(List.of(e.getMessage())));
        }
    }

    /**
     * Returns the file path resolved against the home folder.
     *
     * @throws CommandException if the path leads outside the home folder, as TuteeDex may not read files there.
     */
    private Path getPathInsideHomeFolder() throws CommandException {
        Path resolvedPath = homeFolder.resolve(filePath).normalize();
        if (!resolvedPath.startsWith(homeFolder)) {
            throw new CommandException(String.format(MESSAGE_OUTSIDE_HOME_FOLDER, homeFolder));
        }
        return resolvedPath;
    }

    private static boolean isHeader(List<String> row) {
        String header = row.stream().map(String::trim).collect(Collectors.joining(","));
        return header.equalsIgnoreCase(CsvExporter.HEADER);
    }

    /**
     * Returns the stored student with the same identity as {@code student}, or null if there is none.
     */
    private static Person findStoredStudent(Model model, Person student) {
        return model.getAddressBook().getPersonList().stream()
                .filter(storedStudent -> storedStudent.isSamePerson(student))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the earlier row in the file for a student with the same identity, or null if there is none.
     */
    private static StudentRow findSameStudent(Person student, List<StudentRow> earlierRows) {
        return earlierRows.stream()
                .filter(earlierRow -> earlierRow.student.isSamePerson(student))
                .findFirst()
                .orElse(null);
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
                && isConfirmed == otherCommand.isConfirmed
                && homeFolder.equals(otherCommand.homeFolder);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("filePath", filePath)
                .add("isConfirmed", isConfirmed)
                .toString();
    }

    /**
     * A student read from the file, with the spreadsheet row number it came from.
     */
    private static class StudentRow {
        private final Person student;
        private final int rowNumber;

        StudentRow(Person student, int rowNumber) {
            this.student = student;
            this.rowNumber = rowNumber;
        }
    }
}
