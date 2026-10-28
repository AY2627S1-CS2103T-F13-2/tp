package seedu.address.model;

import seedu.address.commons.core.GuiSettings;

/**
 * Unmodifiable view of user prefs.
 */
public interface ReadOnlyUserPrefs {

    GuiSettings getGuiSettings();

    /**
     * Returns the list or find command that set the displayed list when the app was last closed,
     * or an empty string if the normal list was shown.
     */
    String getLastViewCommand();

    /**
     * Returns the sort command that set the display order when the app was last closed,
     * or an empty string if the default order was used.
     */
    String getLastSortCommand();

}
