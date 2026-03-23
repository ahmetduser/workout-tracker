# Learnings

## Why `FetchType.LAZY`

`FetchType.LAZY` means Hibernate loads the main row first and delays loading related rows until the code actually accesses that relation.

Example:

- Loading a `SetEntry` can query only the `set_entry` table if the code only needs fields like `reps` or `weight`.
- If the code later accesses `setEntry.exercise?.name`, Hibernate then issues a separate query for the related `exercise`.

This avoids unnecessary data loading and keeps queries more predictable.
