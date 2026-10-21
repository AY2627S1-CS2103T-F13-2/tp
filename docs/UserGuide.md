---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TuteeDex User Guide

TuteeDex is a **desktop application for private tutors to manage student profiles, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, TuteeDex can help you manage student information faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-F13-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TuteeDex.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar TuteeDex.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all active contacts.

   * `add n/John Doe p/98765432 a/John street, block 123, #01-01 d/10-Aug s/Maths o/20` : Adds a student profile named `John Doe` to TuteeDex.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit INDEX [d/START_DATE]` can be used as `edit 1 d/10-Aug` or as part of an edit containing other optional fields.

* Items followed by `...` can appear zero or more times.<br>
  This notation is used only when a command explicitly supports repeated values.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for `help` and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a student profile to TuteeDex.

Format: `add n/NAME a/ADDRESS p/PHONE_NUMBER d/START_DATE s/SUBJECT [s/SUBJECT]... [o/OUTSTANDING_AMOUNT] [t/TAG]... [notes/NOTE]`

<box type="tip" seamless>

**Note:** `START_DATE` must use the `DD-MMM` format, for example `10-Aug`. Calendar validity is not checked.

**Note:** `OUTSTANDING_AMOUNT` is optional and defaults to `0`. It must be a nonnegative number with no more than two decimal places. Do not include a currency symbol, sign, separator, or exponent.

**Note:** At least one `s/SUBJECT` is required for new profiles. Repeat `s/` for multiple subjects, for example `s/Maths s/H2 Chemistry`. Each subject must be 1–50 characters after trimming surrounding spaces and may contain English letters, digits, spaces, and hyphens, including at least one letter or digit. Subjects are compared without regard to case; duplicates are stored once, retaining the first spelling. Comma-separated subjects are not supported.

**Note:** `notes/NOTE` is optional and stores one free-text note per student.
Omitting it or supplying a blank value stores no note. Surrounding spaces are
trimmed; internal spaces, punctuation, and display case are preserved.
Specify `notes/` only once. Repeating it is an error and no profile is added.
As with other Add values, recognised field prefixes preceded by a space mark
the next field, rather than literal note text. Notes are saved automatically
and appear in `view INDEX`. Existing data without notes loads with empty notes.
</box>

Tags are optional descriptive labels, separate from subjects:

* Supply one `t/` prefix per tag, such as `t/A-Level t/Needs revision`. Omitting `t/` creates a profile with no tags.
* Tags accept letters (A-Z and a-z), digits, spaces, and hyphens. Surrounding spaces are trimmed, internal spaces are preserved, and tags are stored in lowercase.
* Repeated tags, including case variants such as `t/A-Level t/a-level`, are combined. Tag order does not matter.
* Every tag must be valid. Blank tags, including `t/A-Level t/`, reject the entire command with `Tags must contain only letters, digits, spaces, or hyphens and must not be blank.`

An add command is rejected as a duplicate when name, phone, address, start date,
and outstanding amount match an existing profile, even if subjects, tags, or notes differ.
Outstanding amounts are compared numerically, so `0`, `0.0`, and `0.00` are
equivalent. A duplicate produces `This contact already exists in TuteeDex` and
does not add another profile.

Examples:
* `add n/John Doe a/John street, block 123, #01-01 p/98765432 d/10-Aug s/Maths`
* `add n/Betsy Crowe a/Newgate Prison p/12345678 d/19-Sep o/600 s/H2 Chemistry s/Physics`
* `add n/Amy Bee a/Block 1 p/91234567 d/10-Aug s/Maths notes/Revise fractions`
* `add n/Peter Parker a/Block 9 p/91234567 d/10-Aug s/Physics t/A-Level t/Needs revision`

### Viewing a student profile: `view`

Format: `view INDEX`

Displays the name, phone, address, start date, outstanding amount, subjects, and notes
of the student at `INDEX` in the currently displayed list. A student with no
note has an empty `Notes:` value. The index must be a positive integer and
refer to a displayed student; otherwise, the command shows an error.
Viewing a profile does not change student data or the current list.

### Exporting contacts: `export`

Exports all student contacts, including archived contacts, to a UTF-8 CSV file in the application's working directory.

Format: `export`

The generated file is named `contacts-YYYYMMDD-HHMMSS-milliseconds.csv`. Each contact occupies one row. The CSV
contains the name, phone, address, start date, outstanding amount, subjects, tags, notes, and archive status. Multiple
subjects or tags are joined with `; `.

### Listing and filtering students: `list`

Shows active student profiles, or filters active profiles by one or more subjects or tags. Archived students are not shown.

Format: `list [-summary] [s/SUBJECT]...` or `list [-summary] [t/TAG]...`

* Plain `list` restores all students, including after a Find or filter.
* Supply one `s/` per subject. A student matches if they take **any** requested subject.
  Subject names match completely, ignoring case: `s/Maths` does not match `Additional Maths`.
  Subjects use the same validation as Add; equivalent repeated values are combined.
