# Technical Walkthrough — appointment-system

This document provides a detailed step-by-step explanation of how the Spring Boot JDBC application works, from startup to database query execution and response generation.

---

## Table of Contents

1. [Project Dependencies](#1-project-dependencies)
2. [Application Entry Point & Initialization](#2-application-entry-point--initialization)
3. [Database Connection Setup](#3-database-connection-setup)
4. [Application Architecture & Layers](#4-application-architecture--layers)
5. [Request Flow: HTTP to Database](#5-request-flow-http-to-database)
6. [Database Query Execution Flow](#6-database-query-execution-flow)
7. [Response Path: Database to HTTP](#7-response-path-database-to-http)
8. [Complete Example: Creating a Patient](#8-complete-example-creating-a-patient)

---

## 1. Project Dependencies

### pom.xml — Maven Dependency Declarations

Located at: `appointment-system/pom.xml`

#### Parent Dependency
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.6</version>
</parent>
```
**Purpose**: Provides dependency management, plugin configuration, and Spring Boot defaults. Sets Java version, Maven plugin versions, and common library versions.

#### Core Dependencies

**1. spring-boot-starter-web**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
**What it provides**:
- Embedded Tomcat server (runs on port 8080 by default)
- Spring MVC for REST controllers (`@RestController`, `@RequestMapping`)
- Jackson for JSON serialization/deserialization
- Spring Web utilities

**Role**: Enables HTTP request handling, REST API creation, and JSON processing.

**2. spring-boot-starter-data-jdbc**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
```
**What it provides**:
- `JdbcTemplate` — simplified JDBC operations
- Spring Data JDBC base classes
- Transaction management (`@Transactional` support)
- HikariCP connection pool (default, auto-configured)
- SQL script execution (`schema.sql`, `data.sql`)

**Role**: Database access layer, connection pooling, and SQL execution.

**3. postgresql (runtime)**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
**What it provides**:
- PostgreSQL JDBC driver (`org.postgresql.Driver`)
- Protocol implementation for Postgres wire protocol
- Type conversions (Java ↔ PostgreSQL)

**Role**: Allows Java to connect and communicate with PostgreSQL databases.

**4. springdoc-openapi-starter-webmvc-ui**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
```
**What it provides**:
- OpenAPI 3.0 specification generation from Spring controllers
- Swagger UI web interface at `/swagger-ui/index.html`
- API documentation at `/v3/api-docs`

**Role**: Auto-generates interactive API documentation from REST controllers.

**5. lombok (optional, compile-time)**
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
**What it provides**:
- Annotation processors for boilerplate code reduction
- `@Data`, `@Getter`, `@Setter`, `@AllArgsConstructor`, etc.

**Role**: Reduces boilerplate in domain classes (though currently not heavily used in this project).

**6. spring-boot-devtools (runtime, optional)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```
**What it provides**:
- Automatic application restart on classpath changes
- LiveReload server for browser refresh
- Development-time property defaults

**Role**: Improves developer productivity during coding.

**7. spring-boot-starter-test (test scope)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
**What it provides**:
- JUnit 5 (Jupiter)
- Mockito, AssertJ, Hamcrest
- Spring Test (`@SpringBootTest`, `@WebMvcTest`)
- `TestRestTemplate`, `MockMvc`

**Role**: Enables unit and integration testing.

---

## 2. Application Entry Point & Initialization

### Step 1: JVM Starts
When you run `mvn spring-boot:run` or `java -jar appointment-system.jar`, the JVM:
1. Loads the main class specified in `MANIFEST.MF` (or command line).
2. Invokes `public static void main(String[] args)`.

### Step 2: Main Class Execution

**File**: `src/main/java/com/medical/AppointmentSystemApplication.java`

```java
package com.medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppointmentSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentSystemApplication.class, args);
    }
}
```

**What happens**:
1. `@SpringBootApplication` is a composite annotation containing:
   - `@Configuration` — marks this as a configuration class
   - `@EnableAutoConfiguration` — enables Spring Boot's auto-configuration
   - `@ComponentScan` — scans for `@Component`, `@Service`, `@Repository`, `@Controller` in this package and sub-packages

2. `SpringApplication.run()` does:
   - Creates an `ApplicationContext` (Spring IoC container)
   - Scans classpath for auto-configuration candidates
   - Loads `application.yml` / `application.properties`
   - Registers beans (controllers, services, repositories)
   - Starts embedded Tomcat server
   - Publishes `ApplicationReadyEvent`

### Step 3: Auto-Configuration Phase

Spring Boot detects dependencies on the classpath and auto-configures:

**DataSourceAutoConfiguration**:
- Detects `spring-boot-starter-data-jdbc` and `postgresql` driver on classpath
- Reads `spring.datasource.*` properties from `application.yml`
- Creates a `DataSource` bean (HikariCP pool)

**JdbcTemplateAutoConfiguration**:
- Creates a `JdbcTemplate` bean using the `DataSource`
- Registers `NamedParameterJdbcTemplate` if needed

**DataSourceInitializerConfiguration**:
- Detects `spring.sql.init.mode=always` in `application.yml`
- Executes `schema.sql` (DDL) and `data.sql` (DML) at startup

**WebMvcAutoConfiguration**:
- Configures Spring MVC dispatcher servlet
- Sets up JSON message converters (Jackson)
- Registers controller mappings

**SpringDocAutoConfiguration** (from springdoc library):
- Scans controllers for OpenAPI annotations
- Generates `/v3/api-docs` endpoint
- Serves Swagger UI at `/swagger-ui/index.html`

### Step 4: Component Scanning & Bean Registration

Spring scans `com.medical` package and sub-packages and registers:

**Controllers**:
- `HealthController` (`@RestController`)
- `PatientController` (`@RestController`)

**Services**:
- `PatientService` (`@Service`)

**Repositories**:
- `PatientRepository` (`@Repository`)

**Configuration**:
- `DataInitializer` (`@Configuration` with `@Bean` for `CommandLineRunner`)

### Step 5: Database Initialization

**File**: `src/main/resources/application.yml`
```yaml
spring:
  sql:
    init:
      mode: always
```

Spring executes in order:
1. `src/main/resources/schema.sql`:
   - Drops and recreates `patient` table
   - Defines columns: `id`, `name`, `date_of_birth`, `medical_record_number`

2. `src/main/resources/data.sql`:
   - Inserts 3 sample patient records

### Step 6: CommandLineRunner Execution

**File**: `src/main/java/com/medical/config/DataInitializer.java`

```java
@Configuration
public class DataInitializer {
    private final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner showCounts(PatientService service) {
        return args -> {
            long count = service.findAll().size();
            log.info("Startup: found {} patients", count);
        };
    }
}
```

**What happens**:
- After context is fully initialized, Spring invokes all `CommandLineRunner` beans
- This runner calls `PatientService.findAll()` to count patients
- Logs the count: `Startup: found 3 patients`

### Step 7: Embedded Tomcat Starts

- Tomcat listens on port 8080 (configured in `application.yml`)
- Application is ready to accept HTTP requests
- Console prints: `Started AppointmentSystemApplication in X seconds`

---

## 3. Database Connection Setup

### HikariCP Connection Pool Initialization

**Configuration**: `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical_system
    username: medicaladmin
    password: SecurePass123!
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      pool-name: MedicalHikariPool
```

### Connection Pool Lifecycle

**1. DataSource Bean Creation**

Spring Boot's `DataSourceAutoConfiguration` creates a `HikariDataSource` bean:

```java
// Pseudo-code of what Spring does internally
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/medical_system");
config.setUsername("medicaladmin");
config.setPassword("SecurePass123!");
config.setDriverClassName("org.postgresql.Driver");
config.setMaximumPoolSize(10);
config.setMinimumIdle(5);
config.setIdleTimeout(300000);
config.setConnectionTimeout(20000);
config.setPoolName("MedicalHikariPool");

DataSource dataSource = new HikariDataSource(config);
```

**2. Connection Pool Startup**

When the `DataSource` bean is initialized:
- HikariCP loads the PostgreSQL JDBC driver (`org.postgresql.Driver`)
- Opens `minimum-idle` (5) connections to the database immediately
- Validates connections using a test query (default: `SELECT 1`)
- Stores connections in an internal pool

Log output:
```
INFO com.zaxxer.hikari.HikariDataSource : MedicalHikariPool - Starting...
INFO com.zaxxer.hikari.pool.HikariPool  : MedicalHikariPool - Added connection org.postgresql.jdbc.PgConnection@445821a6
INFO com.zaxxer.hikari.HikariDataSource : MedicalHikariPool - Start completed.
```

**3. Connection Lifecycle**

- **Borrow**: When `JdbcTemplate` needs a connection, it calls `dataSource.getConnection()`. HikariCP returns an available connection from the pool (or creates one if under `maximum-pool-size`).
- **Use**: The connection executes SQL statements.
- **Return**: After use, the connection is returned to the pool (not closed).
- **Idle Timeout**: Connections idle longer than `idle-timeout` (5 minutes) are closed, keeping at least `minimum-idle`.
- **Connection Timeout**: If all connections are busy, a request waits up to `connection-timeout` (20 seconds) before throwing an exception.

---

## 4. Application Architecture & Layers

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         HTTP Client (Browser)           │
│      (Swagger UI / cURL / Postman)      │
└────────────────┬────────────────────────┘
                 │ HTTP Request (JSON)
                 ▼
┌─────────────────────────────────────────┐
│     Spring MVC (DispatcherServlet)      │
│    - Routes requests to controllers     │
│    - Converts JSON ↔ Java Objects       │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│          Controller Layer               │
│      @RestController, @RequestMapping   │
│      - PatientController                │
│      - HealthController                 │
│    Input validation, HTTP response      │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│          Service Layer                  │
│              @Service                   │
│      - PatientService                   │
│    Business logic, transaction boundary │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│         Repository Layer                │
│            @Repository                  │
│      - PatientRepository                │
│    SQL queries, JdbcTemplate operations │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│          JdbcTemplate                   │
│   - Manages connections                 │
│   - Executes SQL                        │
│   - Maps ResultSets to objects          │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│       HikariCP Connection Pool          │
│   - Provides database connections       │
│   - Connection lifecycle management     │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│     PostgreSQL JDBC Driver              │
│  - Wire protocol implementation         │
│  - Type conversions                     │
└────────────────┬────────────────────────┘
                 │ TCP/IP
                 ▼
┌─────────────────────────────────────────┐
│       PostgreSQL Database               │
│    (Tables: patient)                    │
└─────────────────────────────────────────┘
```

---

## 5. Request Flow: HTTP to Database

### Example: GET /api/patients (List All Patients)

**Step-by-Step Execution**:

#### Step 1: HTTP Request Arrives
Client sends:
```
GET http://localhost:8080/api/patients
Accept: application/json
```

#### Step 2: Tomcat Receives Request
- Embedded Tomcat server accepts TCP connection on port 8080
- Parses HTTP headers and request line
- Creates `HttpServletRequest` and `HttpServletResponse` objects

#### Step 3: DispatcherServlet Routes Request
- Spring MVC's `DispatcherServlet` receives the request
- Looks up handler mappings to find matching controller method
- Finds `PatientController.all()` mapped to `GET /api/patients`

#### Step 4: Controller Method Invoked

**File**: `src/main/java/com/medical/controller/PatientController.java`

```java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService service;

    @GetMapping
    public List<Patient> all() {
        return service.findAll();  // ← Calls service layer
    }
}
```

**What happens**:
- Spring injects `PatientService` via constructor injection
- Controller calls `service.findAll()`
- Returns `List<Patient>` (Spring MVC will convert to JSON)

#### Step 5: Service Layer Processes Request

**File**: `src/main/java/com/medical/service/PatientService.java`

```java
@Service
public class PatientService {
    private final PatientRepository repo;

    public List<Patient> findAll() {
        return repo.findAll();  // ← Calls repository layer
    }
}
```

**What happens**:
- Spring injects `PatientRepository`
- Service delegates to repository (in larger apps, business logic would go here)
- Returns `List<Patient>`

#### Step 6: Repository Executes Query

**File**: `src/main/java/com/medical/repository/PatientRepository.java`

```java
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<Patient> findAll() {
        return jdbcTemplate.query(
            "SELECT id, name, date_of_birth, medical_record_number FROM patient ORDER BY id",
            this::mapRowToPatient
        );
    }

    private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        Date dob = rs.getDate("date_of_birth");
        LocalDate localDob = dob != null ? dob.toLocalDate() : null;
        String mrn = rs.getString("medical_record_number");
        return new Patient(id, name, localDob, mrn);
    }
}
```

**What happens**:
- `jdbcTemplate.query()` is called with:
  - SQL string
  - `RowMapper` function (`this::mapRowToPatient`)
- See [Section 6](#6-database-query-execution-flow) for detailed query execution

#### Step 7: Query Results Mapped to Objects
- For each row in the `ResultSet`, `mapRowToPatient()` is invoked
- Creates a `Patient` object from database columns
- Returns `List<Patient>` with all rows

#### Step 8: Response Path (see [Section 7](#7-response-path-database-to-http))

---

## 6. Database Query Execution Flow

### Detailed Flow: `jdbcTemplate.query()` Execution

This section traces the execution of a `SELECT` query from `JdbcTemplate` down to the database and back.

#### Phase 1: JdbcTemplate Prepares Query

**Class**: `org.springframework.jdbc.core.JdbcTemplate`

```java
// Simplified internals
public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
    return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
}
```

**Step 1**: JdbcTemplate receives SQL and RowMapper.

#### Phase 2: Obtain Database Connection

**Step 2**: JdbcTemplate calls `DataSourceUtils.getConnection(dataSource)`

```java
// Pseudo-code
Connection conn = DataSourceUtils.getConnection(this.dataSource);
```

**What happens**:
- `dataSource.getConnection()` is invoked (HikariCP)
- HikariCP checks the pool for available connections
- Returns a connection proxy (HikariProxyConnection)
- Connection is marked as "in-use"

Log output:
```
DEBUG o.s.jdbc.core.JdbcTemplate : Executing SQL query [SELECT id, name, ...]
```

#### Phase 3: Create PreparedStatement

**Step 3**: JdbcTemplate creates a `PreparedStatement`

```java
// Pseudo-code
PreparedStatement ps = conn.prepareStatement(sql);
```

**What happens**:
- PostgreSQL driver creates a `PreparedStatement` object
- SQL is sent to the database server for parsing and planning (if not cached)
- Database returns a statement handle

#### Phase 4: Execute Query

**Step 4**: JdbcTemplate executes the statement

```java
// Pseudo-code
ResultSet rs = ps.executeQuery();
```

**What happens**:
- Driver sends SQL execution command to PostgreSQL
- Database executes the query:
  1. **Parse**: SQL syntax validation
  2. **Plan**: Query planner chooses execution strategy (index scan, seq scan, etc.)
  3. **Execute**: Reads data from disk/cache
  4. **Return rows**: Sends result set back to driver

**On the wire**:
- PostgreSQL wire protocol messages exchanged (Query, RowDescription, DataRow, CommandComplete)

#### Phase 5: Process ResultSet

**Step 5**: JdbcTemplate iterates over `ResultSet`

```java
// Pseudo-code
List<Patient> results = new ArrayList<>();
while (rs.next()) {
    Patient patient = rowMapper.mapRow(rs, rowNum++);
    results.add(patient);
}
```

**What happens**:
- `ResultSet.next()` advances cursor to next row
- For each row:
  - `rs.getLong("id")` fetches column value
  - `rs.getString("name")` fetches string
  - `rs.getDate("date_of_birth")` fetches date
  - Type conversions: PostgreSQL types → Java types
  - `RowMapper` creates `Patient` object
  - Added to result list

#### Phase 6: Cleanup

**Step 6**: JdbcTemplate closes resources

```java
// Pseudo-code (in finally block)
rs.close();
ps.close();
DataSourceUtils.releaseConnection(conn, dataSource);
```

**What happens**:
- `ResultSet` and `PreparedStatement` are closed
- Connection is returned to HikariCP pool (not actually closed)
- Connection becomes available for next query

#### Summary Diagram: Query Execution

```
┌──────────────────────────────────────────────────────────┐
│  1. Repository calls jdbcTemplate.query(sql, rowMapper)  │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  2. JdbcTemplate obtains Connection from HikariCP        │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  3. Creates PreparedStatement: conn.prepareStatement()   │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  4. Executes query: ps.executeQuery()                    │
│     - Sends SQL to PostgreSQL                            │
│     - DB parses, plans, and executes                     │
│     - Returns ResultSet                                  │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  5. JdbcTemplate iterates ResultSet                      │
│     while (rs.next()) {                                  │
│       Patient p = rowMapper.mapRow(rs, rowNum);          │
│       results.add(p);                                    │
│     }                                                    │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  6. Cleanup: close ResultSet, PreparedStatement          │
│     Return connection to pool                            │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  7. Return List<Patient> to service → controller         │
└──────────────────────────────────────────────────────────┘
```

---

## 7. Response Path: Database to HTTP

After the repository returns `List<Patient>`, the response flows back up the layers.

### Step 1: Repository Returns to Service
```java
// In PatientRepository
return jdbcTemplate.query(...);  // Returns List<Patient>
```

### Step 2: Service Returns to Controller
```java
// In PatientService
public List<Patient> findAll() {
    return repo.findAll();  // Returns List<Patient>
}
```

### Step 3: Controller Returns to Spring MVC
```java
// In PatientController
@GetMapping
public List<Patient> all() {
    return service.findAll();  // Returns List<Patient>
}
```

**What happens**:
- Controller method returns `List<Patient>`
- Spring MVC sees return type and `@RestController` annotation
- Triggers HTTP message conversion

### Step 4: JSON Serialization

**Jackson `HttpMessageConverter`**:
- Spring MVC uses Jackson `MappingJackson2HttpMessageConverter`
- Serializes `List<Patient>` to JSON:

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "dateOfBirth": "1985-05-15",
    "medicalRecordNumber": "MRN-001"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "dateOfBirth": "1990-08-22",
    "medicalRecordNumber": "MRN-002"
  },
  {
    "id": 3,
    "name": "Bob Johnson",
    "dateOfBirth": "1978-12-10",
    "medicalRecordNumber": "MRN-003"
  }
]
```

### Step 5: HTTP Response Construction

Spring MVC builds HTTP response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 245

[{"id":1,"name":"John Doe",...}]
```

### Step 6: Tomcat Sends Response
- Response written to socket
- Client receives JSON

---

## 8. Complete Example: Creating a Patient

### Request: POST /api/patients

**HTTP Request**:
```
POST http://localhost:8080/api/patients
Content-Type: application/json

{
  "name": "Alice Walker",
  "dateOfBirth": "1988-07-22",
  "medicalRecordNumber": "MRN-9999"
}
```

### Step-by-Step Flow

#### Step 1: Tomcat & DispatcherServlet
- Request arrives at Tomcat on port 8080
- DispatcherServlet routes to `PatientController.create()`

#### Step 2: JSON Deserialization
- Jackson converts JSON body to `Patient` object:
```java
Patient p = new Patient(null, "Alice Walker", LocalDate.of(1988, 7, 22), "MRN-9999");
```

#### Step 3: Controller Layer

**File**: `PatientController.java`
```java
@PostMapping
public ResponseEntity<Patient> create(@RequestBody Patient p) {
    Patient created = service.create(p);  // ← Call service
    return ResponseEntity.created(URI.create("/api/patients/" + created.getId())).body(created);
}
```

**Input**: `Patient` object (id=null)
**Action**: Call `service.create(p)`

#### Step 4: Service Layer

**File**: `PatientService.java`
```java
public Patient create(Patient p) {
    Long id = repo.save(p);  // ← Call repository
    p.setId(id);
    return p;
}
```

**Action**: Call `repo.save(p)` to insert into DB, then set generated ID on patient object.

#### Step 5: Repository Layer (INSERT with Generated Key)

**File**: `PatientRepository.java`
```java
public Long save(Patient p) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO patient (name, date_of_birth, medical_record_number) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setString(1, p.getName());
        if (p.getDateOfBirth() != null) {
            ps.setDate(2, Date.valueOf(p.getDateOfBirth()));
        } else {
            ps.setNull(2, java.sql.Types.DATE);
        }
        ps.setString(3, p.getMedicalRecordNumber());
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    return key != null ? key.longValue() : null;
}
```

**Detailed Execution**:

**Step 5a**: Get connection from pool
```java
Connection conn = dataSource.getConnection();  // From HikariCP
```

**Step 5b**: Create PreparedStatement with RETURN_GENERATED_KEYS flag
```java
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO patient (name, date_of_birth, medical_record_number) VALUES (?, ?, ?)",
    Statement.RETURN_GENERATED_KEYS
);
```

**Step 5c**: Bind parameters
```java
ps.setString(1, "Alice Walker");
ps.setDate(2, java.sql.Date.valueOf("1988-07-22"));
ps.setString(3, "MRN-9999");
```

**Step 5d**: Execute INSERT
```java
int rowsAffected = ps.executeUpdate();  // Returns 1
```

**What happens in the database**:
```sql
INSERT INTO patient (name, date_of_birth, medical_record_number) 
VALUES ('Alice Walker', '1988-07-22', 'MRN-9999')
RETURNING id;
```

PostgreSQL:
1. Validates SQL
2. Acquires table lock
3. Inserts row
4. Generates `id` from sequence (e.g., 4)
5. Returns generated key

**Step 5e**: Extract generated key
```java
ResultSet generatedKeys = ps.getGeneratedKeys();
if (generatedKeys.next()) {
    Long id = generatedKeys.getLong(1);  // Returns 4
}
keyHolder.getKey() returns 4
```

**Step 5f**: Return to service
```java
return 4L;  // Generated ID
```

#### Step 6: Service Sets ID and Returns

```java
p.setId(4L);  // Update patient object with generated ID
return p;     // Return Patient(id=4, name="Alice Walker", ...)
```

#### Step 7: Controller Builds Response

```java
Patient created = /* Patient(id=4, ...) */;
return ResponseEntity
    .created(URI.create("/api/patients/4"))
    .body(created);
```

**HTTP Response**:
```
HTTP/1.1 201 Created
Location: /api/patients/4
Content-Type: application/json

{
  "id": 4,
  "name": "Alice Walker",
  "dateOfBirth": "1988-07-22",
  "medicalRecordNumber": "MRN-9999"
}
```

---

## Summary: Complete Request-Response Cycle

```
1. HTTP Request arrives
   ↓
2. Tomcat → DispatcherServlet
   ↓
3. Routes to PatientController.create()
   ↓
4. Jackson deserializes JSON → Patient object
   ↓
5. Controller calls PatientService.create()
   ↓
6. Service calls PatientRepository.save()
   ↓
7. Repository calls JdbcTemplate.update()
   ↓
8. JdbcTemplate obtains Connection from HikariCP
   ↓
9. Creates PreparedStatement with RETURN_GENERATED_KEYS
   ↓
10. Binds parameters (name, dob, mrn)
   ↓
11. Executes INSERT via JDBC driver
   ↓
12. PostgreSQL inserts row and returns generated ID
   ↓
13. JdbcTemplate extracts ID from ResultSet
   ↓
14. Returns ID (4) to repository
   ↓
15. Repository returns ID to service
   ↓
16. Service sets ID on Patient object
   ↓
17. Returns Patient to controller
   ↓
18. Controller builds ResponseEntity (201 Created)
   ↓
19. Jackson serializes Patient → JSON
   ↓
20. Spring MVC writes HTTP response
   ↓
21. Tomcat sends response to client
```

---

## Key Takeaways

1. **Dependency Injection**: Spring wires together layers (controller → service → repository → jdbcTemplate) using constructor injection.

2. **Connection Pooling**: HikariCP manages a pool of reusable database connections, improving performance and resource utilization.

3. **JdbcTemplate**: Simplifies JDBC by handling connection management, exception translation, and resource cleanup.

4. **RowMapper**: Converts database rows (`ResultSet`) into Java objects.

5. **Layered Architecture**: Clear separation of concerns (controller, service, repository) makes code maintainable and testable.

6. **Auto-Configuration**: Spring Boot detects dependencies and configures beans automatically, reducing boilerplate.

7. **Transaction Management**: Though not explicitly used here, Spring provides `@Transactional` for declarative transaction boundaries (useful when multiple DB operations must succeed or fail together).

---

## Debugging Tips

- **Set breakpoints** in:
  - Controller methods to inspect HTTP request parameters
  - Service methods to observe business logic
  - Repository methods to see SQL and parameters
  - `mapRowToPatient()` to inspect ResultSet data

- **Enable SQL logging**:
```yaml
logging:
  level:
    org.springframework.jdbc.core: DEBUG
```

- **Use Swagger UI** to manually trigger requests and see responses.

- **Attach remote debugger** on port 5005 (configured in pom.xml) to step through code while the app is running.

---

**End of Technical Walkthrough**
