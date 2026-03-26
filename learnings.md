# Learnings

## Why `FetchType.LAZY`

`FetchType.LAZY` means Hibernate loads the main row first and delays loading related rows until the code actually accesses that relation.

Example:

- Loading a `SetEntry` can query only the `set_entry` table if the code only needs fields like `reps` or `weight`.
- If the code later accesses `setEntry.exercise?.name`, Hibernate then issues a separate query for the related `exercise`.

This avoids unnecessary data loading and keeps queries more predictable.

## HTTP Logging Interceptor

We use a Spring MVC interceptor to log basic HTTP request and response metadata in one central place.

- Incoming log: HTTP method and path
- Outgoing log: HTTP method, path, status, and duration
- We do not log request or response bodies

This is a good baseline because it is simple, readable, and avoids leaking payload data.

Simple flow:

```text
Client
  |
  v
HttpLoggingInterceptor.preHandle
  |
  v
Controller -> Service
  |
  v
HttpLoggingInterceptor.afterCompletion
  |
  v
Log status + duration
```
