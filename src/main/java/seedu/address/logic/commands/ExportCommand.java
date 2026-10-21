package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.stream.Stream;

import seedu.address.commons.util.CsvExporter;
import seedu.address.commons.util.FileUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Exports all contacts to a timestamped CSV file. */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";
    public static final String MESSAGE_USAGE = "Usage: export";
    public static final String MESSAGE_SUCCESS = "Exported %d contact(s) to %s.";
    public static final String MESSAGE_FAILURE = "Could not export contacts: %s";

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final String FILE_PREFIX = "contacts-";
    private static final String FILE_SUFFIX = ".csv";
    private final Supplier<Path> outputPathSupplier;

    /** Creates an export command that generates a timestamped output path. */
    public ExportCommand() {
        this(ExportCommand::generateOutputPath);
    }

    ExportCommand(Supplier<Path> outputPathSupplier) {
        this.outputPathSupplier = outputPathSupplier;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path outputPath = outputPathSupplier.get();
        String csv = CsvExporter.format(model.getAddressBook().getPersonList());
        try {
            FileUtil.writeToFile(outputPath, csv);
        } catch (IOException exception) {
            throw new CommandException(String.format(MESSAGE_FAILURE, exception.getMessage()), exception);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getAddressBook().getPersonList().size(), outputPath.toAbsolutePath()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExportCommand;
    }

    private static Path generateOutputPath() {
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        Path basePath = Path.of(FILE_PREFIX + timestamp + FILE_SUFFIX);
        return Stream.iterate(0, index -> index + 1)
                .map(index -> index == 0 ? basePath : Path.of(FILE_PREFIX + timestamp + "-" + index + FILE_SUFFIX))
                .filter(path -> !Files.exists(path))
                .findFirst()
                .orElseThrow();
    }
}
