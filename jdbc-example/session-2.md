# Session 2: Data Persistence with JDBC & Spring Data JDBC

> **Context**: This session builds a complete Medical Appointment System using Spring Boot 3.3+, PostgreSQL, and VS Code. We'll progress from raw JDBC through JdbcTemplate to Spring Data JDBC, culminating in a brief JPA comparison.

---

## 1. THE BLUEPRINT (Table of Contents)

> **Purpose**: Overview of all JDBC/Spring Data concepts covered in this session with deprecation checks and modern approaches. This is your mental index for data persistence in Spring.

---

### 1.1 Core Persistence Concepts

#### 1.1.1 JDBC (Java Database Connectivity)
**What it is**: Low-level Java API for interacting with relational databases. Provides direct access to database connections, statements, and result sets.

**Key Components**:
- `Connection` - Database connection
- `Statement/PreparedStatement` - SQL execution
- `ResultSet` - Query results
- Manual resource management required

**Deprecation Check**: ⚠️ **Outdated for direct use**  
**Modern Approach (2025)**: Never use raw JDBC in production code—use JdbcTemplate minimum, Spring Data JDBC/JPA preferred.

---

#### 1.1.2 JdbcTemplate
**Key Features**:
- Automatic connection management
- Exception translation (SQLException → DataAccessException)
- Simplified query methods (`query()`, `update()`, `queryForObject()`)
- Template callback pattern for complex operations
- **JdbcTemplate** ≈ `Dapper` (lightweight ORM over ADO.NET)
- Both provide simple mapping without full ORM overhead

**Deprecation Check**: ✅ Still valid for complex queries  
**Modern Approach**: Use for custom SQL, stored procedures, or when Spring Data's auto-generation doesn't fit your needs.

---

#### 1.1.3 Repository Pattern
**Benefits**:
- Decouples business logic from data access
- Enables easy testing with mock repositories
- Provides clean API for domain objects
- Technology-independent interface

**Example**:
```java
public interface PatientRepository {
    Iterable<Patient> findAll();
    Optional<Patient> findById(Long id);
    Patient save(Patient patient);
    void deleteById(Long id);atientRepository {
    private readonly ApplicationDbContext _context;
    // Implementation using EF Core
}
```

**Deprecation Check**: ✅ Core design pattern, timeless  
**Modern Approach**: Use with Spring Data JDBC/JPA for automatic implementation generation.

---

##Key Characteristics**:
- No lazy loading (eager loading only)
- No dirty tracking or session cache
- Simpler than JPA, closer to SQL
- Aggregate-oriented persistence (DDD-friendly)
- Explicit about what gets loaded and saved
**ASP.NET Core Equivalent**: 
- **Spring Data JDBC** ≈ `Entity Framework Core` (but simpler—no change tracking, lazy loading, or caching)
- Focuses on aggregate-oriented persistence

**Deprecation Check**: ✅ Modern standard (Spring Data 2.0+)  
**Modern Approach**: Primary choice for Spring Boot 3.3+ with relational databases when you don't need full ORM features.
Provided Methods**:
```java
public interface CrudRepository<T, ID> {
    <S extends T> S save(S entity);
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
    long count();
    void deleteById(ID id);
    void delete(T entity);
    void deleteAll(Iterable<? extends T> entities);
    void deleteAll(l)
public interface IRepository<T> {
    IEnumerable<T> GetAll();
    T GetById(object id);
    void Add(T entity);
    void Update(T entity);
    void Delete(object id);
}
```

**Deprecation Check**: ✅ Foundation of Spring Data  
**Modern Approach**: Extend `CrudRepository` for basic CRUD, `JpaRepository` for pagination/sorting.

---
Key Features**:
- Connection pooling (reuse connections)
- Connection validation
- Timeout management
- Thread-safe connection distribution

**Common Implementations**:
- HikariCP (default in Spring Boot - fastest)
- Apache Commons DBCP2
- Tomcat JDBC Pool
#### 1.2.1 DataSource
**What it is**: Java interface representing a database connection pool—manages connections, handles failover, provides connection reuse.
Dependency**:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Configuration**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dbname
    username: user
    password: pass
    driver-class-name: org.postgresql.Driver
#### 1.2.2 PostgreSQL Driver
**What it is**: JDBC driver that translates Java database calls into PostgreSQL wire protocol.

**Use Cases**:
- Unit testing (fast, isolated)
- Quick prototyping
- CI/CD pipeline tests
- Demo applications

**Dependency**:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```
**Dependency**:
``Location**: `src/main/resources/schema.sql`

**Execution**: Automatically run by Spring Boot when:
```yaml
spring:
  sql:
    init:
      mode: always  # or 'embedded' for H2 only
```

**Example**:
```sql
CREATE TABLE patient (
  Location**: `src/main/resources/data.sql`

**Example**:
```sql
INSERT INTO patient (name, medical_record_number) 
VALUES ('John Doe', 'MRN001');

INSERT INTO patient (name, medical_record_number) 
VALUES ('Jane Smith', 'MRN002');
```

**Execution Order**:
1. `schema.sql` runs first (creates tables)
2. `data.sql` runs second (populates data)ependency>
```

**Deprecation Check**: ✅ Official PostgreSQL driver  
**Modern Approach**: Use version 42.6+ for PostgreSQL 15/16 support and SCRAM-SHA-256 authentication.

---

#### 1.2.3 H2 Database (Embedded)
**What it is**: In-memory Java database for testing—no separate server required, data lost on restart.

**ASP.NET Core Equivalent**: 
- **H2** ≈ `SQLite` in-memory mode for .NET testing
- Or EF Core's in-memory provider

**Deprecation Check**: ⚠️ **H2 for production → NEVER**  
**Modern Approach**: Use H2 only for unit tests; use PostgreSQL/MySQL with Docker for local development to match production environment.

---

### 1.3 Schema Management

#### 1.3.1 schema.sql
**What it is**: SQL script file (src/main/resources/schema.sql) executed at startup to create database tables.

**ASP.NET Core Equivalent**: 
- **schema.sql** ≈ EF Core Migrations or manual SQL scripts in `DbContext.OnModelCreating()`

**Deprecation Check**: ✅ Valid for simple apps  
**Spring Data JDBC Enforcement**:
```java
// ✅ Correct - save through root
Appointment appointment = new Appointment();
appointment.addPrescription(new Prescription("Medicine X"));
appointmentRepository.save(appointment); // Saves both

// ❌ Wrong - no separate repository for children
// prescriptionRepository.save(prescription); // Not allowed!
```
- Production: Use Flyway or Liquibase for versioned migrations

---

#### 1.3.2 data.sql
**What it is**: SQL script file (src/main/resources/data.sql) executed after schema.sql to insert seed data.

**ASP.NET Core Equivalent**: 
- **data.sql** ≈ `DbContext.OnModelCreating()` with `HasData()` for seeding

```csharp
// .NET seeding
modelBuilder.Entity<Patient>().HasData(
    new Patient { Id = 1, Name = "John Doe" }
);
```

**Deprecation Check**: ⚠️ Limited flexibility  
**Modern Approach**: Use `CommandLineRunner` or `ApplicationRunner` beans for programmatic data loading (more flexible, database-agnostic).

---

### 1.4 Aggregate-Oriented Design (DDD)

#### 1.4.1 Aggregate
**What it is**: Cluster of domain objects treated as a single unit for data changes—has clear boundaries and internal consistency rules.

**Example**: 
- **Appointment** aggregate contains:
  - Appointment (root)
  - Prescriptions (children)
  - All Key Annotations Summary

| Annotation | Purpose | Applied To |
|------------|---------|------------|
| `@Repository` | Marks data access component | Repository classes |
| `@Transactional` | Declarative transaction management | Methods/Classes |
| `@Id` | Marks primary key field | Entity fields |
| `@Table` | Maps entity to table | Entity classes |
| `@Column` | Maps field to column | Entity fields |
| `@GeneratedValue` | Auto-generated ID strategy | ID fields |

### 1.7 Spring Data JDBC Key Interfaces

| Interface | Purpose | Methods Provided |
|-----------|---------|------------------|
| `Repository<T, ID>` | Marker interface | None (just marks as repo) |
| `CrudRepository<T, ID>` | Basic CRUD operations | 12 methods (save, findById, etc.) |
| `PagingAndSortingRepository<T, ID>` | Adds pagination/sorting | Extends CrudRepository + paging |
| `JdbcAggregateOperations` | Low-level JDBC operations | Advanced use cases
**Example**: 
- `Appointment` is aggregate root
- External code can't directly save `Prescription`—must go through `Appointment`

**ASP.NET Core Equivalent**: 
- Same concept; EF Core recommends but doesn't enforce

**Deprecation Check**: ✅ DDD best practice  
**Modern Approach**: Spring Data JDBC enforces this—child entities don't have separate repositories.

---

### 1.5 Modern vs Legacy Patterns

#### ✅ Modern Approach (Spring Boot 3.3+ / 2025)

**Do This:**
- ✅ PostgreSQL with Docker for local development
- ✅ Spring Data JDBC for simple domains
- ✅ Constructor injection for repository dependencies
- ✅ `@Repository` stereotype for JDBC implementations
- ✅ `application.yml` over `.properties`
- ✅ `BIGSERIAL` for PostgreSQL auto-increment IDs
- ✅ `CommandLineRunner` for data seeding
- ✅ `@Transactional` for multi-table operations
- ✅ Spring Boot 3.3.x with Java 17+

---

#### ❌ Deprecated/Discouraged Patterns

**Avoid This:**
- 🚫 Raw JDBC (Connection, PreparedStatement, ResultSet management)
- 🚫 XML-based configuration for data sources
- ⚠️ H2 in production (development only)
- ⚠️ Field injection for repositories (`@Autowired` on fields)
- 🚫 Storing passwords in `application.properties` (use environment variables)
- 🚫 Multiple repositories for aggregate children
- ⚠️ `IDENTITY` keyword (H2/SQL Server syntax—use `BIGSERIAL` for PostgreSQL)

---

### 1.6 Complete .NET to Spring Mapping Table

| Spring Concept | .NET Equivalent | Notes |
|----------------|-----------------|-------|
| `JdbcTemplate` | `Dapper` | Lightweight data access |
| `DataSource` | Connection string + pooling | Connection management |
| `CrudRepository<T, ID>` | `IRepository<T>` + implementation | Spring generates impl |
| `@Repository` | Service registration in DI | Stereotype annotation |
| `@Transactional` | `TransactionScope` or `[Transaction]` | Declarative transactions |
| `RowMapper<T>` | `Func<IDataReader, T>` | ResultSet to object mapping |
| `CommandLineRunner` | `IHostedService` | Startup initialization |
| `schema.sql` | EF Migrations | Schema management |
| Spring Data JDBC | EF Core (simpler) | Auto-generated repos |
| PostgreSQL JDBC driver | Npgsql | Database provider |

---

## 2. FOUNDATION: THE PROBLEM WITH RAW JDBC

> **Core Question**: Why do we need JdbcTemplate or Spring Data JDBC? To answer this, let's first experience the pain of raw JDBC.

---

### 2.1 What Is JDBC?

**JDBC (Java Database Connectivity)** is the low-level Java API for database access—the foundation of all Java database interactions. It provides:

1. **Connection management**: Open connections to databases
2. **Statement execution**: Run SQL queries and updates
3. **Result processing**: Read data from query results
4. **Transaction control**: Commit/rollback changes

Think of it as working directly with the database driver—you have complete control, but also complete responsibility for every detail.

**Real-World Analogy**:

JDBC is like **manually flying an airplane with mechanical controls**:
- You manually check fuel levels (connection pooling)
- You adjust every control surface by hand (statement creation)
- You read raw instruments (result set processing)
- You remember to shut down all systems (resource cleanup)

**JdbcTemplate is like an autopilot system**—handles the routine tasks automatically while you focus on the destination.

---

### 2.2 The Ceremony Problem: Raw JDBC Example

Let's look at a real-world scenario: fetching a patient's medical record from the database.

**Scenario**: Hospital Patient Lookup System  
**Requirement**: Find a patient by their medical record number (MRN)

#### Raw JDBC Implementation (The Hard Way)

```java
@Repository
public class PatientRepository {
    
    private DataSource dataSource; // Injected by Spring
    
    public Optional<Patient> findByMrn(String mrn) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Step 1: Get connection from pool
            connection = dataSource.getConnection();
            
            // Step 2: Create prepared statement
            statement = connection.prepareStatement(
                "SELECT id, name, date_of_birth, blood_type " +
                "FROM patient WHERE medical_record_number = ?"
            );
            
            // Step 3: Set query parameters
            statement.setString(1, mrn);
            
            // Step 4: Execute query
            resultSet = statement.executeQuery();
            
            // Step 5: Process results
            Patient patient = null;
            if (resultSet.next()) {
                patient = new Patient(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getDate("date_of_birth").toLocalDate(),
                    resultSet.getString("blood_type")
                );
            }
            
            return Optional.ofNullable(patient);
            
        } catch (SQLException e) {
            // Step 6: What to do here?
            // Connection failed? SQL syntax error? Constraint violation?
            // All wrapped in generic SQLException
            throw new RuntimeException("Database error: " + e.getMessage(), e);
            
        } finally {
            // Step 7: Cleanup in reverse order (critical!)
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // Swallow exception—what else can we do?
                }
            }
            
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // Swallow exception
                }
            }
            
            if (connection != null) {
                try {
                    connection.close(); // Return to pool
                } catch (SQLException e) {
                    // Swallow exception
                }
            }
        }
    }
}
```

**Count the lines**: 65 lines of code for a simple SELECT query!

---

### 2.3 Breaking Down the Problems

Let's analyze each problem with this raw JDBC approach:

#### Problem 1: Resource Management Complexity

**The Issue**: You must manually close three resources in the correct order:
1. ResultSet
2. PreparedStatement  
3. Connection

**What Goes Wrong**:
- Forget to close `ResultSet` → memory leak
- Forget to close `Statement` → memory leak
- Forget to close `Connection` → **connection pool exhaustion** (critical!)

**Real-World Impact**:

Imagine a hospital lab results system processing 10,000 queries per hour:
- 1 forgotten connection cleanup = 1 leaked connection
- Connection pool size = 20 connections
- **System crashes in 72 seconds** (20 connections ÷ 10,000/hour)

---

#### Problem 2: Nested Try-Catch-Finally Blocks

**The Issue**: To ensure cleanup, you need nested try-catch blocks in the `finally` section. This creates "arrow code":

```java
finally {
    if (resultSet != null) {
        try {
            resultSet.close();
        } catch (SQLException e) {} // Level 1
    }
    if (statement != null) {
        try {
            statement.close();
        } catch (SQLException e) {} // Level 2
    }
    if (connection != null) {
        try {
            connection.close();
        } catch (SQLException e) {} // Level 3
    }
}
```

**Why It's Ugly**: Cleanup code is longer than business logic!

---

#### Problem 3: SQLException Is Too Generic

**The Issue**: `SQLException` is a checked exception covering hundreds of scenarios:
- Network failure
- Wrong credentials
- Table doesn't exist
- Constraint violation
- Syntax error
- Deadlock

**The Problem**: Your catch block can't meaningfully handle most of these:

```java
catch (SQLException e) {
    // What can I do here?
    // Retry? Log? Convert to business exception?
    // Code: 23505 = unique violation
    // Code: 42P01 = table not found
    // Code: 08006 = connection failure
    // But you have to check error codes manually!
}
```

---

#### Problem 4: Boilerplate Everywhere

**The Issue**: For every query method, you repeat:
- Get connection
- Create statement
- Set parameters
- Execute query
- Map ResultSet to object
- Close resources (with nested try-catch)

**Real-World Example**: A Patient repository with 5 methods:
- `findById()`
- `findByMrn()`
- `findByNameLike()`
- `save()`
- `update()`

**Result**: 250+ lines of repetitive boilerplate code!

---

### 2.4 The Manual Mapping Problem

Even the "business logic" part (mapping ResultSet to Patient) is tedious:

```java
if (resultSet.next()) {
    patient = new Patient(
        resultSet.getLong("id"),           // What if column doesn't exist?
        resultSet.getString("name"),        // What if it's NULL?
        resultSet.getDate("date_of_birth").toLocalDate(),  // NullPointerException risk!
        resultSet.getString("blood_type")
    );
}
```

**Problems**:
- Column name typos cause runtime errors
- NULL handling requires verbose checks
- Type conversions are manual (Date → LocalDate)
- No compile-time safety

---

### 2.5 Checkpoint Question 1

**Scenario**: Your hospital appointment system processes 500 appointment bookings per hour during peak times. Each booking requires 3 database queries:

1. Check doctor availability
2. Check patient insurance
3. Create appointment record

You have a PostgreSQL connection pool with **20 connections**. A developer forgets to close connections in the `finally` block. How long before the system crashes?

**Answer**:

**Calculation**:
- 500 bookings/hour × 3 queries = 1,500 queries/hour
- 1,500 queries/hour ÷ 60 minutes = 25 queries/minute
- Connection pool size = 20 connections

**If 100% leak** (all forgotten):  
20 connections ÷ 25 queries/min = **0.8 minutes = 48 seconds**

**If 10% leak** (developer fixed 2 of 3):  
20 connections ÷ 2.5 queries/min = **8 minutes**

**Real Impact**: After connection pool exhaustion, new appointment requests hang indefinitely, waiting for available connections. User experience: "The website is frozen."

**Why JdbcTemplate Matters**: Automatically closes connections—**leak impossible**.

---

### 2.6 Checkpoint Question 2

**Scenario**: You're reviewing code for a medical records system. The developer wrote:

```java
public Patient findById(Long id) {
    Connection conn = dataSource.getConnection();
    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM patient WHERE id = ?");
    stmt.setLong(1, id);
    ResultSet rs = stmt.executeQuery();
    
    if (rs.next()) {
        return new Patient(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("dob").toLocalDate(),
            rs.getString("blood_type")
        );
    }
    return null;
}
```

**What's critically wrong with this code?** List at least 3 issues.

**Answer**:

**Critical Issues**:

1. **No Resource Cleanup**: Connection, statement, and result set are never closed → **connection leak**. After 20 calls, the connection pool is exhausted.

2. **No Exception Handling**: `getConnection()`, `prepareStatement()`, and `executeQuery()` throw `SQLException` (checked exception). Code won't compile as-is. If wrapped in try-catch, exceptions disappear silently.

3. **NullPointerException Risk**: If `date_of_birth` column is NULL, `rs.getDate("dob")` returns null, then calling `.toLocalDate()` throws `NullPointerException`.

4. **Returns null**: Instead of `Optional<Patient>`, returns null—forces callers to remember null checks (error-prone).

**Fixed Version (Still Verbose)**:

```java
public Optional<Patient> findById(Long id) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement("SELECT * FROM patient WHERE id = ?");
        stmt.setLong(1, id);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            Date dob = rs.getDate("dob");
            return Optional.of(new Patient(
                rs.getLong("id"),
                rs.getString("name"),
                dob != null ? dob.toLocalDate() : null,
                rs.getString("blood_type")
            ));
        }
        return Optional.empty();
        
    } catch (SQLException e) {
        throw new DataAccessException("Failed to find patient", e);
    } finally {
        // Close rs, stmt, conn (nested try-catch blocks)
    }
}
```

Still 30+ lines for a simple query!

---

### 2.7 The "Needle in a Haystack" Problem

Look at this actual raw JDBC code from a production system:

```java
public List<Appointment> findUpcoming(Long doctorId) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
        connection = dataSource.getConnection();
        statement = connection.prepareStatement(
            "SELECT a.id, a.scheduled_at, a.status, " +
            "       p.name as patient_name, p.medical_record_number " +
            "FROM appointment a " +
            "JOIN patient p ON a.patient_id = p.id " +
            "WHERE a.doctor_id = ? AND a.scheduled_at > CURRENT_TIMESTAMP " +
            "ORDER BY a.scheduled_at"
        );
        statement.setLong(1, doctorId);
        resultSet = statement.executeQuery();
        
        List<Appointment> appointments = new ArrayList<>();
        while (resultSet.next()) {
            Appointment apt = new Appointment();
            apt.setId(resultSet.getLong("id"));
            apt.setScheduledAt(resultSet.getTimestamp("scheduled_at").toLocalDateTime());
            apt.setStatus(resultSet.getString("status"));
            apt.setPatientName(resultSet.getString("patient_name"));
            appointments.add(apt);
        }
        return appointments;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    } finally {
        if (resultSet != null) { try { resultSet.close(); } catch (SQLException e) {} }
        if (statement != null) { try { statement.close(); } catch (SQLException e) {} }
        if (connection != null) { try { connection.close(); } catch (SQLException e) {} }
    }
}
```

**Question**: Can you quickly spot the actual SQL query and business logic?

**Answer**: It's buried in lines 7-12. Everything else is ceremony—connection management, resource cleanup, exception handling. The **signal-to-noise ratio is terrible**.

---

### 2.8 Summary: Why We Need Better Abstractions

Raw JDBC forces you to:
- ✅ Write 50+ lines for simple queries
- ✅ Remember to close 3 resources in correct order
- ✅ Handle generic `SQLException` that you can't fix
- ✅ Manually map `ResultSet` to objects
- ✅ Repeat the same boilerplate for every method
- ✅ Risk connection leaks with every mistake

**What developers really want**:
```java
// Just this!
public Optional<Patient> findByMrn(String mrn) {
    return jdbcTemplate.query(
        "SELECT id, name, date_of_birth, blood_type FROM patient WHERE medical_record_number = ?",
        this::mapRowToPatient,
        mrn
    ).stream().findFirst();
}

private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
    return new Patient(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getDate("date_of_birth").toLocalDate(),
        rs.getString("blood_type")
    );
}
```

**8 lines instead of 65!**

---

### 2.9 Real-World Impact Story

**True Story**: A fintech startup built a payment processing system using raw JDBC. During Black Friday:

- Traffic spike: 50,000 transactions/hour
- Developer forgot to close connections in one repository method
- Connection pool (30 connections) exhausted in **36 seconds**
- System crashed for 2 hours during peak sales
- **Revenue loss**: $500,000
- **Root cause**: Missing `connection.close()` in finally block

**After Migrating to JdbcTemplate**:
- Connection leaks: Impossible (automatic cleanup)
- Code reduction: 60% less boilerplate
- Next Black Friday: Zero outages

---

**Next Step**: Now that we understand the pain, let's see how JdbcTemplate solves these problems in Step 3!

---

## 3. JDBCTEMPLATE: THE SPRING SOLUTION

> **Goal**: Learn how JdbcTemplate eliminates 90% of JDBC boilerplate while maintaining SQL control and performance.

---

### 3.1 What Is JdbcTemplate?

**JdbcTemplate** is Spring's abstraction over raw JDBC—a template class that handles all the ceremony while you focus on SQL and result mapping.

**What It Provides**:
1. **Automatic resource management** - Opens/closes connections, statements, result sets
2. **Exception translation** - Converts `SQLException` to Spring's `DataAccessException` hierarchy
3. **Simplified query methods** - `query()`, `update()`, `queryForObject()`, `batchUpdate()`
4. **Callback mechanisms** - For complex scenarios requiring fine-grained control

**What It Doesn't Do**:
- ❌ Generate SQL for you (you write SQL explicitly)
- ❌ Track entity changes (no dirty checking)
- ❌ Lazy loading or caching
- ❌ Complex object-relational mapping

**When to Use JdbcTemplate**:
- ✅ Custom queries with complex joins
- ✅ Stored procedure calls
- ✅ Batch operations
- ✅ Database-specific SQL features
- ✅ Performance-critical queries requiring hand-tuned SQL

---

### 3.2 The Same Query: Raw JDBC vs JdbcTemplate

Let's compare the patient lookup we saw earlier.

#### Raw JDBC Version (65 lines)

```java
@Repository
public class PatientRepository {
    private DataSource dataSource;
    
    public Optional<Patient> findByMrn(String mrn) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(
                "SELECT id, name, date_of_birth, blood_type " +
                "FROM patient WHERE medical_record_number = ?"
            );
            statement.setString(1, mrn);
            resultSet = statement.executeQuery();
            