* Supply one `t/` per tag. A student matches if they have **any** requested tag (OR matching).
  Match complete tags, ignoring case and surrounding spaces: `t/Needs revision` matches
  `needs revision`, but `t/revision` does not. Tags use the same validation as Add;
  equivalent repeated values are combined. Profiles without tags do not match.
* Use either subject or tag prefixes in one command; mixing them is invalid.
* Each filter searches all stored students, rather than just the current search results.
  Results retain their stored order. Indexes used by `view`, `edit`, and `delete` refer
  to the newly displayed list.
* The status message reports the displayed count, for example `Listed 2 contacts.`
  or `Listed 1 contact.` A subject filter with no matches shows
  `No contacts match the specified subjects.`; a tag filter with no matches shows
  `No contacts match the specified tags.` Both leave the displayed list empty.
  Plain `list` on an empty address book shows `No contacts stored. Use the add command to add one.`
* Blank or invalid values, uppercase or unknown prefixes, and unprefixed arguments
  such as `list 3` are rejected. Invalid input leaves the displayed list and stored
  profiles unchanged. Listing never changes profile fields.
* Add `-summary` to display total and paid counts for the displayed students, for example
  `list -summary`, `list -summary s/Maths`, or `list -summary t/A-Level`.
  The flag must be entered exactly as shown.

Examples:
* `list s/Maths s/H2 Chemistry` shows students taking either subject.
* `list t/A-Level t/Needs revision` shows students having either tag.
* `find Nobody` followed by `list t/A-Level` still finds matching students in the full address book.
* `list` shows every student again.

### Editing a student profile: `edit`

Updates one or more fields of an existing student profile in TuteeDex.

Format: `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [d/START_DATE] [o/OUTSTANDING_AMOUNT] [s/SUBJECT]... [t/TAG]... [notes/NOTE]`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* After a search such as `find Betsy`, `edit 1 ...` edits the first search result.
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Fields that are not supplied remain unchanged, including the outstanding amount, tags, subjects, and notes.
* Supplied `s/SUBJECT` values replace the entire subject set. Repeat `s/` for multiple subjects.
  Subjects use Add's validation: 1-50 characters after trimming, containing only English letters, digits,
  spaces, or hyphens, with at least one letter or digit. Blank subjects, including a bare `s/`, are invalid.
  Equivalent subjects are combined without regard to case, retaining the first spelling and input order.
  Changing only subject case or order is a no-op and retains the existing spelling and order.
* Supplied `t/TAG` values replace the entire tag set. Repeat `t/` for multiple tags.
  Tags use Add's validation and lowercase normalization; equivalent repeated tags are combined.
  A single blank `t/` clears all tags. Mixing blank and nonblank tags, or repeating blank `t/`, is invalid.
* `notes/NOTE` replaces the note. A blank `notes/` clears it; omitting the prefix preserves the existing note.
  Surrounding whitespace is trimmed; internal spacing, punctuation, display case, and Unicode text are preserved.
  Specify `notes/` only once. The prefix rules below also apply inside notes: a recognized field prefix starts
  another field, while an unknown prefix-shaped token is rejected. URLs such as `https://example.com` remain note text.
* Field values use the same validation rules as `add`: names contain alphabetic characters and spaces, addresses are nonblank, phone numbers contain exactly eight digits, start dates use `DD-MMM`, and outstanding amounts are nonnegative numbers with at most two decimal places. Calendar validity is not checked.
* Use `o/0` to reset the outstanding amount to zero. Blank values for the five scalar fields, including `o/`, are invalid.
* Leading and trailing spaces around values are ignored. Tags are normalized to lowercase;
  other text case and internal spaces are preserved.
* The command word `edit` and all prefixes are lowercase and case-sensitive. Fields may appear in any order after the index.
* Each single-valued field, including notes, may appear only once in an edit command. Tags accept repeated `t/` prefixes,
  and subjects accept repeated `s/` prefixes.
* Unknown prefixes, including uppercase variants such as `P/`, are rejected. A letter-led token ending in `/` at the start of input or after whitespace is treated as a prefix even inside a value; a slash in `#02/03` remains part of the address.
* An edit is rejected as a duplicate only if name, phone, address, start date,
  and outstanding amount match another profile. Tags, subjects, and notes do not distinguish
  duplicates. Amounts are compared numerically, so `20`, `20.0`, and `20.00` are equivalent.
* If any supplied field is invalid or the edit creates a duplicate, no fields are changed and the displayed list remains unchanged.
* A changed profile produces `Edited contact: <updated contact name>`, refreshes its displayed values, and returns to the full contact list. The changes are saved automatically.
* If all supplied values match the existing values, the result is `No changes made to contact: <contact name>` and the current displayed list is preserved.

Errors:

