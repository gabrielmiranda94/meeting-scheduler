# Meeting Scheduler 

This is my solution for the meeting scheduler 

## Feat

* **Concurrency Safety:** I used **Optimistic Locking** (`@Version`) to handle race conditions. If two users try to book the same slot at the exact same millisecond, the database creates a conflict (409) instead of corrupting data.
* **Pagination:** The `GET /availability` endpoint is paginated (`Pageable`). Listing "thousands of slots" without pagination would kill the memory, so I fixed that upfront.
* **Real Database:** I used **Postgres** via Docker instead of H2. H2 is easier to setup, but it hides real-world concurrency issues. I prefer environment parity.
* **Meeting Details:** You can set titles, descriptions and add participants.
* **Observability:** Prometheus metrics are enabled at `/actuator/prometheus`.

## Tech Stack

* **Java 21** & **Spring Boot 3.4**
* **PostgreSQL 16**
* **JUnit 5**
* **Docker Compose**
* **Swagger UI**

## How to Run

**Prerequisite:** Docker installed.

Since I am using a real database, you need to spin up the infrastructure first.

1.  **Start the Database & App:**
    ```bash
    docker-compose up --build -d
    ```

2.  **Run the Tests:**
    *Note: To save time and avoid Windows socket issues during the challenge, the integration tests run against the active Docker container.*
    ```bash
    ./gradlew test
    ```

3.  **Explore:**
    * Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Design Decisions (due to around 4-hour constraints)

Given the tight timeline, I had to make some pragmatic trade-offs:

* **Testing Strategy:** The project includes `Testcontainers` dependencies (you can see them in `build.gradle`), which is my preferred way for CI/CD. However, for this local dev submission, I configured the tests to connect to the local `docker-compose` instance. It was faster to implement and easier to debug locally.
* **Boilerplate:** I used AI tools to generate the DTOs and standard mappers quickly. This freed up my time to write the **Race Condition Integration Test** (`AvailabilityIntegrationTest`), which I believe is the most critical part of this assessment.
* **Security:** I added a non-root user in the `Dockerfile` because running containers as root is a bad habit, even for a test.

## API Usage Examples

**1. List & Filter Slots (Aggregated View)
   Fetch available time slots. Supports pagination and filtering to simulate a specific user's calendar or a time range.**

Endpoint: GET /api/v1/availability

Query Parameters:
userId (optional): Filter by slot owner.
from (optional): Start date range (ISO 8601).
to (optional): End date range (ISO 8601).
page, size, sort: Pagination controls.
Example: Get calendar for User 1 in December 2026

HTTP
GET /api/v1/availability?userId=1&from=2026-12-01T00:00:00&to=2026-12-31T23:59:59&page=0&size=20

**2. Create Availability
   Owners define new time slots available for booking.**

Endpoint: POST /api/v1/availability
Status: 201 Created
JSON
{
"startTime": "2026-12-10T09:00:00",
"endTime": "2026-12-10T10:00:00",
"ownerId": 1
}

**3. Reserve a Slot (Concurrency Safe)
   Clients book an available slot. Note: This endpoint enforces Optimistic Locking. You must provide the correct version of the slot. If another user modified the slot in the meantime, the API returns 409 Conflict.**

Endpoint: POST /api/v1/availability/{blockId}/reserve

Status: 200 OK

JSON
{
"userId": 500,
"version": 0,
"title": "Project Kickoff",
"description": "Discussing Q1 goals",
"participants": ["jane@corp.com", "bob@client.com"]
}

**4. Cancel Reservation
   Frees up a reserved slot, making it available again.**

Endpoint: POST /api/v1/availability/{blockId}/cancel?userId=500

Status: 200 OK

**5. Delete Slot
   Permanently removes a time slot from the system.**

Endpoint: DELETE /api/v1/availability/{blockId}

Status: 204 No Content

**Error Handling (RFC 7807)**
The API uses standardized ProblemDetail responses.

Example: 409 Conflict (Stale Data)

JSON
{
"type": "[https://api.scheduler.com/errors/concurrency](https://api.scheduler.com/errors/concurrency)",
"title": "Concurrency Conflict",
"status": 409,
"detail": "The time block was modified or reserved by another user concurrently. Please refresh and try again.",
"instance": "/api/v1/availability/15/reserve"
}
Example: 404 Not Found

JSON
{
"type": "about:blank",
"title": "Resource Constraints Violated",
"status": 404,
"detail": "Time slot not found with id: 999",
"instance": "/api/v1/availability/999/reserve"
}
Testing
The project includes robust Integration Tests that spin up a clean database context for every run to ensure isolation.

Bash
./gradlew test`/api/v1/availability?page=0&size=10&sort=startTime,asc`