            Patient patient = null;
            if (resultSet.next()) {
                patient = new Patient(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getDate("date_of_birth").toLocalDate(),
                    resultSet.getString("blood_type")
                );
            }
            return Optional.ofNullable(patient);
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            // 20+ lines of cleanup code
            if (resultSet != null) {
                try { resultSet.close(); } catch (SQLException e) {}
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) {}
            }
        }
    }
}
```

#### JdbcTemplate Version (11 lines)

```java
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Optional<Patient> findByMrn(String mrn) {
        List<Patient> results = jdbcTemplate.query(
            "SELECT id, name, date_of_birth, blood_type " +
            "FROM patient WHERE medical_record_number = ?",
            this::mapRowToPatient,
            mrn
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
        return new Patient(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("date_of_birth").toLocalDate(),
            rs.getString("blood_type")
        );
    }
}
```

**Code Reduction**: 65 lines → 11 lines = **83% less code**

---

### 3.3 How JdbcTemplate Works Under the Hood

When you call `jdbcTemplate.query()`, Spring executes this sequence:

**Step 1: Acquire Connection**
```java
Connection conn = dataSource.getConnection(); // From pool
```

**Step 2: Create PreparedStatement**
```java
PreparedStatement ps = conn.prepareStatement(sql);
```

**Step 3: Set Parameters**
```java
ps.setString(1, mrn); // Your parameter
```

**Step 4: Execute Query**
```java
ResultSet rs = ps.executeQuery();
```

**Step 5: Map Results**
```java
while (rs.next()) {
    Patient patient = rowMapper.mapRow(rs, rowNum++);
    results.add(patient);
}
```

**Step 6: Cleanup (Always Executed)**
```java
try {
    rs.close();
    ps.close();
    conn.close(); // Return to pool
} catch (SQLException e) {
    // Logged but doesn't fail
}
```

**The Magic**: All of this happens automatically. You only wrote the SQL and mapping logic.

---

### 3.4 Core JdbcTemplate Methods

#### 3.4.1 query() - SELECT Returning Multiple Rows

**Method Signature**:
```java
<T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args)
```

**Example - Find All Patients by Blood Type**:
```java
public List<Patient> findByBloodType(String bloodType) {
    return jdbcTemplate.query(
        "SELECT id, name, date_of_birth, blood_type " +
        "FROM patient WHERE blood_type = ? " +
        "ORDER BY name",
        this::mapRowToPatient,
        bloodType
    );
}
```

**What Happens**:
1. Spring executes query with parameter `bloodType`
2. For each row in ResultSet, calls `mapRowToPatient()`
3. Collects results into `List<Patient>`
4. Closes all resources automatically

---

#### 3.4.2 queryForObject() - SELECT Returning Single Row

**Method Signature**:
```java
<T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args)
```

**Example - Find Patient by ID**:
```java
public Patient findById(Long id) {
    return jdbcTemplate.queryForObject(
        "SELECT id, name, date_of_birth, blood_type " +
        "FROM patient WHERE id = ?",
        this::mapRowToPatient,
        id
    );
}
```

**Important**: Throws `EmptyResultDataAccessException` if no rows found. Wrap in try-catch or use `query()` for Optional.

**Better with Optional**:
```java
public Optional<Patient> findById(Long id) {
    try {
        Patient patient = jdbcTemplate.queryForObject(
            "SELECT id, name, date_of_birth, blood_type FROM patient WHERE id = ?",
            this::mapRowToPatient,
            id
        );
        return Optional.ofNullable(patient);
    } catch (EmptyResultDataAccessException e) {
        return Optional.empty();
    }
}
```

---

#### 3.4.3 update() - INSERT, UPDATE, DELETE

**Method Signature**:
```java
int update(String sql, Object... args)
```

**Returns**: Number of rows affected

**Example - Insert Patient**:
```java
public void save(Patient patient) {
    jdbcTemplate.update(
        "INSERT INTO patient (name, date_of_birth, blood_type, medical_record_number) " +
        "VALUES (?, ?, ?, ?)",
        patient.getName(),
        Date.valueOf(patient.getDateOfBirth()),
        patient.getBloodType(),
        patient.getMedicalRecordNumber()
    );
}
```

**Example - Update Patient**:
```java
public int updateBloodType(Long patientId, String newBloodType) {
    return jdbcTemplate.update(
        "UPDATE patient SET blood_type = ? WHERE id = ?",
        newBloodType,
        patientId
    );
}
```

**Example - Delete Patient**:
```java
public int deleteById(Long id) {
    return jdbcTemplate.update(
        "DELETE FROM patient WHERE id = ?",
        id
    );
}
```

---

#### 3.4.4 queryForObject() - Single Value Queries

**Method Signature**:
```java
<T> T queryForObject(String sql, Class<T> requiredType, Object... args)
```

**Example - Count Patients**:
```java
public long countPatients() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM patient",
        Long.class
    );
}
```

**Example - Get Patient Name**:
```java
public String getPatientName(Long id) {
    return jdbcTemplate.queryForObject(
        "SELECT name FROM patient WHERE id = ?",
        String.class,
        id
    );
}
```

---

### 3.5 RowMapper: The Bridge Between SQL and Objects

**RowMapper** is a functional interface that maps a `ResultSet` row to a Java object.

**Interface Definition**:
```java
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```

**Three Ways to Implement**:

#### Option 1: Method Reference (Cleanest)
```java
public List<Patient> findAll() {
    return jdbcTemplate.query(
        "SELECT id, name, date_of_birth, blood_type FROM patient",
        this::mapRowToPatient
    );
}

private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
    return new Patient(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getDate("date_of_birth").toLocalDate(),
        rs.getString("blood_type")
    );
}
```

#### Option 2: Lambda Expression
```java
public List<Patient> findAll() {
    return jdbcTemplate.query(
        "SELECT id, name, date_of_birth, blood_type FROM patient",
        (rs, rowNum) -> new Patient(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("date_of_birth").toLocalDate(),
            rs.getString("blood_type")
        )
    );
}
```

#### Option 3: Explicit Implementation
```java
public List<Patient> findAll() {
    return jdbcTemplate.query(
        "SELECT id, name, date_of_birth, blood_type FROM patient",
        new RowMapper<Patient>() {
            @Override
            public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Patient(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getDate("date_of_birth").toLocalDate(),
                    rs.getString("blood_type")
                );
            }
        }
    );
}
```

**Best Practice**: Use method reference for reusable mappers, lambda for one-off queries.

---

### 3.6 Handling NULL Values Safely

**Problem**: `ResultSet.getXxx()` returns Java primitive defaults for NULL:
- `getLong()` returns `0` for NULL
- `getInt()` returns `0` for NULL
- `getDate()` returns `null` for NULL

**Solution**: Check `wasNull()` or use wrapper types.

**Example - Safe NULL Handling**:
```java
private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
    Long id = rs.getLong("id");
    String name = rs.getString("name");
    
    // Handle nullable date
    Date dob = rs.getDate("date_of_birth");
    LocalDate dateOfBirth = (dob != null) ? dob.toLocalDate() : null;
    
    // Handle nullable string
    String bloodType = rs.getString("blood_type");
    
    return new Patient(id, name, dateOfBirth, bloodType);
}
```

---

### 3.7 Checkpoint Question 3

**Scenario**: You need to fetch all appointments for a specific doctor scheduled after a certain date. Write the method using JdbcTemplate.

**Requirements**:
- Table: `appointment`
- Columns: `id`, `doctor_id`, `patient_id`, `scheduled_at`, `status`
- Filter by `doctor_id` and `scheduled_at > ?`
- Return `List<Appointment>`

**Answer**:

```java
@Repository
public class AppointmentRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public List<Appointment> findUpcomingByDoctor(Long doctorId, LocalDateTime fromDate) {
        return jdbcTemplate.query(
            "SELECT id, doctor_id, patient_id, scheduled_at, status " +
            "FROM appointment " +
            "WHERE doctor_id = ? AND scheduled_at > ? " +
            "ORDER BY scheduled_at",
            this::mapRowToAppointment,
            doctorId,
            Timestamp.valueOf(fromDate)
        );
    }
    
    private Appointment mapRowToAppointment(ResultSet rs, int rowNum) throws SQLException {
        return new Appointment(
            rs.getLong("id"),
            rs.getLong("doctor_id"),
            rs.getLong("patient_id"),
            rs.getTimestamp("scheduled_at").toLocalDateTime(),
            rs.getString("status")
        );
    }
}
```

**Key Points**:
- Two parameters: `doctorId` and `fromDate`
- `LocalDateTime` converted to `Timestamp` for JDBC
- Method reference for clean code
- `ORDER BY` for consistent results

---

### 3.8 Exception Translation: SQLException → DataAccessException

**The Problem with SQLException**:
Raw JDBC throws `SQLException` (checked exception) for everything:
- Network failure (can't reach database)
- Wrong credentials (authentication failed)
- Syntax error (bad SQL)
- Constraint violation (duplicate key)
- Deadlock (concurrent transaction conflict)

**Spring's Solution**: Translate to a hierarchy of `DataAccessException` (unchecked):

```
DataAccessException (root)
├── DataIntegrityViolationException
│   ├── DuplicateKeyException
│   └── DataIntegrityViolationException
├── DataAccessResourceFailureException
│   ├── CannotAcquireLockException
│   └── DeadlockLoserDataAccessException
├── BadSqlGrammarException
├── EmptyResultDataAccessException
└── IncorrectResultSizeDataAccessException
```

**Example - Catching Specific Exceptions**:
```java
public void savePatient(Patient patient) {
    try {
        jdbcTemplate.update(
            "INSERT INTO patient (medical_record_number, name) VALUES (?, ?)",
            patient.getMedicalRecordNumber(),
            patient.getName()
        );
    } catch (DuplicateKeyException e) {
        throw new BusinessException("Patient with MRN " + 
            patient.getMedicalRecordNumber() + " already exists");
    } catch (DataAccessException e) {
        throw new BusinessException("Failed to save patient: " + e.getMessage());
    }
}
```

**Benefits**:
- ✅ Catch only exceptions you can handle
- ✅ Let others bubble up (network failures, etc.)
- ✅ Database-agnostic code (works with PostgreSQL, MySQL, Oracle)

---

### 3.9 Medical System Complete Repository Example

Let's build a complete `PatientRepository` with all CRUD operations.

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPatientRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public JdbcPatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // CREATE
    public void save(Patient patient) {
        jdbcTemplate.update(
            "INSERT INTO patient (name, date_of_birth, blood_type, medical_record_number) " +
            "VALUES (?, ?, ?, ?)",
            patient.getName(),
            Date.valueOf(patient.getDateOfBirth()),
            patient.getBloodType(),
            patient.getMedicalRecordNumber()
        );
    }
    
    // READ - All
    public List<Patient> findAll() {
        return jdbcTemplate.query(
            "SELECT id, name, date_of_birth, blood_type, medical_record_number " +
            "FROM patient ORDER BY name",
            this::mapRowToPatient
        );
    }
    
    // READ - By ID
    public Optional<Patient> findById(Long id) {
        try {
            Patient patient = jdbcTemplate.queryForObject(
                "SELECT id, name, date_of_birth, blood_type, medical_record_number " +
                "FROM patient WHERE id = ?",
                this::mapRowToPatient,
                id
            );
            return Optional.ofNullable(patient);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    // READ - By Medical Record Number
    public Optional<Patient> findByMrn(String mrn) {
        List<Patient> results = jdbcTemplate.query(
            "SELECT id, name, date_of_birth, blood_type, medical_record_number " +
            "FROM patient WHERE medical_record_number = ?",
            this::mapRowToPatient,
            mrn
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    // READ - By Blood Type
    public List<Patient> findByBloodType(String bloodType) {
        return jdbcTemplate.query(
            "SELECT id, name, date_of_birth, blood_type, medical_record_number " +
            "FROM patient WHERE blood_type = ? ORDER BY name",
            this::mapRowToPatient,
            bloodType
        );
    }
    
    // UPDATE
    public int update(Patient patient) {
        return jdbcTemplate.update(
            "UPDATE patient SET name = ?, date_of_birth = ?, " +
            "blood_type = ?, medical_record_number = ? WHERE id = ?",
            patient.getName(),
            Date.valueOf(patient.getDateOfBirth()),
            patient.getBloodType(),
            patient.getMedicalRecordNumber(),
            patient.getId()
        );
    }
    
    // DELETE
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM patient WHERE id = ?", id);
    }
    
    // COUNT
    public long count() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM patient",
            Long.class
        );
    }
    
    // EXISTS
    public boolean existsByMrn(String mrn) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM patient WHERE medical_record_number = ?",
            Long.class,
            mrn
        );
        return count != null && count > 0;
    }
    
    // Private mapper
    private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getLong("id"));
        patient.setName(rs.getString("name"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }
        
        patient.setBloodType(rs.getString("blood_type"));
        patient.setMedicalRecordNumber(rs.getString("medical_record_number"));
        
        return patient;
    }
}
```

---

### 3.10 Summary: JdbcTemplate Benefits

| Aspect | Raw JDBC | JdbcTemplate |
|--------|----------|--------------|
| **Lines of code** | 50-70 per method | 5-15 per method |
| **Resource cleanup** | Manual (error-prone) | Automatic |
| **Exception handling** | Generic SQLException | Specific exceptions |
| **Connection leaks** | Possible | Impossible |
| **Testability** | Difficult | Easy (mock JdbcTemplate) |
| **SQL control** | Full | Full |
| **Performance** | Optimal | Optimal (no overhead) |

**When to Use JdbcTemplate**:
- ✅ You need full control over SQL
- ✅ Complex queries with joins, subqueries
- ✅ Database-specific features (PostgreSQL arrays, JSON columns)
- ✅ Batch operations for performance
- ✅ Stored procedure calls

**When NOT to Use** (use Spring Data JDBC instead):
- ❌ Simple CRUD operations
- ❌ Standard entity persistence
- ❌ Want generated queries

---

**Next Step**: Now that we understand JdbcTemplate, let's prepare our domain objects for persistence in Step 4!

---

## 4. DOMAIN MODELING FOR PERSISTENCE

> **Goal**: Design domain classes that map cleanly to database tables while maintaining object-oriented principles.

---

### 4.1 Why Domain Modeling Matters

**The Challenge**: Databases think in tables/rows/columns. Java thinks in objects/classes/fields. We need to bridge this gap.

**Key Principles**:
1. **Identity**: Every persisted entity needs a unique identifier (ID)
2. **Timestamps**: Track when entities are created/modified for auditing
3. **Aggregates**: Group related entities that change together
4. **Value Objects**: Represent concepts without identity (e.g., Address)

---

### 4.2 The Medical Appointment System Domain

Let's design a complete domain model for our hospital system.

**Core Entities**:
- **Patient** - Person receiving medical care
- **Doctor** - Medical professional
- **Appointment** - Scheduled meeting between patient and doctor
- **Prescription** - Medication prescribed during appointment
- **MedicalRecord** - Historical medical data

**Relationships**:
- Patient → Appointments (one-to-many)
- Doctor → Appointments (one-to-many)
- Appointment → Prescriptions (one-to-many)
- Patient → MedicalRecords (one-to-many)

---

### 4.3 Adding ID and Timestamp Fields

Every entity that will be persisted needs:
1. **ID field** - Unique identifier (usually auto-generated)
2. **Timestamp field(s)** - Creation/modification tracking

#### Before: Simple POJO

```java
public class Patient {
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    
    // Constructor, getters, setters
}
```

#### After: Persistence-Ready Entity

```java
public class Patient {
    private Long id;                    // ← Added for database identity
    private LocalDateTime createdAt;    // ← Added for audit trail
    
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    
    // Constructor, getters, setters
}
```

**Why Long for ID?**
- `Long` supports null (entity not yet saved)
- `long` primitive defaults to `0` (confusing)
- PostgreSQL BIGSERIAL generates 64-bit integers

---

### 4.4 Complete Patient Entity

```java
package com.medical.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Patient {
    
    // Database identity
    private Long id;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business fields
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    private String email;
    private String phoneNumber;
    
    // Constructors
    public Patient() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Patient(String name, LocalDate dateOfBirth, String bloodType, String mrn) {
        this();
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.medicalRecordNumber = mrn;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }
    
    public void setMedicalRecordNumber(String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    // equals() and hashCode() based on business key (MRN)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(medicalRecordNumber, patient.medicalRecordNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(medicalRecordNumber);
    }
    
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mrn='" + medicalRecordNumber + '\'' +
                '}';
    }
}
```

**Key Design Decisions**:

1. **No-arg constructor**: Required by many frameworks (including Spring Data JDBC later)
2. **Business constructor**: Sets required fields, initializes `createdAt`
3. **equals/hashCode**: Based on `medicalRecordNumber` (business key), not `id`
4. **toString()**: Includes only key fields (not all for brevity)

---

### 4.5 Using Lombok to Reduce Boilerplate

**Problem**: The Patient class above has 150+ lines, mostly getters/setters.

**Solution**: Use Lombok annotations to generate boilerplate at compile-time.

#### Add Lombok Dependency

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

#### Patient with Lombok

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    private String email;
    private String phoneNumber;
}
```

**Code Reduction**: 150 lines → 20 lines = **87% reduction**

**Lombok Annotations Explained**:
- `@Data` - Generates getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - Generates no-argument constructor
- `@AllArgsConstructor` - Generates constructor with all fields

**Additional Useful Annotations**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Enables Patient.builder().name("John").build()
public class Patient {
    // fields
}
```

---

### 4.6 Appointment Entity with Relationships

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Appointment {
    
    // Identity
    private Long id;
    
    // Audit
    private LocalDateTime createdAt;
    
    // Foreign keys
    private Long patientId;
    private Long doctorId;
    
    // Business fields
    private LocalDateTime scheduledAt;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private String notes;
    
    // Child entities (aggregate)
    private List<Prescription> prescriptions = new ArrayList<>();
    
    public Appointment(Long patientId, Long doctorId, LocalDateTime scheduledAt) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.scheduledAt = scheduledAt;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }
    
    // Helper method to add prescriptions
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }
}
```

---

### 4.7 Prescription Entity (Child of Appointment)

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    
    private Long id;
    
    // Foreign key to parent aggregate
    private Long appointmentId;
    
    // Business fields
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
}
```

**Key Point**: `Prescription` has `appointmentId` foreign key—it's part of the `Appointment` aggregate.

---

### 4.8 Understanding Aggregates

**Aggregate**: A cluster of domain objects treated as a single unit for data changes.

**Appointment Aggregate**:
```
Appointment (root)
├── Prescription 1
├── Prescription 2
└── Prescription 3
```

**Rules**:
1. **Appointment is the aggregate root** - Only entry point for modifications
2. **Prescriptions are children** - Can only be accessed through Appointment
3. **Transactional boundary** - Save/delete Appointment saves/deletes all Prescriptions
4. **No separate repository for Prescription** - Managed through AppointmentRepository

**Example - Saving Aggregate**:
```java
// Create appointment with prescriptions
Appointment apt = new Appointment(patientId, doctorId, scheduledAt);
apt.addPrescription(new Prescription(null, null, "Aspirin", "100mg", "Daily", 30, "Take with food"));
apt.addPrescription(new Prescription(null, null, "Vitamin D", "1000 IU", "Daily", 90, "Morning"));

// Save through repository - both appointment and prescriptions saved
appointmentRepository.save(apt);
```

---

### 4.9 Checkpoint Question 4

**Scenario**: Design a `Doctor` entity for the medical system with these requirements:

1. Must have database identity (ID)
2. Track creation timestamp
3. Business fields: name, specialization, licenseNumber, email, phoneNumber
4. License number is unique and serves as business key
5. Use Lombok to minimize boilerplate

**Answer**:

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "licenseNumber") // Business key
public class Doctor {
    
    // Database identity
    private Long id;
    
    // Audit
    private LocalDateTime createdAt;
    
    // Business fields
    private String name;
    private String specialization;
    private String licenseNumber; // Business key (unique)
    private String email;
    private String phoneNumber;
    
    // Business constructor
    public Doctor(String name, String specialization, String licenseNumber) {
        this.name = name;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.createdAt = LocalDateTime.now();
    }
}
```

**Key Design Choices**:
- `@EqualsAndHashCode(of = "licenseNumber")` - equals/hashCode based on business key, not ID
- Business constructor for required fields only
- `createdAt` initialized automatically
- No `updatedAt` (doctors rarely change core info)

---

### 4.10 Value Objects vs Entities

**Entity**: Has identity (ID), lifecycle, mutable
- Example: Patient, Appointment, Doctor

**Value Object**: No identity, immutable, defined by attributes
- Example: Address, Money, DateRange

**Address Value Object**:
```java
package com.medical.domain;

import lombok.Value;

@Value // Immutable class
public class Address {
    String street;
    String city;
    String state;
    String zipCode;
    String country;
}
```

**Using in Patient**:
```java
@Data
public class Patient {
    private Long id;
    private String name;
    private Address homeAddress; // Value object embedded
    
    // Getters/setters
}
```

**Database Mapping**: Address fields flattened into patient table:
```sql
CREATE TABLE patient (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    home_address_street VARCHAR(100),
    home_address_city VARCHAR(50),
    home_address_state VARCHAR(2),
    home_address_zip_code VARCHAR(10),
    home_address_country VARCHAR(50)
);
```

---

### 4.11 Summary: Domain Design Best Practices

**Always Include**:
- ✅ `Long id` field (database identity)
- ✅ `LocalDateTime createdAt` (audit trail)
- ✅ No-arg constructor (framework requirement)
- ✅ Business constructor for required fields

**Consider Including**:
- 🟡 `LocalDateTime updatedAt` (if entity changes frequently)
- 🟡 `@Version` field (for optimistic locking in concurrent scenarios)
- 🟡 Soft delete flag (e.g., `boolean deleted`)

**equals/hashCode**:
- ✅ Base on business key (e.g., `medicalRecordNumber`), not `id`
- ✅ Use `@EqualsAndHashCode(of = "businessKey")` with Lombok

**Lombok Usage**:
- ✅ Use `@Data` for entities with mutable fields
- ✅ Use `@Value` for immutable value objects
- ✅ Use `@Builder` for complex object construction

**Aggregate Design**:
- ✅ Identify aggregate roots (e.g., Appointment)
- ✅ Children reference parent (e.g., Prescription → appointmentId)
- ✅ No separate repositories for children

---

**Next Step**: With domain objects ready, we'll set up VS Code and PostgreSQL Docker in Step 5!

---

## 5. VS CODE + POSTGRESQL DOCKER SETUP

> **Goal**: Set up a professional development environment with PostgreSQL running in Docker and VS Code configured for Java/Spring development.

---

### 5.1 Why PostgreSQL in Docker?

**Docker Benefits**:
- ✅ Consistent environment across team members
- ✅ No local PostgreSQL installation required
- ✅ Easy to reset/recreate database
- ✅ Matches production environment
- ✅ Version pinning (PostgreSQL 16)

**Alternative (Not Recommended)**: Local PostgreSQL installation
- ❌ Different versions across team
- ❌ Port conflicts with other apps
- ❌ Hard to reset to clean state
- ❌ OS-specific installation steps

---

### 5.2 Prerequisites

**Required Software**:
1. **Docker Desktop** - [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. **VS Code** - [https://code.visualstudio.com](https://code.visualstudio.com)
3. **Java 17 or 21** - [https://adoptium.net](https://adoptium.net) (Eclipse Temurin)

**Verify Installation**:
```powershell
# Check Docker
docker --version
# Expected: Docker version 24.0.0 or higher

# Check Java
java --version
# Expected: openjdk 17.0.x or 21.0.x

# Check Maven (comes with Spring Boot project)
mvn --version
# Expected: Apache Maven 3.9.x
```

---

### 5.3 PostgreSQL Docker Compose Configuration

**Create `docker-compose.yml` in project root**:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: medical-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: medical_system
      POSTGRES_USER: medicaladmin
      POSTGRES_PASSWORD: SecurePass123!
      PGDATA: /var/lib/postgresql/data/pgdata
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U medicaladmin -d medical_system"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres-data:
    driver: local
```

**Configuration Explained**:
- `image: postgres:16-alpine` - PostgreSQL 16 with minimal Alpine Linux (smaller image)
- `container_name` - Easy to reference in commands
- `restart: unless-stopped` - Auto-restart on system reboot
- `POSTGRES_DB` - Default database name
- `POSTGRES_USER/PASSWORD` - Admin credentials
- `ports: 5432:5432` - Expose PostgreSQL on localhost:5432
- `volumes` - Persist data across container restarts
- `healthcheck` - Docker knows when database is ready

---

### 5.4 Database Initialization Script

**Create `init-scripts/01-schema.sql`**:

```sql
-- Medical Appointment System Schema
-- PostgreSQL 16

-- Enable UUID extension (for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Patient table
CREATE TABLE patient (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    blood_type VARCHAR(5),
    medical_record_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    
    CONSTRAINT chk_blood_type CHECK (blood_type IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'))
);

-- Doctor table
CREATE TABLE doctor (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(50) NOT NULL,
    license_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    phone_number VARCHAR(20)
);

-- Appointment table
CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    
    CONSTRAINT chk_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))
);

-- Prescription table (child of appointment)
CREATE TABLE prescription (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointment(id) ON DELETE CASCADE,
    
    medication_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration_days INTEGER NOT NULL,
    instructions TEXT
);

-- Indexes for performance
CREATE INDEX idx_patient_mrn ON patient(medical_record_number);
CREATE INDEX idx_doctor_license ON doctor(license_number);
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor ON appointment(doctor_id);
CREATE INDEX idx_appointment_scheduled ON appointment(scheduled_at);
CREATE INDEX idx_prescription_appointment ON prescription(appointment_id);

-- Comments for documentation
COMMENT ON TABLE patient IS 'Patients registered in the medical system';
COMMENT ON TABLE doctor IS 'Medical professionals who can see patients';
COMMENT ON TABLE appointment IS 'Scheduled appointments between patients and doctors';
COMMENT ON TABLE prescription IS 'Medications prescribed during appointments';

COMMENT ON COLUMN patient.medical_record_number IS 'Unique identifier for patient records (MRN)';
COMMENT ON COLUMN doctor.license_number IS 'Medical license number (unique per doctor)';
COMMENT ON COLUMN appointment.status IS 'Current status: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW';
```

**Key Design Elements**:
- `BIGSERIAL` - Auto-incrementing 64-bit integers
- `created_at` with `DEFAULT CURRENT_TIMESTAMP` - Automatic timestamping
- `UNIQUE` constraints on business keys (MRN, license number)
- `REFERENCES` with `ON DELETE CASCADE` - Referential integrity
- `CHECK` constraints for valid values
- Indexes on foreign keys and frequently queried columns

---

### 5.5 Sample Data Script

**Create `init-scripts/02-seed-data.sql`**:

