# Write-up

## 1. Collaboration with AI tools.

I used an AI assistant throughout the exercise as a development and review tool.

I asked it to help with:

- Reviewing service and controller responsibilities
- Identifying important edge cases
- Producing initial test-case structures.
- Organising the README and this write-up

I reviewed and adapted the generated code rather than copying it without verification. I ran the application and tests locally.

I personally decided:

- To use Java, Spring Boot and PostgreSQL
- To use a PostgreSQL sequence followed by Base62 encoding
- To support custom aliases through the same short-code column
- To generate a new code when the same URL is submitted more than once
- Which functionality to omit in order to remain within the time limit and scope of the assignment.

## 2. Where I corrected, overrode or rejected AI output

One early option was to generate random short codes and check the database for collisions. I rejected that as the primary design because checking and then inserting introduces additional collision and concurrency considerations.

Instead, I selected a PostgreSQL sequence. Every generated sequence number is unique within the database, and Base62 encoding converts it into a compact URL-safe value.

I also chose not to add retry infrastructure around the generated-code path because the sequence-based generator does not depend on probabilistic uniqueness. The database unique constraint remains a final safeguard.

I also avoided adding `@Transactional` where the current service methods contain only one repository operation and do not require a multi-operation atomic boundary.

During testing, an assertion exposed that the arguments passed to `ShortenUrlResponseDTO` did not match its constructor field order. I corrected the response mapping so that `originalUrl`, `shortUrl` and `shortCode` are returned in their proper fields. This was an example where tests provided a useful check against code that looked valid at first glance.

## 3. Biggest trade-offs

### PostgreSQL sequence versus random short codes

I chose a PostgreSQL sequence and Base62 encoding.

A random generator would provide less predictable codes and would be easier to distribute, but it would require collision detection and retry handling.

### New mapping versus idempotent duplicate handling

The same original URL receives a new generated code on every request.

This keeps URL creation straightforward and allows multiple links to point to the same destination.

### PostgreSQL versus an in-memory datastore

I used PostgreSQL because persistence was explicitly required and PostgreSQL sequences support the selected generation strategy.

For unit and controller tests, I mocked dependencies so the core test suite does not require a running database.

## 4. What is missing, and what I would do with another day

The current implementation focuses on the required shortening and redirect flow.

With another day, I would add:

- Integration tests using Testcontainers and PostgreSQL
- Docker Compose for one-command local setup
- URL expiration support
- Click-count and last-accessed analytics
- Rate limiting
- Structured logging and application metrics
- Caching for frequently accessed short codes
- More explicit handling of database constraint violations under concurrent requests
- Reserved aliases for paths such as `shorten`, `health` and future API endpoints