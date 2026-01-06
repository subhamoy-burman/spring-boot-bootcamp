Getting Started — appointment-system

This guide walks a developer from a clean workstation to running the Spring Boot `appointment-system` application and connecting it to a local database (PostgreSQL, MySQL, or Oracle). It assumes Windows, macOS, and Linux commands where appropriate. Keep an eye on the OS-specific notes.

1. Install Java (JDK 17+ recommended)
- Windows/macOS/Linux: Download an LTS JDK (Adoptium Temurin or Oracle JDK).
- Verify installation:

```bash
java -version
javac -version
```

- If the commands are not found, add Java to your PATH. On Windows, update `System Properties → Environment Variables → Path` and add the JDK `bin` folder (for example `C:\Program Files\Java\jdk-17\bin`).

2. Install Maven (optional: use Maven Wrapper)
- Download Apache Maven and unpack.
- Add Maven `bin` to PATH (e.g., `C:\apache-maven-3.9.12\bin`).
- Verify:

```bash
mvn -version
```

- Alternatively generate and commit the Maven Wrapper so contributors don't need Maven installed: run `mvn -N -B io.takari:maven:wrapper` in the project root.

3. Clone the repository / get project files
- Place the `appointment-system` project folder on your machine.

4. Configure a database
This project uses JDBC and Spring Boot's `spring.datasource.*` configuration. Pick one of the following and update `src/main/resources/application.yml` (or create a profile `application-mysql.yml` / `application-oracle.yml`).

PostgreSQL (example)
- Install Postgres (local install or Docker):

```bash
# Docker example
docker run --name my-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:latest
```

- Create database & user:

```bash
docker exec -it my-postgres psql -U postgres -c "CREATE DATABASE medical_system;"
docker exec -it my-postgres psql -U postgres -c "CREATE USER medicaladmin WITH PASSWORD 'SecurePass123!';"
docker exec -it my-postgres psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE medical_system TO medicaladmin;"
```

- application.yml example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical_system
    username: medicaladmin
    password: SecurePass123!
    driver-class-name: org.postgresql.Driver
```

MySQL (example)
- Docker example:

```bash
docker run --name my-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=medical_system -e MYSQL_USER=medicaladmin -e MYSQL_PASSWORD=SecurePass123! -p 3306:3306 -d mysql:8
```

- application-mysql.yml example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medical_system?useSSL=false&serverTimezone=UTC
    username: medicaladmin
    password: SecurePass123!
    driver-class-name: com.mysql.cj.jdbc.Driver
```

Oracle (example — uses an existing Oracle instance)
- application-oracle.yml example:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//host:1521/ORCLCDB
    username: youruser
    password: yourpassword
    driver-class-name: oracle.jdbc.OracleDriver
```

Notes on schema initialization
- This project uses `schema.sql` and `data.sql` in `src/main/resources` with `spring.sql.init.mode=always` to initialize DB schema on startup. If you use Flyway or Liquibase in production, remove or disable `schema.sql`.

5. Build and run
- Build and run tests:

```bash
cd appointment-system
mvn clean test
```

- Run the application (with Maven installed):

```bash
mvn spring-boot:run
```

- Or use the wrapper if present:

```bash
./mvnw spring-boot:run   # macOS/Linux
mvnw.cmd spring-boot:run # Windows
```

6. Debug mode
- Start with JDWP debug agent (so IDEs can attach) — you can use MAVEN_OPTS or pass JVM args to `spring-boot:run`:

```bash
# Windows PowerShell example
$env:MAVEN_OPTS='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Duser.timezone=UTC'
mvn spring-boot:run
```

7. Swagger UI
- The project includes `springdoc-openapi`. After the app starts open:
  - http://localhost:8080/swagger-ui/index.html
- Use the UI to call endpoints (POST/PUT/DELETE etc.)

8. Common troubleshooting
- `mvn` not found: add Maven `bin` to PATH or use the Maven wrapper.
- YAML parsing errors: check for duplicate keys in `application.yml`.
- TimeZone errors (Postgres): either set JVM timezone (`-Duser.timezone=UTC`) or ensure JDBC URL/driver options are correct for your DB.
- Schema init failed due to permissions: ensure the DB user has rights to create/alter tables or run the schema manually as an admin and assign ownership.

9. Example Swagger payloads
- Create (POST /api/patients):

```json
{
  "name": "Alice Walker",
  "dateOfBirth": "1988-07-22",
  "medicalRecordNumber": "MRN-9999"
}
```

- Update (PUT /api/patients/{id}): same body; id in path.

10. Optional: add integration tests
- Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` and `TestRestTemplate` to write end-to-end tests.

If you want, I can:
- Add `application-mysql.yml` and `application-oracle.yml` sample files to the repo.
- Add a `Makefile` or PowerShell helper script to setup a Docker DB quickly.

*** End of SETUP.md