```sql
-- Sample data for development/testing

-- Insert doctors
INSERT INTO doctor (name, specialization, license_number, email, phone_number) VALUES
('Dr. Sarah Johnson', 'Cardiology', 'MED-2018-001', 'sjohnson@medical.com', '555-0101'),
('Dr. Michael Chen', 'Pediatrics', 'MED-2019-042', 'mchen@medical.com', '555-0102'),
('Dr. Emily Rodriguez', 'Orthopedics', 'MED-2020-089', 'erodriguez@medical.com', '555-0103'),
('Dr. James Williams', 'Neurology', 'MED-2017-125', 'jwilliams@medical.com', '555-0104');

-- Insert patients
INSERT INTO patient (name, date_of_birth, blood_type, medical_record_number, email, phone_number) VALUES
('John Smith', '1980-05-15', 'O+', 'MRN-2024-001', 'jsmith@email.com', '555-1001'),
('Emma Davis', '1992-08-22', 'A+', 'MRN-2024-002', 'edavis@email.com', '555-1002'),
('Robert Brown', '1975-03-10', 'B-', 'MRN-2024-003', 'rbrown@email.com', '555-1003'),
('Lisa Anderson', '1988-11-30', 'AB+', 'MRN-2024-004', 'landerson@email.com', '555-1004'),
('David Wilson', '2010-07-18', 'O-', 'MRN-2024-005', 'dwilson@email.com', '555-1005');

-- Insert appointments
INSERT INTO appointment (patient_id, doctor_id, scheduled_at, status, notes) VALUES
(1, 1, '2024-12-28 09:00:00', 'SCHEDULED', 'Annual checkup'),
(2, 2, '2024-12-28 10:30:00', 'SCHEDULED', 'Child vaccination'),
(3, 3, '2024-12-27 14:00:00', 'COMPLETED', 'Knee pain evaluation'),
(4, 4, '2024-12-29 11:00:00', 'SCHEDULED', 'Follow-up consultation'),
(5, 2, '2024-12-30 15:30:00', 'SCHEDULED', 'Routine pediatric exam');

-- Insert prescriptions for completed appointment
INSERT INTO prescription (appointment_id, medication_name, dosage, frequency, duration_days, instructions) VALUES
(3, 'Ibuprofen', '400mg', 'Twice daily', 14, 'Take with food'),
(3, 'Physical Therapy', 'N/A', 'Three times weekly', 30, 'Focus on knee strengthening exercises');

-- Verify data
SELECT 
    'Doctors' as entity,
    COUNT(*) as count
FROM doctor
UNION ALL
SELECT 
    'Patients',
    COUNT(*)
FROM patient
UNION ALL
SELECT 
    'Appointments',
    COUNT(*)
FROM appointment
UNION ALL
SELECT 
    'Prescriptions',
    COUNT(*)
FROM prescription;
```

---

### 5.6 Starting PostgreSQL

**Start the database**:
```powershell
# From project root directory
docker-compose up -d
```

**Verify it's running**:
```powershell
# Check container status
docker ps

# Expected output:
# CONTAINER ID   IMAGE                PORTS                    STATUS
# abc123def456   postgres:16-alpine   0.0.0.0:5432->5432/tcp   Up (healthy)
```

**View logs** (to see initialization):
```powershell
docker logs medical-postgres
```

**Stop the database**:
```powershell
docker-compose down
```

**Reset database** (delete all data):
```powershell
docker-compose down -v
docker-compose up -d
```

---

### 5.7 VS Code Extensions for Spring Development

**Required Extensions**:

1. **Extension Pack for Java** (Microsoft)
   - ID: `vscjava.vscode-java-pack`
   - Includes: Language Support, Debugger, Test Runner, Maven, Project Manager

2. **Spring Boot Extension Pack** (VMware)
   - ID: `vmware.vscode-boot-dev-pack`
   - Includes: Spring Initializr, Boot Dashboard, Properties Editor

3. **PostgreSQL** (Chris Kolkman)
   - ID: `ckolkman.vscode-postgres`
   - For querying database from VS Code

**Install via Command Palette**:
```
Ctrl+Shift+P → Extensions: Install Extensions
Search for extension name
Click Install
```

**Or via Command Line**:
```powershell
code --install-extension vscjava.vscode-java-pack
code --install-extension vmware.vscode-boot-dev-pack
code --install-extension ckolkman.vscode-postgres
```

---

### 5.8 Connecting to PostgreSQL from VS Code

**Step 1: Open PostgreSQL Extension**
- Click PostgreSQL icon in Activity Bar (left sidebar)
- Click "+" to add connection

**Step 2: Enter Connection Details**
```
Host: localhost
Port: 5432
User: medicaladmin
Password: SecurePass123!
Database: medical_system
Connection Name: Medical System DB
```

**Step 3: Test Connection**
- Expand connection in PostgreSQL explorer
- You should see tables: patient, doctor, appointment, prescription

**Step 4: Run Test Query**
- Right-click connection → "New Query"
- Type: `SELECT * FROM patient;`
- Press `F5` to execute
- Should see 5 sample patients

---

### 5.9 Verifying Data with SQL Queries

**Count all entities**:
```sql
SELECT 
    'Patients' as entity, 
    COUNT(*) as total 
FROM patient
UNION ALL
SELECT 'Doctors', COUNT(*) FROM doctor
UNION ALL
SELECT 'Appointments', COUNT(*) FROM appointment
UNION ALL
SELECT 'Prescriptions', COUNT(*) FROM prescription;
```

**Expected Output**:
```
entity          | total
----------------|------
Patients        | 5
Doctors         | 4
Appointments    | 5
Prescriptions   | 2
```

**Find upcoming appointments with patient/doctor names**:
```sql
SELECT 
    a.scheduled_at,
    p.name AS patient_name,
    d.name AS doctor_name,
    d.specialization,
    a.status
FROM appointment a
JOIN patient p ON a.patient_id = p.id
JOIN doctor d ON a.doctor_id = d.id
WHERE a.scheduled_at > CURRENT_TIMESTAMP
ORDER BY a.scheduled_at;
```

---

### 5.10 Checkpoint Question 5

**Scenario**: A hospital IT team needs to add a new table for storing patient allergies. Design the SQL schema with these requirements:

1. Table name: `allergy`
2. Links to patient table (one patient can have many allergies)
3. Fields: allergy name, severity (MILD, MODERATE, SEVERE), discovered date
4. Include proper indexes and constraints

**Answer**:

```sql
-- Allergy table
CREATE TABLE allergy (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    
    allergy_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MILD',
    discovered_date DATE NOT NULL,
    notes TEXT,
    
    CONSTRAINT chk_severity CHECK (severity IN ('MILD', 'MODERATE', 'SEVERE')),
    CONSTRAINT unique_patient_allergy UNIQUE (patient_id, allergy_name)
);

-- Index for finding all allergies for a patient
CREATE INDEX idx_allergy_patient ON allergy(patient_id);

-- Index for finding severe allergies across all patients
CREATE INDEX idx_allergy_severity ON allergy(severity);

-- Comment
COMMENT ON TABLE allergy IS 'Patient allergies for safety checks before prescribing medication';

-- Sample data
INSERT INTO allergy (patient_id, allergy_name, severity, discovered_date, notes) VALUES
(1, 'Penicillin', 'SEVERE', '2015-03-10', 'Anaphylactic reaction in 2015'),
(1, 'Peanuts', 'MODERATE', '1985-06-20', 'Childhood allergy'),
(3, 'Aspirin', 'MILD', '2020-11-05', 'Causes stomach upset');

-- Verify
SELECT 
    p.name AS patient_name,
    a.allergy_name,
    a.severity,
    a.discovered_date
FROM allergy a
JOIN patient p ON a.patient_id = p.id
ORDER BY p.name, a.severity DESC;
```

**Key Design Decisions**:
- `ON DELETE CASCADE` - Delete allergies when patient deleted
- `UNIQUE (patient_id, allergy_name)` - No duplicate allergies per patient
- `CHECK` constraint for valid severity values
- Index on `patient_id` for fast lookups
- Index on `severity` for finding critical allergies

---

### 5.11 Docker Compose Best Practices

**Production-Ready Enhancements**:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: medical-postgres
    restart: unless-stopped
    
    environment:
      POSTGRES_DB: medical_system
      POSTGRES_USER: medicaladmin
      POSTGRES_PASSWORD: ${DB_PASSWORD:-SecurePass123!}  # Use env var
      PGDATA: /var/lib/postgresql/data/pgdata
      
      # Performance tuning
      POSTGRES_SHARED_BUFFERS: 256MB
      POSTGRES_EFFECTIVE_CACHE_SIZE: 1GB
      POSTGRES_MAX_CONNECTIONS: 100
    
    ports:
      - "${DB_PORT:-5432}:5432"  # Configurable port
    
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d:ro  # Read-only
      - ./backups:/backups  # For database backups
    
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U medicaladmin -d medical_system"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s  # Give it time to initialize
    
    networks:
      - medical-network
    
    # Resource limits
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G
        reservations:
          cpus: '1'
          memory: 512M

volumes:
  postgres-data:
    driver: local

networks:
  medical-network:
    driver: bridge
```

**Environment Variables** (create `.env` file):
```
DB_PASSWORD=SuperSecurePassword!
DB_PORT=5432
```

**Add `.env` to `.gitignore`**:
```
.env
```

---

### 5.12 Database Backup and Restore

**Backup database**:
```powershell
docker exec medical-postgres pg_dump -U medicaladmin medical_system > backup.sql
```

**Restore database**:
```powershell
# Method 1: Using docker exec
docker exec -i medical-postgres psql -U medicaladmin -d medical_system < backup.sql

# Method 2: Drop and recreate
docker exec medical-postgres psql -U medicaladmin -c "DROP DATABASE IF EXISTS medical_system;"
docker exec medical-postgres psql -U medicaladmin -c "CREATE DATABASE medical_system;"
docker exec -i medical-postgres psql -U medicaladmin -d medical_system < backup.sql
```

**Automated backup script** (`backup.ps1`):
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "backups/medical_system_$timestamp.sql"

Write-Host "Creating backup: $backupFile"
docker exec medical-postgres pg_dump -U medicaladmin medical_system > $backupFile

# Keep only last 7 backups
Get-ChildItem backups/*.sql | 
    Sort-Object CreationTime -Descending | 
    Select-Object -Skip 7 | 
    Remove-Item

Write-Host "Backup completed successfully"
```

---

### 5.13 Summary: Development Environment Checklist

**✅ Completed Setup**:
- [x] Docker Desktop installed and running
- [x] PostgreSQL 16 running in Docker container
- [x] Database schema created with init scripts
- [x] Sample data loaded (4 doctors, 5 patients, 5 appointments)
- [x] VS Code installed with Java and Spring extensions
- [x] PostgreSQL VS Code extension configured
- [x] Test query executed successfully

**📁 Project Structure**:
```
medical-appointment-system/
├── docker-compose.yml
├── .env
├── init-scripts/
│   ├── 01-schema.sql
│   └── 02-seed-data.sql
├── backups/
│   └── (backup files)
└── src/
    └── (Spring Boot code - next step)
```

**🔗 Connection Details**:
- Host: `localhost`
- Port: `5432`
- Database: `medical_system`
- User: `medicaladmin`
- Password: `SecurePass123!` (or from `.env`)

---

**Next Step**: With PostgreSQL ready, let's create our Spring Boot project in Step 6!

---

## 6. SPRING BOOT PROJECT WITH JDBC DEPENDENCIES

> **Goal**: Create a Spring Boot 3.3+ project with JDBC dependencies and connect to PostgreSQL.

---

### 6.1 Creating Project with Spring Initializr

**Option 1: Using VS Code**

1. Open Command Palette (`Ctrl+Shift+P`)
2. Type: `Spring Initializr: Create a Maven Project`
3. Select options:
   - Spring Boot version: `3.3.6` or latest 3.3.x
   - Language: `Java`
   - Group Id: `com.medical`
   - Artifact Id: `appointment-system`
   - Packaging: `Jar`
   - Java version: `17` or `21`

4. Select dependencies:
   - `Spring Web` - REST APIs
   - `Spring Data JDBC` - Database access
   - `PostgreSQL Driver` - PostgreSQL connector
   - `Lombok` - Reduce boilerplate
   - `Spring Boot DevTools` - Hot reload

5. Choose folder location
6. Click "Generate into this folder"

---

**Option 2: Using Web Interface**

1. Visit [https://start.spring.io](https://start.spring.io)
2. Configure:
   ```
   Project: Maven
   Language: Java
   Spring Boot: 3.3.6
   
   Project Metadata:
   Group: com.medical
   Artifact: appointment-system
   Name: appointment-system
   Description: Medical Appointment Management System
   Package name: com.medical
   Packaging: Jar
   Java: 17 or 21
   ```

3. Add Dependencies:
   - Spring Web
   - Spring Data JDBC
   - PostgreSQL Driver
   - Lombok
   - Spring Boot DevTools

4. Click "Generate"
5. Extract ZIP to workspace folder

---

### 6.2 Project Structure

**Generated Structure**:
```
appointment-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── medical/
│   │   │           └── AppointmentSystemApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           └── (web assets)
│   └── test/
│       └── java/
│           └── com/
│               └── medical/
│                   └── AppointmentSystemApplicationTests.java
├── target/
├── .gitignore
├── mvnw (Maven wrapper for Unix)
├── mvnw.cmd (Maven wrapper for Windows)
├── pom.xml
└── README.md
```

---

### 6.3 Understanding pom.xml Dependencies

**Open `pom.xml`** and examine key sections:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.6</version>
        <relativePath/>
    </parent>
    
    <groupId>com.medical</groupId>
    <artifactId>appointment-system</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>appointment-system</name>
    <description>Medical Appointment Management System</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Web for REST APIs -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JDBC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>
        
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot DevTools (hot reload) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Dependency Breakdown**:

| Dependency | Purpose | Includes |
|------------|---------|----------|
| `spring-boot-starter-web` | REST APIs, embedded Tomcat | Spring MVC, Jackson JSON, Tomcat |
| `spring-boot-starter-data-jdbc` | Database access | JdbcTemplate, transaction management |
| `postgresql` | Database driver | PostgreSQL JDBC driver |
| `lombok` | Reduce boilerplate | @Data, @Builder, etc. |
| `spring-boot-devtools` | Developer experience | Hot reload, live reload |
| `spring-boot-starter-test` | Testing | JUnit 5, Mockito, AssertJ |

---

### 6.4 Configuring Database Connection

**Edit `src/main/resources/application.properties`**:

```properties
# Application Name
spring.application.name=appointment-system

# Server Configuration
server.port=8080

# PostgreSQL Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/medical_system
spring.datasource.username=medicaladmin
spring.datasource.password=SecurePass123!
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Connection Pool (auto-configured)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000

# Logging
logging.level.org.springframework.jdbc.core=DEBUG
logging.level.com.medical=DEBUG
```

**Configuration Explained**:

| Property | Value | Purpose |
|----------|-------|---------|
| `spring.datasource.url` | JDBC URL | Connection string to PostgreSQL |
| `spring.datasource.username` | medicaladmin | Database user |
| `spring.datasource.password` | SecurePass123! | Database password |
| `spring.datasource.driver-class-name` | org.postgresql.Driver | PostgreSQL driver class |
| `hikari.maximum-pool-size` | 10 | Max concurrent connections |
| `hikari.minimum-idle` | 5 | Keep 5 connections ready |
| `logging.level.org.springframework.jdbc` | DEBUG | Log SQL statements |

---

### 6.5 Better Configuration with YAML

**Rename `application.properties` to `application.yml`**:

```yaml
spring:
  application:
    name: appointment-system
  
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

server:
  port: 8080

logging:
  level:
    org.springframework.jdbc.core: DEBUG
    com.medical: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**YAML Benefits**:
- ✅ More readable hierarchy
- ✅ Less repetition
- ✅ Better for complex configurations
- ✅ Supports lists and nested objects

---

### 6.6 Environment-Specific Configuration

**Create profile-specific configs**:

**`application-dev.yml`** (development):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical_system
    username: medicaladmin
    password: SecurePass123!

logging:
  level:
    org.springframework.jdbc.core: DEBUG
    com.medical: DEBUG
```

**`application-prod.yml`** (production):
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

logging:
  level:
    org.springframework.jdbc.core: WARN
    com.medical: INFO
```

**Activate profile**:
```properties
# In application.yml
spring.profiles.active=dev
```

**Or via command line**:
```powershell
java -jar appointment-system.jar --spring.profiles.active=prod
```

---

### 6.7 Main Application Class

**`src/main/java/com/medical/AppointmentSystemApplication.java`**:

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

**`@SpringBootApplication` combines three annotations**:
1. `@Configuration` - Marks class as configuration source
2. `@EnableAutoConfiguration` - Auto-configure based on dependencies
3. `@ComponentScan` - Scan `com.medical` package for components

---

### 6.8 Creating Package Structure

**Create these packages** in `src/main/java/com/medical/`:

```
com.medical/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Database access
├── domain/         # Entity classes
├── dto/            # Data Transfer Objects
├── exception/      # Custom exceptions
└── config/         # Configuration classes
```

**Why this structure?**
- ✅ **Separation of concerns** - Each layer has one responsibility
- ✅ **Testability** - Easy to mock dependencies
- ✅ **Maintainability** - Clear where code belongs
- ✅ **Industry standard** - Familiar to all Spring developers

---

### 6.9 Testing Database Connection

**Create `DatabaseConnectionTest.java`**:

```java
package com.medical;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseConnectionTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void testDatabaseConnection() {
        // Query database
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM patient",
            Long.class
        );
        
        // Verify we have sample data
        assertThat(count).isGreaterThan(0);
        System.out.println("✅ Database connection successful! Found " + count + " patients.");
    }
    
    @Test
    void testPatientQuery() {
        // Query patient name
        String patientName = jdbcTemplate.queryForObject(
            "SELECT name FROM patient WHERE medical_record_number = ?",
            String.class,
            "MRN-2024-001"
        );
        
        assertThat(patientName).isEqualTo("John Smith");
        System.out.println("✅ Patient query successful: " + patientName);
    }
}
```

**Run test**:
```powershell
# Using Maven wrapper
./mvnw test

# Or specific test
./mvnw test -Dtest=DatabaseConnectionTest
```

**Expected Output**:
```
✅ Database connection successful! Found 5 patients.
✅ Patient query successful: John Smith

BUILD SUCCESS
```

---

### 6.10 Running the Application

**Start application**:
```powershell
# Using Maven wrapper
./mvnw spring-boot:run

# Or build JAR and run
./mvnw clean package
java -jar target/appointment-system-0.0.1-SNAPSHOT.jar
```

**Expected Console Output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.3.6)

2024-12-26 10:30:15.123  INFO 12345 --- [main] c.m.AppointmentSystemApplication         : Starting AppointmentSystemApplication
2024-12-26 10:30:15.456  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080
2024-12-26 10:30:16.789  INFO 12345 --- [main] com.zaxxer.hikari.HikariDataSource       : MedicalHikariPool - Starting...
2024-12-26 10:30:17.012  INFO 12345 --- [main] com.zaxxer.hikari.HikariDataSource       : MedicalHikariPool - Start completed.
2024-12-26 10:30:18.345  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080
2024-12-26 10:30:18.456  INFO 12345 --- [main] c.m.AppointmentSystemApplication         : Started AppointmentSystemApplication in 3.567 seconds
```

**Key Indicators**:
- ✅ Tomcat started on port 8080
- ✅ HikariCP pool initialized
- ✅ Application ready in ~3-4 seconds

---

### 6.11 Creating a Health Check Endpoint

**Create `HealthController.java`**:

```java
package com.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Medical Appointment System");
        
        try {
            Long patientCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM patient",
                Long.class
            );
            health.put("database", "Connected");
            health.put("patients", patientCount);
        } catch (Exception e) {
            health.put("database", "Error: " + e.getMessage());
        }
        
        return health;
    }
}
```

**Test in browser**: [http://localhost:8080/health](http://localhost:8080/health)

**Expected Response**:
```json
{
  "status": "UP",
  "application": "Medical Appointment System",
  "database": "Connected",
  "patients": 5
}
```

---

### 6.12 Checkpoint Question 6

**Scenario**: You need to add Spring Boot Actuator for production monitoring. What steps are required?

**Requirements**:
1. Add dependency to `pom.xml`
2. Configure to expose health and metrics endpoints
3. Verify it works

**Answer**:

**Step 1: Add dependency to `pom.xml`**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Step 2: Configure in `application.yml`**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
  endpoint:
    health:
      show-details: always
  
  info:
    env:
      enabled: true

info:
  app:
    name: Medical Appointment System
    version: 1.0.0
    description: Hospital appointment management
```

**Step 3: Rebuild and restart**:
```powershell
./mvnw clean package
./mvnw spring-boot:run
```

**Step 4: Test endpoints**:
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Metrics: [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
- Info: [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

**Expected Health Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Production Considerations**:
- ⚠️ Secure actuator endpoints (require authentication)
- ⚠️ Don't expose sensitive metrics publicly
- ✅ Use for health checks in load balancers
- ✅ Monitor metrics with Prometheus/Grafana

---

### 6.13 DevTools Hot Reload in Action

**DevTools automatically restarts when you change code**:

1. **Modify `HealthController.java`**:
```java
@GetMapping("/health")
public Map<String, Object> health() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("application", "Medical Appointment System v2.0"); // Changed
    // ... rest of code
}
```

2. **Save file** (`Ctrl+S`)

3. **Watch console**:
```
2024-12-26 10:35:20.123  INFO 12345 --- [restartedMain] c.m.AppointmentSystemApplication : Restarting...
2024-12-26 10:35:20.456  INFO 12345 --- [restartedMain] c.m.AppointmentSystemApplication : Restart completed in 0.333 seconds
```

4. **Refresh browser** - See updated response

**DevTools Features**:
- ✅ Automatic restart on code changes
- ✅ LiveReload browser extension support
- ✅ Disabled in production (automatically)
- ✅ Excludes static resources by default

---

### 6.14 Common Connection Issues

**Problem 1: "Connection refused to localhost:5432"**

**Cause**: PostgreSQL container not running

**Solution**:
```powershell
# Check if container is running
docker ps

# If not, start it
docker-compose up -d

# Verify it's healthy
docker ps
```

---

**Problem 2: "password authentication failed for user medicaladmin"**

**Cause**: Wrong credentials in `application.yml`

**Solution**: Verify credentials match `docker-compose.yml`:
```yaml
spring:
  datasource:
    username: medicaladmin
    password: SecurePass123!
```

---

**Problem 3: "database medical_system does not exist"**

**Cause**: Database not created during initialization

**Solution**: Reset Docker container:
```powershell
docker-compose down -v
docker-compose up -d
docker logs medical-postgres  # Check initialization logs
```

---

**Problem 4: Connection pool exhausted**

**Symptoms**: Application hangs or times out

**Cause**: Not closing connections, too few pool connections

**Solution**: Increase pool size:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase from 10
```

---

### 6.15 Summary: Project Setup Checklist

**✅ Completed Tasks**:
- [x] Created Spring Boot 3.3+ project with Spring Initializr
- [x] Added dependencies: Web, Data JDBC, PostgreSQL, Lombok
- [x] Configured `application.yml` with database connection
- [x] Created package structure (controller, service, repository, domain)
- [x] Tested database connection with JUnit test
- [x] Started application successfully
- [x] Created health check endpoint
- [x] Verified DevTools hot reload

**📁 Current Project Structure**:
```
appointment-system/
├── src/
│   ├── main/
│   │   ├── java/com/medical/
│   │   │   ├── controller/
│   │   │   │   └── HealthController.java
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── domain/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── config/
│   │   │   └── AppointmentSystemApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/medical/
│           ├── AppointmentSystemApplicationTests.java
│           └── DatabaseConnectionTest.java
├── pom.xml
└── README.md
```

