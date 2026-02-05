# Meeting Scheduler

meeting scheduler simulation project.

##  Running Tests

This project uses integration tests to validate high-performance concurrency scenarios (Optimistic Locking).

Due to environment restrictions (WSL2/Windows networking), the tests are configured to connect to a local database instance rather than spawning ephemeral containers via Testcontainers.

**To run the tests:**

1. Start the database infrastructure:
   ```bash
   docker-compose up -d postgres