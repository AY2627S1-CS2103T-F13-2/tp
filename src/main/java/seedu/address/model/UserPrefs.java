package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.core.GuiSettings;

/**
 * Represents User's preferences.
 */
public class UserPrefs implements ReadOnlyUserPrefs {

    private GuiSettings guiSettings = new GuiSettings();
    private String lastViewCommand = "";
    private String lastSortCommand = "";

    /**
     * Creates a {@code UserPrefs} with default values.
     */
    public UserPrefs() {}

    /**
     * Creates a {@code UserPrefs} with the prefs in {@code userPrefs}.
     */
    public UserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        setGuiSettings(userPrefs.getGuiSettings());
        setLastViewCommand(userPrefs.getLastViewCommand());
        setLastSortCommand(userPrefs.getLastSortCommand());
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        this.guiSettings = guiSettings;
    }

    public String getLastViewCommand() {
        return lastViewCommand;
    }

    public void setLastViewCommand(String lastViewCommand) {
        requireNonNull(lastViewCommand);
        this.lastViewCommand = lastViewCommand;
    }

    public String getLastSortCommand() {
        return lastSortCommand;
    }

    public void setLastSortCommand(String lastSortCommand) {
        requireNonNull(lastSortCommand);
        this.lastSortCommand = lastSortCommand;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UserPrefs otherUserPrefs)) {
            return false;
        }

        return guiSettings.equals(otherUserPrefs.guiSettings)
                && lastViewCommand.equals(otherUserPrefs.lastViewCommand)
                && lastSortCommand.equals(otherUserPrefs.lastSortCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiSettings, lastViewCommand, lastSortCommand);
    }

    @Override
    public String toString() {
        return "Gui Settings : " + guiSettings + ", Last view command : " + lastViewCommand
                + ", Last sort command : " + lastSortCommand;
    }

}
