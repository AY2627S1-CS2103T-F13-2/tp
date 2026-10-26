package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class UserPrefsTest {

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void setLastViewCommand_nullCommand_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setLastViewCommand(null));
    }

    @Test
    public void lastViewCommand_defaultAndCopy_keptInEquality() {
        UserPrefs userPref = new UserPrefs();
        assertEquals("", userPref.getLastViewCommand());

        userPref.setLastViewCommand("list -subjects");
        UserPrefs copy = new UserPrefs(userPref);
        assertEquals("list -subjects", copy.getLastViewCommand());
        assertEquals(userPref, copy);
        assertEquals(userPref.hashCode(), copy.hashCode());

        copy.setLastViewCommand("find Alice");
        assertNotEquals(userPref, copy);
    }

    @Test
    public void lastSortCommand_defaultAndCopy_keptInEquality() {
        UserPrefs userPref = new UserPrefs();
        assertEquals("", userPref.getLastSortCommand());
        assertThrows(NullPointerException.class, () -> userPref.setLastSortCommand(null));

        userPref.setLastSortCommand("sort o/desc n/asc");
        UserPrefs copy = new UserPrefs(userPref);
        assertEquals("sort o/desc n/asc", copy.getLastSortCommand());
        assertEquals(userPref, copy);

        copy.setLastSortCommand("");
        assertNotEquals(userPref, copy);
    }

}
