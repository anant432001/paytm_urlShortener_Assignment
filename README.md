# URL Shortener Service

A small Spring Boot service that converts long URLs into short codes and redirects short-code requests to their original URLs.

The project was implemented as a time-boxed engineering exercise with emphasis on working software, clear design decisions, input validation, and automated tests.

## Features

- Shorten a valid URL.
- Generate URL-safe Base62 short codes.
- Redirect short codes using HTTP `301 Moved Permanently`.
- Support user-provided custom aliases.
- Return `404 Not Found` for unknown short codes.
- Reject malformed or invalid URLs.
- Reject duplicate custom aliases.
- Persist URL mappings in PostgreSQL.
- Automated service and controller tests.

## Technology Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Maven

## API

### Shorten a URL

```http
POST /shorten
Content-Type: application/json
```

Request:

```json
{
  "url": "https://example.com/articles/123"
}
```

Response:

```http
201 Created
```

```json
{
  "originalUrl": "https://example.com/articles/123",
  "shortUrl": "http://localhost:8080/1z0",
  "shortCode": "1z0",
  "createdAt": "2026-07-16T13:30:00"
}
```

### Shorten a URL with a custom alias

```http
POST /shorten
Content-Type: application/json
```

Request:

```json
{
  "url": "https://example.com",
  "customAlias": "example"
}
```

Response:

```json
{
  "originalUrl": "https://example.com",
  "shortUrl": "http://localhost:8080/example",
  "shortCode": "example",
  "createdAt": "2026-07-16T13:30:00"
}
```

Custom aliases:

- Must contain between 4 and 20 characters
- May contain letters, numbers, hyphens and underscores
- Must be unique

### Redirect to the original URL

```http
GET /{code}
```

Example:

```http
GET /example
```

Response:

```http
301 Moved Permanently
Location: https://example.com
```

An unknown code returns:

```http
404 Not Found
```

## Short-Code Generation

Automatically generated short codes use a PostgreSQL sequence.

Each request obtains the next sequence value:

```sql
SELECT nextval('short_code_sequence');
```

The numeric sequence value is then encoded using Base62:

```text
0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ
```

This approach was selected because:

- PostgreSQL sequence values are unique within the database
- Base62 output contains URL-safe characters
- Codes are shorter than their numeric sequence representations
- Collision handling does not depend on probability
- Generation remains simple and deterministic

A database unique constraint on `short_code` provides an additional persistence-level safeguard.

Custom aliases use the same `short_code` column and therefore cannot overlap with generated codes or other aliases.

## Duplicate URL Behaviour

Shortening the same original URL multiple times without a custom alias creates a new short code each time.

This is intentional. The URL itself is not treated as unique because the same destination may need multiple independently shareable links.

For example, two requests containing:

```json
{
  "url": "https://example.com"
}
```

may return different short codes.

Custom aliases remain unique. Reusing an existing custom alias is rejected.

## URL Validation

The service validates incoming URLs before storing them.

A valid URL must:

- Be syntactically valid
- Be absolute

## Project Structure

```text
src
├── main
│   ├── java/com/paytm/urlShortener
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── exception
│   │   ├── generator
│   │   ├── repository
│   │   └── service
│   └── resources
│       ├── application.properties
│       └── schema.sql
└── test
    └── java/com/paytm/urlShortener
        ├── controller
        └── service
```

## Prerequisites

Install:

- Java 21
- PostgreSQL
- Maven, or use the included Maven wrapper

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE url_shortener;
```

The application creates the URL mapping table through Hibernate and initializes the short-code sequence through `schema.sql`.

The sequence definition is:

```sql
CREATE SEQUENCE IF NOT EXISTS short_code_sequence
START WITH 100000
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 20;
```

## Configuration

Update `src/main/resources/application.properties` for your local PostgreSQL instance.

Recommended configuration:

```properties
spring.application.name=urlShortener
server.port=8080

app.base-url=http://localhost:8080

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5435/url_shortener}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

Environment variables may be supplied instead of modifying the file:

```bash
export DB_URL=jdbc:postgresql://localhost:5435/url_shortener
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

## Running the Application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

## Example Commands

Shorten a generated URL:

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/articles/123"
  }'
```

Create a custom alias:

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com",
    "customAlias": "example"
  }'
```

Inspect the redirect without automatically following it:

```bash
curl -i http://localhost:8080/example
```

Follow the redirect:

```bash
curl -L http://localhost:8080/example
```

## Running Tests

Run all tests:

```bash
./mvnw clean test
```

On Windows:

```bash
mvnw.cmd clean test
```

The test suite covers:

- Generated short-code creation
- Custom alias creation
- Duplicate alias rejection
- Generated-code collision handling
- URL validation
- Redirect lookup
- Unknown short codes
- HTTP `201` shortening responses
- HTTP `301` redirect responses
- Invalid request HTTP `400` responses

## Design Decisions

### PostgreSQL instead of an in-memory database

PostgreSQL was used because persistence is an explicit requirement and its sequence mechanism provides a straightforward source of unique IDs.

An H2 database would make local execution easier but would not demonstrate the final datastore and sequence behaviour as directly.

### Sequence-based codes instead of random codes

Random codes require collision detection and retry logic. A database sequence produces unique numeric values, which can be converted directly to Base62.

The trade-off is that generated codes are predictable and generation depends on the database.

For this scoped exercise, deterministic uniqueness was prioritised over unpredictability.

### New code for duplicate URLs

The same original URL can have multiple short codes.

The alternative was to return the existing mapping, but that would require defining URL normalisation rules and making the original URL unique. Creating a new mapping keeps behaviour simple and explicit.

## Limitations and Future Improvements

Given additional time, I would consider:

- URL expiration
- Click analytics
- Rate limiting
- Docker Compose for local PostgreSQL setup
- Testcontainers-based database integration tests
- Caching frequently accessed mappings
- Metrics and structured logging
- URL normalisation
- Custom alias reservation rules
- Distributed generation across multiple databases
- Protection against redirecting to unsafe destinations

These were intentionally excluded to keep the implementation within the expected exercise scope.