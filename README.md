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

**1. Create Availability (The Calendar Owner)**
POST /api/v1/availability

{
"startTime": "2026-12-01T09:00:00",
"endTime": "2026-12-01T10:00:00",
"ownerId": 1
}

**2. Reserve a Slot (The Client) 
Note: You must send the correct version.
If the slot was updated by someone else,
you'll get a 409 Conflict.**

POST /api/v1/availability/{id}/reserve

{
"userId": 500,
"version": 0,
"title": "Project Kickoff",
"description": "Discussing Q4 goals",
"participants": ["jane.doe@company.com", "bob@client.com"]
}

**3. Cancel Reservation Only the user who reserved it (or the owner)
can cancel.**

POST /api/v1/availability/{id}/cancel?userId=500

**4. List Available Slots (Paginated)**
GET `/api/v1/availability?page=0&size=10&sort=startTime,asc`