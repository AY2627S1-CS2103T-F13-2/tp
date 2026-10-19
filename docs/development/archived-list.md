# Archived-list integration plan

Work in progress for #75. This document is not a description of a released
feature. `list -archived` is not implemented yet. The archive model and storage
are owned by #78 and are not present on the common base of this branch.

## Implementation boundary

- Reuse the archive state and accessor supplied by #78. Do not introduce a
  second archive field, reflective lookup, or a temporary hard-coded result.
- Add `list -archived` to display only archived students. Use the existing
  filtered-list mechanism so View, Edit, and Delete use displayed indexes.
- Report an archived-specific empty result, not the message saying that no
  contacts are stored. Filtering must not remove or rewrite student records.
- Agree with the archive owner whether plain `list` and startup display only
  active students. Find must exclude archived students once the field exists.
- Coordinate parser and command integration with subject filtering (#89),
  tag filtering (#93), and the already merged payment summary (#91).
- Decide whether archive, subject, tag, and summary options can combine before
  implementing them. If combinations are unsupported, reject them clearly
  without changing the current filter. No combination policy is committed by
  this preparatory branch.
- Check Add and Edit filter resets too: a search-only fix must not accidentally
  expose archived students after another command restores the default list.

## Checks to add after the archive contract is available

1. A mixed active/archived address book lists only archived students with
   `list -archived`, including after a previous name or subject/tag filter.
2. An empty book and an active-only book both return an empty archived list
   with appropriate feedback. An archived-only book still lists its records.
3. Switching back to plain `list` follows the agreed default visibility.
4. Find excludes matching archived students and does not search only the
   previous filtered subset.
5. Invalid arguments preserve the current list, model data, and saved JSON.
6. View uses the displayed archived index, not the unfiltered backing index.
7. Existing subject/tag filtering and summary behaviour remain correct.
8. Archive status survives save/reload using #78's implementation.

Keep the PR draft until the real archive field is integrated, the command and
parser are implemented, these checks pass, and the User Guide is updated.
