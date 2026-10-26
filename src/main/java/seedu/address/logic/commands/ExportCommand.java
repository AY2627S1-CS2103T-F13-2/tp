package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import seedu.address.commons.util.CsvExporter;
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
        this(() -> generateOutputPath(LocalDateTime.now(), Path.of(".")));
    }

    ExportCommand(Supplier<Path> outputPathSupplier) {
        this.outputPathSupplier = outputPathSupplier;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path outputPath = outputPathSupplier.get();
        String csv = CsvExporter.format(model.getAddressBook().getPersonList());
        int suffix = 0;
        while (true) {
            try (OutputStream outputStream = Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW)) {
                outputStream.write(csv.getBytes(StandardCharsets.UTF_8));
                break;
            } catch (FileAlreadyExistsException exception) {
                suffix++;
                outputPath = outputPath.resolveSibling(addSuffix(outputPath.getFileName().toString(), suffix));
            } catch (IOException exception) {
                throw new CommandException(String.format(MESSAGE_FAILURE, exception.getMessage()), exception);
            }
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getAddressBook().getPersonList().size(), outputPath.toAbsolutePath()),
                false, false, CommandResult.ListPresentation.UNCHANGED);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExportCommand;
    }

    static Path generateOutputPath(LocalDateTime dateTime, Path directory) {
        String timestamp = dateTime.format(FILE_TIMESTAMP);
        return directory.resolve(FILE_PREFIX + timestamp + FILE_SUFFIX);
    }

    private static String addSuffix(String fileName, int suffix) {
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart < 0) {
            return fileName + "-" + suffix;
        }
        return fileName.substring(0, extensionStart) + "-" + suffix + fileName.substring(extensionStart);
    }
}
