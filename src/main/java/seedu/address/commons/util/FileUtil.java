package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     * @throws NullPointerException if {@code file} is null.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Reads all bytes from {@code file} and decodes them as UTF-8.
     *
     * @throws IOException if the file cannot be read.
     * @throws NullPointerException if {@code file} is null.
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes {@code content} to {@code file} as UTF-8, replacing any existing content.
     * The file must already have an existing parent directory.
     *
     * @throws IOException if the file cannot be opened or written.
     * @throws NullPointerException if {@code file} or {@code content} is null.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

}