**🌐 Working Endpoints**:
- Health Check: [http://localhost:8080/health](http://localhost:8080/health)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

**Next Step**: In Step 7, we'll implement JdbcTemplate repositories for CRUD operations!

---

## 7. JDBCTEMPLATE REPOSITORY IMPLEMENTATION

> **Goal**: Build production-ready repository classes using JdbcTemplate for all CRUD operations with proper error handling.

---

### 7.1 Repository Pattern Overview

**Repository Pattern**: Encapsulates data access logic, providing a collection-like interface for domain objects.

**Benefits**:
- ✅ **Separation of concerns** - Controllers don't know about SQL
- ✅ **Testability** - Easy to mock repositories
- ✅ **Maintainability** - SQL changes don't affect business logic
- ✅ **Reusability** - Repository methods used across multiple services

**Repository Interface Example**:
```java
public interface PatientRepository {
    void save(Patient patient);
    Optional<Patient> findById(Long id);
    Optional<Patient> findByMrn(String mrn);
    List<Patient> findAll();
    int update(Patient patient);
    int deleteById(Long id);
}
```

---

### 7.2 Patient Domain Class

**Create `src/main/java/com/medical/domain/Patient.java`**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    private String email;
    private String phoneNumber;
    
    // Business constructor (for new patients)
    public Patient(String name, LocalDate dateOfBirth, String bloodType, String mrn) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.medicalRecordNumber = mrn;
        this.createdAt = LocalDateTime.now();
    }
}
```

---

### 7.3 Complete PatientRepository Implementation

**Create `src/main/java/com/medical/repository/PatientRepository.java`**:

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PatientRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Save a new patient and return with generated ID
     */
    public Patient save(Patient patient) {
        String sql = "INSERT INTO patient (name, date_of_birth, blood_type, " +
                     "medical_record_number, email, phone_number, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getName());
            ps.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(3, patient.getBloodType());
            ps.setString(4, patient.getMedicalRecordNumber());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getPhoneNumber());
            ps.setTimestamp(7, Timestamp.valueOf(patient.getCreatedAt()));
            return ps;
        }, keyHolder);
        
        patient.setId(keyHolder.getKey().longValue());
        return patient;
    }
    
    /**
     * Find patient by ID
     */
    public Optional<Patient> findById(Long id) {
        String sql = "SELECT * FROM patient WHERE id = ?";
        try {
            Patient patient = jdbcTemplate.queryForObject(sql, this::mapRowToPatient, id);
            return Optional.ofNullable(patient);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Find patient by Medical Record Number
     */
    public Optional<Patient> findByMrn(String mrn) {
        String sql = "SELECT * FROM patient WHERE medical_record_number = ?";
        List<Patient> results = jdbcTemplate.query(sql, this::mapRowToPatient, mrn);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Find all patients
     */
    public List<Patient> findAll() {
        String sql = "SELECT * FROM patient ORDER BY name";
        return jdbcTemplate.query(sql, this::mapRowToPatient);
    }
    
    /**
     * Find patients by blood type
     */
    public List<Patient> findByBloodType(String bloodType) {
        String sql = "SELECT * FROM patient WHERE blood_type = ? ORDER BY name";
        return jdbcTemplate.query(sql, this::mapRowToPatient, bloodType);
    }
    
    /**
     * Update existing patient
     */
    public int update(Patient patient) {
        String sql = "UPDATE patient SET name = ?, date_of_birth = ?, blood_type = ?, " +
                     "medical_record_number = ?, email = ?, phone_number = ?, updated_at = ? " +
                     "WHERE id = ?";
        
        return jdbcTemplate.update(sql,
            patient.getName(),
            Date.valueOf(patient.getDateOfBirth()),
            patient.getBloodType(),
            patient.getMedicalRecordNumber(),
            patient.getEmail(),
            patient.getPhoneNumber(),
            Timestamp.valueOf(LocalDateTime.now()),
            patient.getId()
        );
    }
    
    /**
     * Delete patient by ID
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM patient WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    /**
     * Check if patient exists by MRN
     */
    public boolean existsByMrn(String mrn) {
        String sql = "SELECT COUNT(*) FROM patient WHERE medical_record_number = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, mrn);
        return count != null && count > 0;
    }
    
    /**
     * Count total patients
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM patient";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    /**
     * Map ResultSet row to Patient object
     */
    private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getLong("id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        patient.setName(rs.getString("name"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }
        
        patient.setBloodType(rs.getString("blood_type"));
        patient.setMedicalRecordNumber(rs.getString("medical_record_number"));
        patient.setEmail(rs.getString("email"));
        patient.setPhoneNumber(rs.getString("phone_number"));
        
        return patient;
    }
}
```

**Key Implementation Details**:

1. **save() with Generated Keys**:
   - Uses `KeyHolder` to capture auto-generated ID
   - Returns Patient object with ID populated
   - Automatically sets `created_at` timestamp

2. **findById() with Optional**:
   - Returns `Optional<Patient>` instead of null
   - Catches `EmptyResultDataAccessException` for not found

3. **update() with Timestamp**:
   - Automatically sets `updated_at` to current time
   - Returns affected row count (0 if not found, 1 if updated)

4. **NULL-safe mapping**:
   - Checks for NULL timestamps and dates before converting
   - Prevents `NullPointerException`

---

### 7.4 Testing the PatientRepository

**Create `src/test/java/com/medical/repository/PatientRepositoryTest.java`**:

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // Rollback after each test
class PatientRepositoryTest {
    
    @Autowired
    private PatientRepository patientRepository;
    
    private Patient testPatient;
    
    @BeforeEach
    void setUp() {
        testPatient = new Patient(
            "Test Patient",
            LocalDate.of(1990, 5, 15),
            "O+",
            "MRN-TEST-001"
        );
        testPatient.setEmail("test@example.com");
        testPatient.setPhoneNumber("555-9999");
    }
    
    @Test
    void testSavePatient() {
        // Save patient
        Patient saved = patientRepository.save(testPatient);
        
        // Verify ID was generated
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        
        // Verify all fields saved correctly
        assertThat(saved.getName()).isEqualTo("Test Patient");
        assertThat(saved.getMedicalRecordNumber()).isEqualTo("MRN-TEST-001");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
    
    @Test
    void testFindById() {
        // Save patient first
        Patient saved = patientRepository.save(testPatient);
        
        // Find by ID
        Optional<Patient> found = patientRepository.findById(saved.getId());
        
        // Verify found
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Patient");
    }
    
    @Test
    void testFindByIdNotFound() {
        // Find non-existent patient
        Optional<Patient> found = patientRepository.findById(99999L);
        
        // Verify empty
        assertThat(found).isEmpty();
    }
    
    @Test
    void testFindByMrn() {
        // Save patient
        patientRepository.save(testPatient);
        
        // Find by MRN
        Optional<Patient> found = patientRepository.findByMrn("MRN-TEST-001");
        
        // Verify found
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Patient");
    }
    
    @Test
    void testFindAll() {
        // Save patient
        patientRepository.save(testPatient);
        
        // Find all
        List<Patient> all = patientRepository.findAll();
        
        // Verify at least one patient (from seed data + our test)
        assertThat(all).hasSizeGreaterThan(0);
    }
    
    @Test
    void testFindByBloodType() {
        // Save patient with O+ blood type
        patientRepository.save(testPatient);
        
        // Find by blood type
        List<Patient> oPositivePatients = patientRepository.findByBloodType("O+");
        
        // Verify at least our test patient
        assertThat(oPositivePatients).hasSizeGreaterThan(0);
    }
    
    @Test
    void testUpdate() {
        // Save patient
        Patient saved = patientRepository.save(testPatient);
        
        // Update name
        saved.setName("Updated Name");
        saved.setEmail("updated@example.com");
        int rowsAffected = patientRepository.update(saved);
        
        // Verify update succeeded
        assertThat(rowsAffected).isEqualTo(1);
        
        // Verify changes persisted
        Optional<Patient> updated = patientRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
        assertThat(updated.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testDelete() {
        // Save patient
        Patient saved = patientRepository.save(testPatient);
        
        // Delete
        int rowsAffected = patientRepository.deleteById(saved.getId());
        
        // Verify deletion
        assertThat(rowsAffected).isEqualTo(1);
        
        // Verify not found
        Optional<Patient> deleted = patientRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void testExistsByMrn() {
        // Save patient
        patientRepository.save(testPatient);
        
        // Check exists
        boolean exists = patientRepository.existsByMrn("MRN-TEST-001");
        assertThat(exists).isTrue();
        
        // Check not exists
        boolean notExists = patientRepository.existsByMrn("MRN-NONEXISTENT");
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testCount() {
        // Get initial count
        long initialCount = patientRepository.count();
        
        // Save patient
        patientRepository.save(testPatient);
        
        // Verify count increased
        long newCount = patientRepository.count();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
}
```

**Run tests**:
```powershell
./mvnw test -Dtest=PatientRepositoryTest
```

**Expected Output**:
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

---

### 7.5 Appointment and Prescription Repositories

**Create `src/main/java/com/medical/domain/Appointment.java`**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    
    private Long id;
    private LocalDateTime createdAt;
    
    private Long patientId;
    private Long doctorId;
    
    private LocalDateTime scheduledAt;
    private String status;
    private String notes;
    
    private List<Prescription> prescriptions = new ArrayList<>();
    
    public Appointment(Long patientId, Long doctorId, LocalDateTime scheduledAt) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.scheduledAt = scheduledAt;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }
    
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        prescription.setAppointmentId(this.id);
    }
}
```

**Create `src/main/java/com/medical/domain/Prescription.java`**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    
    private Long id;
    private Long appointmentId;
    
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
}
```

**Create `src/main/java/com/medical/repository/AppointmentRepository.java`**:

```java
package com.medical.repository;

import com.medical.domain.Appointment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Appointment save(Appointment appointment) {
        String sql = "INSERT INTO appointment (patient_id, doctor_id, scheduled_at, status, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, appointment.getPatientId());
            ps.setLong(2, appointment.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getScheduledAt()));
            ps.setString(4, appointment.getStatus());
            ps.setString(5, appointment.getNotes());
            ps.setTimestamp(6, Timestamp.valueOf(appointment.getCreatedAt()));
            return ps;
        }, keyHolder);
        
        appointment.setId(keyHolder.getKey().longValue());
        return appointment;
    }
    
    public Optional<Appointment> findById(Long id) {
        String sql = "SELECT * FROM appointment WHERE id = ?";
        try {
            Appointment appointment = jdbcTemplate.queryForObject(sql, this::mapRowToAppointment, id);
            return Optional.ofNullable(appointment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public List<Appointment> findByPatientId(Long patientId) {
        String sql = "SELECT * FROM appointment WHERE patient_id = ? ORDER BY scheduled_at DESC";
        return jdbcTemplate.query(sql, this::mapRowToAppointment, patientId);
    }
    
    public List<Appointment> findByDoctorId(Long doctorId) {
        String sql = "SELECT * FROM appointment WHERE doctor_id = ? ORDER BY scheduled_at DESC";
        return jdbcTemplate.query(sql, this::mapRowToAppointment, doctorId);
    }
    
    public List<Appointment> findUpcomingByDoctor(Long doctorId, LocalDateTime fromDate) {
        String sql = "SELECT * FROM appointment WHERE doctor_id = ? AND scheduled_at > ? " +
                     "AND status = 'SCHEDULED' ORDER BY scheduled_at";
        return jdbcTemplate.query(sql, this::mapRowToAppointment, doctorId, Timestamp.valueOf(fromDate));
    }
    
    public int updateStatus(Long id, String status) {
        String sql = "UPDATE appointment SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }
    
    public int deleteById(Long id) {
        String sql = "DELETE FROM appointment WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    private Appointment mapRowToAppointment(ResultSet rs, int rowNum) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getLong("id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            appointment.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        appointment.setPatientId(rs.getLong("patient_id"));
        appointment.setDoctorId(rs.getLong("doctor_id"));
        
        Timestamp scheduledAt = rs.getTimestamp("scheduled_at");
        if (scheduledAt != null) {
            appointment.setScheduledAt(scheduledAt.toLocalDateTime());
        }
        
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        
        return appointment;
    }
}
```

---

### 7.6 Checkpoint Question 7

**Scenario**: Implement a `DoctorRepository` with these methods:
1. `save(Doctor doctor)` - Save new doctor, return with ID
2. `findById(Long id)` - Find by ID
3. `findByLicenseNumber(String licenseNumber)` - Find by business key
4. `findBySpecialization(String specialization)` - Find all doctors in a specialty
5. `update(Doctor doctor)` - Update existing doctor
6. `deleteById(Long id)` - Delete doctor

**Answer**:

**Doctor Domain Class**:
```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    
    private Long id;
    private LocalDateTime createdAt;
    
    private String name;
    private String specialization;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    
    public Doctor(String name, String specialization, String licenseNumber) {
        this.name = name;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.createdAt = LocalDateTime.now();
    }
}
```

**DoctorRepository Implementation**:
```java
package com.medical.repository;

import com.medical.domain.Doctor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class DoctorRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DoctorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Doctor save(Doctor doctor) {
        String sql = "INSERT INTO doctor (name, specialization, license_number, email, phone_number, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getLicenseNumber());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getPhoneNumber());
            ps.setTimestamp(6, Timestamp.valueOf(doctor.getCreatedAt()));
            return ps;
        }, keyHolder);
        
        doctor.setId(keyHolder.getKey().longValue());
        return doctor;
    }
    
    public Optional<Doctor> findById(Long id) {
        String sql = "SELECT * FROM doctor WHERE id = ?";
        try {
            Doctor doctor = jdbcTemplate.queryForObject(sql, this::mapRowToDoctor, id);
            return Optional.ofNullable(doctor);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
        String sql = "SELECT * FROM doctor WHERE license_number = ?";
        List<Doctor> results = jdbcTemplate.query(sql, this::mapRowToDoctor, licenseNumber);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    public List<Doctor> findBySpecialization(String specialization) {
        String sql = "SELECT * FROM doctor WHERE specialization = ? ORDER BY name";
        return jdbcTemplate.query(sql, this::mapRowToDoctor, specialization);
    }
    
    public int update(Doctor doctor) {
        String sql = "UPDATE doctor SET name = ?, specialization = ?, license_number = ?, " +
                     "email = ?, phone_number = ? WHERE id = ?";
        
        return jdbcTemplate.update(sql,
            doctor.getName(),
            doctor.getSpecialization(),
            doctor.getLicenseNumber(),
            doctor.getEmail(),
            doctor.getPhoneNumber(),
            doctor.getId()
        );
    }
    
    public int deleteById(Long id) {
        String sql = "DELETE FROM doctor WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    private Doctor mapRowToDoctor(ResultSet rs, int rowNum) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setId(rs.getLong("id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            doctor.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        doctor.setName(rs.getString("name"));
        doctor.setSpecialization(rs.getString("specialization"));
        doctor.setLicenseNumber(rs.getString("license_number"));
        doctor.setEmail(rs.getString("email"));
        doctor.setPhoneNumber(rs.getString("phone_number"));
        
        return doctor;
    }
}
```

---

### 7.7 Handling Transactions

**Why Transactions?**
When saving an appointment with prescriptions, we need to ensure either:
- ✅ Both appointment AND all prescriptions are saved
- ❌ OR nothing is saved (rollback)

**Without transactions**: Appointment saved but prescriptions fail = inconsistent data

**Solution**: Use `@Transactional` annotation

**Example Service with Transaction**:
```java
package com.medical.service;

import com.medical.domain.Appointment;
import com.medical.domain.Prescription;
import com.medical.repository.AppointmentRepository;
import com.medical.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PrescriptionRepository prescriptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }
    
    @Transactional  // This method runs in a transaction
    public Appointment createAppointmentWithPrescriptions(Appointment appointment) {
        // Save appointment first (gets ID)
        Appointment saved = appointmentRepository.save(appointment);
        
        // Save all prescriptions
        for (Prescription prescription : appointment.getPrescriptions()) {
            prescription.setAppointmentId(saved.getId());
            prescriptionRepository.save(prescription);
        }
        
        // If any exception occurs, ALL changes are rolled back
        return saved;
    }
}
```

**Transaction Behavior**:
- If `appointmentRepository.save()` succeeds but `prescriptionRepository.save()` fails → **rollback both**
- If any runtime exception thrown → **rollback all**
- If method completes successfully → **commit all**

---

### 7.8 PrescriptionRepository Implementation

**Create `src/main/java/com/medical/repository/PrescriptionRepository.java`**:

```java
package com.medical.repository;

import com.medical.domain.Prescription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class PrescriptionRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PrescriptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Prescription save(Prescription prescription) {
        String sql = "INSERT INTO prescription (appointment_id, medication_name, dosage, " +
                     "frequency, duration_days, instructions) VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, prescription.getAppointmentId());
            ps.setString(2, prescription.getMedicationName());
            ps.setString(3, prescription.getDosage());
            ps.setString(4, prescription.getFrequency());
            ps.setInt(5, prescription.getDurationDays());
            ps.setString(6, prescription.getInstructions());
            return ps;
        }, keyHolder);
        
        prescription.setId(keyHolder.getKey().longValue());
        return prescription;
    }
    
    public List<Prescription> findByAppointmentId(Long appointmentId) {
        String sql = "SELECT * FROM prescription WHERE appointment_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToPrescription, appointmentId);
    }
    
    public int deleteById(Long id) {
        String sql = "DELETE FROM prescription WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    public int deleteByAppointmentId(Long appointmentId) {
        String sql = "DELETE FROM prescription WHERE appointment_id = ?";
        return jdbcTemplate.update(sql, appointmentId);
    }
    
    private Prescription mapRowToPrescription(ResultSet rs, int rowNum) throws SQLException {
        Prescription prescription = new Prescription();
        prescription.setId(rs.getLong("id"));
        prescription.setAppointmentId(rs.getLong("appointment_id"));
        prescription.setMedicationName(rs.getString("medication_name"));
        prescription.setDosage(rs.getString("dosage"));
        prescription.setFrequency(rs.getString("frequency"));
        prescription.setDurationDays(rs.getInt("duration_days"));
        prescription.setInstructions(rs.getString("instructions"));
        return prescription;
    }
}
```

---

### 7.9 Summary: Repository Best Practices

**✅ DO**:
- Use `@Repository` annotation for Spring component scanning
- Return `Optional<T>` instead of null for single results
- Use `KeyHolder` to capture generated IDs
- Handle NULL values in mappers
- Use descriptive method names (`findByMrn`, not `findBy`)
- Add javadoc comments for public methods
- Use constructor injection for dependencies

**❌ DON'T**:
- Return null (use `Optional.empty()`)
- Swallow exceptions (let Spring translate them)
- Put business logic in repositories (belongs in services)
- Use field injection (`@Autowired` on fields)
- Hardcode SQL strings across multiple places (use constants)

**Repository Method Naming Conventions**:
- `save()` - Insert new entity
- `findById()` - Query by primary key
- `findByXxx()` - Query by other criteria
- `update()` - Update existing entity
- `deleteById()` - Delete by primary key
- `existsById()` - Check existence
- `count()` - Count entities

---

**Next Step**: In Step 8, we'll build REST APIs using these repositories!

---

## 8. REST API LAYER WITH CONTROLLERS

> **Goal**: Expose repository functionality through RESTful HTTP endpoints with proper error handling and validation.

---

### 8.1 REST API Design Principles

**RESTful URL Structure**:
```
GET    /api/patients           - List all patients
GET    /api/patients/{id}      - Get patient by ID
POST   /api/patients           - Create new patient
PUT    /api/patients/{id}      - Update patient
DELETE /api/patients/{id}      - Delete patient

GET    /api/patients/mrn/{mrn} - Get patient by MRN
GET    /api/patients/blood-type/{type} - Get patients by blood type
```

**HTTP Status Codes**:
- `200 OK` - Successful GET/PUT/DELETE
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE with no response body
- `400 Bad Request` - Invalid input data
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server-side error

---

### 8.2 PatientController Implementation

**Create `src/main/java/com/medical/controller/PatientController.java`**:

```java
package com.medical.controller;

import com.medical.domain.Patient;
import com.medical.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    
    private final PatientRepository patientRepository;
    
    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }
    
    /**
     * GET /api/patients - Get all patients
     */
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    /**
     * GET /api/patients/{id} - Get patient by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/patients/mrn/{mrn} - Get patient by Medical Record Number
     */
    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<Patient> getPatientByMrn(@PathVariable String mrn) {
        return patientRepository.findByMrn(mrn)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/patients/blood-type/{type} - Get patients by blood type
     */
    @GetMapping("/blood-type/{type}")
    public List<Patient> getPatientsByBloodType(@PathVariable String type) {
        return patientRepository.findByBloodType(type);
    }
    
    /**
     * POST /api/patients - Create new patient
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient createPatient(@RequestBody Patient patient) {
        return patientRepository.save(patient);
    }
    
    /**
     * PUT /api/patients/{id} - Update existing patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, 
                                                   @RequestBody Patient patient) {
        // Verify patient exists
        if (patientRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Set ID from path (ignore any ID in request body)
        patient.setId(id);
        patientRepository.update(patient);
        
        return ResponseEntity.ok(patient);
    }
    
    /**
     * DELETE /api/patients/{id} - Delete patient
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        int rowsDeleted = patientRepository.deleteById(id);
        
        if (rowsDeleted == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/patients/count - Count total patients
     */
    @GetMapping("/count")
    public long countPatients() {
        return patientRepository.count();
    }
}
```

**Key Annotations Explained**:

| Annotation | Purpose |
|------------|---------|
| `@RestController` | Combines `@Controller` + `@ResponseBody` |
| `@RequestMapping("/api/patients")` | Base URL for all methods |
| `@GetMapping` | HTTP GET request handler |
| `@PostMapping` | HTTP POST request handler |
| `@PutMapping` | HTTP PUT request handler |
| `@DeleteMapping` | HTTP DELETE request handler |
| `@PathVariable` | Extract value from URL path |
| `@RequestBody` | Parse JSON from request body |
| `@ResponseStatus(CREATED)` | Return 201 status code |

---

### 8.3 Testing with REST Client

**Install REST Client extension** (if not already installed):
- Extension ID: `humao.rest-client`

**Create `test-api.http` in project root**:

```http
### Get all patients
GET http://localhost:8080/api/patients
Accept: application/json

### Get patient by ID
GET http://localhost:8080/api/patients/1
Accept: application/json

### Get patient by MRN
GET http://localhost:8080/api/patients/mrn/MRN-2024-001
Accept: application/json

### Get patients by blood type
GET http://localhost:8080/api/patients/blood-type/O+
Accept: application/json

### Count patients
GET http://localhost:8080/api/patients/count
Accept: application/json

### Create new patient
POST http://localhost:8080/api/patients
Content-Type: application/json

{
  "name": "Alice Johnson",
  "dateOfBirth": "1995-03-20",
  "bloodType": "A+",
  "medicalRecordNumber": "MRN-2024-NEW-001",
  "email": "alice.johnson@email.com",
  "phoneNumber": "555-2001"
}

### Update patient
PUT http://localhost:8080/api/patients/1
Content-Type: application/json

{
  "name": "John Smith Updated",
  "dateOfBirth": "1980-05-15",
  "bloodType": "O+",
  "medicalRecordNumber": "MRN-2024-001",
  "email": "john.updated@email.com",
  "phoneNumber": "555-1001"
}

### Delete patient (use carefully!)
DELETE http://localhost:8080/api/patients/999
Accept: application/json

### Get non-existent patient (should return 404)
GET http://localhost:8080/api/patients/99999
Accept: application/json
```

**How to use**:
1. Open `test-api.http` in VS Code
2. Click "Send Request" above any request
3. View response in new panel

---

### 8.4 AppointmentController Implementation

**Create `src/main/java/com/medical/controller/AppointmentController.java`**:

```java
package com.medical.controller;

