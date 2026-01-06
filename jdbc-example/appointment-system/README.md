# Appointment System (minimal scaffold)

This is a minimal Spring Boot 3.3.x project scaffold for the session exercises.

Prerequisites:
- Java 17+
- Maven (or use the included mvnw/mvnw.cmd)
- PostgreSQL running (the instructions assume a Docker container with database `medical_system` and user `medicaladmin`)

Run locally:

Windows (PowerShell):
```powershell
mvnw.cmd spring-boot:run
```

Run tests:
```powershell
mvnw.cmd test
```

Database setup:
- The project includes `src/main/resources/schema.sql` and `data.sql` which will run on startup (spring.sql.init.mode=always).
- `schema.sql` drops `patient` table if exists then creates it.

If you want the container recreated via Docker Compose, ensure your running container matches credentials in `application.yml`.
