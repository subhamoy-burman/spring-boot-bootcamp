# Troubleshooting & Fixes

This document summarizes the problems encountered while scaffolding and running the `appointment-system` project, diagnostics performed, commands executed, and the fixes applied.

## 1) Maven Not Recognized

Symptom:
- Running `mvn` or `mvnw.cmd` returned "mvn is not recognized" in PowerShell.

Diagnosis:
- Maven binaries existed at `C:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin` but that directory was not in the current PowerShell session `PATH`.

Fix Applied (temporary for current session):
- Added Maven bin to the session PATH and verified:

```powershell
$env:PATH += ";C:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin"
mvn -version
```

Notes:
- To persist the change, add that directory to the system/user PATH via Windows Environment Variables or set it in your PowerShell profile.

---

## 2) Duplicate `spring:` Key in `application.yml`

Symptom:
- Spring Boot failed to start with a SnakeYAML `DuplicateKeyException`.

Diagnosis:
- `application.yml` contained two top-level `spring:` keys (accidental duplication), causing YAML parsing to fail.

Fix Applied:
- Merged the two `spring:` sections into a single block and moved `spring.sql.init.mode: always` under the same `spring:` hierarchy.

File edited:
- `src/main/resources/application.yml`

---

## 3) PostgreSQL Time Zone Error

Symptom:
- While initializing DB, the Postgres driver threw: `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"`.

Diagnosis:
- The database/driver rejected the system timezone string `Asia/Calcutta`. Postgres expects `Asia/Kolkata` (canonical name) or a supported timezone value. Java/Postgres startup was using the system timezone.

Fixes Applied:
- Two-pronged approach:
  1. Added a JVM timezone override for the test runs by configuring the Maven Surefire plugin in `pom.xml`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>-Duser.timezone=UTC</argLine>
  </configuration>
</plugin>
```

  2. Also adjusted the JDBC URL to include an explicit timezone fallback (if desired):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical_system?serverTimezone=UTC
```

Notes:
- The `-Duser.timezone=UTC` ensures tests/app use a timezone Postgres accepts during startup.

---

## 4) Must Be Owner Of Table Error (DB Schema Initialization)

Symptom:
- `schema.sql` failed with: `ERROR: must be owner of table patient` when Drop/Create ran during Spring Boot init.

Diagnosis:
- The `patient` table had been created earlier under the `postgres` user (or another owner). Spring Boot's SQL init runs as `medicaladmin` (configured user) and therefore could not drop/replace the table.

Fix Applied:
- Ensured the `medical_system` database is owned by `medicaladmin` and replaced the `patient` table as `medicaladmin`. Commands used inside the running container:

```powershell
docker exec my-postgres psql -U postgres -c "CREATE ROLE medicaladmin WITH LOGIN PASSWORD 'SecurePass123!'" || true
docker exec my-postgres createdb -U postgres medical_system || true
docker exec my-postgres psql -U postgres -d medical_system -c "DROP TABLE IF EXISTS patient CASCADE; CREATE TABLE patient (id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, date_of_birth DATE, medical_record_number VARCHAR(100) UNIQUE NOT NULL); INSERT INTO patient ...;"
docker exec my-postgres psql -U postgres -c "ALTER DATABASE medical_system OWNER TO medicaladmin;"
```

Result:
- After ensuring the database ownership and recreating the table under `medicaladmin`, Spring Boot could execute `schema.sql` and `data.sql` successfully.

---

## 5) Tests and Verification

Commands run locally (to validate everything):

```powershell
# From project root (where pom.xml is)
cd appointment-system
mvn clean test
# or run the app
mvn spring-boot:run
```

Observed:
- Tests show the JDBC query returning 3 patients (seed data).

---

## 6) Recommendations / Next Steps

- Persist Maven path: add `C:\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin` to Windows user or system PATH.
- If you prefer not to run schema/data initialization via Spring Boot SQL scripts in an environment where ownership differs, switch to a migration tool like Flyway/Liquibase and run as DB admin during setup only.
- Consider normalizing timezone on the host to canonical names (Asia/Kolkata) or keep `-Duser.timezone` set for CI/tests.
- Optionally add the Maven Wrapper to the repo so `mvnw.cmd` works for all developers.

---

If you want, I can:

- Add `mvnw` wrapper files for consistent builds across machines.
- Add a `CommandLineRunner` that prints the patient count on startup as a quick app-run verification.
- Replace SQL-init with Flyway migrations and mark those steps for production readiness.

If you want me to proceed with any of the above, tell me which item and I’ll implement it.