import com.medical.domain.Appointment;
import com.medical.repository.AppointmentRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    private final AppointmentRepository appointmentRepository;
    
    public AppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * GET /api/appointments/{id} - Get appointment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/appointments/patient/{patientId} - Get appointments for patient
     */
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getAppointmentsByPatient(@PathVariable Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
    
    /**
     * GET /api/appointments/doctor/{doctorId} - Get appointments for doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    /**
     * GET /api/appointments/doctor/{doctorId}/upcoming - Get upcoming appointments for doctor
     */
    @GetMapping("/doctor/{doctorId}/upcoming")
    public List<Appointment> getUpcomingAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate) {
        
        if (fromDate == null) {
            fromDate = LocalDateTime.now();
        }
        
        return appointmentRepository.findUpcomingByDoctor(doctorId, fromDate);
    }
    
    /**
     * POST /api/appointments - Create new appointment
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    
    /**
     * PATCH /api/appointments/{id}/status - Update appointment status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateAppointmentStatus(@PathVariable Long id, 
                                                          @RequestParam String status) {
        int rowsUpdated = appointmentRepository.updateStatus(id, status);
        
        if (rowsUpdated == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * DELETE /api/appointments/{id} - Delete appointment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        int rowsDeleted = appointmentRepository.deleteById(id);
        
        if (rowsDeleted == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.noContent().build();
    }
}
```

**Add to `test-api.http`**:

```http
### Get appointment by ID
GET http://localhost:8080/api/appointments/1
Accept: application/json

### Get appointments for patient
GET http://localhost:8080/api/appointments/patient/1
Accept: application/json

### Get appointments for doctor
GET http://localhost:8080/api/appointments/doctor/1
Accept: application/json

### Get upcoming appointments for doctor
GET http://localhost:8080/api/appointments/doctor/1/upcoming
Accept: application/json

### Get upcoming appointments from specific date
GET http://localhost:8080/api/appointments/doctor/1/upcoming?fromDate=2024-12-28T00:00:00
Accept: application/json

### Create new appointment
POST http://localhost:8080/api/appointments
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 2,
  "scheduledAt": "2024-12-30T14:00:00",
  "status": "SCHEDULED",
  "notes": "Follow-up consultation"
}

### Update appointment status
PATCH http://localhost:8080/api/appointments/1/status?status=COMPLETED
Accept: application/json

### Cancel appointment
PATCH http://localhost:8080/api/appointments/1/status?status=CANCELLED
Accept: application/json
```

---

### 8.5 Error Handling with @ControllerAdvice

**Problem**: Right now, exceptions throw ugly stack traces. We need consistent error responses.

**Create `src/main/java/com/medical/exception/ErrorResponse.java`**:

```java
package com.medical.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
```

**Create `src/main/java/com/medical/exception/GlobalExceptionHandler.java`**:

```java
package com.medical.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(DuplicateKeyException ex, 
                                                              WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            "Resource already exists: " + extractConstraintName(ex),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                 WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, 
                                                                  WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private String extractConstraintName(DuplicateKeyException ex) {
        String message = ex.getMessage();
        if (message.contains("medical_record_number")) {
            return "Medical Record Number already exists";
        } else if (message.contains("license_number")) {
            return "License Number already exists";
        }
        return "Duplicate key constraint violation";
    }
}
```

**Test error handling** (add to `test-api.http`):

```http
### Try to create duplicate patient (should return 409 Conflict)
POST http://localhost:8080/api/patients
Content-Type: application/json

{
  "name": "Duplicate Patient",
  "dateOfBirth": "1990-01-01",
  "bloodType": "A+",
  "medicalRecordNumber": "MRN-2024-001",
  "email": "duplicate@email.com",
  "phoneNumber": "555-0000"
}
```

**Expected Response**:
```json
{
  "timestamp": "2024-12-26T15:30:45.123",
  "status": 409,
  "error": "Conflict",
  "message": "Resource already exists: Medical Record Number already exists",
  "path": "/api/patients"
}
```

---

### 8.6 Checkpoint Question 8

**Scenario**: Create a `DoctorController` with these endpoints:
1. `GET /api/doctors` - Get all doctors
2. `GET /api/doctors/{id}` - Get doctor by ID
3. `GET /api/doctors/license/{licenseNumber}` - Get doctor by license number
4. `GET /api/doctors/specialization/{specialization}` - Get doctors by specialization
5. `POST /api/doctors` - Create new doctor
6. `PUT /api/doctors/{id}` - Update doctor
7. `DELETE /api/doctors/{id}` - Delete doctor

**Answer**:

```java
package com.medical.controller;

import com.medical.domain.Doctor;
import com.medical.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    
    private final DoctorRepository doctorRepository;
    
    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }
    
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<Doctor> getDoctorByLicense(@PathVariable String licenseNumber) {
        return doctorRepository.findByLicenseNumber(licenseNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/specialization/{specialization}")
    public List<Doctor> getDoctorsBySpecialization(@PathVariable String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorRepository.save(doctor);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id,
                                                 @RequestBody Doctor doctor) {
        if (doctorRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        doctor.setId(id);
        doctorRepository.update(doctor);
        
        return ResponseEntity.ok(doctor);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        int rowsDeleted = doctorRepository.deleteById(id);
        
        if (rowsDeleted == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.noContent().build();
    }
}
```

**Missing `findAll()` method in DoctorRepository**:
```java
// Add to DoctorRepository class
public List<Doctor> findAll() {
    String sql = "SELECT * FROM doctor ORDER BY name";
    return jdbcTemplate.query(sql, this::mapRowToDoctor);
}
```

**Test requests** (add to `test-api.http`):
```http
### Get all doctors
GET http://localhost:8080/api/doctors
Accept: application/json

### Get doctor by ID
GET http://localhost:8080/api/doctors/1
Accept: application/json

### Get doctor by license number
GET http://localhost:8080/api/doctors/license/MED-2018-001
Accept: application/json

### Get cardiologists
GET http://localhost:8080/api/doctors/specialization/Cardiology
Accept: application/json

### Create new doctor
POST http://localhost:8080/api/doctors
Content-Type: application/json

{
  "name": "Dr. Rachel Green",
  "specialization": "Dermatology",
  "licenseNumber": "MED-2024-999",
  "email": "rgreen@medical.com",
  "phoneNumber": "555-0200"
}
```

---

### 8.7 API Documentation with Swagger/OpenAPI

**Add Swagger dependency to `pom.xml`**:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Configure in `application.yml`**:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
```

**Restart application** and visit:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

**Benefits**:
- ✅ Interactive API documentation
- ✅ Test endpoints directly from browser
- ✅ Auto-generated from controller annotations
- ✅ Share with frontend developers

---

### 8.8 CORS Configuration

**Problem**: Frontend running on `localhost:3000` can't call API on `localhost:8080` (CORS error).

**Solution**: Create CORS configuration.

**Create `src/main/java/com/medical/config/WebConfig.java`**:

```java
package com.medical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

**For production**, restrict origins:
```java
.allowedOrigins("https://medical-app.com")
```

---

### 8.9 Summary: REST API Best Practices

**✅ DO**:
- Use standard HTTP methods (GET, POST, PUT, DELETE)
- Return appropriate status codes (200, 201, 404, etc.)
- Use `ResponseEntity` for flexible responses
- Handle errors with `@ControllerAdvice`
- Use plural nouns in URLs (`/patients` not `/patient`)
- Version your API (`/api/v1/patients`)
- Document with Swagger/OpenAPI

**❌ DON'T**:
- Put business logic in controllers (use services)
- Expose database exceptions to clients
- Return null (use `Optional` and `ResponseEntity`)
- Use verbs in URLs (`/getPatient` - wrong)
- Ignore security (add authentication later)

**Controller Responsibilities**:
- ✅ Parse HTTP requests
- ✅ Validate input
- ✅ Call service layer
- ✅ Format HTTP responses
- ❌ Business logic
- ❌ Direct database access

---

**Next Step**: We've completed JdbcTemplate implementation! Next, we'll explore Spring Data JDBC for even less boilerplate in Step 9!

---

## 9. SPRING DATA JDBC INTRODUCTION

> **Goal**: Eliminate repository boilerplate by using Spring Data JDBC's auto-generated implementations.

---

### 9.1 What Is Spring Data JDBC?

**Spring Data JDBC**: An opinionated persistence framework that auto-generates repository implementations from interfaces.

**How It Works**:
```java
// You write this interface
public interface PatientRepository extends CrudRepository<Patient, Long> {
    // Spring generates the implementation automatically!
}

// Spring provides: save(), findById(), findAll(), delete(), count(), etc.
```

**Key Differences from JdbcTemplate**:

| Aspect | JdbcTemplate | Spring Data JDBC |
|--------|--------------|------------------|
| **Code you write** | Full SQL + mapping | Interface only |
| **CRUD operations** | Manual implementation | Auto-generated |
| **Custom queries** | Full SQL control | `@Query` annotation |
| **Learning curve** | Moderate | Low |
| **Flexibility** | Maximum | High |
| **Boilerplate** | Medium | Minimal |

---

### 9.2 Spring Data JDBC Philosophy

**Opinionated Choices**:
1. **No lazy loading** - All data loaded eagerly (predictable performance)
2. **No caching** - Always fresh data from database
3. **No dirty tracking** - Explicit `save()` required for updates
4. **Aggregate-oriented** - DDD aggregates mapped to tables
5. **Simple is better** - Less magic than JPA/Hibernate

**When to Use Spring Data JDBC**:
- ✅ Standard CRUD operations dominate your app
- ✅ You want simple, predictable behavior
- ✅ Domain model follows DDD aggregates
- ✅ You don't need lazy loading or caching
- ✅ You want to avoid JPA complexity

**When to Use JdbcTemplate Instead**:
- ✅ Complex queries with many joins
- ✅ Stored procedures
- ✅ Batch operations with fine-tuned control
- ✅ Database-specific features (PostgreSQL JSON, arrays)
- ✅ Dynamic query building

---

### 9.3 Configuring Domain Objects for Spring Data JDBC

**Key Annotations**:
- `@Table` - Maps class to table (optional if names match)
- `@Id` - Marks primary key field
- `@Column` - Maps field to column (optional if names match)
- `@Transient` - Exclude field from persistence

**Updated Patient Entity**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("patient")
public class Patient {
    
    @Id
    private Long id;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    private String name;
    
    @Column("date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column("blood_type")
    private String bloodType;
    
    @Column("medical_record_number")
    private String medicalRecordNumber;
    
    private String email;
    
    @Column("phone_number")
    private String phoneNumber;
    
    public Patient(String name, LocalDate dateOfBirth, String bloodType, String mrn) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.medicalRecordNumber = mrn;
        this.createdAt = LocalDateTime.now();
    }
}
```

**Column Naming Convention**:
- Database: `medical_record_number` (snake_case)
- Java: `medicalRecordNumber` (camelCase)
- `@Column` annotation bridges the gap

**Alternative: Configure Naming Strategy**:

```java
package com.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

@Configuration
public class DataJdbcConfig {
    
    @Bean
    public NamingStrategy namingStrategy() {
        return new NamingStrategy() {
            @Override
            public String getColumnName(RelationalPersistentProperty property) {
                // Convert camelCase to snake_case automatically
                return property.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            }
        };
    }
}
```

With this, you can **remove all `@Column` annotations** - Spring will auto-convert.

---

### 9.4 Creating Spring Data JDBC Repository

**Create `src/main/java/com/medical/repository/SpringDataPatientRepository.java`**:

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPatientRepository extends CrudRepository<Patient, Long> {
    
    // Spring auto-generates these methods:
    // - save(Patient) - Insert or update
    // - findById(Long) - Find by ID
    // - findAll() - Get all
    // - deleteById(Long) - Delete by ID
    // - count() - Count all
    // - existsById(Long) - Check existence
    
    // Custom query methods (Spring generates implementation)
    Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);
    
    List<Patient> findByBloodType(String bloodType);
    
    List<Patient> findByNameContainingIgnoreCase(String name);
    
    boolean existsByMedicalRecordNumber(String medicalRecordNumber);
    
    // Custom SQL query
    @Query("SELECT * FROM patient WHERE EXTRACT(YEAR FROM date_of_birth) = :year")
    List<Patient> findByBirthYear(@Param("year") int year);
    
    // Count query
    @Query("SELECT COUNT(*) FROM patient WHERE blood_type = :bloodType")
    long countByBloodType(@Param("bloodType") String bloodType);
}
```

**Method Naming Magic**:

Spring parses method names to generate queries:

| Method Name | Generated SQL |
|-------------|---------------|
| `findByMedicalRecordNumber` | `WHERE medical_record_number = ?` |
| `findByBloodType` | `WHERE blood_type = ?` |
| `findByNameContainingIgnoreCase` | `WHERE LOWER(name) LIKE LOWER(?)` |
| `existsByMedicalRecordNumber` | `SELECT COUNT(*) WHERE medical_record_number = ?` |

**Supported Keywords**:
- `findBy`, `getBy`, `queryBy` - SELECT query
- `countBy` - COUNT query
- `existsBy` - Existence check
- `deleteBy`, `removeBy` - DELETE query
- `Containing`, `StartingWith`, `EndingWith` - LIKE patterns
- `IgnoreCase` - Case-insensitive
- `OrderBy` - Sorting
- `And`, `Or` - Multiple conditions

---

### 9.5 Using the Spring Data JDBC Repository

**Create Service Layer**:

```java
package com.medical.service;

import com.medical.domain.Patient;
import com.medical.repository.SpringDataPatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    
    private final SpringDataPatientRepository patientRepository;
    
    public PatientService(SpringDataPatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }
    
    @Transactional
    public Patient createPatient(Patient patient) {
        // Check for duplicate MRN
        if (patientRepository.existsByMedicalRecordNumber(patient.getMedicalRecordNumber())) {
            throw new IllegalArgumentException("Patient with MRN " + 
                patient.getMedicalRecordNumber() + " already exists");
        }
        
        return patientRepository.save(patient);
    }
    
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }
    
    public Optional<Patient> getPatientByMrn(String mrn) {
        return patientRepository.findByMedicalRecordNumber(mrn);
    }
    
    public List<Patient> getAllPatients() {
        return (List<Patient>) patientRepository.findAll();
    }
    
    public List<Patient> getPatientsByBloodType(String bloodType) {
        return patientRepository.findByBloodType(bloodType);
    }
    
    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Patient> getPatientsByBirthYear(int year) {
        return patientRepository.findByBirthYear(year);
    }
    
    @Transactional
    public Patient updatePatient(Long id, Patient updatedPatient) {
        Patient existing = patientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
        
        // Update fields
        existing.setName(updatedPatient.getName());
        existing.setDateOfBirth(updatedPatient.getDateOfBirth());
        existing.setBloodType(updatedPatient.getBloodType());
        existing.setEmail(updatedPatient.getEmail());
        existing.setPhoneNumber(updatedPatient.getPhoneNumber());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return patientRepository.save(existing);
    }
    
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found: " + id);
        }
        patientRepository.deleteById(id);
    }
    
    public long countPatients() {
        return patientRepository.count();
    }
    
    public long countPatientsByBloodType(String bloodType) {
        return patientRepository.countByBloodType(bloodType);
    }
}
```

**Update Controller to Use Service**:

```java
package com.medical.controller;

import com.medical.domain.Patient;
import com.medical.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/patients")  // v2 for Spring Data JDBC version
public class PatientV2Controller {
    
    private final PatientService patientService;
    
    public PatientV2Controller(PatientService patientService) {
        this.patientService = patientService;
    }
    
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<Patient> getPatientByMrn(@PathVariable String mrn) {
        return patientService.getPatientByMrn(mrn)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/blood-type/{type}")
    public List<Patient> getPatientsByBloodType(@PathVariable String type) {
        return patientService.getPatientsByBloodType(type);
    }
    
    @GetMapping("/search")
    public List<Patient> searchPatients(@RequestParam String name) {
        return patientService.searchPatientsByName(name);
    }
    
    @GetMapping("/birth-year/{year}")
    public List<Patient> getPatientsByBirthYear(@PathVariable int year) {
        return patientService.getPatientsByBirthYear(year);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id,
                                                   @RequestBody Patient patient) {
        try {
            Patient updated = patientService.updatePatient(id, patient);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/count")
    public long countPatients() {
        return patientService.countPatients();
    }
    
    @GetMapping("/count/blood-type/{type}")
    public long countByBloodType(@PathVariable String type) {
        return patientService.countPatientsByBloodType(type);
    }
}
```

---

### 9.6 Testing Spring Data JDBC Repository

**Create `src/test/java/com/medical/repository/SpringDataPatientRepositoryTest.java`**:

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SpringDataPatientRepositoryTest {
    
    @Autowired
    private SpringDataPatientRepository patientRepository;
    
    private Patient testPatient;
    
    @BeforeEach
    void setUp() {
        testPatient = new Patient(
            "Spring Data Test Patient",
            LocalDate.of(1985, 8, 15),
            "B+",
            "MRN-SD-TEST-001"
        );
        testPatient.setEmail("sdtest@example.com");
        testPatient.setPhoneNumber("555-8888");
    }
    
    @Test
    void testSave() {
        Patient saved = patientRepository.save(testPatient);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Spring Data Test Patient");
    }
    
    @Test
    void testFindById() {
        Patient saved = patientRepository.save(testPatient);
        
        Optional<Patient> found = patientRepository.findById(saved.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Spring Data Test Patient");
    }
    
    @Test
    void testFindByMedicalRecordNumber() {
        patientRepository.save(testPatient);
        
        Optional<Patient> found = patientRepository.findByMedicalRecordNumber("MRN-SD-TEST-001");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Spring Data Test Patient");
    }
    
    @Test
    void testFindByBloodType() {
        patientRepository.save(testPatient);
        
        List<Patient> bPositive = patientRepository.findByBloodType("B+");
        
        assertThat(bPositive).isNotEmpty();
        assertThat(bPositive).anyMatch(p -> p.getMedicalRecordNumber().equals("MRN-SD-TEST-001"));
    }
    
    @Test
    void testFindByNameContaining() {
        patientRepository.save(testPatient);
        
        List<Patient> results = patientRepository.findByNameContainingIgnoreCase("spring");
        
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.getName().contains("Spring Data"));
    }
    
    @Test
    void testExistsByMedicalRecordNumber() {
        patientRepository.save(testPatient);
        
        boolean exists = patientRepository.existsByMedicalRecordNumber("MRN-SD-TEST-001");
        boolean notExists = patientRepository.existsByMedicalRecordNumber("MRN-NONEXISTENT");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testFindByBirthYear() {
        patientRepository.save(testPatient);
        
        List<Patient> patients1985 = patientRepository.findByBirthYear(1985);
        
        assertThat(patients1985).isNotEmpty();
        assertThat(patients1985).anyMatch(p -> p.getMedicalRecordNumber().equals("MRN-SD-TEST-001"));
    }
    
    @Test
    void testUpdate() {
        Patient saved = patientRepository.save(testPatient);
        
        saved.setName("Updated Name");
        saved.setUpdatedAt(LocalDateTime.now());
        Patient updated = patientRepository.save(saved);
        
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testDelete() {
        Patient saved = patientRepository.save(testPatient);
        
        patientRepository.deleteById(saved.getId());
        
        Optional<Patient> deleted = patientRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void testCount() {
        long initialCount = patientRepository.count();
        
        patientRepository.save(testPatient);
        
        long newCount = patientRepository.count();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
    
    @Test
    void testCountByBloodType() {
        patientRepository.save(testPatient);
        
        long bPositiveCount = patientRepository.countByBloodType("B+");
        
        assertThat(bPositiveCount).isGreaterThan(0);
    }
}
```

**Run tests**:
```powershell
./mvnw test -Dtest=SpringDataPatientRepositoryTest
```

---

### 9.7 Query Method Keywords Reference

**Complete list of supported keywords**:

```java
public interface PatientRepository extends CrudRepository<Patient, Long> {
    
    // Equality
    List<Patient> findByName(String name);
    List<Patient> findByBloodType(String bloodType);
    
    // Comparison
    List<Patient> findByIdGreaterThan(Long id);
    List<Patient> findByIdLessThan(Long id);
    List<Patient> findByIdBetween(Long start, Long end);
    
    // Null checks
    List<Patient> findByEmailIsNull();
    List<Patient> findByEmailIsNotNull();
    
    // String operations
    List<Patient> findByNameContaining(String substring);
    List<Patient> findByNameStartingWith(String prefix);
    List<Patient> findByNameEndingWith(String suffix);
    List<Patient> findByNameIgnoreCase(String name);
    
    // Logical operators
    List<Patient> findByNameAndBloodType(String name, String bloodType);
    List<Patient> findByNameOrBloodType(String name, String bloodType);
    
    // Ordering
    List<Patient> findByBloodTypeOrderByNameAsc(String bloodType);
    List<Patient> findByBloodTypeOrderByCreatedAtDesc(String bloodType);
    
    // Limiting results
    Patient findFirstByOrderByCreatedAtDesc();
    List<Patient> findTop5ByOrderByCreatedAtDesc();
    
    // Existence
    boolean existsByMedicalRecordNumber(String mrn);
    
    // Counting
    long countByBloodType(String bloodType);
    
    // Deletion
    long deleteByBloodType(String bloodType);
}
```

---

### 9.8 Checkpoint Question 9

**Scenario**: Create a Spring Data JDBC repository for the Doctor entity with these query methods:

1. Find by license number
2. Find by specialization (case-insensitive)
3. Find all doctors whose name contains a substring
4. Count doctors by specialization
5. Check if doctor exists by license number
6. Find top 3 most recently created doctors
7. Custom query: Find doctors with appointments scheduled after a date

**Answer**:

```java
package com.medical.repository;

import com.medical.domain.Doctor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataDoctorRepository extends CrudRepository<Doctor, Long> {
    
    // 1. Find by license number
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    // 2. Find by specialization (case-insensitive)
    List<Doctor> findBySpecializationIgnoreCase(String specialization);
    
    // 3. Find doctors whose name contains substring
    List<Doctor> findByNameContainingIgnoreCase(String name);
    
    // 4. Count doctors by specialization
    long countBySpecialization(String specialization);
    
    // 5. Check if doctor exists by license number
    boolean existsByLicenseNumber(String licenseNumber);
    
    // 6. Find top 3 most recently created doctors
    List<Doctor> findTop3ByOrderByCreatedAtDesc();
    
    // 7. Custom query: Find doctors with appointments after date
    @Query("""
        SELECT DISTINCT d.* 
        FROM doctor d
        JOIN appointment a ON d.id = a.doctor_id
        WHERE a.scheduled_at > :date
        ORDER BY d.name
    """)
    List<Doctor> findDoctorsWithAppointmentsAfter(@Param("date") LocalDateTime date);
}
```

**Usage Example**:

```java
@Service
public class DoctorService {
    
    private final SpringDataDoctorRepository doctorRepository;
    
    public DoctorService(SpringDataDoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }
    
    public List<Doctor> searchDoctors(String query) {
        return doctorRepository.findByNameContainingIgnoreCase(query);
    }
    
    public List<Doctor> getCardiologists() {
        return doctorRepository.findBySpecializationIgnoreCase("cardiology");
    }
    
    public long countSpecialists(String specialization) {
        return doctorRepository.countBySpecialization(specialization);
    }
    
    public List<Doctor> getRecentDoctors() {
        return doctorRepository.findTop3ByOrderByCreatedAtDesc();
    }
    
    public List<Doctor> getDoctorsWithUpcomingAppointments() {
        return doctorRepository.findDoctorsWithAppointmentsAfter(LocalDateTime.now());
    }
}
```

---

### 9.9 @Query Annotation for Custom SQL

**When to use `@Query`**:
- ✅ Complex joins not expressible with method names
- ✅ Aggregate functions (SUM, AVG, MAX)
- ✅ Subqueries
- ✅ Database-specific functions

**Example - Complex Queries**:

```java
@Repository
public interface SpringDataPatientRepository extends CrudRepository<Patient, Long> {
    
    // Join with appointments
    @Query("""
        SELECT p.* FROM patient p
        JOIN appointment a ON p.id = a.patient_id
        WHERE a.doctor_id = :doctorId
        GROUP BY p.id
        ORDER BY COUNT(a.id) DESC
        LIMIT 10
    """)
    List<Patient> findTop10PatientsByDoctorAppointments(@Param("doctorId") Long doctorId);
    
    // Aggregate function
    @Query("""
        SELECT AVG(EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM date_of_birth))
        FROM patient
        WHERE blood_type = :bloodType
    """)
    Double findAverageAgeByBloodType(@Param("bloodType") String bloodType);
    
    // Subquery
    @Query("""
        SELECT * FROM patient
        WHERE id IN (
            SELECT DISTINCT patient_id FROM appointment
            WHERE status = 'COMPLETED'
            AND scheduled_at > :date
        )
    """)
    List<Patient> findPatientsWithCompletedAppointmentsSince(@Param("date") LocalDateTime date);
}
```

---

### 9.10 Pagination and Sorting

**Add PagingAndSortingRepository**:

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataPatientRepository 
        extends CrudRepository<Patient, Long>, 
                PagingAndSortingRepository<Patient, Long> {
    
    // Query methods with pagination
    Page<Patient> findByBloodType(String bloodType, Pageable pageable);
    
    // Sorted results
    List<Patient> findByBloodType(String bloodType, Sort sort);
}
```

**Usage in Service**:

```java
@Service
public class PatientService {
    
    private final SpringDataPatientRepository patientRepository;
    
    public Page<Patient> getPatients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return patientRepository.findAll(pageable);
    }
    
    public Page<Patient> getPatientsByBloodType(String bloodType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.findByBloodType(bloodType, pageable);
    }
    
    public List<Patient> getPatientsSorted() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return (List<Patient>) patientRepository.findAll(sort);
    }
}
```

**Controller with Pagination**:

```java
@GetMapping
public Page<Patient> getAllPatients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return patientService.getPatients(page, size);
}
```

**Response**:
```json
{
  "content": [
    { "id": 1, "name": "John Smith", ... },
    { "id": 2, "name": "Emma Davis", ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalPages": 5,
  "totalElements": 87,
  "last": false,
  "first": true
}
```

---

### 9.11 Summary: Spring Data JDBC Benefits

**Code Reduction**:

| Approach | Lines of Code | What You Write |
|----------|---------------|----------------|
| **Raw JDBC** | ~150 per entity | SQL, mapping, cleanup, exception handling |
| **JdbcTemplate** | ~100 per entity | SQL, mapping |
| **Spring Data JDBC** | ~10 per entity | Interface + query methods |

**Comparison Example**:

```java
// JdbcTemplate (100 lines)
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public Patient save(Patient patient) { /* 15 lines */ }
    public Optional<Patient> findById(Long id) { /* 10 lines */ }
    public List<Patient> findAll() { /* 8 lines */ }
    // ... 8 more methods, each 8-15 lines
}

// Spring Data JDBC (10 lines)
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    Optional<Patient> findByMedicalRecordNumber(String mrn);
    List<Patient> findByBloodType(String bloodType);
}
```

**90% less code!**

---

### 9.12 When NOT to Use Spring Data JDBC

**Limitations**:
- ❌ No lazy loading (all relationships loaded eagerly)
- ❌ No automatic dirty checking (must call `save()` explicitly)
- ❌ Limited support for complex joins (use `@Query` instead)
- ❌ No caching (every query hits database)
- ❌ No bi-directional relationships

**Fallback to JdbcTemplate**:
```java
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    
    // Spring Data JDBC for simple queries
    List<Patient> findByBloodType(String bloodType);
    
    // Inject JdbcTemplate for complex queries
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    default List<PatientStatistics> getComplexStatistics() {
        String sql = """
            SELECT p.blood_type, COUNT(*) as count, 
                   AVG(EXTRACT(YEAR FROM AGE(p.date_of_birth))) as avg_age
            FROM patient p
            GROUP BY p.blood_type
            ORDER BY count DESC
        """;
        return jdbcTemplate.query(sql, this::mapToStatistics);
    }
}
```

---

**Next Step**: In Step 10, we'll explore advanced Spring Data JDBC features like aggregates, modifying queries, and callbacks!

---

## 10. SPRING DATA JDBC ADVANCED FEATURES

> **Goal**: Master aggregates, embedded entities, auditing, and lifecycle callbacks in Spring Data JDBC.

---

### 10.1 Aggregate Roots and References

**Aggregate Pattern**: Group related entities that must be modified together.

**Example: Appointment is Aggregate Root**

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Table("appointment")
public class Appointment {
    
    @Id
    private Long id;
    private LocalDateTime createdAt;
    
    private Long patientId;  // Reference to Patient (not loaded by default)
    private Long doctorId;   // Reference to Doctor (not loaded by default)
    
    private LocalDateTime scheduledAt;
    private String status;
    private String notes;
    
    // Prescriptions are part of the aggregate - loaded eagerly
    @MappedCollection(idColumn = "appointment_id")
    private Set<Prescription> prescriptions = new HashSet<>();
    
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }
}
```

**Prescription as Aggregate Member**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("prescription")
public class Prescription {
    
    @Id
    private Long id;
    
    // No appointmentId field needed - Spring Data JDBC manages it
    
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
    
    public Prescription(String medicationName, String dosage, String frequency, Integer durationDays) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
    }
}
```

**AppointmentRepository**:

```java
package com.medical.repository;

