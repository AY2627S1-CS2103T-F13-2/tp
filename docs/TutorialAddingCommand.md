# Tutorial 2: adding a remark command

Based on the [SE-EDU tutorial](https://se-education.org/guides/tutorials/ab3AddRemark.html).
The implementation uses TuteeDex's current fields (name, phone, address, start date).

- Add or replace a note: `remark 1 r/Likes baseball`.
- Remove a note: `remark 1 r/` (or `remark 1`, as in the tutorial).
- Indices refer to the currently displayed list, not the entire address book.
- Remarks appear on contact cards and survive saving/reloading.
- Older JSON files load with an empty remark; no personal data files need deleting.

## Concepts to retain

The command word is dispatched by `AddressBookParser`. The command parser turns
text into an `Index` and `Remark`. The command replaces an immutable `Person`
through the model, and the existing observable list refreshes the GUI.
Storage adapts the new field into JSON. Normal edits must preserve the remark.
Remarks affect full equality, but do not change the identity of a contact.

This branch is tutorial practice only; the submission PR is closed without merging.

## Verification

The complete suite of 276 tests and both main/test Checkstyle checks pass.
Coverage includes adding/replacing/removing remarks, filtered indices, invalid
indices, preserving remarks through edits, and JSON round trips.
