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

## Prometheus And Grafana Config Files

There are two different `prometheus.yml` files in the Docker setup, and they serve different tools.

### `docker/prometheus/prometheus.yml`

This file belongs to Prometheus.

- It tells Prometheus which app to scrape
- It tells Prometheus which endpoint to call
- In this project, it scrapes `app:8080/actuator/prometheus`

Without it, Prometheus starts but does not know where to collect metrics from.

### `docker/grafana/provisioning/datasources/prometheus.yml`

This file belongs to Grafana.

- It tells Grafana that Prometheus is available as a datasource
- It points Grafana to `http://prometheus:9090`
- It makes Prometheus the default datasource

Without it, Grafana still starts, but you have to configure the Prometheus datasource manually in the UI.

Simple picture:

```text
Spring Boot app -> exposes metrics at /actuator/prometheus
Prometheus -> scrapes the app using docker/prometheus/prometheus.yml
Grafana -> reads from Prometheus using docker/grafana/provisioning/datasources/prometheus.yml
```