import com.medical.domain.Appointment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpringDataAppointmentRepository extends CrudRepository<Appointment, Long> {
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorId(Long doctorId);
    
    List<Appointment> findByStatus(String status);
    
    List<Appointment> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
```

**Saving Aggregate**:

```java
@Service
public class AppointmentService {
    
    private final SpringDataAppointmentRepository appointmentRepository;
    
    @Transactional
    public Appointment createAppointmentWithPrescriptions(
            Long patientId, 
            Long doctorId, 
            LocalDateTime scheduledAt) {
        
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setScheduledAt(scheduledAt);
        appointment.setStatus("SCHEDULED");
        appointment.setCreatedAt(LocalDateTime.now());
        
        // Add prescriptions
        appointment.addPrescription(new Prescription(
            "Aspirin", "100mg", "Daily", 30
        ));
        appointment.addPrescription(new Prescription(
            "Vitamin D", "1000 IU", "Daily", 90
        ));
        
        // One save() saves appointment + all prescriptions
        return appointmentRepository.save(appointment);
    }
    
    public Appointment getAppointmentWithPrescriptions(Long id) {
        // Prescriptions loaded automatically
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
    }
}
```

**Key Points**:
- ✅ Saving aggregate root saves all children
- ✅ Loading aggregate root loads all children eagerly
- ✅ Deleting aggregate root deletes all children (cascade)
- ✅ No separate repository needed for Prescription

---

### 10.2 Embedded Entities

**Value Objects** can be embedded in entities without separate tables.

**Example: Address Value Object**:

```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
}
```

**Patient with Embedded Address**:

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table("patient")
public class Patient {
    
    @Id
    private Long id;
    private LocalDateTime createdAt;
    
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    
    @Embedded.Nullable
    private Address homeAddress;  // Flattened into patient table
    
    private String email;
    private String phoneNumber;
}
```

**Database Schema** (flattened columns):

```sql
CREATE TABLE patient (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    name VARCHAR(100),
    date_of_birth DATE,
    blood_type VARCHAR(5),
    medical_record_number VARCHAR(20),
    
    -- Embedded Address fields
    home_address_street VARCHAR(100),
    home_address_city VARCHAR(50),
    home_address_state VARCHAR(2),
    home_address_zip_code VARCHAR(10),
    
    email VARCHAR(100),
    phone_number VARCHAR(20)
);
```

**Custom Prefix for Embedded Fields**:

```java
@Embedded.Nullable(prefix = "billing_")
private Address billingAddress;

// Results in columns: billing_street, billing_city, billing_state, billing_zip_code
```

---

### 10.3 Auditing with @CreatedDate and @LastModifiedDate

**Enable Auditing**:

```java
package com.medical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration
@EnableJdbcAuditing
public class AuditingConfig {
}
```

**Entity with Auditing Annotations**:

```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table("patient")
public class Patient {
    
    @Id
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;  // Auto-set on insert
    
    @LastModifiedDate
    private LocalDateTime updatedAt;  // Auto-set on update
    
    private String name;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalRecordNumber;
    private String email;
    private String phoneNumber;
}
```

**How It Works**:
```java
Patient patient = new Patient();
patient.setName("John Doe");
patientRepository.save(patient);
// createdAt is automatically set to now()

patient.setName("John Smith");
patientRepository.save(patient);
// updatedAt is automatically set to now()
```

---

### 10.4 Custom Auditor with @CreatedBy and @LastModifiedBy

**Track WHO made changes**:

```java
@Data
@Table("patient")
public class Patient {
    
    @Id
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @CreatedBy
    private String createdBy;  // Username who created
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @LastModifiedBy
    private String lastModifiedBy;  // Username who last modified
    
    // ... other fields
}
```

**Implement AuditorAware**:

```java
package com.medical.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        // In real app, get from Spring Security context
        // SecurityContextHolder.getContext().getAuthentication().getName()
        
        // For now, return system user
        return Optional.of("SYSTEM");
    }
}
```

**Enable Auditor**:

```java
@Configuration
@EnableJdbcAuditing(auditorAwareRef = "auditorAwareImpl")
public class AuditingConfig {
}
```

---

### 10.5 Lifecycle Callbacks

**Spring Data JDBC Callbacks**:

```java
package com.medical.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Data
@Table("patient")
public class Patient {
    @Id
    private Long id;
    private String name;
    private String medicalRecordNumber;
    private LocalDateTime createdAt;
    
    // ... other fields
}

// Callback Component
@Component
class PatientCallbacks implements 
        BeforeConvertCallback<Patient>, 
        BeforeSaveCallback<Patient> {
    
    @Override
    public Patient onBeforeConvert(Patient patient) {
        // Called before entity is converted to database row
        System.out.println("Before Convert: " + patient.getName());
        
        // Auto-generate MRN if not set
        if (patient.getMedicalRecordNumber() == null) {
            patient.setMedicalRecordNumber("MRN-" + UUID.randomUUID().toString());
        }
        
        return patient;
    }
    
    @Override
    public Patient onBeforeSave(Patient patient, MutableAggregateChange<Patient> aggregateChange) {
        // Called before save to database
        System.out.println("Before Save: " + patient.getName());
        
        if (patient.getCreatedAt() == null) {
            patient.setCreatedAt(LocalDateTime.now());
        }
        
        return patient;
    }
}
```

**Available Callbacks**:
- `BeforeConvertCallback` - Before entity → database conversion
- `BeforeSaveCallback` - Before INSERT/UPDATE
- `AfterSaveCallback` - After INSERT/UPDATE
- `AfterLoadCallback` - After entity loaded from database
- `BeforeDeleteCallback` - Before DELETE

---

### 10.6 Modifying Queries

**Problem**: Built-in `save()` always updates ALL fields. What if you want to update specific fields?

**Solution**: Use `@Modifying` queries.

```java
@Repository
public interface SpringDataPatientRepository extends CrudRepository<Patient, Long> {
    
    // Update specific field
    @Modifying
    @Query("UPDATE patient SET email = :email WHERE id = :id")
    int updateEmail(@Param("id") Long id, @Param("email") String email);
    
    // Batch update
    @Modifying
    @Query("UPDATE patient SET blood_type = :newType WHERE blood_type = :oldType")
    int updateBloodType(@Param("oldType") String oldType, @Param("newType") String newType);
    
    // Delete by criteria
    @Modifying
    @Query("DELETE FROM patient WHERE created_at < :date")
    int deleteOldPatients(@Param("date") LocalDateTime date);
}
```

**Usage**:

```java
@Service
public class PatientService {
    
    @Transactional
    public void updatePatientEmail(Long patientId, String newEmail) {
        int rowsAffected = patientRepository.updateEmail(patientId, newEmail);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Patient not found");
        }
    }
}
```

**Important**: `@Modifying` queries return number of affected rows, not entities.

---

### 10.7 Checkpoint Question 10

**Scenario**: Implement a Doctor entity with these requirements:

1. Doctor is an aggregate root
2. Each doctor has multiple `Certification` records (certifications aggregate member)
3. Doctor has embedded `ContactInfo` value object (phone, email, office address)
4. Enable auditing (created date, created by, last modified date, last modified by)
5. Add lifecycle callback to auto-generate license number if not provided

**Answer**:

**ContactInfo Value Object**:
```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    private String phoneNumber;
    private String email;
    private String officeAddress;
}
```

**Certification Entity**:
```java
package com.medical.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("certification")
public class Certification {
    
    @Id
    private Long id;
    
    private String certificationName;
    private String issuingOrganization;
    private LocalDate issueDate;
    private LocalDate expiryDate;
}
```

**Doctor Aggregate Root**:
```java
package com.medical.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Table("doctor")
public class Doctor {
    
    @Id
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @LastModifiedBy
    private String lastModifiedBy;
    
    private String name;
    private String specialization;
    private String licenseNumber;
    
    @Embedded.Nullable
    private ContactInfo contactInfo;
    
    @MappedCollection(idColumn = "doctor_id")
    private Set<Certification> certifications = new HashSet<>();
    
    public void addCertification(Certification certification) {
        certifications.add(certification);
    }
}
```

**Lifecycle Callback**:
```java
package com.medical.config;

import com.medical.domain.Doctor;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DoctorCallbacks implements BeforeConvertCallback<Doctor> {
    
    @Override
    public Doctor onBeforeConvert(Doctor doctor) {
        // Auto-generate license number if not set
        if (doctor.getLicenseNumber() == null || doctor.getLicenseNumber().isEmpty()) {
            doctor.setLicenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return doctor;
    }
}
```

**Database Schema**:
```sql
CREATE TABLE doctor (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    last_modified_by VARCHAR(50),
    
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(50) NOT NULL,
    license_number VARCHAR(20) NOT NULL UNIQUE,
    
    -- Embedded ContactInfo
    contact_info_phone_number VARCHAR(20),
    contact_info_email VARCHAR(100),
    contact_info_office_address VARCHAR(200)
);

CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    
    certification_name VARCHAR(100) NOT NULL,
    issuing_organization VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE
);
```

**Usage Example**:
```java
@Service
public class DoctorService {
    
    private final SpringDataDoctorRepository doctorRepository;
    
    @Transactional
    public Doctor createDoctorWithCertifications(String name, String specialization) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        // licenseNumber auto-generated by callback
        
        ContactInfo contact = new ContactInfo(
            "555-1234",
            "doctor@medical.com",
            "123 Medical Plaza"
        );
        doctor.setContactInfo(contact);
        
        doctor.addCertification(new Certification(
            null,
            "Board Certified",
            "American Medical Association",
            LocalDate.of(2020, 1, 15),
            LocalDate.of(2030, 1, 15)
        ));
        
        return doctorRepository.save(doctor);
    }
}
```

---

### 10.8 Custom Converters

**Problem**: Store complex types (e.g., JSON, enums with custom values) in database.

**Example: Store List<String> as JSON**:

```java
package com.medical.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class JdbcConfig extends AbstractJdbcConfiguration {
    
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
            new ListToJsonConverter(),
            new JsonToListConverter()
        ));
    }
    
    @WritingConverter
    static class ListToJsonConverter implements Converter<List<String>, String> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public String convert(List<String> source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert list to JSON", e);
            }
        }
    }
    
    @ReadingConverter
    static class JsonToListConverter implements Converter<String, List<String>> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public List<String> convert(String source) {
            try {
                return objectMapper.readValue(source, List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSON to list", e);
            }
        }
    }
}
```

**Usage**:
```java
@Data
@Table("patient")
public class Patient {
    @Id
    private Long id;
    private String name;
    
    // Stored as JSON string in database
    private List<String> allergies;
}
```

---

### 10.9 Summary: Spring Data JDBC Advanced Features

**Aggregates**:
- ✅ Use `@MappedCollection` for child entities
- ✅ One save() saves root + all children
- ✅ Children loaded eagerly with root
- ✅ Cascade delete automatic

**Embedded Entities**:
- ✅ Use `@Embedded.Nullable` for value objects
- ✅ Flattened into parent table
- ✅ Custom prefixes with `@Embedded.Nullable(prefix = "...")`

**Auditing**:
- ✅ Enable with `@EnableJdbcAuditing`
- ✅ `@CreatedDate`, `@LastModifiedDate` - automatic timestamps
- ✅ `@CreatedBy`, `@LastModifiedBy` - automatic user tracking
- ✅ Implement `AuditorAware` for custom user resolution

**Lifecycle Callbacks**:
- ✅ `BeforeConvertCallback` - before entity → row conversion
- ✅ `BeforeSaveCallback` - before INSERT/UPDATE
- ✅ `AfterSaveCallback` - after INSERT/UPDATE
- ✅ Use for validation, auto-generation, logging

**Modifying Queries**:
- ✅ Use `@Modifying` + `@Query` for custom UPDATE/DELETE
- ✅ Returns affected row count
- ✅ More efficient than load → modify → save

**Custom Converters**:
- ✅ `@WritingConverter` - Java → database
- ✅ `@ReadingConverter` - database → Java
- ✅ Register in `JdbcCustomConversions`

---

**Next Step**: In Step 11, we'll compare JdbcTemplate vs Spring Data JDBC to choose the right tool for each scenario!

---

## 11. JDBCTEMPLATE VS SPRING DATA JDBC: CHOOSING THE RIGHT TOOL

> **Goal**: Understand when to use JdbcTemplate, Spring Data JDBC, or both together for optimal results.

---

### 11.1 Side-by-Side Feature Comparison

| Feature | JdbcTemplate | Spring Data JDBC | Winner |
|---------|--------------|------------------|--------|
| **Code Volume** | Medium (~100 lines/entity) | Low (~10 lines/entity) | 🏆 Spring Data JDBC |
| **SQL Control** | Full control | Full control with @Query | 🤝 Tie |
| **Learning Curve** | Moderate | Low | 🏆 Spring Data JDBC |
| **Complex Queries** | Excellent | Good (via @Query) | 🏆 JdbcTemplate |
| **Batch Operations** | Excellent | Limited | 🏆 JdbcTemplate |
| **Custom Mapping** | Full control | Good (converters) | 🏆 JdbcTemplate |
| **Dynamic Queries** | Easy | Difficult | 🏆 JdbcTemplate |
| **Stored Procedures** | Full support | Limited | 🏆 JdbcTemplate |
| **CRUD Operations** | Manual | Auto-generated | 🏆 Spring Data JDBC |
| **Aggregate Support** | Manual | Built-in | 🏆 Spring Data JDBC |
| **Type Safety** | Runtime errors | Compile-time checks | 🏆 Spring Data JDBC |
| **Testing** | Mock JdbcTemplate | Mock Repository | 🤝 Tie |
| **Performance** | Optimal | Optimal | 🤝 Tie |

---

### 11.2 Real-World Scenario Analysis

#### Scenario 1: Simple Patient CRUD Operations

**Best Choice**: 🏆 **Spring Data JDBC**

**Why**: 
- Standard CRUD operations
- No complex business logic
- Query methods handle all needs

**Code Comparison**:

```java
// JdbcTemplate (100 lines)
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbc;
    
    public Patient save(Patient p) { /* 15 lines */ }
    public Optional<Patient> findById(Long id) { /* 10 lines */ }
    public List<Patient> findAll() { /* 8 lines */ }
    public List<Patient> findByBloodType(String type) { /* 10 lines */ }
    // ... more methods
}

// Spring Data JDBC (8 lines)
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    List<Patient> findByBloodType(String bloodType);
    Optional<Patient> findByMedicalRecordNumber(String mrn);
}
```

**Verdict**: Spring Data JDBC wins with **92% less code**.

---

#### Scenario 2: Complex Reporting Query

**Task**: Generate monthly appointment statistics with doctor performance metrics.

**Best Choice**: 🏆 **JdbcTemplate**

**Why**:
- Multiple joins across 4 tables
- Aggregate functions (COUNT, AVG, SUM)
- Complex GROUP BY with HAVING
- Custom result mapping

**JdbcTemplate Implementation**:

```java
@Repository
public class ReportingRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public List<DoctorPerformanceReport> generateMonthlyReport(YearMonth month) {
        String sql = """
            SELECT 
                d.id,
                d.name,
                d.specialization,
                COUNT(a.id) as total_appointments,
                COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_appointments,
                COUNT(CASE WHEN a.status = 'CANCELLED' THEN 1 END) as cancelled_appointments,
                COUNT(CASE WHEN a.status = 'NO_SHOW' THEN 1 END) as no_shows,
                AVG(EXTRACT(EPOCH FROM (a.scheduled_at - a.created_at)) / 86400) as avg_booking_days,
                COUNT(DISTINCT p.id) as unique_patients,
                COUNT(pr.id) as total_prescriptions
            FROM doctor d
            LEFT JOIN appointment a ON d.id = a.doctor_id 
                AND EXTRACT(YEAR FROM a.scheduled_at) = ?
                AND EXTRACT(MONTH FROM a.scheduled_at) = ?
            LEFT JOIN patient p ON a.patient_id = p.id
            LEFT JOIN prescription pr ON a.id = pr.appointment_id
            WHERE d.id IN (
                SELECT DISTINCT doctor_id FROM appointment
                WHERE EXTRACT(YEAR FROM scheduled_at) = ?
                AND EXTRACT(MONTH FROM scheduled_at) = ?
            )
            GROUP BY d.id, d.name, d.specialization
            HAVING COUNT(a.id) > 0
            ORDER BY completed_appointments DESC
        """;
        
        return jdbcTemplate.query(sql, this::mapToPerformanceReport, 
            month.getYear(), month.getMonthValue(),
            month.getYear(), month.getMonthValue());
    }
    
    private DoctorPerformanceReport mapToPerformanceReport(ResultSet rs, int rowNum) 
            throws SQLException {
        return new DoctorPerformanceReport(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("specialization"),
            rs.getInt("total_appointments"),
            rs.getInt("completed_appointments"),
            rs.getInt("cancelled_appointments"),
            rs.getInt("no_shows"),
            rs.getDouble("avg_booking_days"),
            rs.getInt("unique_patients"),
            rs.getInt("total_prescriptions")
        );
    }
}
```

**Spring Data JDBC Attempt**:

```java
// Possible but awkward
@Query("""
    SELECT d.id, d.name, d.specialization,
           COUNT(a.id) as total_appointments,
           COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed,
           -- ... 50 more lines of complex SQL in annotation
    FROM doctor d
    LEFT JOIN appointment a ON d.id = a.doctor_id
    -- ... complex joins and aggregations
""")
List<DoctorPerformanceReport> generateMonthlyReport(int year, int month);
```

**Verdict**: JdbcTemplate wins for maintainability and flexibility.

---

#### Scenario 3: Appointment with Prescriptions (Aggregate)

**Best Choice**: 🏆 **Spring Data JDBC**

**Why**:
- Natural aggregate pattern (appointment → prescriptions)
- Transactional consistency needed
- All children loaded together

**Spring Data JDBC Implementation**:

```java
@Data
@Table("appointment")
public class Appointment {
    @Id
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime scheduledAt;
    
    @MappedCollection(idColumn = "appointment_id")
    private Set<Prescription> prescriptions = new HashSet<>();
}

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    // Spring handles aggregate loading/saving automatically
}

@Service
public class AppointmentService {
    @Transactional
    public Appointment createWithPrescriptions(Appointment appointment) {
        // One save() persists appointment + all prescriptions
        return appointmentRepository.save(appointment);
    }
}
```

**JdbcTemplate Implementation** (requires manual coordination):

```java
@Service
public class AppointmentService {
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional
    public Appointment createWithPrescriptions(Appointment appointment) {
        // Manual: Insert appointment
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO appointment (...) VALUES (...)",
                Statement.RETURN_GENERATED_KEYS
            );
            // Set parameters...
            return ps;
        }, keyHolder);
        Long appointmentId = keyHolder.getKey().longValue();
        
        // Manual: Insert each prescription
        for (Prescription p : appointment.getPrescriptions()) {
            jdbcTemplate.update(
                "INSERT INTO prescription (...) VALUES (...)",
                appointmentId, p.getMedicationName(), p.getDosage(), ...
            );
        }
        
        return appointment;
    }
}
```

**Verdict**: Spring Data JDBC wins with **70% less code** and automatic aggregate handling.

---

#### Scenario 4: Batch Import of 10,000 Patients

**Best Choice**: 🏆 **JdbcTemplate**

**Why**:
- Batch operations critical for performance
- Fine-tuned control over batch size
- Custom error handling per batch

**JdbcTemplate Implementation**:

```java
@Repository
public class PatientBatchRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional
    public void batchInsert(List<Patient> patients) {
        String sql = "INSERT INTO patient (name, date_of_birth, blood_type, " +
                     "medical_record_number) VALUES (?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, patients, 500, (ps, patient) -> {
            ps.setString(1, patient.getName());
            ps.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(3, patient.getBloodType());
            ps.setString(4, patient.getMedicalRecordNumber());
        });
    }
}
```

**Performance**: Inserts 10,000 patients in ~2 seconds vs 20+ seconds with individual saves.

**Spring Data JDBC**: 
```java
// Works but slower (no batch optimization)
patientRepository.saveAll(patients);
```

**Verdict**: JdbcTemplate wins with **10x better performance**.

---

#### Scenario 5: Dynamic Search with Optional Filters

**Task**: Search patients with optional filters (name, blood type, age range).

**Best Choice**: 🏆 **JdbcTemplate**

**Why**:
- SQL built dynamically based on provided filters
- Spring Data JDBC requires separate methods for each combination

**JdbcTemplate Implementation**:

```java
@Repository
public class PatientSearchRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public List<Patient> search(PatientSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT * FROM patient WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (criteria.getName() != null) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + criteria.getName() + "%");
        }
        
        if (criteria.getBloodType() != null) {
            sql.append(" AND blood_type = ?");
            params.add(criteria.getBloodType());
        }
        
        if (criteria.getMinAge() != null) {
            sql.append(" AND date_of_birth <= ?");
            params.add(LocalDate.now().minusYears(criteria.getMinAge()));
        }
        
        if (criteria.getMaxAge() != null) {
            sql.append(" AND date_of_birth >= ?");
            params.add(LocalDate.now().minusYears(criteria.getMaxAge()));
        }
        
        sql.append(" ORDER BY name");
        
        return jdbcTemplate.query(sql.toString(), this::mapRowToPatient, params.toArray());
    }
}
```

**Spring Data JDBC** (requires explosion of methods):

```java
// Need separate methods for each combination - impractical!
List<Patient> findByName(String name);
List<Patient> findByBloodType(String bloodType);
List<Patient> findByNameAndBloodType(String name, String bloodType);
List<Patient> findByNameAndBloodTypeAndDateOfBirthBetween(...);
// ... 16+ methods for all combinations!
```

**Verdict**: JdbcTemplate wins for dynamic queries.

---

### 11.3 Hybrid Approach: Best of Both Worlds

**Strategy**: Use both together in the same repository!

```java
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    
    // Spring Data JDBC for simple queries
    Optional<Patient> findByMedicalRecordNumber(String mrn);
    List<Patient> findByBloodType(String bloodType);
    
    // Custom implementation for complex queries
    List<Patient> search(PatientSearchCriteria criteria);
    List<PatientStatistics> getStatistics(LocalDate from, LocalDate to);
}

// Custom implementation
@Component
class PatientRepositoryImpl {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Patient> search(PatientSearchCriteria criteria) {
        // Use JdbcTemplate for dynamic queries
        StringBuilder sql = new StringBuilder("SELECT * FROM patient WHERE 1=1");
        // ... dynamic SQL building
        return jdbcTemplate.query(sql.toString(), this::mapRowToPatient);
    }
    
    public List<PatientStatistics> getStatistics(LocalDate from, LocalDate to) {
        // Use JdbcTemplate for complex aggregations
        String sql = """
            SELECT blood_type, COUNT(*) as count, 
                   AVG(EXTRACT(YEAR FROM AGE(date_of_birth))) as avg_age
            FROM patient
            WHERE created_at BETWEEN ? AND ?
            GROUP BY blood_type
        """;
        return jdbcTemplate.query(sql, this::mapToStatistics, from, to);
    }
}
```

**Result**: Simple queries use Spring Data JDBC magic, complex queries use JdbcTemplate power.

---

### 11.4 Decision Matrix

Use this matrix to choose the right tool:

```
┌─────────────────────────────────────────────────────────────────┐
│                    DECISION TREE                                 │
└─────────────────────────────────────────────────────────────────┘

Start: Do you need to persist data?
│
├─ YES → Continue
│
└─ NO → Use plain POJOs

Is it a simple CRUD operation?
│
├─ YES → Spring Data JDBC ✓
│   └─ Examples: findById, save, delete, findByXxx
│
└─ NO → Continue

Does it involve aggregates (parent with children)?
│
├─ YES → Spring Data JDBC ✓
│   └─ Example: Appointment with Prescriptions
│
└─ NO → Continue

Is the query complex (multiple joins, subqueries)?
│
├─ YES → JdbcTemplate ✓
│   └─ Example: Multi-table reports with aggregations
│
└─ NO → Continue

Do you need dynamic query building?
│
├─ YES → JdbcTemplate ✓
│   └─ Example: Search with optional filters
│
└─ NO → Continue

Is it a batch operation (1000+ records)?
│
├─ YES → JdbcTemplate ✓
│   └─ Example: Bulk import/update
│
└─ NO → Continue

Do you need database-specific features?
│
├─ YES → JdbcTemplate ✓
│   └─ Example: PostgreSQL JSON, arrays, stored procedures
│
└─ NO → Spring Data JDBC ✓
```

---

### 11.5 Performance Comparison

**Benchmark**: Insert 1000 patients, query all, update 100, delete 10

| Operation | JdbcTemplate | Spring Data JDBC | Winner |
|-----------|--------------|------------------|--------|
| **Insert 1000 (batch)** | 150ms | 1200ms | 🏆 JdbcTemplate (8x faster) |
| **Insert 1000 (individual)** | 1180ms | 1220ms | 🤝 Tie |
| **Find by ID** | 2ms | 2ms | 🤝 Tie |
| **Find all** | 45ms | 48ms | 🤝 Tie |
| **Complex join** | 12ms | 15ms | 🏆 JdbcTemplate (slight edge) |
| **Update 100** | 80ms | 85ms | 🤝 Tie |
| **Delete 10** | 5ms | 5ms | 🤝 Tie |

**Key Takeaway**: Performance is similar except for batch operations where JdbcTemplate dominates.

---

### 11.6 Code Maintenance Comparison

**Scenario**: Add a new field `insuranceProvider` to Patient entity.

**JdbcTemplate Changes Required**:
1. Update `Patient` class - add field + getter/setter
2. Update `save()` method - add to INSERT
3. Update `update()` method - add to UPDATE
4. Update `mapRowToPatient()` - add mapping
5. Update database schema

**Lines changed**: ~15 lines across 4 methods

**Spring Data JDBC Changes Required**:
1. Update `Patient` class - add field
2. Update database schema

**Lines changed**: ~2 lines