| Input problem | Feedback |
| --- | --- |
| Missing or invalid index | `INDEX must be a positive integer! (e.g., 1, 2, 3, …)` |
| Index beyond the displayed list | `No matching contact found at the specified index.` |
| No field supplied, e.g. `edit 1` | `At least one field must be provided to edit.` |
| Repeated single-valued field | `Each contact field may be specified only once in an edit command.` |
| Unknown or uppercase prefix | `Invalid command format.` followed by Edit usage instructions |
| Invalid field value | The same field validation error as `add` |
| Duplicate profile | `This contact already exists in TuteeDex.` |

Examples:
* `edit 1 p/91234567 d/20-Oct` edits the phone number and start date of the first profile.
* `edit 2 n/Betsy Crower` edits the name of the second profile.
* `edit 1 o/600` changes the first profile's outstanding amount to SGD 600.00.
* `edit 1 o/0` resets the first profile's outstanding amount to zero.
* `edit 1 t/A-Level t/Needs revision` replaces the first profile's tags with these two tags.
* `edit 1 t/` clears the first profile's tags while keeping its other fields.
* `edit 1 s/Maths s/H2 Chemistry` replaces the first profile's subjects while keeping its other fields.
* `edit 1 notes/Revise fractions` replaces the first profile's note while keeping its other fields.
* `edit 1 notes/` clears the first profile's note.
* `find Betsy` followed by `edit 1 a/32, Clementi Road` updates only the address of the first search result.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a contact: `delete`

Permanently deletes the specified student contact from TuteeDex immediately, without a confirmation prompt. The displayed contact list refreshes and the remaining contacts are renumbered.

Format: `delete INDEX`

* `INDEX` is the one-based position in the currently displayed contact list, including search results.
* `INDEX` must be one positive integer, such as `1`, `2`, or `3`; no other parameters are accepted.
* A successful command displays `Deleted person: <contact details>`.
* If `INDEX` is invalid, the app explains that it must be a positive integer. If it is larger than the displayed list, the app reports that no matching contact was found.

Examples:
* `list` followed by `delete 2` deletes the 2nd contact in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st contact in the results of the `find` command.

### Archiving a student: `archive`

Archives a student without deleting the student profile. Archived students are hidden from `list` and are excluded from its summary counts, but their data remains saved in TuteeDex.

Format: `archive s/STUDENT_NAME`

* `STUDENT_NAME` must contain letters and spaces and must begin with a letter.
* The name must match the student's full name exactly, ignoring letter case. For example, `archive s/john doe` can archive a student named `John Doe`.
* A successful command displays `Student archived.` and refreshes the active student list.
* If no student with the specified name exists, the command displays `Student cannot be found.`
* If the student is already archived, the command displays `Student already archived.`

Example:
* `archive s/John Doe` archives John Doe and removes the student from the active list.

### Unarchiving a student: `unarchive`

Restores an archived student to the active student list. Unarchiving does not change the student's profile details.

Format: `unarchive s/STUDENT_NAME`

* `STUDENT_NAME` must contain letters and spaces and must begin with a letter.
* The name must match the student's full name exactly, ignoring letter case.
* A successful command displays `Student unarchived.` and makes the student appear in `list` again.
* If no student with the specified name exists, the command displays `Student cannot be found.`
* If the student is already active, the command displays `Student already active.`

Example:
* `unarchive s/John Doe` restores John Doe to the active student list.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TuteeDex automatically saves data after every command. You do not need to save manually.

### Editing the data file

TuteeDex data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, TuteeDex starts with an empty address book at the next run. The invalid file remains on disk until you run a command (TuteeDex saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause TuteeDex to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TuteeDex home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.
3. **Data created before the student-profile schema change is not compatible.** Delete the old address-book JSON file before starting this version of TuteeDex.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME a/ADDRESS p/PHONE_NUMBER d/START_DATE s/SUBJECT [s/SUBJECT]... [o/OUTSTANDING_AMOUNT] [t/TAG]... [notes/NOTE]` <br> e.g., `add n/James Ho a/123, Clementi Rd, 123466 p/22224444 d/10-Aug s/Maths o/20 t/A-Level notes/Revise algebra`
**View**   | `view INDEX`<br> e.g., `view 1`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [a/ADDRESS] [d/START_DATE] [o/OUTSTANDING_AMOUNT] [s/SUBJECT]... [t/TAG]... [notes/NOTE]`<br> e.g., `edit 2 n/James Lee d/19-Sep o/20 s/Maths t/A-Level notes/Revise algebra`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list [-summary] [s/SUBJECT]...` or `list [-summary] [t/TAG]...`<br> e.g., `list -summary t/A-Level t/Needs revision`
**Archive** | `archive s/STUDENT_NAME`<br> e.g., `archive s/James Ho`
**Unarchive** | `unarchive s/STUDENT_NAME`<br> e.g., `unarchive s/James Ho`
**Help**   | `help`