**Verdict**: Spring Data JDBC wins with **87% less maintenance**.

---

### 11.7 Testing Comparison

**JdbcTemplate Test**:

```java
@SpringBootTest
@Transactional
class PatientRepositoryTest {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Test
    void testSave() {
        Patient patient = new Patient("John", LocalDate.of(1980, 1, 1), "O+", "MRN-001");
        Patient saved = patientRepository.save(patient);
        
        assertThat(saved.getId()).isNotNull();
    }
}
```

**Spring Data JDBC Test**: (identical!)

```java
@SpringBootTest
@Transactional
class PatientRepositoryTest {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Test
    void testSave() {
        Patient patient = new Patient("John", LocalDate.of(1980, 1, 1), "O+", "MRN-001");
        Patient saved = patientRepository.save(patient);
        
        assertThat(saved.getId()).isNotNull();
    }
}
```

**Verdict**: 🤝 Tie - testing approach is identical.

---

### 11.8 Checkpoint Question 11

**Scenario**: You're building a medical appointment system with these requirements:

1. Standard patient CRUD (create, read, update, delete)
2. Complex monthly billing report (joins patient, appointment, prescription, doctor)
3. Bulk import of 5000 patients from legacy system
4. Search patients with optional filters (name, blood type, city, age range)
5. Appointment aggregate (appointment with prescriptions loaded together)

**Question**: For each requirement, choose JdbcTemplate or Spring Data JDBC. Justify your choice.

**Answer**:

**1. Standard patient CRUD**
- **Choice**: 🏆 Spring Data JDBC
- **Justification**: 
  - Simple CRUD operations
  - No complex business logic
  - Query methods handle all needs
  - 90% less code than JdbcTemplate
  ```java
  interface PatientRepository extends CrudRepository<Patient, Long> {
      Optional<Patient> findByMedicalRecordNumber(String mrn);
  }
  ```

**2. Complex monthly billing report**
- **Choice**: 🏆 JdbcTemplate
- **Justification**:
  - Multiple joins across 4+ tables
  - Aggregate functions (SUM, COUNT, AVG)
  - Complex GROUP BY with calculations
  - Custom result mapping to BillingReport DTO
  ```java
  public List<BillingReport> generateMonthlyBilling(YearMonth month) {
      String sql = """
          SELECT p.name, d.name, 
                 COUNT(a.id) as appointments,
                 SUM(...) as total_amount
          FROM patient p
          JOIN appointment a ON p.id = a.patient_id
          JOIN doctor d ON a.doctor_id = d.id
          JOIN prescription pr ON a.id = pr.appointment_id
          WHERE ...
          GROUP BY p.id, d.id
      """;
      return jdbcTemplate.query(sql, this::mapToBillingReport, ...);
  }
  ```

**3. Bulk import of 5000 patients**
- **Choice**: 🏆 JdbcTemplate
- **Justification**:
  - Batch operations critical for performance
  - JdbcTemplate.batchUpdate() is 10x faster
  - Fine control over batch size (e.g., 500 per batch)
  - Better error handling per batch
  ```java
  public void batchInsert(List<Patient> patients) {
      jdbcTemplate.batchUpdate(sql, patients, 500, (ps, patient) -> {
          ps.setString(1, patient.getName());
          // ... set parameters
      });
  }
  ```

**4. Search with optional filters**
- **Choice**: 🏆 JdbcTemplate
- **Justification**:
  - Dynamic SQL required (filters applied conditionally)
  - Spring Data JDBC would need 16+ methods for all combinations
  - SQL built at runtime based on provided criteria
  ```java
  public List<Patient> search(SearchCriteria criteria) {
      StringBuilder sql = new StringBuilder("SELECT * FROM patient WHERE 1=1");
      List<Object> params = new ArrayList<>();
      
      if (criteria.getName() != null) {
          sql.append(" AND name LIKE ?");
          params.add("%" + criteria.getName() + "%");
      }
      // ... add other filters dynamically
      
      return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
  }
  ```

**5. Appointment aggregate**
- **Choice**: 🏆 Spring Data JDBC
- **Justification**:
  - Natural aggregate pattern (parent → children)
  - Transactional consistency needed
  - All prescriptions loaded with appointment automatically
  - One save() persists everything
  ```java
  @Data
  @Table("appointment")
  public class Appointment {
      @Id
      private Long id;
      
      @MappedCollection(idColumn = "appointment_id")
      private Set<Prescription> prescriptions = new HashSet<>();
  }
  
  // One line to save entire aggregate
  appointmentRepository.save(appointment);
  ```

**Summary**:
- Requirements 1, 5 → **Spring Data JDBC** (simple CRUD, aggregates)
- Requirements 2, 3, 4 → **JdbcTemplate** (complex queries, batch ops, dynamic SQL)
- **Hybrid approach**: Use both in the same application!

---

### 11.9 Migration Path

**Moving from JdbcTemplate to Spring Data JDBC**:

**Step 1**: Identify simple repositories (CRUD only)
```java
// Before: JdbcTemplate (100 lines)
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbc;
    public Patient save(...) { /* 15 lines */ }
    public Optional<Patient> findById(...) { /* 10 lines */ }
    // ... 8 more methods
}

// After: Spring Data JDBC (5 lines)
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    Optional<Patient> findByMedicalRecordNumber(String mrn);
}
```

**Step 2**: Keep complex queries in JdbcTemplate
```java
@Component
public class PatientComplexQueries {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<PatientStatistics> getStatistics() {
        // Keep complex query as-is
    }
}
```

**Step 3**: Refactor aggregates to Spring Data JDBC
```java
// Convert appointment + prescriptions to aggregate
@Table("appointment")
public class Appointment {
    @MappedCollection(idColumn = "appointment_id")
    private Set<Prescription> prescriptions;
}
```

**Step 4**: Test incrementally
- Migrate one entity at a time
- Keep both implementations running in parallel
- Compare results in tests
- Switch over when confident

---

### 11.10 Summary: Choosing the Right Tool

**Use Spring Data JDBC when**:
- ✅ Standard CRUD operations
- ✅ Aggregate patterns (parent with children)
- ✅ Query method names express intent clearly
- ✅ You want less code to maintain
- ✅ Team prefers declarative style

**Use JdbcTemplate when**:
- ✅ Complex queries (multiple joins, subqueries)
- ✅ Batch operations (1000+ records)
- ✅ Dynamic query building
- ✅ Database-specific features (stored procedures, JSON, arrays)
- ✅ Custom result mapping to DTOs
- ✅ You need maximum control

**Use Both Together when**:
- ✅ App has mix of simple and complex queries (most real-world apps!)
- ✅ Want Spring Data JDBC for CRUD, JdbcTemplate for reports
- ✅ Need best tool for each job

**Golden Rule**: Start with Spring Data JDBC, drop to JdbcTemplate when needed.

---

**Next Step**: In Step 12, we'll cover production-ready practices for deployment and monitoring!

---

## 12. PRODUCTION READINESS AND BEST PRACTICES

> **Goal**: Prepare your Spring Boot JDBC application for production with proper configuration, monitoring, and error handling.

---

### 12.1 Database Connection Pool Tuning

**HikariCP Configuration** (Spring Boot default):

```yaml
spring:
  datasource:
    hikari:
      # Connection pool sizing
      maximum-pool-size: 20           # Max concurrent connections
      minimum-idle: 5                 # Keep 5 connections ready
      
      # Timeout settings
      connection-timeout: 30000       # 30s to get connection from pool
      idle-timeout: 600000            # 10 min before idle connection removed
      max-lifetime: 1800000           # 30 min max connection lifetime
      
      # Validation
      validation-timeout: 5000        # 5s to validate connection
      connection-test-query: SELECT 1 # Health check query
      
      # Leak detection
      leak-detection-threshold: 60000 # 60s - log if connection not returned
      
      # Pool name for monitoring
      pool-name: MedicalAppHikariPool
      
      # Advanced
      auto-commit: true
      read-only: false
```

**How to Calculate Pool Size**:

Formula: `connections = ((core_count * 2) + effective_spindle_count)`

Example:
- 4-core CPU + 2 disk drives = (4 × 2) + 2 = **10 connections**
- Add buffer: 10 × 1.5 = **15 connections**

**Production Settings**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}  # Override via env var
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      leak-detection-threshold: 60000
```

---

### 12.2 Transaction Management

**@Transactional Best Practices**:

```java
@Service
public class AppointmentService {
    
    // ✅ GOOD: Transactional at service layer
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // Multiple DB operations in single transaction
        Appointment saved = appointmentRepository.save(appointment);
        prescriptionRepository.saveAll(appointment.getPrescriptions());
        auditService.logCreation(saved);
        return saved;
    }
    
    // ✅ GOOD: Read-only for queries (optimization)
    @Transactional(readOnly = true)
    public List<Appointment> findUpcoming() {
        return appointmentRepository.findByStatus("SCHEDULED");
    }
    
    // ✅ GOOD: Custom timeout for long-running operations
    @Transactional(timeout = 60)
    public void generateMonthlyReports() {
        // Complex report generation
    }
    
    // ❌ BAD: Never use @Transactional on repository methods
    // Spring Data JDBC already manages transactions
}
```

**Transaction Isolation Levels**:

```java
@Service
public class BillingService {
    
    // SERIALIZABLE: Strictest - prevents phantom reads
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processPayment(Long appointmentId, BigDecimal amount) {
        // Critical financial operation
    }
    
    // READ_COMMITTED: Default - prevents dirty reads
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateAppointment(Appointment appointment) {
        // Standard update
    }
    
    // READ_UNCOMMITTED: Fastest - allows dirty reads (rarely used)
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<AppointmentSummary> getApproximateCounts() {
        // Report where accuracy isn't critical
    }
}
```

---

### 12.3 Error Handling and Retry Logic

**Custom Exception Hierarchy**:

```java
package com.medical.exception;

public class MedicalSystemException extends RuntimeException {
    public MedicalSystemException(String message) {
        super(message);
    }
    
    public MedicalSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PatientNotFoundException extends MedicalSystemException {
    public PatientNotFoundException(Long id) {
        super("Patient not found with ID: " + id);
    }
}

public class DuplicateMedicalRecordException extends MedicalSystemException {
    public DuplicateMedicalRecordException(String mrn) {
        super("Patient already exists with MRN: " + mrn);
    }
}

public class AppointmentConflictException extends MedicalSystemException {
    public AppointmentConflictException(String message) {
        super(message);
    }
}
```

**Global Exception Handler**:

```java
package com.medical.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(
            PatientNotFoundException ex, WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(
            DuplicateKeyException ex, WebRequest request) {
        
        String message = extractUserFriendlyMessage(ex);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(
            DataAccessException ex, WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Database Error",
            "A database error occurred. Please try again later.",
            request.getDescription(false).replace("uri=", "")
        );
        
        // Log full exception for debugging
        log.error("Database error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private String extractUserFriendlyMessage(DuplicateKeyException ex) {
        String message = ex.getMessage().toLowerCase();
        
        if (message.contains("medical_record_number")) {
            return "A patient with this Medical Record Number already exists";
        } else if (message.contains("license_number")) {
            return "A doctor with this License Number already exists";
        } else if (message.contains("email")) {
            return "This email address is already registered";
        }
        
        return "This record already exists in the system";
    }
}
```

**Retry Logic with Spring Retry**:

Add dependency:
```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
```

Enable retry:
```java
@Configuration
@EnableRetry
public class RetryConfig {
}
```

Use in service:
```java
@Service
public class PatientService {
    
    @Retryable(
        value = {DataAccessResourceFailureException.class, TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }
    
    @Recover
    public Patient recover(DataAccessException ex, Patient patient) {
        log.error("Failed to create patient after retries: {}", patient.getName(), ex);
        throw new MedicalSystemException("Unable to create patient. Please try again later.", ex);
    }
}
```

**Retry behavior**:
- Attempt 1: Immediate
- Attempt 2: After 1 second
- Attempt 3: After 2 seconds (1s × 2 multiplier)
- If all fail: Call `@Recover` method

---

### 12.4 Monitoring and Health Checks

**Actuator Endpoints for Production**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true  # Kubernetes liveness/readiness probes
  
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
  
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:production}

info:
  app:
    name: Medical Appointment System
    version: @project.version@
    description: Hospital appointment management
```

**Custom Health Indicator**:

```java
package com.medical.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM patient", Long.class
            );
            
            // Check if database is responding within threshold
            long startTime = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (responseTime > 1000) {
                return Health.down()
                    .withDetail("error", "Database response time too high")
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
            }
            
            return Health.up()
                .withDetail("patientCount", count)
                .withDetail("responseTime", responseTime + "ms")
                .build();
                
        } catch (Exception ex) {
            return Health.down()
                .withDetail("error", ex.getMessage())
                .build();
        }
    }
}
```

**Health check response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "database": {
      "status": "UP",
      "details": {
        "patientCount": 1247,
        "responseTime": "15ms"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    }
  }
}
```

---

### 12.5 Logging Strategy

**Logback Configuration** (`src/main/resources/logback-spring.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <!-- Console Appender for Development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender for Production -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/medical-app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/medical-app-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy 
                class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Async Appender for Performance -->
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="FILE"/>
    </appender>
    
    <!-- Application Loggers -->
    <logger name="com.medical" level="INFO"/>
    <logger name="com.medical.repository" level="DEBUG"/>
    
    <!-- Spring Framework -->
    <logger name="org.springframework" level="INFO"/>
    <logger name="org.springframework.jdbc.core" level="DEBUG"/>
    <logger name="org.springframework.data" level="DEBUG"/>
    
    <!-- HikariCP Pool -->
    <logger name="com.zaxxer.hikari" level="INFO"/>
    
    <!-- SQL Logging -->
    <logger name="org.springframework.jdbc.core.JdbcTemplate" level="DEBUG"/>
    <logger name="org.springframework.jdbc.core.StatementCreatorUtils" level="TRACE"/>
    
    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </root>
    
</configuration>
```

**Structured Logging in Service**:

```java
@Service
@Slf4j
public class PatientService {
    
    private final PatientRepository patientRepository;
    
    public Patient createPatient(Patient patient) {
        log.info("Creating patient: mrn={}, name={}", 
            patient.getMedicalRecordNumber(), patient.getName());
        
        try {
            Patient saved = patientRepository.save(patient);
            log.info("Patient created successfully: id={}, mrn={}", 
                saved.getId(), saved.getMedicalRecordNumber());
            return saved;
            
        } catch (DuplicateKeyException ex) {
            log.warn("Duplicate patient creation attempted: mrn={}", 
                patient.getMedicalRecordNumber(), ex);
            throw new DuplicateMedicalRecordException(patient.getMedicalRecordNumber());
            
        } catch (Exception ex) {
            log.error("Failed to create patient: mrn={}, error={}", 
                patient.getMedicalRecordNumber(), ex.getMessage(), ex);
            throw new MedicalSystemException("Failed to create patient", ex);
        }
    }
}
```

---

### 12.6 Security Best Practices

**1. SQL Injection Prevention**:

```java
// ✅ SAFE: Using parameterized queries
public List<Patient> findByName(String name) {
    return jdbcTemplate.query(
        "SELECT * FROM patient WHERE name = ?",  // Parameter placeholder
        this::mapRow,
        name  // Parameter bound safely
    );
}

// ❌ UNSAFE: String concatenation (SQL injection risk!)
public List<Patient> findByNameUnsafe(String name) {
    return jdbcTemplate.query(
        "SELECT * FROM patient WHERE name = '" + name + "'",  // VULNERABLE!
        this::mapRow
    );
}
// If name = "'; DROP TABLE patient; --", you delete the table!
```

**2. Sensitive Data Protection**:

```java
@Data
@Table("patient")
public class Patient {
    @Id
    private Long id;
    private String name;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Never return in API
    private String socialSecurityNumber;
    
    // Override toString to exclude sensitive fields
    @Override
    public String toString() {
        return "Patient{id=" + id + ", name=" + name + ", mrn=" + medicalRecordNumber + "}";
    }
}
```

**3. Connection String Security**:

```yaml
# ❌ NEVER hardcode credentials in application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medical_system
    username: admin
    password: SuperSecret123!  # INSECURE!

# ✅ Use environment variables
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
```

**4. Encrypted Passwords in Database**:

```java
@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(String username, String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        // Store hashedPassword in database, NEVER rawPassword
        return userRepository.save(new User(username, hashedPassword));
    }
}
```

---

### 12.7 Performance Optimization

**1. Connection Pool Monitoring**:

```java
@Component
@Slf4j
public class HikariPoolMonitor {
    
    @Autowired
    private HikariDataSource dataSource;
    
    @Scheduled(fixedRate = 60000)  // Every minute
    public void logPoolStatistics() {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        log.info("HikariCP Pool Stats - Active: {}, Idle: {}, Waiting: {}, Total: {}",
            poolBean.getActiveConnections(),
            poolBean.getIdleConnections(),
            poolBean.getThreadsAwaitingConnection(),
            poolBean.getTotalConnections()
        );
    }
}
```

**2. Query Performance Monitoring**:

```java
@Aspect
@Component
@Slf4j
public class QueryPerformanceAspect {
    
    @Around("execution(* com.medical.repository..*(..))")
    public Object logQueryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {  // Slow query threshold
                log.warn("Slow query detected: method={}, time={}ms",
                    joinPoint.getSignature().getName(), executionTime);
            }
            
            return result;
        } catch (Exception ex) {
            log.error("Query failed: method={}, error={}",
                joinPoint.getSignature().getName(), ex.getMessage());
            throw ex;
        }
    }
}
```

**3. Result Set Limiting**:

```java
// ✅ GOOD: Paginated results
public Page<Patient> getPatients(Pageable pageable) {
    return patientRepository.findAll(pageable);
}

// ❌ BAD: Loading all records (memory exhaustion risk)
public List<Patient> getAllPatients() {
    return patientRepository.findAll();  // Could be 1 million records!
}
```

---

### 12.8 Checkpoint Question 12

**Scenario**: Your medical appointment system is going to production. The operations team asks you to:

1. Configure connection pooling for 8-core server with SSD
2. Add retry logic for transient database failures
3. Create a custom health check that verifies:
   - Database connectivity
   - At least one doctor exists
   - Response time < 500ms
4. Set up logging to capture slow queries (>1 second)
5. Secure database credentials

**Answer**:

**1. Connection Pool Configuration**:
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      # Calculation: (8 cores * 2) + 1 SSD = 17, rounded to 20
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      pool-name: MedicalProdHikariPool
```

**2. Retry Logic**:
```java
@Configuration
@EnableRetry
public class RetryConfig {
}

@Service
public class AppointmentService {
    
    @Retryable(
        value = {TransientDataAccessException.class, 
                 DataAccessResourceFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    
    @Recover
    public Appointment recover(Exception ex, Appointment appointment) {
        log.error("Failed to create appointment after retries", ex);
        throw new MedicalSystemException(
            "System temporarily unavailable. Please try again.", ex
        );
    }
}
```

**3. Custom Health Check**:
```java
@Component
public class MedicalSystemHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Check database connectivity
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // Verify at least one doctor exists
            Long doctorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM doctor", Long.class
            );
            
            if (doctorCount == null || doctorCount == 0) {
                return Health.down()
                    .withDetail("error", "No doctors found in system")
                    .build();
            }
            
            // Check response time
            long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > 500) {
                return Health.down()
                    .withDetail("error", "Database response time exceeded threshold")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("threshold", "500ms")
                    .build();
            }
            
            return Health.up()
                .withDetail("doctorCount", doctorCount)
                .withDetail("responseTime", responseTime + "ms")
                .withDetail("database", "PostgreSQL")
                .build();
                
        } catch (Exception ex) {
            return Health.down()
                .withDetail("error", ex.getMessage())
                .withException(ex)
                .build();
        }
    }
}
```

**4. Slow Query Logging**:
```java
@Aspect
@Component
@Slf4j
public class SlowQueryLogger {
    
    @Around("execution(* com.medical.repository..*(..))")
    public Object logSlowQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {
                log.warn("SLOW QUERY DETECTED: method={}, args={}, time={}ms",
                    methodName, Arrays.toString(args), executionTime);
            } else {
                log.debug("Query executed: method={}, time={}ms", 
                    methodName, executionTime);
            }
            
            return result;
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Query failed: method={}, time={}ms, error={}", 
                methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }
}
```

Also configure SQL logging in `application-prod.yml`:
```yaml
logging:
  level:
    org.springframework.jdbc.core.JdbcTemplate: DEBUG
    org.springframework.jdbc.core.StatementCreatorUtils: TRACE
```

**5. Secure Credentials**:
```yaml
# application-prod.yml - Use environment variables
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    hikari:
      data-source-properties:
        ssl: true
        sslmode: require
```

Set environment variables in deployment:
```bash
export DATABASE_URL="jdbc:postgresql://prod-db.example.com:5432/medical_system?ssl=true"
export DATABASE_USER="medical_app_user"
export DATABASE_PASSWORD="$(cat /run/secrets/db_password)"  # From secrets manager
```

Or use Spring Cloud Config / Kubernetes Secrets:
```yaml
# ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: medical-app-config
data:
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/medical_system"

---
# Secret
apiVersion: v1
kind: Secret
metadata:
  name: medical-app-secrets
type: Opaque
stringData:
  DATABASE_USER: "medical_app_user"
  DATABASE_PASSWORD: "SuperSecurePassword!"
```

---

### 12.9 Deployment Checklist

**Pre-Production Verification**:

- [ ] **Database**
  - [ ] Connection pool configured appropriately
  - [ ] Credentials stored securely (not in code)
  - [ ] SSL/TLS enabled for database connections
  - [ ] Indexes created on frequently queried columns
  - [ ] Foreign key constraints in place

- [ ] **Application**
  - [ ] Health check endpoint responding correctly
  - [ ] Logging configured (file + rotation)
  - [ ] Exception handling covers all scenarios
  - [ ] Retry logic for transient failures
  - [ ] Timeouts configured for all operations

- [ ] **Monitoring**
  - [ ] Actuator endpoints secured
  - [ ] Metrics exported to monitoring system
  - [ ] Alerts configured for errors/slow queries
  - [ ] Connection pool statistics tracked
  - [ ] Disk space monitoring enabled

- [ ] **Security**
  - [ ] All SQL queries use parameterized statements
  - [ ] Sensitive data never logged
  - [ ] API endpoints have authentication
  - [ ] CORS configured restrictively
  - [ ] Input validation on all endpoints

- [ ] **Performance**
  - [ ] Query performance tested under load
  - [ ] Connection pool sized appropriately
  - [ ] Slow query logging enabled
  - [ ] Caching strategy implemented where needed
  - [ ] Load testing completed

---

### 12.10 Summary: Production Readiness

**Configuration**:
- ✅ Connection pool tuned for your infrastructure
- ✅ Timeouts configured at all levels
- ✅ Environment-specific settings (dev/prod)

**Monitoring**:
- ✅ Health checks for database connectivity
- ✅ Metrics exposed (Prometheus/Grafana)
- ✅ Slow query logging enabled
- ✅ Connection pool statistics tracked

**Error Handling**:
- ✅ Custom exception hierarchy
- ✅ Global exception handler
- ✅ Retry logic for transient failures
- ✅ Graceful degradation

**Security**:
- ✅ Credentials externalized
- ✅ SQL injection prevention
- ✅ Sensitive data protected
- ✅ SSL/TLS for database connections

**Performance**:
- ✅ Queries optimized and indexed
- ✅ Result sets paginated
- ✅ Connection pooling tuned
- ✅ Load tested

---

**Next Step**: In Step 13, we'll briefly explore JPA/Hibernate as an alternative to JDBC!

---

## 13. JPA/HIBERNATE: A BRIEF COMPARISON

> **Goal**: Understand Spring Data JPA/Hibernate as an alternative to JDBC, when to use each, and the trade-offs involved.

---

### 13.1 What Is JPA?

**JPA (Jakarta Persistence API)**: A specification for object-relational mapping (ORM) in Java.

**Hibernate**: The most popular JPA implementation (others: EclipseLink, OpenJPA).

**Key Concept**: JPA maps Java objects to database tables automatically, eliminating most SQL.

```java
// JDBC: You write SQL
jdbcTemplate.query("SELECT * FROM patient WHERE id = ?", ...);

// JPA: Framework generates SQL
Patient patient = entityManager.find(Patient.class, id);
```

---

### 13.2 JPA Entity Example

**Patient Entity with JPA Annotations**:

```java
package com.medical.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "blood_type", length = 5)
    private String bloodType;
    
    @Column(name = "medical_record_number", unique = true, nullable = false, length = 20)
    private String medicalRecordNumber;
    
    @Column(length = 100)
    private String email;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Key JPA Annotations**:
- `@Entity` - Marks class as JPA entity
- `@Table` - Maps to database table
- `@Id` - Primary key
- `@GeneratedValue` - Auto-increment strategy
- `@Column` - Column mapping with constraints
- `@PrePersist` - Lifecycle callback before insert
- `@PreUpdate` - Lifecycle callback before update

---

### 13.3 JPA Repository

**Spring Data JPA Repository** (very similar to Spring Data JDBC):

```java
package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    // Method name queries (same as Spring Data JDBC)
    Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);
    
    List<Patient> findByBloodType(String bloodType);
    
    List<Patient> findByNameContainingIgnoreCase(String name);
    
    boolean existsByMedicalRecordNumber(String medicalRecordNumber);
    
    // JPQL (Java Persistence Query Language) - object-oriented queries
    @Query("SELECT p FROM Patient p WHERE YEAR(p.dateOfBirth) = :year")
    List<Patient> findByBirthYear(@Param("year") int year);
    
    // Native SQL (when you need it)
    @Query(value = "SELECT * FROM patient WHERE blood_type = ?1", nativeQuery = true)
    List<Patient> findByBloodTypeNative(String bloodType);
}
```

**Differences from Spring Data JDBC**:
- Extends `JpaRepository` instead of `CrudRepository`
- Uses JPQL for queries (object-oriented) instead of SQL
- Can still use native SQL with `nativeQuery = true`

---

### 13.4 One-to-Many Relationships in JPA

**Appointment with Prescriptions** (JPA way):

```java
@Entity
@Table(name = "appointment")
@Data
@NoArgsConstructor
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;  // Full Patient object, not just ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;  // Full Doctor object, not just ID
    
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    
    @Column(length = 20, nullable = false)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();
    
    // Helper method
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        prescription.setAppointment(this);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

@Entity
@Table(name = "prescription")
@Data
@NoArgsConstructor
public class Prescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;  // Back-reference to parent
    
    @Column(name = "medication_name", nullable = false, length = 100)
    private String medicationName;
    
    @Column(nullable = false, length = 50)
    private String dosage;
    
    @Column(nullable = false, length = 50)
    private String frequency;
    
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
}
```

**Key JPA Relationship Annotations**:
- `@ManyToOne` - Many appointments to one patient/doctor
- `@OneToMany` - One appointment to many prescriptions
- `@JoinColumn` - Foreign key column
- `FetchType.LAZY` - Load related entities only when accessed
- `FetchType.EAGER` - Load related entities immediately
- `cascade = CascadeType.ALL` - Operations cascade to children
- `orphanRemoval = true` - Delete prescriptions when removed from appointment

---

### 13.5 Lazy Loading vs Eager Loading

**The N+1 Query Problem**:

```java
// Fetch all appointments (1 query)
List<Appointment> appointments = appointmentRepository.findAll();

// Access patient for each appointment (N queries if lazy loaded)
for (Appointment apt : appointments) {
    System.out.println(apt.getPatient().getName());  // Triggers query per appointment!
}

// Total: 1 + N queries = N+1 problem
```

**Solutions**:

**1. Fetch Join (JPQL)**:
```java
@Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor")
List<Appointment> findAllWithPatientAndDoctor();

// Single query with JOIN - no N+1 problem
```

**2. Entity Graph**:
```java
@EntityGraph(attributePaths = {"patient", "doctor"})
List<Appointment> findAll();
```

**3. Eager Fetching** (use cautiously):
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "patient_id")
private Patient patient;  // Always loaded
```

**Spring Data JDBC**: No lazy loading - everything loaded eagerly (predictable but less flexible).

---

### 13.6 JPA Features Not in JDBC

**1. Dirty Checking**:

```java
@Service
public class PatientService {
    
    @Transactional
    public void updatePatientEmail(Long patientId, String newEmail) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        patient.setEmail(newEmail);  // Change tracked automatically
        
        // No need to call save()! JPA detects change and updates at commit
    }
}
```

**JDBC equivalent** (requires explicit save):
```java
Patient patient = patientRepository.findById(patientId).orElseThrow();
patient.setEmail(newEmail);
patientRepository.save(patient);  // Must explicitly save!
```

---

**2. First-Level Cache (Session Cache)**:

```java
@Transactional
public void demonstrateCache() {
    Patient p1 = entityManager.find(Patient.class, 1L);  // Query 1: SELECT from DB
    Patient p2 = entityManager.find(Patient.class, 1L);  // No query - from cache!
    
    assertThat(p1 == p2).isTrue();  // Same object instance
}
```

**JDBC**: No cache - every query hits database.

---

**3. Second-Level Cache** (across sessions):

```java
@Entity
@Table(name = "patient")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Patient {
    // ...
}
```

Requires cache provider (Ehcache, Redis, etc.):
```yaml
spring:
  jpa:
    properties:
      hibernate.cache.use_second_level_cache: true
      hibernate.cache.region.factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

---

**4. Inheritance Mapping**:

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Person {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}

@Entity
public class Patient extends Person {
    private String medicalRecordNumber;
}

@Entity
public class Doctor extends Person {
    private String licenseNumber;
}
```

**JDBC**: Must handle inheritance manually with separate tables or discriminator columns.

---

### 13.7 Comprehensive Comparison: JDBC vs JPA

| Feature | Raw JDBC | JdbcTemplate | Spring Data JDBC | Spring Data JPA |
|---------|----------|--------------|------------------|-----------------|
| **Code Volume** | Very High | Medium | Low | Very Low |
| **SQL Control** | Full | Full | Full (@Query) | Limited (JPQL) |
| **Learning Curve** | High | Medium | Low | High |
| **Object Mapping** | Manual | Manual | Automatic | Automatic |
| **Relationships** | Manual joins | Manual joins | Aggregates | Full ORM |
| **Lazy Loading** | N/A | N/A | No | Yes |
| **Dirty Checking** | No | No | No | Yes |
| **Caching** | No | No | No | Yes (1st & 2nd level) |
| **Query Language** | SQL | SQL | SQL | JPQL/HQL |
| **Performance** | Optimal | Optimal | Optimal | Good (if tuned) |
| **Complexity** | Low | Low | Low | High |
| **N+1 Problem** | N/A | N/A | N/A | Yes (must handle) |
| **Batch Ops** | Excellent | Excellent | Limited | Good |
| **Dynamic Queries** | Easy | Easy | Hard | Medium |
| **Transaction Mgmt** | Manual | Spring | Spring | Spring |
| **Portability** | DB-specific | DB-specific | DB-agnostic | DB-agnostic |

---

### 13.8 When to Use Each Approach

**Use Raw JDBC when**:
- ❌ Almost never in modern applications
- ✅ Extremely performance-critical code (rare)
- ✅ Learning purposes only

**Use JdbcTemplate when**:
- ✅ Complex queries with multiple joins
- ✅ Reporting and analytics
- ✅ Batch operations (bulk insert/update)
- ✅ Stored procedures
- ✅ Dynamic query building
- ✅ Database-specific features (PostgreSQL JSON, arrays)
- ✅ You want full SQL control

**Use Spring Data JDBC when**:
- ✅ Standard CRUD operations
- ✅ Simple domain model
- ✅ Predictable performance (no lazy loading surprises)
- ✅ DDD aggregates
- ✅ You want simplicity over features
- ✅ Small to medium projects

**Use Spring Data JPA when**:
- ✅ Complex domain models with many relationships
- ✅ Need lazy loading for performance
- ✅ Want dirty checking (automatic updates)
- ✅ Caching required (read-heavy applications)
- ✅ Database portability important
- ✅ Large enterprise applications
- ✅ Team experienced with JPA/Hibernate

---

### 13.9 Real-World Scenario: Which to Choose?

**Scenario 1: E-commerce Product Catalog**
- Thousands of products
- Each product has categories, reviews, ratings, images
- Read-heavy (90% reads, 10% writes)
- Need caching

**Choice**: 🏆 **Spring Data JPA**
- Lazy loading prevents loading all reviews for every product
- Second-level cache speeds up repeated product views
- Complex relationships handled elegantly

---

**Scenario 2: Financial Transaction Processing**
- High-volume transactions (10,000+ per second)
- Simple data model (transaction, account)
- Write-heavy
- Predictable performance critical

**Choice**: 🏆 **Spring Data JDBC + JdbcTemplate**
- No lazy loading surprises
- No caching complexity
- Batch operations for bulk processing
- Predictable, consistent performance

---

**Scenario 3: Medical Appointment System (Our Example)**
- Moderate complexity (patients, doctors, appointments, prescriptions)
- Mix of simple CRUD and complex reports
- Moderate traffic
- Aggregates (appointment → prescriptions)

**Choice**: 🏆 **Hybrid: Spring Data JDBC + JdbcTemplate**
- Spring Data JDBC for CRUD and aggregates
- JdbcTemplate for monthly billing reports
- No ORM complexity
- Predictable performance

---

### 13.10 JPA Configuration Example

**Add JPA Dependencies** (pom.xml):

```xml
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Optional: Show SQL in logs -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
    </dependency>
</dependencies>
```

**Configure JPA** (application.yml):

```yaml
spring:
  jpa:
    # Hibernate DDL mode
    hibernate:
      ddl-auto: validate  # validate, update, create, create-drop
    
    # Show SQL in console
    show-sql: true
    
    # Format SQL for readability
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        
        # Dialect
        dialect: org.hibernate.dialect.PostgreSQLDialect
        
        # JDBC batch size
        jdbc.batch_size: 50
        order_inserts: true
        order_updates: true
        
        # Statistics
        generate_statistics: true
        
        # Query plan cache
        query.plan_cache_max_size: 2048
        
    # Naming strategy
    naming:
      physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
      implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
```

**Hibernate DDL Modes**:
- `validate` - Validate schema, don't make changes (PRODUCTION)
- `update` - Update schema if needed (DEVELOPMENT)
- `create` - Drop and recreate schema on startup (TESTING)
- `create-drop` - Drop schema on shutdown (TESTING)
- `none` - Do nothing (use with Flyway/Liquibase)

---

### 13.11 Migration Example: JDBC to JPA

**Before: Spring Data JDBC**:

```java
@Data
@Table("patient")
public class Patient {
    @Id
    private Long id;
    private String name;
    private String medicalRecordNumber;
}

public interface PatientRepository extends CrudRepository<Patient, Long> {
    Optional<Patient> findByMedicalRecordNumber(String mrn);
}
```

**After: Spring Data JPA**:

```java
@Entity
@Table(name = "patient")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "medical_record_number", unique = true, nullable = false)
    private String medicalRecordNumber;
}

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByMedicalRecordNumber(String mrn);
}
```

**Changes Required**:
1. Replace `@Table` with `@Entity` + `@Table`
2. Add `@GeneratedValue` to `@Id`
3. Add `@Column` annotations for constraints
4. Change `CrudRepository` to `JpaRepository`
5. Update dependencies (remove JDBC, add JPA)

**Service layer**: No changes required! Same API.

---

### 13.12 Common JPA Pitfalls and Solutions

**Pitfall 1: N+1 Query Problem**

```java
// ❌ BAD: Triggers N+1 queries
List<Appointment> appointments = appointmentRepository.findAll();
for (Appointment apt : appointments) {
    System.out.println(apt.getPatient().getName());  // N queries!
}

// ✅ GOOD: Fetch join - single query
@Query("SELECT a FROM Appointment a JOIN FETCH a.patient")
List<Appointment> findAllWithPatient();
```

---

**Pitfall 2: Lazy Loading Outside Transaction**

```java
// ❌ BAD: LazyInitializationException
public Patient getPatient(Long id) {
    return patientRepository.findById(id).orElseThrow();
}
// Later: patient.getAppointments() throws exception!

// ✅ GOOD: Fetch in transaction
@Transactional(readOnly = true)
public Patient getPatientWithAppointments(Long id) {
    Patient patient = patientRepository.findById(id).orElseThrow();
    patient.getAppointments().size();  // Force load
    return patient;
}

// ✅ BETTER: Fetch join
@Query("SELECT p FROM Patient p LEFT JOIN FETCH p.appointments WHERE p.id = :id")
Optional<Patient> findByIdWithAppointments(@Param("id") Long id);
```

---

**Pitfall 3: Bidirectional Relationship Sync**

```java
// ❌ BAD: Only one side updated
appointment.getPrescriptions().add(prescription);
// prescription.setAppointment(appointment) missing!

// ✅ GOOD: Helper method keeps both sides in sync
public void addPrescription(Prescription prescription) {
    prescriptions.add(prescription);
    prescription.setAppointment(this);  // Keep relationship consistent
}
```

---

**Pitfall 4: Overusing Eager Loading**

```java
// ❌ BAD: Everything loaded always
@OneToMany(fetch = FetchType.EAGER)
private List<Appointment> appointments;

@OneToMany(fetch = FetchType.EAGER)
private List<MedicalRecord> medicalRecords;
// Fetching one patient loads hundreds of records!

// ✅ GOOD: Lazy by default, eager when needed
@OneToMany(fetch = FetchType.LAZY)
private List<Appointment> appointments;

@EntityGraph(attributePaths = {"appointments"})
Patient findByIdWithAppointments(Long id);  // Eager only when needed
```

---

### 13.13 Checkpoint Question 13

**Scenario**: You're evaluating JPA vs JDBC approaches for three new features:

**Feature A**: Patient dashboard showing:
- Patient info
- Last 5 appointments with doctor names
- Total appointment count
- Upcoming appointments

**Feature B**: Bulk import of 50,000 patient records from CSV file

**Feature C**: Monthly analytics report with:
- Appointments per doctor
- Revenue per specialization
- Patient demographics
- Prescription trends

**Question**: For each feature, choose Raw JDBC, JdbcTemplate, Spring Data JDBC, or Spring Data JPA. Justify your choice.

**Answer**:

**Feature A: Patient Dashboard**

**Choice**: 🏆 **Spring Data JPA**

**Justification**:
- Complex relationships (Patient → Appointments → Doctor)
- Lazy loading beneficial (load appointments only when viewing dashboard)
- Can use `@EntityGraph` to prevent N+1 problem
- Dashboard is read-heavy - caching helps
- Clean object-oriented code

```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    @EntityGraph(attributePaths = {"appointments", "appointments.doctor"})
    @Query("SELECT p FROM Patient p WHERE p.id = :id")
    Optional<Patient> findByIdWithDashboardData(@Param("id") Long id);
}

@Service
@Transactional(readOnly = true)
public class PatientDashboardService {
    
    public PatientDashboard getDashboard(Long patientId) {
        Patient patient = patientRepository.findByIdWithDashboardData(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId));
        
        List<Appointment> last5 = patient.getAppointments()
            .stream()
            .sorted(Comparator.comparing(Appointment::getScheduledAt).reversed())
            .limit(5)
            .toList();
        
        long upcomingCount = patient.getAppointments()
            .stream()
            .filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now()))
            .count();
        
        return new PatientDashboard(patient, last5, upcomingCount);
    }
}
```

**Benefits**:
- Single query with JOIN FETCH (no N+1)
- Relationships managed automatically
- Clean, readable code
- Can add second-level cache for performance

---

**Feature B: Bulk Import of 50,000 Records**

**Choice**: 🏆 **JdbcTemplate with Batch Operations**

**Justification**:
- High-volume operation (50,000 records)
- JdbcTemplate batch insert is 10x faster than JPA
- No need for object mapping during import
- Fine control over batch size and error handling
- Transactional per batch (not all-or-nothing)

```java
@Service
public class PatientImportService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional
    public ImportResult bulkImport(List<PatientCsvRecord> csvRecords) {
        String sql = """
            INSERT INTO patient (name, date_of_birth, blood_type, 
                                medical_record_number, email, phone_number, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        int batchSize = 1000;
        int totalInserted = 0;
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < csvRecords.size(); i += batchSize) {
            List<PatientCsvRecord> batch = csvRecords.subList(
                i, Math.min(i + batchSize, csvRecords.size())
            );
            
            try {
                int[] results = jdbcTemplate.batchUpdate(sql, batch, batchSize, 
                    (ps, record) -> {
                        ps.setString(1, record.getName());
                        ps.setDate(2, Date.valueOf(record.getDateOfBirth()));
                        ps.setString(3, record.getBloodType());
                        ps.setString(4, record.getMedicalRecordNumber());
                        ps.setString(5, record.getEmail());
                        ps.setString(6, record.getPhoneNumber());
                        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    }
                );
                totalInserted += results.length;
                
            } catch (DataAccessException ex) {
                errors.add("Batch " + (i / batchSize) + ": " + ex.getMessage());
                // Continue with next batch instead of failing entire import
            }
        }
        
        return new ImportResult(totalInserted, errors);
    }
}
```

**Performance**:
- JdbcTemplate batch: ~5 seconds for 50,000 records
- JPA saveAll(): ~60+ seconds for 50,000 records
- **12x faster with JdbcTemplate!**

---

**Feature C: Monthly Analytics Report**

**Choice**: 🏆 **JdbcTemplate with Complex SQL**

**Justification**:
- Complex aggregations (COUNT, SUM, AVG, GROUP BY)
- Multiple table joins
- Database-level computations more efficient
- Result doesn't map to entities (DTO projection)
- JPQL would be awkward for this complexity

```java
@Repository
public class AnalyticsRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public MonthlyReport generateMonthlyReport(YearMonth month) {
        // Appointments per doctor
        List<DoctorStats> doctorStats = getDoctorStatistics(month);
        
        // Revenue per specialization
        List<SpecializationRevenue> revenueStats = getRevenueBySpecialization(month);
        
        // Patient demographics
        PatientDemographics demographics = getPatientDemographics(month);
        
        // Prescription trends
        List<PrescriptionTrend> prescriptionTrends = getPrescriptionTrends(month);
        
        return new MonthlyReport(doctorStats, revenueStats, demographics, prescriptionTrends);
    }
    
    private List<DoctorStats> getDoctorStatistics(YearMonth month) {
        String sql = """
            SELECT 
                d.id,
                d.name,
                d.specialization,
                COUNT(a.id) as total_appointments,
                COUNT(DISTINCT a.patient_id) as unique_patients,
                COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed,
                COUNT(CASE WHEN a.status = 'CANCELLED' THEN 1 END) as cancelled,
                ROUND(AVG(EXTRACT(EPOCH FROM (a.scheduled_at - a.created_at)) / 86400), 2) 
                    as avg_booking_days
            FROM doctor d
            LEFT JOIN appointment a ON d.id = a.doctor_id 
                AND EXTRACT(YEAR FROM a.scheduled_at) = ?
                AND EXTRACT(MONTH FROM a.scheduled_at) = ?
            GROUP BY d.id, d.name, d.specialization
            HAVING COUNT(a.id) > 0
            ORDER BY total_appointments DESC
        """;
        
        return jdbcTemplate.query(sql, this::mapToDoctorStats, 
            month.getYear(), month.getMonthValue());
    }
    
    private List<SpecializationRevenue> getRevenueBySpecialization(YearMonth month) {
        String sql = """
            SELECT 
                d.specialization,
                COUNT(a.id) as appointment_count,
                SUM(a.fee) as total_revenue,
                AVG(a.fee) as avg_fee
            FROM doctor d
            JOIN appointment a ON d.id = a.doctor_id
            WHERE EXTRACT(YEAR FROM a.scheduled_at) = ?
                AND EXTRACT(MONTH FROM a.scheduled_at) = ?
                AND a.status = 'COMPLETED'
            GROUP BY d.specialization
            ORDER BY total_revenue DESC
        """;
        
        return jdbcTemplate.query(sql, this::mapToRevenue, 
            month.getYear(), month.getMonthValue());
    }
    
    // ... other methods
}
```

**Why not JPA?**:
- JPQL gets very complex with multiple aggregations
- Native queries lose type safety
- Database does aggregations faster than loading all data to Java
- Report data doesn't map to entities

---

**Summary**:
- Feature A (Dashboard): **Spring Data JPA** - Complex relationships, lazy loading, caching
- Feature B (Bulk Import): **JdbcTemplate** - Batch performance (12x faster)
- Feature C (Analytics): **JdbcTemplate** - Complex aggregations, DTO projections

**Key Takeaway**: Use the right tool for each job. Real applications use multiple approaches!

---

### 13.14 Summary: The Persistence Landscape

**What We Learned in This Session**:

```
Raw JDBC (Step 2)
    ↓ Too much boilerplate
    
JdbcTemplate (Step 3)
    ↓ Still writing SQL for everything
    
Spring Data JDBC (Step 9)
    ↓ Query methods, but no lazy loading
    
Spring Data JPA (Step 13)
    ↓ Full ORM with caching and lazy loading
```

**Final Recommendations**:

**Start with**: Spring Data JDBC
- Simple, predictable, sufficient for 80% of applications
- Use JdbcTemplate for complex queries and batch operations

**Upgrade to**: Spring Data JPA
- When relationships become very complex
- When you need lazy loading and caching
- When team has JPA expertise

**Always keep**: JdbcTemplate
- For reporting and analytics
- For bulk operations
- For database-specific features

---

### 13.15 Where to Go Next

**Further Learning**:

1. **Spring Data JPA Deep Dive**
   - Specification pattern for dynamic queries
   - Query DSL integration
   - Hibernate second-level cache tuning
   - Performance optimization

2. **Database Migrations**
   - Flyway for versioned migrations
   - Liquibase for complex changes
   - Schema evolution strategies

3. **Testing**
   - Testcontainers for integration tests
   - In-memory H2 for unit tests
   - Database seeding strategies

4. **Advanced Topics**
   - Event sourcing with JDBC
   - CQRS pattern (Command Query Responsibility Segregation)
   - Read replicas and write masters
   - Sharding strategies

5. **Reactive Database Access**
   - R2DBC (Reactive Relational Database Connectivity)
   - Spring Data R2DBC
   - WebFlux + R2DBC integration

---

### 13.16 Final Checkpoint: Complete Medical System

**Scenario**: You've been asked to build a complete medical appointment system from scratch. Make architecture decisions:

**Requirements**:
1. 20,000 patients, 200 doctors
2. 500 appointments per day
3. Patient portal (view appointments, prescriptions)
4. Doctor dashboard (daily schedule, patient history)
5. Admin reports (revenue, utilization, trends)
6. Bulk import from legacy system (quarterly)
7. Mobile app + web app (REST API)
8. 99.9% uptime requirement

**Your Answers Should Cover**:
- Database choice (PostgreSQL, MySQL, etc.)
- Persistence approach (JDBC, JPA, or hybrid)
- Connection pool configuration
- Caching strategy
- API design
- Monitoring and logging
- Deployment strategy

**Suggested Solution**:

**1. Database**: PostgreSQL 16
- JSONB support for flexible fields
- Excellent performance
- Strong ACID compliance
- Mature tooling

**2. Persistence Strategy**: Hybrid Approach

```java
// Domain entities: Spring Data JDBC
@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {
    Optional<Patient> findByMedicalRecordNumber(String mrn);
}

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdAndScheduledAtBetween(
        Long doctorId, LocalDateTime start, LocalDateTime end);
}

// Complex reports: JdbcTemplate
@Component
public class ReportingRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public MonthlyReport generateReport(YearMonth month) {
        // Complex SQL with joins and aggregations
    }
}

// Bulk operations: JdbcTemplate batch
@Component
public class BulkImportService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void importPatients(List<PatientDto> patients) {
        jdbcTemplate.batchUpdate(...);
    }
}
```

**3. Connection Pool Configuration**:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30  # 16-core server = (16*2) = 32, use 30
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

**4. Caching Strategy**:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new CaffeineCacheManager("doctors", "specializations");
    }
}

@Service
public class DoctorService {
    
    @Cacheable("doctors")  // Cache doctor lookups
    public Doctor getDoctor(Long id) {
        return doctorRepository.findById(id).orElseThrow();
    }
}
```

**5. API Design**:

```
GET    /api/v1/patients
POST   /api/v1/patients
GET    /api/v1/patients/{id}
PUT    /api/v1/patients/{id}
DELETE /api/v1/patients/{id}

GET    /api/v1/appointments
POST   /api/v1/appointments
GET    /api/v1/appointments/{id}
PATCH  /api/v1/appointments/{id}/status

GET    /api/v1/doctors
GET    /api/v1/doctors/{id}/schedule?date=2024-12-26

GET    /api/v1/reports/monthly?year=2024&month=12
```

**6. Monitoring**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Integrate with:
- Prometheus for metrics collection
- Grafana for dashboards
- ELK Stack (Elasticsearch, Logstash, Kibana) for log aggregation
- Sentry for error tracking

**7. Deployment**:

```yaml
# Docker Compose for development
services:
  postgres:
    image: postgres:16-alpine
    # ...
  
  app:
    image: medical-app:latest
    depends_on:
      - postgres
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/medical_system
      DATABASE_USER: ${DB_USER}
      DATABASE_PASSWORD: ${DB_PASSWORD}

# Kubernetes for production
apiVersion: apps/v1
kind: Deployment
metadata:
  name: medical-app
spec:
  replicas: 3  # High availability
  template:
    spec:
      containers:
      - name: app
        image: medical-app:1.0.0
        resources:
          limits:
            cpu: "2"
            memory: "2Gi"
          requests:
            cpu: "1"
            memory: "1Gi"
```

**Expected Results**:
- ✅ 99.9% uptime (3-replica deployment)
- ✅ <200ms API response time (connection pooling + caching)
- ✅ 500 appointments/day handled easily
- ✅ Quarterly bulk imports in <30 minutes
- ✅ Scalable architecture (add replicas as needed)

---

## Conclusion

**Congratulations!** You've completed a comprehensive journey through Spring Boot data persistence:

✅ **Understood the problem**: Raw JDBC boilerplate and resource management issues  
✅ **Learned the solution**: JdbcTemplate for SQL control, Spring Data JDBC for simplicity  
✅ **Mastered the tools**: Repository pattern, query methods, aggregates, transactions  
✅ **Built production-ready apps**: Connection pooling, error handling, monitoring, security  
✅ **Made informed decisions**: When to use JDBC vs JPA, hybrid approaches  

**Key Takeaways**:

1. **No single tool is perfect** - Use the right tool for each job
2. **Start simple** - Spring Data JDBC handles 80% of needs
3. **Add complexity when needed** - JdbcTemplate for reports, JPA for complex domains
4. **Think production-first** - Connection pools, monitoring, error handling from day one
5. **Measure everything** - Profile queries, monitor connections, log slow operations

**Your Next Steps**:

1. Build the Medical Appointment System from scratch
2. Add authentication with Spring Security
3. Implement real-time notifications with WebSockets
4. Deploy to cloud (AWS, Azure, GCP)
5. Monitor in production with Prometheus + Grafana

**Happy coding!** 🚀

---

