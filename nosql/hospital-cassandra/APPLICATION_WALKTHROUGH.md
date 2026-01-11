# Hospital Cassandra System - Application Walkthrough

**Complete Technical Deep Dive for Senior Engineers & Architects**

---

## STEP 1: Architecture Overview & Request Flow Analysis

---

## 1.1 System Architecture Overview

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  (Swagger UI / REST Client / curl / HTTP Requests)              │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP Request
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                  (Embedded Tomcat on port 8080)                 │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  REST Layer (PatientController)                          │  │
│  │  - POST /api/patients                                    │  │
│  │  - GET /api/patients/{id}                                │  │
│  │  - POST /api/patients/{id}/events                        │  │
│  │  - GET /api/patients/{id}/events?days=N                 │  │
│  └────────────┬─────────────────────────────────────────────┘  │
│               │                                                  │
│  ┌────────────▼──────────────────────────────────────────────┐  │
│  │  Business Logic & Data Mapping                            │  │
│  │  (Spring Data Cassandra Template)                         │  │
│  │  - Entity → Row Conversion                                │  │
│  │  - Query Parameter Binding                                │  │
│  │  - Codec Registry for Type Mapping                        │  │
│  └────────────┬─────────────────────────────────────────────┘  │
│               │                                                  │
│  ┌────────────▼──────────────────────────────────────────────┐  │
│  │  Repository Layer (Spring Data)                           │  │
│  │  - PatientRepository (CassandraRepository)                │  │
│  │  - MedicalEventRepository (CassandraRepository)           │  │
│  │  - Automatic Query Method Derivation                      │  │
│  │  - Custom @Query Support                                  │  │
│  └────────────┬─────────────────────────────────────────────┘  │
│               │                                                  │
│  ┌────────────▼──────────────────────────────────────────────┐  │
│  │  Configuration Layer                                      │  │
│  │  - CassandraConfig Bean                                   │  │
│  │  - CqlSessionBuilderCustomizer                            │  │
│  │  - CassandraSessionInitializer                            │  │
│  │  - application.yml Property Loading                       │  │
│  └────────────┬─────────────────────────────────────────────┘  │
│               │                                                  │
│  ┌────────────▼──────────────────────────────────────────────┐  │
│  │  DataStax Java Driver 4.18.1                              │  │
│  │  - CqlSession (Connection Pool)                           │  │
│  │  - Protocol Buffer (Binary Protocol v4)                   │  │
│  │  - Load Balancing (Local DC awareness)                    │  │
│  │  - Connection Pooling & Health Checks                     │  │
│  └────────────┬─────────────────────────────────────────────┘  │
└────────────────┼──────────────────────────────────────────────┘
                 │ CQL Queries
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│              Apache Cassandra 4.1 (Docker)                       │
│              Port: 9042 (CQL Native Protocol)                    │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Keyspace: hospital_system                               │  │
│  │  Replication Factor: 1 (Single Node for Dev)             │  │
│  │  Consistency Level: LOCAL_ONE (Default)                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌────────────────────┐      ┌─────────────────────────────┐   │
│  │ Table: patients    │      │ Table: medical_events       │   │
│  │ ─────────────────  │      │ ───────────────────────      │   │
│  │ PK: patientid      │      │ PK: (patient_id,            │   │
│  │ Cols: name, dob,   │      │      event_timestamp DESC,  │   │
│  │       blood_type,  │      │      event_id)              │   │
│  │       email        │      │ Cols: event_type,           │   │
│  │                    │      │       description,          │   │
│  │                    │      │       diagnosis_codes,      │   │
│  │                    │      │       created_by            │   │
│  └────────────────────┘      └─────────────────────────────┘   │
│                                                                  │
│  Replication Strategy: SimpleStrategy (1 replica)               │
│  Partitioner: Murmur3Partitioner (Default)                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1.2 Component Interactions

### Spring Boot Autoconfiguration Chain

```
Application Start (HospitalApplication.main())
         ↓
@SpringBootApplication Annotation Processing
         ↓
Spring IoC Container Initialization
         ↓
CassandraAutoConfiguration Triggered
  ├─ Reads application.yml properties
  ├─ Loads spring.data.cassandra.* properties
  ├─ Initializes CqlSessionBuilder with contact-points
  └─ Looks for CqlSessionBuilderCustomizer beans
         ↓
CassandraConfig Bean Loaded (@Configuration)
  ├─ @Bean CqlSessionBuilderCustomizer
  ├─ Applies .withLocalDatacenter("datacenter1")
  ├─ Applies .withKeyspace("hospital_system")
  └─ Returns customizer function
         ↓
CqlSession Created & Connected to Cassandra
         ↓
Spring Data Cassandra Repositories Scanned
  ├─ PatientRepository registered
  ├─ MedicalEventRepository registered
  └─ Spring proxies generated for method derivation
         ↓
CassandraSessionInitializer @Component Initialized
  ├─ Injects CqlSession dependency
  ├─ Waits for ApplicationReadyEvent
  └─ Executes early session test to catch config errors
         ↓
PatientController Bean Created
  ├─ Injects PatientRepository
  ├─ Injects MedicalEventRepository
  └─ REST endpoints exposed
         ↓
Tomcat Server Started on Port 8080
         ↓
✓ Application Ready for Requests
```

---

## 1.3 Complete Request-Response Flow Analysis

### **Endpoint 1: POST /api/patients (Create Patient)**

#### Request Flow Diagram

```
HTTP Client (Swagger UI / curl)
         │
         │ POST /api/patients
         │ Content-Type: application/json
         │ Body: {patientName, dateOfBirth, bloodType, email}
         ↓
PatientController.createPatient(@RequestBody Patient patient)
  │
  ├─ Step 1: Receive request body
  │  └─ Spring's HttpMessageConverter deserializes JSON → Patient object
  │     (Jackson ObjectMapper)
  │
  ├─ Step 2: Generate UUID
  │  └─ patientId = UUID.randomUUID()
  │     (Client-side UUID generation - good for distributed systems)
  │
  ├─ Step 3: Call repository
  │  └─ patientRepository.save(patient)
  │     └─ PatientRepository (extends CassandraRepository<Patient, UUID>)
  │        └─ Spring Data Generated Implementation
  │           └─ SimpleCassandraRepository.save()
  │
  ├─ Step 4: Entity → Row Conversion
  │  └─ CassandraTemplate.insert(patient)
  │     └─ MappingCassandraConverter processes entity
  │        ├─ Reads @Table("patients") annotation
  │        ├─ Maps fields to columns:
  │        │  ├─ patientId (UUID) → patientid (PK)
  │        │  ├─ patientName (String) → patient_name
  │        │  ├─ dateOfBirth (LocalDate) → date_of_birth
  │        │  ├─ bloodType (String) → blood_type
  │        │  └─ email (String) → email
  │        └─ Creates parameter bindings for ?
  │
  ├─ Step 5: Query Preparation
  │  └─ CqlTemplate.query()
  │     └─ Prepares statement:
  │        "INSERT INTO patients (patientid, patient_name, date_of_birth, blood_type, email) VALUES (?, ?, ?, ?, ?)"
  │
  ├─ Step 6: Type Codec Registry
  │  └─ DataStax Driver CodecRegistry
  │     ├─ UUID → com.datastax.oss.driver.internal.core.type.codec.UuidCodec
  │     ├─ String → com.datastax.oss.driver.internal.core.type.codec.AsciiCodec
  │     └─ LocalDate → com.datastax.oss.driver.internal.core.type.codec.DateCodec
  │        (Handles Java type → Cassandra wire protocol conversion)
  │
  ├─ Step 7: Execute Query
  │  └─ CqlSession.execute(boundStatement)
  │     ├─ Sends query to Cassandra via binary protocol (TCP port 9042)
  │     ├─ Load balancer routes to local datacenter node
  │     └─ Query executed with LOCAL_ONE consistency
  │
  ├─ Step 8: Cassandra Processing
  │  └─ Cassandra receives INSERT
  │     ├─ Partition key (patientid) → Murmur3 hash → determines node
  │     ├─ In single-node Docker: always local node
  │     ├─ Writes to memtable (in-memory)
  │     ├─ Returns success response
  │     └─ Eventually flushed to SSTable (disk)
  │
  ├─ Step 9: Response Marshalling
  │  └─ patientRepository.save() returns saved Patient
  │     └─ Spring's ResponseBodyAdvice
  │        └─ Jackson converts Patient → JSON
  │           {
  │             "patientId": "3b4264f5-...",
  │             "patientName": "John Doe",
  │             "dateOfBirth": "1990-05-15",
  │             "bloodType": "O+",
  │             "email": "john.doe@hospital.com"
  │           }
  │
  └─ Step 10: HTTP Response
     └─ 201 Created
        Content-Type: application/json
        Location: /api/patients/3b4264f5-...
        Body: [JSON above]
         ↓
HTTP Client receives response
```

#### Code Flow Trace

```
PatientController.createPatient() [Line 55-65]
  │
  ├─ Patient patient = new Patient();  // Deserialized from JSON
  ├─ patient.setPatientId(UUID.randomUUID());  // [Line 57]
  │
  └─ patientRepository.save(patient);  // [Line 58]
     │
     └─ SimpleCassandraRepository<Patient, UUID>.save()
        │
        └─ CassandraTemplate.insert(patient)
           │
           ├─ getMappingContext().getRequiredPersistentEntity(Patient.class)
           │  └─ Returns CassandraPersistentEntity describing table structure
           │
           ├─ converter.write(patient, row)  // [MappingCassandraConverter]
           │  ├─ row.setString(0, "patients")  // table name
           │  ├─ row.setUuid(1, patient.getPatientId())
           │  ├─ row.setString(2, patient.getPatientName())
           │  ├─ row.setLocalDate(3, patient.getDateOfBirth())
           │  ├─ row.setString(4, patient.getBloodType())
           │  └─ row.setString(5, patient.getEmail())
           │
           └─ cqlTemplate.execute(INSERT_STATEMENT, boundValues)
              │
              └─ CqlSession.execute(boundStatement)
                 │
                 └─ DataStax Driver → TCP/IP → Cassandra (127.0.0.1:9042)
```

#### Key Technical Points

| Aspect | Detail |
|--------|--------|
| **Entity Deserialization** | Jackson's `@JsonProperty` works with Lombok `@Getter/@Setter` |
| **UUID Generation** | Client-side generation (Java 21's `UUID.randomUUID()`) |
| **Partition Key** | `patientId` determines which node stores data |
| **Type Mapping** | LocalDate → Cassandra DATE (not timestamp) |
| **Consistency** | LOCAL_ONE (fastest, eventual consistency) |
| **HTTP Status** | 201 Created (RESTful convention) |
| **Transaction Support** | Cassandra is NOT ACID - single partition = atomic, cross-partition = NOT atomic |

---

### **Endpoint 2: GET /api/patients/{id} (Retrieve Patient)**

#### Request Flow Diagram

```
HTTP Client
         │
         │ GET /api/patients/3b4264f5-c6e5-45fd-afda-1c1318f2c207
         ↓
PatientController.getPatient(@PathVariable UUID patientId)
  │
  ├─ Step 1: Path Variable Extraction
  │  └─ Spring converts string UUID to java.util.UUID object
  │
  ├─ Step 2: Call Repository
  │  └─ Optional<Patient> = patientRepository.findById(patientId)
  │     └─ Spring Data Generated Implementation
  │        └─ CassandraRepository extends CrudRepository
  │
  ├─ Step 3: Generate Query
  │  └─ Spring derives from method name: findById(ID)
  │     └─ SELECT * FROM patients WHERE patientid = ?
  │
  ├─ Step 4: Execute Query
  │  └─ CqlSession.execute(selectStatement)
  │     ├─ Cassandra routes to partition containing patientid
  │     ├─ Single partition key lookup = O(1) performance
  │     ├─ Reads from memtable or SSTable
  │     └─ Returns result set
  │
  ├─ Step 5: Row → Entity Conversion
  │  └─ MappingCassandraConverter.read(Patient.class, row)
  │     ├─ Reads columns and maps to Java fields
  │     ├─ UUID codec → patient.patientId
  │     ├─ VARCHAR codec → patient.patientName
  │     ├─ DATE codec → patient.dateOfBirth
  │     ├─ VARCHAR codec → patient.bloodType
  │     └─ VARCHAR codec → patient.email
  │
  ├─ Step 6: Optional Wrapping
  │  └─ Spring wraps result in Optional<Patient>
  │     ├─ If found: Optional.of(patient)
  │     └─ If not found: Optional.empty()
  │
  ├─ Step 7: HTTP Response Generation
  │  └─ PatientController returns ResponseEntity<Patient>
  │     ├─ If found: return ResponseEntity.ok(patient) → 200 OK
  │     └─ If not found: return ResponseEntity.notFound() → 404 Not Found
  │
  └─ Step 8: JSON Serialization
     └─ Jackson converts Patient to JSON
        {
          "patientId": "3b4264f5-c6e5-45fd-afda-1c1318f2c207",
          "patientName": "John Doe",
          "dateOfBirth": "1990-05-15",
          "bloodType": "O+",
          "email": "john.doe@hospital.com"
        }
         ↓
HTTP Response: 200 OK
```

#### Cassandra Query Analysis

```
CQL Query: SELECT * FROM patients WHERE patientid = ?

Execution Plan:
├─ Partition Key Lookup (patientid)
│  ├─ Hash patientid with Murmur3Partitioner
│  ├─ Determine partition token
│  └─ O(1) lookup in memtable/SSTable
│
├─ Single Row Result Expected
│  └─ Most cases: 1 row returned (or 0 if not found)
│
└─ Performance Profile
   ├─ Latency: 1-5ms (in-memory) to 10-50ms (disk)
   ├─ No full table scan
   ├─ Ideal for partition key queries (most common pattern)
   └─ Scale linearly with cluster size (local DC)
```

#### Key Technical Points

| Aspect | Detail |
|--------|--------|
| **Query Derivation** | Spring Data derives `SELECT * FROM patients WHERE patientid = ?` from method name |
| **Type Conversion** | String UUID → java.util.UUID via Spring's ConversionService |
| **Performance** | O(1) lookup - direct partition access |
| **Consistency** | LOCAL_ONE consistency |
| **Caching** | No built-in caching (can add @Cacheable if needed) |
| **Optional Pattern** | Java 8 Optional for null-safe handling |

---

### **Endpoint 3: POST /api/patients/{id}/events (Add Medical Event)**

#### Request Flow Diagram

```
HTTP Client
         │
         │ POST /api/patients/3b4264f5-.../events
         │ Body: {eventType, description, diagnosisCodes, createdBy}
         ↓
PatientController.addMedicalEvent(@PathVariable UUID patientId, @RequestBody MedicalEvent event)
  │
  ├─ Step 1: Receive Multi-Part Request
  │  ├─ Path parameter: patientId (UUID)
  │  └─ Request body: MedicalEvent object (JSON)
  │
  ├─ Step 2: Initialize Medical Event Object
  │  └─ MedicalEvent event = new MedicalEvent();
  │     ├─ Set patientId (from path parameter)
  │     ├─ Set eventId = UUID.randomUUID()
  │     ├─ Set eventTimestamp = Instant.now()  // Current time in UTC
  │     └─ Other fields already set from JSON
  │
  ├─ Step 3: Business Logic (isUrgent calculation)
  │  └─ event.setIsUrgent(event.getEventType().equals("EMERGENCY"))
  │     (Note: @Transient field, not persisted to DB)
  │
  ├─ Step 4: Save to Repository
  │  └─ medicalEventRepository.save(event)
  │     └─ SimpleCassandraRepository.save()
  │        └─ CassandraTemplate.insert(event)
  │
  ├─ Step 5: Composite Primary Key Processing
  │  └─ MappingCassandraConverter analyzes @PrimaryKeyColumn annotations
  │     ├─ @PrimaryKeyColumn(type=PARTITIONED) patientId
  │     │  └─ PARTITION KEY: patientId (determines node)
  │     │
  │     ├─ @PrimaryKeyColumn(ordinal=0, type=CLUSTERED) eventTimestamp
  │     │  └─ CLUSTERING KEY: eventTimestamp DESC (sort within partition)
  │     │
  │     └─ @PrimaryKeyColumn(ordinal=1, type=CLUSTERED) eventId
  │        └─ CLUSTERING KEY: eventId ASC (secondary sort)
  │
  ├─ Step 6: Prepare INSERT Statement
  │  └─ "INSERT INTO medical_events 
  │       (patient_id, event_timestamp, event_id, event_type, description, diagnosis_codes, created_by)
  │       VALUES (?, ?, ?, ?, ?, ?, ?)"
  │
  ├─ Step 7: Type Codec Resolution
  │  └─ DataStax Driver CodecRegistry
  │     ├─ UUID → UuidCodec
  │     ├─ Instant → TimestampCodec (wire → long milliseconds)
  │     ├─ String → AsciiCodec
  │     └─ List<String> → ListCodec<String>
  │
  ├─ Step 8: Execute Query on Cassandra
  │  └─ CqlSession.execute(boundStatement)
  │     ├─ Partition key (patientId) routed to specific node
  │     ├─ Cassandra stores in memtable
  │     ├─ Eventually flush to SSTable
  │     └─ Returns success
  │
  ├─ Step 9: Cassandra Internal Processing
  │  └─ Cassandra inserts row into medical_events
  │     ├─ Partition: determined by Murmur3(patientId)
  │     ├─ Within partition: sorted by (eventTimestamp DESC, eventId ASC)
  │     ├─ This enables efficient range queries
  │     └─ Timestamp ordering enables efficient "last N events" queries
  │
  ├─ Step 10: Response Construction
  │  └─ Spring converts MedicalEvent to JSON
  │     {
  │       "patientId": "3b4264f5-...",
  │       "eventTimestamp": "2026-01-11T15:45:30.123Z",
  │       "eventId": "a1b2c3d4-...",
  │       "eventType": "LAB_RESULT",
  │       "description": "COVID-19 PCR Test",
  │       "diagnosisCodes": ["B97.29"],
  │       "createdBy": "Dr. Smith",
  │       "urgent": false  // @Transient field
  │     }
  │
  └─ Step 11: HTTP Response
     └─ 201 Created with Location header
```

#### Composite Key Deep Dive

```
PRIMARY KEY Structure in Cassandra:
─────────────────────────────────

CREATE TABLE medical_events (
    patient_id uuid,
    event_timestamp timestamp,
    event_id uuid,
    ... data columns ...
    PRIMARY KEY (patient_id, event_timestamp, event_id)
) WITH CLUSTERING ORDER BY (event_timestamp DESC, event_id ASC);

Breakdown:
├─ PARTITION KEY: (patient_id)
│  ├─ Determines which node stores this row
│  ├─ Hash function: Murmur3(patient_id) → token
│  ├─ Token matched against token ring
│  └─ All events for a patient on same node(s)
│
└─ CLUSTERING KEYS: (event_timestamp DESC, event_id ASC)
   ├─ Order of rows WITHIN partition
   ├─ First sort: event_timestamp DESC (newest first)
   ├─ Then sort: event_id ASC (consistent order for same timestamp)
   ├─ Enables efficient range queries:
   │  └─ "Get all events for patient where timestamp between X and Y"
   └─ Enables efficient "last N" queries:
      └─ "Get 10 latest events" via LIMIT 10

Example Data Layout (Single Partition):

Patient: 3b4264f5-c6e5-45fd-afda-1c1318f2c207 (one partition)
│
├─ Row 1: timestamp=2026-01-11T15:47:00Z  id=event-003  type=DISCHARGE
├─ Row 2: timestamp=2026-01-11T15:46:00Z  id=event-002  type=ADMISSION
└─ Row 3: timestamp=2026-01-11T15:45:00Z  id=event-001  type=LAB_RESULT
   (Ordered DESC by timestamp, ASC by id within same timestamp)
```

#### Key Technical Points

| Aspect | Detail |
|--------|--------|
| **Composite Key** | (patientId, eventTimestamp DESC, eventId) |
| **Partition Key** | patientId - all events for patient on same node |
| **Clustering Keys** | Determine sort order within partition |
| **Timestamp** | Instant.now() generates UTC timestamp |
| **Client-side UUID** | Avoids server-side UUID generation bottleneck |
| **DESC Ordering** | Newest events first (natural for time-series) |
| **Transient Field** | isUrgent not persisted (calculated at runtime) |
| **List Type** | diagnosis_codes: List<String> mapped to LIST<text> |

---

### **Endpoint 4: GET /api/patients/{id}/events?days=N (Query with Range)**

#### Request Flow Diagram

```
HTTP Client
         │
         │ GET /api/patients/3b4264f5-.../events?days=30
         ↓
PatientController.getPatientEvents(@PathVariable UUID patientId, @RequestParam int days)
  │
  ├─ Step 1: Extract Parameters
  │  ├─ patientId from path (UUID)
  │  └─ days from query parameter (int, default=30)
  │
  ├─ Step 2: Calculate Time Range
  │  ├─ endTime = Instant.now()  // Current time
  │  ├─ startTime = endTime.minus(Duration.ofDays(days))
  │  └─ Creates range for last N days
  │
  ├─ Step 3: Call Repository Method
  │  └─ List<MedicalEvent> = medicalEventRepository
  │       .findByPatientIdAndEventTimestampBetween(patientId, startTime, endTime)
  │
  ├─ Step 4: Spring Data Query Derivation
  │  └─ Method name decoding:
  │     findByPatientIdAndEventTimestampBetween(UUID, Instant, Instant)
  │     ↓ generates CQL query:
  │     SELECT * FROM medical_events
  │     WHERE patient_id = ?
  │       AND event_timestamp >= ?
  │       AND event_timestamp <= ?
  │     ALLOW FILTERING;
  │     (Note: ALLOW FILTERING not ideal but needed for range on clustering key)
  │
  ├─ Step 5: Parameter Binding
  │  └─ Bind parameters: (patientId, startTime, endTime)
  │     └─ DataStax Driver codec handling:
  │        ├─ UUID → patientId
  │        ├─ Instant → startTime (milliseconds since epoch)
  │        └─ Instant → endTime
  │
  ├─ Step 6: Execute Query on Cassandra
  │  └─ CqlSession.execute(selectStatement)
  │     ├─ Partition key lookup: patientId
  │     ├─ Direct partition access (one node)
  │     ├─ Range scan on clustering key (event_timestamp)
  │     │  └─ Cassandra reads rows from timestamp DESC order
  │     │  └─ Efficient because data physically ordered this way on disk
  │     ├─ Filter by eventTimestamp >= startTime AND <= endTime
  │     └─ Returns matching rows
  │
  ├─ Step 7: Result Set Processing
  │  └─ DataStax driver returns ResultSet
  │     ├─ Lazy evaluation by default
  │     ├─ Rows fetched in batches
  │     └─ Convert each row to MedicalEvent entity
  │
  ├─ Step 8: Entity Mapping
  │  └─ MappingCassandraConverter.read(MedicalEvent.class, row)
  │     For each returned row:
  │     ├─ Read patientId (UUID) → field
  │     ├─ Read eventTimestamp (Instant) → field
  │     ├─ Read eventId (UUID) → field
  │     ├─ Read eventType (String) → field
  │     ├─ Read description (String) → field
  │     ├─ Read diagnosisCodes (List<String>) → field
  │     └─ Read createdBy (String) → field
  │
  ├─ Step 9: Return as List
  │  └─ medicalEventRepository returns List<MedicalEvent>
  │     └─ Sorted by eventTimestamp DESC (from Cassandra)
  │
  ├─ Step 10: JSON Serialization
  │  └─ Jackson converts List<MedicalEvent> to JSON array
  │     [
  │       {
  │         "patientId": "3b4264f5-...",
  │         "eventTimestamp": "2026-01-11T15:47:30.000Z",
  │         "eventId": "event-003",
  │         "eventType": "DISCHARGE",
  │         ...
  │       },
  │       {
  │         "patientId": "3b4264f5-...",
  │         "eventTimestamp": "2026-01-11T15:46:15.000Z",
  │         "eventId": "event-002",
  │         "eventType": "ADMISSION",
  │         ...
  │       },
  │       {
  │         "patientId": "3b4264f5-...",
  │         "eventTimestamp": "2026-01-11T15:45:00.000Z",
  │         "eventId": "event-001",
  │         "eventType": "LAB_RESULT",
  │         ...
  │       }
  │     ]
  │
  └─ Step 11: HTTP Response
     └─ 200 OK with array of events
```

#### CQL Query Analysis

```
Generated CQL:
──────────────

SELECT * FROM medical_events
WHERE patient_id = ? AND event_timestamp >= ? AND event_timestamp <= ?
ALLOW FILTERING;

Execution Strategy:
├─ Partition Key (patient_id = ?)
│  ├─ Hash lookup: Murmur3(patient_id) → specific partition
│  ├─ Read entire partition (all events for this patient)
│  └─ Most efficient operation for Cassandra
│
├─ Clustering Key Range (event_timestamp >= ? AND <= ?)
│  ├─ Cassandra reads in DESC order (as stored on disk)
│  ├─ Returns rows matching timestamp range
│  ├─ No full partition scan needed if ranges aligned with clustering
│  └─ Efficient for time-series queries
│
├─ ALLOW FILTERING Implications
│  ├─ Required when range query on clustering keys
│  ├─ NOT ideal but necessary for this use case
│  ├─ Filters at read level (not full table scan)
│  └─ Performance acceptable for single partition
│
└─ Performance Characteristics
   ├─ Latency: 5-50ms (depends on partition size)
   ├─ Network: Minimal (local partition)
   ├─ Disk I/O: Sequential read on SSTable (very fast)
   ├─ Memory: Row buffering during deserialization
   └─ Scale: O(n) where n = rows in time range
```

#### Advanced Cassandra Concepts Demonstrated

| Concept | How Applied |
|---------|------------|
| **Partition Pruning** | patient_id filter eliminates other partitions |
| **Clustering Range Query** | event_timestamp between range uses clustering key ordering |
| **Sequential I/O** | DESC ordering on disk = sequential read = fast |
| **Lazy Deserialization** | Rows materialized as needed (not all at once) |
| **Time-Series Pattern** | DESC timestamp + clustering = efficient event history queries |

---

## 1.4 Data Model: Cassandra Schema Design

### Patient Table Design

```cassandra
CREATE TABLE patients (
    patientid uuid PRIMARY KEY,
    patient_name text,
    date_of_birth date,
    blood_type text,
    email text
);
```

**Design Rationale:**

| Design Decision | Reason |
|-----------------|--------|
| **UUID Primary Key** | Unique identifier, distributed generation, avoids hotspots |
| **Simple Primary Key** | One patient per row, no clustering needed |
| **date_of_birth as DATE** | Not timestamp - we only care about date, not time |
| **TEXT for strings** | No length limit, flexible |
| **No TTL** | Patient records persistent |
| **No secondary indexes** | Query only by patient ID |
| **No frozen collections** | Simple scalar types only |

**Cassandra Internals:**

```
Storage Layout:
Patient table (sorted by patientid token):

Partition 1:
├─ 3b4264f5-c6e5-45fd-afda-1c1318f2c207
│  ├─ patientid: 3b4264f5-c6e5-45fd-afda-1c1318f2c207
│  ├─ patient_name: "John Doe"
│  ├─ date_of_birth: 1990-05-15
│  ├─ blood_type: "O+"
│  └─ email: "john.doe@hospital.com"

Partition 2:
├─ 4bb7767b-c4cd-4796-9f41-14ef19d3e5a4
│  ├─ patientid: 4bb7767b-c4cd-4796-9f41-14ef19d3e5a4
│  ├─ patient_name: "Jane Smith"
│  ├─ date_of_birth: 1985-03-20
│  ├─ blood_type: "A+"
│  └─ email: "jane.smith@hospital.com"
```

---

### Medical Events Table Design

```cassandra
CREATE TABLE medical_events (
    patient_id uuid,
    event_timestamp timestamp,
    event_id uuid,
    event_type text,
    description text,
    diagnosis_codes list<text>,
    created_by text,
    PRIMARY KEY (patient_id, event_timestamp, event_id)
) WITH CLUSTERING ORDER BY (event_timestamp DESC, event_id ASC);
```

**Design Rationale:**

| Design Decision | Reason |
|-----------------|--------|
| **Composite Primary Key** | Enable efficient time-series queries per patient |
| **patient_id as PARTITION KEY** | Groups all events for patient on same node(s) |
| **event_timestamp as CLUSTERING KEY (DESC)** | Newest events first, sequential disk reads |
| **event_id as secondary CLUSTERING KEY** | Deterministic ordering for same-second events |
| **Instant for timestamp** | UTC timestamp, precise to milliseconds |
| **UUID for event_id** | Unique event identifier, client-generated |
| **LIST<text> for diagnosis_codes** | Multiple diagnosis codes per event, flexible |
| **No TTL** | Events permanent (medical records) |
| **CLUSTERING ORDER DESC** | Efficient "latest N events" queries |

**Cassandra Internals:**

```
Storage Layout:
Medical Events table (organized by patient partition, sorted within partition):

Partition for patient_id = 3b4264f5-c6e5-45fd-afda-1c1318f2c207:
(Events stored in DESC order by timestamp, ASC by id for same timestamp)

Row 1:
├─ patient_id: 3b4264f5-c6e5-45fd-afda-1c1318f2c207
├─ event_timestamp: 2026-01-11T15:47:30.123Z  ← Newest
├─ event_id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
├─ event_type: DISCHARGE
├─ description: "Patient discharged - stable condition"
├─ diagnosis_codes: []
└─ created_by: "Dr. Sarah Smith"

Row 2:
├─ patient_id: 3b4264f5-c6e5-45fd-afda-1c1318f2c207
├─ event_timestamp: 2026-01-11T15:46:15.456Z  ← Middle
├─ event_id: b2c3d4e5-f6a7-b890-cdef-f12345678901
├─ event_type: ADMISSION
├─ description: "Emergency admission - chest pain"
├─ diagnosis_codes: ["R07.9"]
└─ created_by: "Dr. Michael Chen"

Row 3:
├─ patient_id: 3b4264f5-c6e5-45fd-afda-1c1318f2c207
├─ event_timestamp: 2026-01-11T15:45:00.789Z  ← Oldest
├─ event_id: c3d4e5f6-a7b8-c901-def0-f23456789012
├─ event_type: LAB_RESULT
├─ description: "COVID-19 PCR Test - Negative"
├─ diagnosis_codes: ["B97.29"]
└─ created_by: "Dr. Sarah Smith"

(All ordered DESC by timestamp)
```

---

## 1.5 Spring Data Cassandra Annotations Explained

### `@Table` Annotation

```java
@Table("patients")  // Specifies Cassandra table name
public class Patient { ... }
```

**Effect:**
- Maps Java class to Cassandra table
- Automatic CQL table creation (if schema-action: create-if-not-exists)
- Codec registry lookups use table metadata

---

### `@PrimaryKey` Annotation (Simple Key)

```java
@PrimaryKey
private UUID patientId;  // Maps to PRIMARY KEY in Cassandra
```

**Effect:**
- Marks single column as primary key (partition key)
- Cassandra routes by hash of this key
- Enables direct lookups: O(1) performance

---

### `@PrimaryKeyColumn` Annotation (Composite Key)

```java
@PrimaryKeyColumn(name = "patient_id", type = PrimaryKeyType.PARTITIONED)
private UUID patientId;

@PrimaryKeyColumn(name = "event_timestamp", ordinal = 0, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
private Instant eventTimestamp;

@PrimaryKeyColumn(name = "event_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
private UUID eventId;
```

**Effect:**
- `type = PARTITIONED`: Determines partition
- `type = CLUSTERED`: Determines sort within partition
- `ordinal`: Order of clustering columns (0 = first sort, 1 = second sort)
- `ordering = DESCENDING`: Newest first (for timestamp)

---

### `@Column` Annotation

```java
@Column("patient_name")
private String patientName;

@Column("date_of_birth")
private LocalDate dateOfBirth;
```

**Effect:**
- Maps Java field to Cassandra column name
- Enables camelCase → snake_case conversion
- Optional (default uses field name)

---

### `@Transient` Annotation

```java
@Transient
private boolean isUrgent;  // Not persisted to Cassandra
```

**Effect:**
- Field calculated at runtime, not stored
- Useful for computed properties
- Not included in INSERT/UPDATE statements

---

## 1.6 Configuration Flow: YAML → Cassandra Connection

### application.yml Processing

```yaml
spring:
  application:
    name: hospital-cassandra-system
    
  data:
    cassandra:
      keyspace-name: hospital_system          # ← Keyspace to use
      contact-points: 127.0.0.1              # ← Node addresses
      port: 9042                              # ← CQL port
      local-datacenter: datacenter1           # ← DC name (CRITICAL)
      schema-action: create-if-not-exists    # ← Auto-create tables

logging:
  level:
    com.datastax.oss.driver: INFO
    org.springframework.data.cassandra: DEBUG
```

### Configuration Processing Chain

```
1. Spring Application Startup
        ↓
2. YamlPropertySourceLoader reads application.yml
   └─ Populates Spring Environment with spring.data.cassandra.* properties
        ↓
3. CassandraAutoConfiguration examines Environment
   └─ spring-boot-starter-data-cassandra activates auto-config
        ↓
4. CassandraProperties bean created
   └─ Properties object populated with:
      ├─ keyspace-name: "hospital_system"
      ├─ contact-points: ["127.0.0.1"]
      ├─ port: 9042
      ├─ local-datacenter: "datacenter1"
      └─ schema-action: CREATE_IF_NOT_EXISTS
        ↓
5. CqlSessionBuilder configured (DataStax Driver)
   └─ withContactPoints("127.0.0.1:9042")
        ↓
6. Spring looks for CqlSessionBuilderCustomizer beans
   └─ Finds: CassandraConfig.sessionBuilderCustomizer()
        ↓
7. CassandraConfig bean applies customizations
   ├─ builder.withLocalDatacenter("datacenter1")  ← *** CRITICAL ***
   │  └─ Tells driver which DC is local
   │  └─ Avoids "local DC must be explicitly set" error
   │
   └─ builder.withKeyspace("hospital_system")
      └─ Sets default keyspace for all queries
        ↓
8. CqlSession created and connected
   ├─ Establishes TCP connection to 127.0.0.1:9042
   ├─ Handshakes with Cassandra cluster
   ├─ Initializes connection pool
   └─ Registers with Spring context
        ↓
9. Spring Data Repositories configured
   ├─ Scans classpath for @Repository interfaces
   ├─ Finds: PatientRepository, MedicalEventRepository
   ├─ Generates dynamic implementations
   ├─ Registers as Spring beans
   └─ Ready for dependency injection
        ↓
10. CassandraSessionInitializer component loads
    ├─ Injects CqlSession dependency
    ├─ Waits for ApplicationReadyEvent (app fully initialized)
    ├─ Executes test query: session.getMetadata()
    └─ Validates connection works, logs success
        ↓
11. Application ready for requests ✓
```

### CassandraConfig Bean Deep Dive

```java
@Configuration
public class CassandraConfig {
    
    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;
    
    @Value("${spring.data.cassandra.keyspace-name:hospital_system}")
    private String keyspace;
    
    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
        return (CqlSessionBuilder builder) -> builder
                .withLocalDatacenter(localDatacenter)
                .withKeyspace(keyspace);
    }
}
```

**How It Works:**

1. **@Value Injection**: Spring reads properties from application.yml
2. **@Bean Registration**: Method returns CqlSessionBuilderCustomizer
3. **CqlSessionBuilderCustomizer**: Functional interface (one method)
4. **Lambda Application**: Receives builder, modifies, returns modified builder
5. **Chaining**: CqlSession created with customizations applied

**Without this config:**
```
ERROR: Since you provided explicit contact points, 
the local DC must be explicitly set
```

**With this config:**
```
✓ Connection established to local-datacenter1
✓ Ready to execute queries
```

---

## 1.7 Key Cassandra Concepts Applied

### Partition Key vs Clustering Key

```
Medical Events Table Structure:

PRIMARY KEY (patient_id, event_timestamp DESC, event_id)
            │                   │                  │
            └─ PARTITION KEY    └─ CLUSTERING KEY ─┘

PARTITION KEY (patient_id):
├─ Determines which node(s) store this row
├─ All rows with same patient_id → same partition
├─ Hash function: Murmur3(patient_id) → token
├─ Token → node lookup → specific Cassandra nodes
└─ Example: All events for patient X on Node A

CLUSTERING KEY (event_timestamp DESC, event_id):
├─ Determines order of rows WITHIN partition
├─ First: Sort by event_timestamp DESC (newest first)
├─ Then: Sort by event_id ASC (consistent for same timestamp)
├─ Stored physically on disk in this order
├─ Sequential read = very fast (SSD sequential I/O)
└─ Enables efficient range queries
```

### Consistency Level

```
Application uses: LOCAL_ONE (Cassandra default)

LOCAL_ONE means:
├─ Write succeeds when 1 replica in local DC acknowledges
├─ Read succeeds when 1 replica in local DC responds
├─ Fast performance (no cross-DC latency)
├─ Less durable than QUORUM (2 replicas)
└─ For single-node dev: trivial (only 1 replica)

CAP Trade-off:
├─ Consistency: LOW (eventual consistency after write)
├─ Availability: HIGH (any node can serve)
├─ Partition tolerance: HIGH (distributed design)
└─ This is AP system, not CP
```

### Type Mapping (Java ↔ Cassandra)

```
Java Type          Cassandra Type    Wire Format
─────────────────  ──────────────── ─────────────────
java.util.UUID     UUID              16 bytes
java.time.LocalDate DATE             4 bytes (days since epoch)
java.time.Instant  TIMESTAMP         8 bytes (ms since epoch)
java.lang.String   TEXT/VARCHAR      Variable length
java.util.List     LIST              Variable length collection
boolean            BOOLEAN           1 byte
int                INT               4 bytes
long               BIGINT            8 bytes
```

---

## Summary: Step 1 Complete

This section covered:

✅ **Architecture**: Multi-layer design from HTTP client → Spring → Cassandra
✅ **Request Flows**: Detailed trace of all 4 endpoints with step-by-step execution
✅ **Data Model**: Composite key design for time-series data
✅ **Annotations**: Spring Data Cassandra mapping and configuration
✅ **Configuration**: YAML → CqlSessionBuilder → Connection
✅ **Cassandra Concepts**: Partition keys, clustering keys, consistency

---

**Next: STEP 2 (Debug Points & Troubleshooting)**
- Where to set breakpoints in IDE
- Variable inspection at each layer
- Common errors and how to diagnose
- Performance analysis techniques

---

# STEP 2: Debug Points, Troubleshooting & Known Issues

---

## 2.1 Setting Breakpoints in IDE

### VS Code Setup

#### Prerequisites:
- Extension Pack for Java (Microsoft)
- Debugger for Java (Microsoft)
- Project open in VS Code

#### Steps:

1. **Open File with Breakpoint**
   - Navigate to: `src/main/java/com/healthcare/hospital/controller/PatientController.java`
   - Click on line number to set breakpoint
   - Red circle appears = breakpoint set

2. **Start Debugging (F5)**
   - Open `HospitalApplication.java`
   - Press **F5** or go to Run → Debug
   - Select "Debug: HospitalApplication" from dropdown
   - Application starts with debugger attached

3. **Trigger Breakpoint**
   - Make HTTP request to endpoint
   - Execution pauses at breakpoint
   - Debugger opens Variables/Call Stack panels

#### Example: Breakpoint in createPatient

```java
@PostMapping
@Operation(summary = "Create a new patient")
public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
    patient.setPatientId(UUID.randomUUID());  // ← SET BREAKPOINT HERE (Line 57)
    
    Patient saved = patientRepository.save(patient);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Debugger Will Show:**
- `patient`: Object with all fields
- `UUID.randomUUID()`: Generated UUID value
- `patientRepository`: Spring proxy object
- Call Stack: HTTP handler → Controller → Method

---

### IntelliJ IDEA Setup

#### Steps:

1. **Open Run/Debug Configuration**
   - Run → Edit Configurations
   - Click "+" → Application
   - Name: "HospitalApplication"
   - Main class: `com.healthcare.hospital.HospitalApplication`
   - Click OK

2. **Set Breakpoint**
   - Click on line number in code
   - Red circle appears

3. **Debug Application (Shift+F9)**
   - Select configuration
   - Press Shift+F9 or Run → Debug
   - Application starts with debugger attached

4. **Debug Console**
   - Variables tab shows local variables
   - Watches tab for custom expressions
   - Step Over (F8), Step Into (F7), Step Out (Shift+F8)

---

## 2.2 Critical Debug Points

### Debug Point 1: CassandraSessionInitializer

**File:** `src/main/java/com/healthcare/hospital/config/CassandraSessionInitializer.java`

**Lines:** 25-35

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    try {
        // SET BREAKPOINT HERE (Line 29)
        var metadata = cqlSession.getMetadata();
        log.info("✓ Cassandra session initialized successfully");
        log.info("  - Connected nodes: {}", metadata.getNodes().size());
        log.info("  - Keyspace: hospital_system");
    } catch (Exception e) {
        log.error("✗ Failed to initialize Cassandra session: {}", e.getMessage());
        throw new RuntimeException("Cassandra initialization failed", e);
    }
}
```

**What to Inspect:**
- `cqlSession`: Check if null or connected
  - Hover over `cqlSession` → Should show `CqlSession` object
  - Expand → See `_nodes` list (should have 1 node for Docker setup)
  - Check `_keyspace`: Should be "hospital_system"

- `metadata.getNodes()`: Number of connected nodes
  - Should be 1 for single-node Docker
  - If empty, Cassandra not running

- Exception details: If caught, explains connection issue

**Success Indicators:**
```
✓ cqlSession object shows Node(endPoint=/127.0.0.1:9042)
✓ metadata.getNodes().size() == 1
✓ Current keyspace == "hospital_system"
```

**Failure Indicators:**
```
✗ cqlSession is null → Configuration not applied
✗ metadata.getNodes() is empty → Cassandra not running
✗ Exception in catch block → Connection error
```

---

### Debug Point 2: PatientController.createPatient

**File:** `src/main/java/com/healthcare/hospital/controller/PatientController.java`

**Lines:** 55-65

```java
@PostMapping
@Operation(summary = "Create a new patient")
public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
    // BREAKPOINT 1: HERE (Line 56)
    patient.setPatientId(UUID.randomUUID());
    
    // BREAKPOINT 2: HERE (Line 58)
    Patient saved = patientRepository.save(patient);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

**Breakpoint 1 (Request Entry):**

Inspect:
- `patient`: Deserialized request body
  - `patientName`: Should match request JSON
  - `dateOfBirth`: Check LocalDate formatting
  - `bloodType`: Should be present
  - `email`: Should be valid format
  - `patientId`: Initially null (not set by JSON)

Check for Deserialization Issues:
- If any field null unexpectedly → JSON property name mismatch
- If `dateOfBirth` is null → JSON might have string like "1990-05-15"
- Compare to request you sent

**Breakpoint 2 (Post-Save):**

Inspect:
- `saved` object: Should have patientId now
  - `patientId`: UUID generated in previous step
  - All fields from original patient
  - Confirms database write successful

- Inspect `patientRepository`:
  - Right-click → Force Return to skip actual DB save
  - Useful for testing without DB

**Watch Expressions:**
```
patient.getPatientId() != null
patient.getPatientName().length() > 0
saved.getPatientId().toString()
```

---

### Debug Point 3: MappingCassandraConverter (Type Conversion)

**File:** `org/springframework/data/cassandra/core/MappingCassandraConverter.java` (Spring Data)

**What to Watch:**
When this gets called during save, your breakpoint shows type coercion

In **PatientController.createPatient** at the save line:
```java
Patient saved = patientRepository.save(patient);  // ← Pause here
```

Then **Step Into (F7)** to enter:
- SimpleCassandraRepository.save()
- → CassandraTemplate.insert()
- → MappingCassandraConverter.write()

**In converter, inspect:**
- `source`: Original Patient object
- `sink`: Row being written to
- Column name mappings:
  - `patient_name` ← patientName
  - `date_of_birth` ← dateOfBirth (LocalDate → DATE codec)
  - `blood_type` ← bloodType

**Common Issue:**
```
If you see: "Codec not found for DATE <-> java.lang.String"
│
Reason: dateOfBirth is String, but table expects DATE
│
Fix: Change Patient.dateOfBirth to LocalDate
│
Debug: Set breakpoint in converter.write() to see exact field type mismatch
```

---

### Debug Point 4: MedicalEventRepository.findByPatientIdAndEventTimestampBetween

**File:** `src/main/java/com/healthcare/hospital/controller/PatientController.java`

**Lines:** 83-92

```java
@GetMapping("/{patientId}/events")
@Operation(summary = "Get medical events for a patient")
public ResponseEntity<List<MedicalEvent>> getPatientEvents(
        @PathVariable UUID patientId,
        @RequestParam(defaultValue = "30") int days) {
    
    Instant endTime = Instant.now();
    Instant startTime = endTime.minus(Duration.ofDays(days));
    
    // BREAKPOINT HERE (Line 89)
    List<MedicalEvent> events = medicalEventRepository
        .findByPatientIdAndEventTimestampBetween(patientId, startTime, endTime);
    
    return ResponseEntity.ok(events);
}
```

**Breakpoint Inspection:**

Before the query:
- `patientId`: UUID from path parameter
- `startTime`: Calculated Instant
  - Should be N days ago
  - Check: `endTime.minus(Duration.ofDays(days))`
- `endTime`: Current time (Instant.now())
  - Should be recent timestamp

After the query (Step Over then pause):
- `events`: List<MedicalEvent>
  - Size should match records in DB
  - Order should be DESC by timestamp (newest first)
  - Check first element timestamp > last element timestamp

**Watch for Missing Data:**
```
If events is empty but you created events:
├─ patientId mismatch → Wrong UUID passed
├─ Time range too narrow → startTime/endTime calculation wrong
└─ Database events deleted → Check Cassandra directly
```

**Watch Expression:**
```
events.stream().map(e -> e.getEventTimestamp()).toList()
// Should show DESC order (newest first)
```

---

### Debug Point 5: CassandraTemplate.execute

**File:** `org/springframework/data/cassandra/core/CassandraTemplate.java` (Spring Data)

**When to Set:**
In **PatientController.createPatient**, Step Into the save:
```java
Patient saved = patientRepository.save(patient);  // Step Into (F7)
└─ SimpleCassandraRepository.save()
   └─ CassandraTemplate.insert()
      └─ CassandraTemplate.execute()  ← BREAKPOINT HERE
```

**Inspect Here:**
- `boundStatement`: Prepared statement with bound parameters
  - Hover to see CQL: `INSERT INTO patients (patientid, ...) VALUES (?, ...)`
  - Parameters: Shows bound values

- `callback`: The operation being executed
  - Type: PreparedStatementHandler or similar

- Exception handling:
  - If exception caught, shows error details
  - "table patients does not exist" → Schema issue
  - "local DC must be explicitly set" → Configuration issue

**Performance Observation:**
- Set breakpoint, execute, measure time
- Typical time: 5-50ms (includes network round-trip)

---

## 2.3 Variable Inspection Guide

### Inspecting Complex Objects

#### Patient Object (Simple)

```
patient: Patient
├─ patientId: UUID = 3b4264f5-c6e5-45fd-afda-1c1318f2c207
├─ patientName: String = "John Doe"
├─ dateOfBirth: LocalDate = 1990-05-15
├─ bloodType: String = "O+"
└─ email: String = "john.doe@hospital.com"
```

**How to Inspect (VS Code):**
1. Pause at breakpoint
2. Look at Variables panel (left sidebar)
3. Expand patient object
4. Click on field to see value in tooltip

---

#### MedicalEvent Object (Complex with Composite Key)

```
event: MedicalEvent
├─ patientId: UUID = 3b4264f5-c6e5-45fd-afda-1c1318f2c207
├─ eventTimestamp: Instant = 2026-01-11T15:45:30.123Z
├─ eventId: UUID = a1b2c3d4-e5f6-7890-abcd-ef1234567890
├─ eventType: String = "LAB_RESULT"
├─ description: String = "COVID-19 PCR Test - Negative"
├─ diagnosisCodes: List = [B97.29]
├─ createdBy: String = "Dr. Sarah Smith"
└─ isUrgent: boolean = false
```

**Inspecting List Field:**
1. Expand diagnosisCodes
2. Shows [0]: "B97.29"
3. Size shown in bracket: [1 element]

---

#### CqlSession Object (Connection Pool)

```
cqlSession: CqlSessionImpl
├─ _context: DriverContext
│  ├─ protocolVersion: V4 (binary protocol version 4)
│  └─ _nodes: List = [Node(endPoint=/127.0.0.1:9042)]
├─ _keyspace: String = "hospital_system"
├─ _requestThrottler: RequestThrottler
│  └─ _maxConcurrentRequests: int = 2048
├─ _connectionPool: ConnectionPool
│  ├─ _nodes: Map = {Node(...) → PooledConnection(...)}
│  └─ [Shows each node and its connection pool state]
└─ _metadata: Metadata
   ├─ _nodes: Map = {Node → NodeMetadata}
   └─ _keyspaces: Map = {hospital_system → KeyspaceMetadata}
```

**Key Fields to Check:**
- `_keyspace`: Should be "hospital_system"
- `_context._nodes.size()`: Should be 1 (Docker single node)
- `_connectionPool._nodes`: Should have connections to node
- `_metadata._keyspaces.get("hospital_system")`: Confirms keyspace exists

---

### Conditional Breakpoints

**Example: Only Break if Patient Name is Empty**

In VS Code:
1. Right-click on breakpoint (red circle)
2. Select "Edit Breakpoint"
3. Enter condition: `patient.getPatientName() == null || patient.getPatientName().isEmpty()`
4. Click away

Now breakpoint only triggers when condition true (for debugging deserialization issues)

**Example 2: Only Break if Event is Missing Diagnosis**

```
event.getDiagnosisCodes() == null || event.getDiagnosisCodes().isEmpty()
```

---

### Watch Expressions

**Add Custom Watch in VS Code:**

1. Pause at breakpoint
2. Click + in Watch section
3. Enter expression:
   - `patient.getPatientId().toString()`
   - `events.size()`
   - `startTime.toString()`
   - `patientRepository.getClass().getName()`

**Useful Expressions:**

```
// Check if UUID is valid
patient.getPatientId() != null && !patient.getPatientId().toString().isEmpty()

// Check list size and first element
events != null && events.size() > 0 && events.get(0).getEventTimestamp() != null

// Check time range
startTime.isBefore(endTime)

// Check Cassandra connection
cqlSession.isClosed() == false
```

---

## 2.4 Common Errors & Diagnostics

### ERROR 1: "local DC must be explicitly set"

**Error Message:**
```
IllegalStateException: Since you provided explicit contact points, 
the local DC must be explicitly set (see basic.load-balancing-policy.local-datacenter in the config, 
or set it programmatically with SessionBuilder.withLocalDatacenter)
```

**When:** Application startup, when initializing CqlSession

**Root Cause:**
- DataStax driver requires explicit local datacenter
- Spring YAML property not passed to driver
- CassandraConfig bean not applied

**Debug Steps:**

1. **Check CassandraConfig exists:**
   ```
   Set breakpoint in: CassandraConfig.sessionBuilderCustomizer()
   │
   Should trigger at startup
   If NOT triggered: Config class not found or @Configuration missing
   ```

2. **Verify application.yml:**
   ```yaml
   spring:
     data:
       cassandra:
         local-datacenter: datacenter1  ← Must be present
   ```

3. **Inspect CqlSessionBuilder:**
   ```
   In CassandraConfig breakpoint, step into method
   Check builder has withLocalDatacenter("datacenter1") applied
   If not: Spring not calling the customizer
   ```

4. **Check Cassandra Container:**
   ```powershell
   docker logs hospital-cassandra | grep "datacenter"
   Should show: Datacenter: datacenter1
   ```

**Fix:**
- Ensure `CassandraConfig.java` exists in `config/` package
- Rebuild: `mvn clean package -DskipTests`
- Verify in jar: `jar -tf target/hospital-cassandra-1.0.0.jar | grep CassandraConfig`

---

### ERROR 2: "table patients does not exist"

**Error Message:**
```
InvalidQueryException: Query; CQL [...]; table patients does not exist
```

**When:** First POST /api/patients request (INSERT)

**Root Cause:**
- Schema not created in Cassandra
- Table exists in wrong keyspace
- Cassandra container not fully initialized

**Debug Steps:**

1. **Verify Cassandra Running:**
   ```powershell
   docker ps | grep hospital-cassandra
   
   Should show: UP (healthy)
   If not: docker-compose up -d
   ```

2. **Check Tables Exist:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLES;"
   
   Should output: medical_events  patients
   If not: Tables not created → Run schema.cql
   ```

3. **Verify schema.cql Executed:**
   ```
   Set breakpoint in CassandraSessionInitializer.onApplicationReady()
   Inspect: metadata.getKeyspaces().get("hospital_system")
   │
   Should show table list
   If empty: schema.cql never executed
   ```

**Fix:**
```powershell
# Copy schema file
docker cp schema.cql hospital-cassandra:/tmp/schema.cql

# Execute it
docker exec hospital-cassandra cqlsh -f /tmp/schema.cql

# Verify
docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLES;"
```

---

### ERROR 3: "Codec not found for requested operation: [DATE <-> java.lang.String]"

**Error Message:**
```
CodecNotFoundException: Codec not found for requested operation: [DATE <-> java.lang.String]
```

**When:** INSERT statement execution for patient with dateOfBirth

**Root Cause:**
- Java field `Patient.dateOfBirth` is String
- Cassandra table `date_of_birth` column is DATE type
- DataStax driver can't convert String → DATE

**Debug Steps:**

1. **Check Patient Class:**
   ```java
   // BAD:
   private String dateOfBirth;  // ✗ Wrong type
   
   // GOOD:
   private LocalDate dateOfBirth;  // ✓ Correct type
   ```

2. **Verify at Breakpoint:**
   ```
   Set breakpoint in PatientController.createPatient()
   Inspect: patient.getDateOfBirth()
   │
   If class is String: Problem found
   If class is LocalDate: Problem is elsewhere
   ```

3. **Check Table Schema:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLE patients;"
   
   Should show: date_of_birth | date
   ```

**Fix:**
```java
// In Patient.java
import java.time.LocalDate;  // ← Add import

@Column("date_of_birth")
private LocalDate dateOfBirth;  // ← Change from String
```

Rebuild and restart application.

---

### ERROR 4: "Connection refused" or "Unable to connect to remote server"

**Error Message:**
```
Unable to connect to the remote server
AllHostsFailedException: All host(s) tried for query failed
```

**When:** First HTTP request to any endpoint

**Root Cause:**
- Cassandra not running or not accessible
- Contact-points wrong in configuration
- Network issue between app and Cassandra

**Debug Steps:**

1. **Check Cassandra Container:**
   ```powershell
   docker ps | grep hospital-cassandra
   
   STATUS should be: Up X seconds (healthy)
   If not: Start with docker-compose up -d
   ```

2. **Check Container Logs:**
   ```powershell
   docker logs hospital-cassandra
   
   Look for:
   - "Startup complete" ← Ready
   - Errors about disk space, port already in use, etc.
   ```

3. **Check Port Accessibility:**
   ```powershell
   Test-NetConnection -ComputerName 127.0.0.1 -Port 9042
   
   Should show: TcpTestSucceeded : True
   ```

4. **Set Breakpoint in CassandraSessionInitializer:**
   ```
   If breakpoint is hit but metadata.getNodes() is empty:
   └─ No connected nodes → Network issue
   ```

**Fix:**
```powershell
# Restart Cassandra
docker-compose restart

# Wait 60 seconds for initialization
Start-Sleep -Seconds 60

# Verify healthy
docker ps
```

---

### ERROR 5: "Query; CQL [SELECT ...]; No results"

**Error Message:**
```
When calling getPatient() with nonexistent UUID:
Returns 404 (expected behavior)

But if should exist and doesn't:
Empty Optional returned
```

**When:** GET /api/patients/{id} with wrong UUID

**Debug Steps:**

1. **Verify UUID in Request:**
   ```
   Set breakpoint in getPatient()
   Inspect: patientId from path parameter
   │
   Should be valid UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   ```

2. **Check Database:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT * FROM patients;"
   
   Compare returned patientid with requested UUID
   Exact match required (case-sensitive, dashes matter)
   ```

3. **Verify Time-Series (for events):**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT * FROM medical_events WHERE patient_id = 3b4264f5-c6e5-45fd-afda-1c1318f2c207;"
   
   (Replace with actual UUID)
   Should show rows if any events created
   ```

**Fix:**
- Use actual UUID from POST response
- Copy exactly (check for hidden spaces)
- Test with curl first: `curl http://localhost:8080/api/patients | grep patientid`

---

### ERROR 6: "ALLOW FILTERING" Performance Warning

**When:** Querying events with time range (expected)

**CQL Generated:**
```sql
SELECT * FROM medical_events
WHERE patient_id = ? 
  AND event_timestamp >= ? 
  AND event_timestamp <= ?
ALLOW FILTERING;
```

**Not Really an Error:**
- ALLOW FILTERING is required for clustering key range queries
- In this app, acceptable because:
  - Queries scoped to single partition (patient_id)
  - Not a full table scan
  - Performance still good (<100ms)

**Debug Performance:**

1. **Set Breakpoint at Query:**
   ```
   In PatientController.getPatientEvents()
   Before: Note system time
   After: Note system time
   Duration: Time - Start
   
   Typical: 5-50ms
   ```

2. **Monitor Query Latency:**
   ```
   Add to Watch:
   System.currentTimeMillis()  // Before query
   └─ Then step and repeat → Delta = query time
   ```

3. **Check Event Count:**
   ```
   Set breakpoint after query
   Inspect: events.size()
   
   If very large (>10000):
   └─ Consider pagination in real app
   ```

---

### ERROR 7: "Connection pool exhausted"

**Error Message:**
```
RequestThrottledException: RequestQueue overloaded: queue has XX pending requests
```

**When:** High volume of requests, connection pool full

**Debug Steps:**

1. **Inspect Connection Pool:**
   ```
   Set breakpoint in any controller
   In Watch, enter: cqlSession
   
   Expand → _connectionPool → Check available connections
   ```

2. **Check Pool Configuration:**
   ```yaml
   # In application.yml
   spring.data.cassandra.pool.core-connections: 8
   spring.data.cassandra.pool.max-connections: 32
   ```

3. **Monitor Active Requests:**
   ```
   Debugger can't directly show active requests
   But high queue count indicates bottleneck
   ```

**Fix (for Production):**
```yaml
spring:
  data:
    cassandra:
      pool:
        core-connections: 16        # ← Increase from default 8
        max-connections: 64         # ← Increase from default 32
        idle-timeout: PT5M          # ← Connection TTL
        validation-query: "SELECT 1"
```

**For Development:**
- Unlikely to hit this unless doing stress testing
- Default pool size (8 core) handles dev traffic

---

## 2.5 Performance Analysis Techniques

### Measuring Query Latency

**Method 1: Stopwatch in Code (Most Reliable)**

Add to PatientController:

```java
@GetMapping("/{patientId}/events")
public ResponseEntity<List<MedicalEvent>> getPatientEvents(
        @PathVariable UUID patientId,
        @RequestParam(defaultValue = "30") int days) {
    
    long startTime = System.currentTimeMillis();
    
    Instant endTime = Instant.now();
    Instant startInstant = endTime.minus(Duration.ofDays(days));
    
    List<MedicalEvent> events = medicalEventRepository
        .findByPatientIdAndEventTimestampBetween(patientId, startInstant, endTime);
    
    long endTimeMs = System.currentTimeMillis();
    long durationMs = endTimeMs - startTime;
    
    log.info("Query took {}ms for {} events", durationMs, events.size());
    
    return ResponseEntity.ok(events);
}
```

**Then Check Logs:**
```
Query took 15ms for 5 events   ← Healthy
Query took 200ms for 100 events ← Investigate if >100ms
```

**Method 2: Debugger Timing**

1. Set breakpoint before query
2. Set breakpoint after query
3. Hit first breakpoint, note time in debugger
4. Hit second breakpoint, note time
5. Calculate delta

---

### Memory Usage Analysis

**Check Heap Memory:**

In VS Code Debug Console:
```javascript
// Note: In Java debugger context
Runtime.getRuntime().totalMemory()
Runtime.getRuntime().freeMemory()
Runtime.getRuntime().maxMemory()
```

**Memory-Intensive Operations:**

1. **Loading Large Result Sets:**
   ```
   List<MedicalEvent> events = repository.findAll();  // ✗ Bad
   
   Better:
   events.stream()
     .filter(predicate)
     .limit(100)
     .collect()  // ✓ Good
   ```

2. **Connection Pool Size:**
   ```
   Watch: cqlSession._connectionPool._nodes.size()
   
   Typical: 1 node × 8 connections = 8 connection objects
   Memory per connection: ~1-2 MB
   Total pool memory: ~16 MB (acceptable)
   ```

---

### Database Metrics from Cassandra

**Query via Docker:**

1. **Keyspace Replication:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "DESCRIBE KEYSPACE hospital_system;"
   
   Shows replication factor, tables, etc.
   ```

2. **Table Statistics:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT * FROM system.local;"
   
   Shows node info, compaction stats, etc.
   ```

3. **Row Count:**
   ```powershell
   docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT COUNT(*) FROM patients;"
   
   Note: COUNT in Cassandra is approximate and slow for large tables
   Use only for dev verification
   ```

---

### Thread Activity

**Check Active Threads:**

Set breakpoint, in debugger find "Threads" panel:

```
ThreadGroup: main
├─ main thread (your code)
├─ http-nio-8080-exec-1 (HTTP request thread)
├─ http-nio-8080-exec-2 (HTTP request thread)
├─ pool-1-thread-1 (Cassandra driver I/O)
├─ pool-1-thread-2 (Cassandra driver I/O)
├─ scheduler-thread (Spring Scheduler)
└─ [Additional daemon threads]
```

**What's Normal:**
- Multiple http-nio threads (Tomcat thread pool)
- Multiple pool threads (Cassandra driver async operations)
- Main thread paused at breakpoint

**What's Abnormal:**
- Hundreds of threads → Thread leak
- All threads blocked → Deadlock

---

## Summary: STEP 2 Complete

This section covered:

✅ **IDE Setup**: VS Code and IntelliJ breakpoint configuration
✅ **5 Critical Debug Points**: CassandraSessionInitializer, createPatient, converter, range query, CassandraTemplate
✅ **Variable Inspection**: Patient, MedicalEvent, CqlSession objects
✅ **7 Common Errors**: With root causes and step-by-step diagnostics
✅ **Performance Analysis**: Query latency, memory usage, thread activity

---

---

## STEP 3: Senior Engineer Interview Questions & Answers

This section contains checkpoint questions designed for senior engineers (10+ years experience) to verify deep understanding of the system's architectural decisions and distributed data patterns.

### Interview Question 1: Primary Key Design Strategy

**Question:**
"Why does this application use UUID (`java.util.UUID`) for `patientId` instead of an auto-incrementing long integer? What are the architectural implications when scaling this to a multi-node Cassandra cluster?"

**Expected Answer - Level 1 (Good):**
"UUIDs avoid hotspots because they're generated client-side and distribute naturally across Cassandra nodes. Auto-incrementing IDs would create a sequential range that might all hash to the same partition initially, causing uneven load."

**Expected Answer - Level 2 (Better):**
"UUIDs (specifically UUID v4 random) generate large 128-bit random values that distribute uniformly across the token ring using Cassandra's Murmur3 hash function. This prevents partition hotspots. Auto-incrementing IDs generate sequential values (1, 2, 3...) that, despite hashing, create temporal clustering because newly created patients all go to the same token range until sufficient data accumulates. This is the 'sequential ID bottleneck' problem."

**Expected Answer - Level 3 (Expert):**
"UUIDs provide natural load distribution across all nodes in a cluster regardless of write rate. Each node can independently generate UUIDs with near-zero collision probability (2^122 possibilities for v4 random UUIDs). Auto-incrementing IDs create a temporal write hotspot—all current writes target the node owning the token range [n, n+1). As the ID counter advances, writes move to adjacent nodes, but for any given moment, only one node handles the 'hot' partition. This violates Cassandra's primary advantage: parallel write scaling. In a 10-node cluster, you get 1/10th throughput with auto-incrementing IDs vs true parallel distribution with UUIDs. For a hospital system, this means patient registration throughput is limited to a single node's capacity (~50K writes/sec) rather than the full cluster capacity."

**Follow-up Probe Questions:**
1. "Could we use `TimeUUID` instead of random UUIDs? What are the trade-offs?" 
   - Expected: TimeUUID adds temporal ordering (earlier timestamps have smaller byte values), enabling range queries on ID generation time alone. Trade-off: slight non-uniformity in token distribution (timestamps are somewhat ordered), but acceptable for observability.

2. "What happens if patient registration must use sequential IDs for regulatory compliance?"
   - Expected: Use UUID internally in Cassandra for distribution, generate sequential IDs in application layer, store mapping table (`uuid_to_sequence` or `sequence_to_uuid`). Acknowledge the lookup overhead and potential consistency lag.

3. "How would UUID collision be detected and handled?"
   - Expected: Cassandra treats collisions as updates (same partition key = overwrite). For UUID v4 (139 trillion UUIDs per millisecond globally), collision is astronomical. If detected (duplicate patient entry), application must validate business logic or use conditional updates (Lightweight Transactions).

### Interview Question 2: Clustering Key Ordering and Query Optimization

**Question:**
"Explain the design choice: `CLUSTERING ORDER BY (event_timestamp DESC, event_id ASC)`. What queries does this optimize? What queries become inefficient? How does this decision affect write performance?"

**Expected Answer - Level 1 (Good):**
"DESC on event_timestamp means newest events are stored first within each patient's partition. This optimizes the 'recent events' query. event_id ASC breaks ties deterministically. It makes 'get oldest events' inefficient because you'd need to skip all recent events first."

**Expected Answer - Level 2 (Better):**
"Cassandra stores clustering columns in sort order within each partition. `DESC` on event_timestamp means the clustering key bytes are physically ordered newest-to-oldest on disk. This enables:
- **Efficient queries:** `SELECT * FROM medical_events WHERE patient_id = ? ORDER BY event_timestamp DESC LIMIT 10` (touches only first 10 rows on disk)
- **Inefficient queries:** `ORDER BY event_timestamp ASC` requires reverse scan or full partition read then sort in memory
- **event_id ASC:** Breaking timestamp ties consistently (e.g., two events with same millisecond timestamp) ensures deterministic ordering, important for pagination using `event_id > ?` in subsequent requests
- **Write performance:** No impact. Clustering order is storage layout only; Cassandra sorts on write regardless. DESC vs ASC are equivalent performance-wise at write time."

**Expected Answer - Level 3 (Expert):**
"The clustering order selection reflects the application's dominant access pattern: 'get recent N events for patient.' Within each partition (patient_id), event_timestamp DESC physically positions recent events at partition start on disk. Cassandra's internal index skip-list structure means querying `LIMIT 10 DESC` accesses ~10-20 rows; querying `LIMIT 10 ASC` (if we'd chosen ASC) would access ~partition_size-10 rows backward, much slower.

**Write-side impact:** Clustering order doesn't affect write speed (SSTable already stores in clustered order, DESC is byte-inverted before storage). However, long partition lifespans can cause issue: a patient with 1M events and frequent reads of 'last 10 events' causes cache pressure because those rows are far from recently-written rows. Solutions: (1) bucket timestamp into ranges (shard by date), (2) use Time Window Compaction Strategy for older partitions, (3) separate hot vs cold data into different tables.

**Consequence:** `ORDER BY event_timestamp ASC` becomes expensive—Cassandra must reverse-scan or read-all-then-sort. For 'give me oldest 10 events,' this partition could be scanned backward (acceptable) or pulled fully and sorted in-memory (unacceptable for large partitions). The application instead provides 'events since date X' queries leveraging the clustering key range: `event_timestamp >= ? AND event_timestamp <= ?` in forward or reverse direction."

**Follow-up Probe Questions:**
1. "How would you handle a query like 'all events of type LAB_RESULT, most recent first'?"
   - Expected: Can't filter efficiently on event_type alone without ALLOW FILTERING or secondary index. Solutions: (1) materialized view `lab_results_by_patient_time` with same clustering, (2) duplicate table with event_type as partition key, (3) search engine (Elasticsearch). Pure Cassandra: secondary index on event_type then filter by timestamp (inefficient for large result sets).

2. "What if business logic required oldest events first by default?"
   - Expected: Flip to `CLUSTERING ORDER BY (event_timestamp ASC, event_id DESC)`. Trade-off: reverses the optimization—now 'recent events' queries are expensive, older ones cheap. Benchmark the dominant query pattern and optimize for it.

3. "Can you change clustering order on a live table?"
   - Expected: No. Clustering order is immutable in Cassandra. Changing requires: (1) create new table with new order, (2) migrate data with bulk write tool, (3) cutover application, (4) drop old table. Downtime required if synchronous, complex if online migration needed.

### Interview Question 3: Consistency Levels and CAP Trade-offs

**Question:**
"This application uses `LOCAL_ONE` consistency for all reads and writes. In a production multi-node cluster with replication factor 3, how would you justify this choice or argue for stronger consistency? What are the RPO/RTO implications?"

**Expected Answer - Level 1 (Good):**
"LOCAL_ONE means 'wait for 1 replica in the local DC to respond.' It's fast and tolerates 2 node failures (RF=3). For a single-DC setup, it's reasonable. For multi-DC, LOCAL_QUORUM might be better to guarantee the local copy is current. QUORUM ensures strong consistency but is slower."

**Expected Answer - Level 2 (Better):**
"LOCAL_ONE reads: return data from any 1 local replica immediately, no cross-node coordination. Write succeeds after 1 local replica ACK.
- **Advantages:** Single round-trip latency (~1-2ms), high throughput, tolerates 2 node failures
- **Trade-off:** If multiple writes occur for same patient ID simultaneously across nodes, one write might be seen as 'lost' (really, just delayed). Read-repair eventual consistency applies.
- **RPO (Recovery Point Objective):** In single DC, essentially zero—replication is synchronous, data on 3 nodes
- **RTO (Recovery Time Objective):** ~seconds—if node fails, read-repair pulls from replicas on next request

**For patient data:** LOW consistency is acceptable. Patient *registration* (name, DOB) changes infrequently. Medical *events* (labs, diagnoses) are append-only, so eventual consistency is natural.

**For sensitive queries:** Cardiac event alert (high priority) might need LOCAL_QUORUM: 'confirmed by 2 nodes' feels more authoritative than 1."

**Expected Answer - Level 3 (Expert):**
"Consistency level is a tunable CAP trade-off:
- **LOCAL_ONE:** C=1 (minimum), A=high, P=strong (all replicas see write eventually). Best throughput. Acceptable where data loss isn't catastrophic.
- **LOCAL_QUORUM (RF=3):** C=2 (majority), A=medium, P=strong. Balances consistency and latency.
- **QUORUM (RF=3):** C=2 across all DCs, A=low, P=very strong. Heavyweight.

**For hospital_system:**

*Patient registration (infrequent write):* Use LOCAL_ONE. Patient created once, name/DOB rarely change. If John Smith and John Smith Jr. registers in different DCs simultaneously with same UUID (collision, basically impossible), the 'first' in Cassandra timestamp wins. Application must validate. Cost benefit: register endpoint latency 5ms (LOCAL_QUORUM) vs 2ms (LOCAL_ONE).

*Medical events (append-only, frequent):* Use LOCAL_ONE. Each event is immutable once written. Temporal ordering on `event_timestamp` is application-aware (hospital's clock, not Cassandra's cluster time). Two labs at timestamp 14:30:00 are assumed different events (event_id breaks ties). No correctness issue with eventual consistency.

*Critical events (life-threatening alerts):* Elevate to LOCAL_QUORUM. Alert read requires consensus confirmation ('seen by 2 nodes'). 1-2ms latency cost is acceptable for critical paths.

**RPO/RTO with RF=3, 1 DC:**
- RPO: ~0 (all writes replicated before ACK at LOCAL_ONE)
- RTO: ~1-5 seconds (failed node replaced, read-repair pulls replicas)

**Multi-DC scenario (2 DCs, RF=3 total):**
- If LOCAL_QUORUM, writes synchronously replicate within local DC only—remote DC replication is async (Hinted Handoff). In local DC failure, can failover to remote DC but might lose ~seconds of writes.
- If QUORUM, writes must succeed in both DCs—strong consistency but adds latency and cross-DC coordination complexity."

**Follow-up Probe Questions:**
1. "How would consistency level change for HIPAA compliance?"
   - Expected: HIPAA requires audit trails and data integrity verification. Stronger consistency (LOCAL_QUORUM or QUORUM) might be mandated. Also discuss lightweight transactions for conditional updates, ensuring no duplicate prescriptions.

2. "What happens if your local DC fails with LOCAL_ONE consistency?"
   - Expected: Cassandra can failover to remote DC but clients see 'unavailable' until configured for failover. If you connect to remote DC, reads might return stale data (last hinted handoff). Automatic DC failover requires application awareness of topology.

3. "How do you monitor consistency violations in production?"
   - Expected: Cassandra doesn't expose 'consistency violation' metrics directly. Indirect signals: (1) read-repair latency spikes, (2) digest mismatch counts, (3) application layer: detect stale patient records through business logic (e.g., patient deleted but reappeared). Implement application-level versioning or checksums.

### Interview Question 4: Spring Data Abstraction vs Raw CQL Trade-offs

**Question:**
"Compare the current approach (Spring Data Cassandra with repository interfaces and method derivation) vs writing raw CQL queries directly with `CqlTemplate` or `CqlSession`. When would you abandon the abstraction layer?"

**Expected Answer - Level 1 (Good):**
"Spring Data provides convenience: `repository.save(patient)` handles entity mapping, type conversion, and prepared statement management. Raw CQL gives control and performance. Spring Data is easier for CRUD; raw CQL is better for complex queries."

**Expected Answer - Level 2 (Better):**
"Spring Data Cassandra abstraction layers:
1. **Entity mapping:** `Patient` object ↔ Cassandra row codec
2. **Type conversion:** LocalDate ↔ Cassandra DATE, UUID ↔ UUID type, List ↔ COLLECTION
3. **Prepared statement caching:** Spring caches prepared statements by method signature
4. **Batch operations:** `saveAll()` issues multiple INSERT statements (not atomic batch)

When to use Spring Data:
- Standard CRUD (save, find by ID, delete)
- Method derivation queries (`findByPatientIdAndEventTimestampBetween`)
- Minimal latency penalty (1-2ms overhead per query)

When to use raw CQL:
- **Batch writes with atomicity:** Cassandra BATCH keyword for conditional multiple writes (e.g., insert patient + insert admin record atomically or neither)
- **Lightweight transactions (LWT):** `IF NOT EXISTS` for idempotent operations
- **Prepared statement reuse:** Raw `CqlSession.prepare()` and bind same statement 1000s of times (avoids Spring's per-method caching)
- **Streaming large result sets:** Raw CQL's `AsyncResultSet` with manual pagination, Spring Data loads all rows in memory"

**Expected Answer - Level 3 (Expert):**
"Spring Data's abstraction is optimized for typical CRUD, but introduces indirection:

**Performance cost breakdown:**
1. **Reflection overhead:** Spring inspects entity class, finds fields, matches to Cassandra columns (~100µs per query)
2. **Codec registry lookup:** Spring searches codec cache for `LocalDate` → `DATE` conversion (~10µs)
3. **Type conversion:** Actual conversion execution (~1µs)
4. **Prepared statement lifecycle:** Spring caches per method name, but initial cache miss requires compilation (~50ms first time, then ~0ms)
5. **Total overhead:** ~100µs per query (compared to hand-written CQL)

**For hospital_system volumes (patient ops ~100/sec, events ~1000/sec):**
- Spring Data: 100 patients/sec * 100µs = 10ms/sec overhead—negligible
- 1000 events/sec * 100µs = 100ms/sec overhead—negligible (Cassandra throughput is ~50ms per write, overhead is 0.2%)

**When to bypass Spring Data:**
1. **Bulk inserts (>10K rows):** Use raw CQL BATCH or async streaming
   ```cql
   BEGIN BATCH
   INSERT INTO patients (...) VALUES (...)
   INSERT INTO patients (...) VALUES (...)
   ...
   APPLY BATCH
   ```
   Spring's `saveAll()` issues individual INSERT statements sequentially.

2. **Lightweight transactions:** Cassandra 4.1 supports LWT for exactly-once semantics
   ```cql
   INSERT INTO patients (...) VALUES (...) IF NOT EXISTS
   ```
   Spring Data doesn't expose IF conditions directly; must use `@Query` annotation.

3. **Streaming result sets:** 10M events query
   ```java
   CqlSession session = ...;
   ResultSet rs = session.execute("SELECT * FROM medical_events WHERE patient_id = ?");
   rs.forEach(row -> processEvent(row)); // handles paging automatically
   ```
   Spring Data's `findAll()` loads all rows into memory, causes OOM on large result sets.

4. **Prepared statement reuse:** Hand-tuned prepared statements
   ```java
   PreparedStatement stmt = session.prepare("SELECT * FROM medical_events WHERE patient_id = ? AND event_timestamp < ?");
   for (UUID patientId : patients) {
     BoundStatement bound = stmt.bind(patientId, cutoffTime);
     session.execute(bound);  // reuses compiled statement
   }
   ```

**N+1 Query Problem:**
Spring Data doesn't inherently prevent N+1 (e.g., load all patients, then for each patient load their events). Must structure queries carefully:
- Use custom `@Query` with JOINs (inefficient in Cassandra, denormalize instead)
- Or accept N+1 and cache (e.g., load all patients' recent events in single query with composite key)

**Debug challenges:**
Spring abstraction hides the actual CQL generated. No visibility into prepared statement binding. When queries fail, Spring's stack trace is 10 frames deep before reaching the actual error. Raw CQL provides direct error origin."

**Follow-up Probe Questions:**
1. "How would you implement 'upsert with conditional logic' (e.g., only update patient name if patient version hasn't changed)?"
   - Expected: Use raw `@Query` with LWT: `INSERT INTO patients (...) VALUES (...) IF patient_version = ?`

2. "Spring Data doesn't support Cassandra BATCH atomicity. How would you redesign if you need atomic multi-partition writes?"
   - Expected: Restructure data model to avoid multi-partition writes, or move logic to application layer (e.g., insert patient, then emit event to Kafka that updates another table asynchronously). Cassandra doesn't support distributed transactions.

3. "How would you profile which queries are slow in production?"
   - Expected: Cassandra query logs, Spring Data's `@EnableSpringDataWebSupport` with metrics, or DataStax Insights for driver-level stats. Raw CQL allows `System.nanoTime()` measurement; Spring Data abstracts this.

### Interview Question 5: Distributed System Patterns and Scaling Implications

**Question:**
"If this hospital system scaled to 10M patients and 10B medical events across 3 geographic regions (US, EU, APAC), each with its own Cassandra cluster, how would the application architecture change? What new problems emerge?"

**Expected Answer - Level 1 (Good):**
"We'd need multi-DC replication. Data is replicated across regions. Queries might need to route to the nearest DC. Consistency becomes harder—writes to one DC must replicate to others, causing lag."

**Expected Answer - Level 2 (Better):**
"Multi-DC architecture (3 nodes per region, RF=3 total across regions typically):
- **Write:** Local DC (US) receives write, replicates to 2 other DCs asynchronously (Hinted Handoff). Eventually consistent (RTO ~seconds to minutes)
- **Read:** Route to nearest DC (lower latency), might see stale data
- **Partition strategy:** Still partitioned by patient_id. Patient ID hashing distributes globally (uniform distribution across all 9 nodes)

**Problems:**
1. **Cross-region latency:** US-EU round trip ~150ms. Write to EU and read from US might see stale data
2. **Partition hot-spot:** Celebrity patient (1B events) creates uneven load across 9 nodes
3. **Replication lag:** Write to US takes ~150ms to replicate to EU (network round trip), consistency window is open
4. **Read-repair cross-region:** Cheap reads (LOCAL_ONE) miss updates from other regions temporarily

**Solutions:**
- Use LOCAL_QUORUM: ensures data is in 2 nodes in same DC (but replication to other DCs still async)
- Implement read-your-write guarantees in application (local cache or version checking)
- Use DataStax Change Data Capture (CDC) to stream updates for real-time sync to other DCs"

**Expected Answer - Level 3 (Expert):**
"At 10B events scale, single patient partitions can reach multi-GB sizes. Cassandra's partition size limit (~500MB–2GB typically, configurable) means we must redesign:

**Option 1: Shard by time bucket**
```cql
CREATE TABLE medical_events_v2 (
  patient_id UUID,
  event_date DATE,  -- shard key: e.g., 2026-01-01
  event_timestamp TIMESTAMP,
  event_id UUID,
  ...
  PRIMARY KEY ((patient_id, event_date), event_timestamp DESC, event_id)
)
```
Now queries must specify `event_date`: `SELECT * FROM medical_events_v2 WHERE patient_id = ? AND event_date = 2026-01-01`
Old query (all events for patient) becomes: iterate across all dates, issue 365 queries (one per day). Cost: 365x more requests. Benefit: partitions stay <500MB.

**Option 2: Separate hot-cold data**
- `medical_events_hot`: (patient_id, event_timestamp) with RF=3, higher compaction
- `medical_events_archive`: events >90 days old, RF=1, lower cost

Queries check both tables or rely on application logic ('first check hot, if not found check archive').

**Multi-region replication:**
- **NetworkTopologyStrategy:** RF=3 per region (RF=9 total, 3 in each region)
- **Region affinity:** Application sends read/write to nearest region's replica set
- **Cross-region replication:** CDC stream (`cdc_log` table) captures writes, Kafka sink replicates to other regions asynchronously
- **Consistency model:** LOCAL_QUORUM reads (2/3 local nodes), writes succeed in local region first, async to others

**Write latency with multi-region:**
- Write to patient in US: ~1-2ms (LOCAL_QUORUM in US DC)
- Hinted Handoff propagates to EU: ~150ms (async)
- EU reads: see write after ~150ms window
- If patient immediately transfers to EU hospital and queries EU DC, might see pre-transfer data for ~150ms

**Solutions to RTO window:**
1. **Application-level read-your-write:** After write, cache result locally, serve from cache for N seconds
2. **Stronger consistency:** Upgrade to QUORUM (wait for 50% of global replicas), adds ~150ms latency
3. **Event sourcing:** All events go to immutable log (Kafka), processed asynchronously. Each DC materializes its view. Guarantees eventual consistency but decouples read/write latency

**Cassandra limitations at scale:**
- **Digest query:** Reads compare replicas (digest mismatch = read repair), cross-region digests suffer high latency
- **Tombstone accumulation:** Deleted events create tombstones that persist for `gc_grace_seconds` (~10 days). At 10B events with 0.1% deletion rate, millions of tombstones must be scanned
- **Compaction overhead:** 9 nodes, 10B events each, compaction becomes 24/7 operation, impacts read latency

**Remediation:**
- Move to time-bucketed model (daily partitions, monthly compaction windows)
- Archive older data to S3/cloud storage, Cassandra becomes 'hot tier' only
- Implement secondary index on separate cluster (Elasticsearch) for complex queries
- Use Cassandra as 'write buffer,' stream events to data warehouse for analytics"

**Follow-up Probe Questions:**
1. "How do you handle a patient transferring from US region to EU? How does the system ensure no data loss?"
   - Expected: Implement patient migration procedure: (1) disable writes to old region, (2) verify replication complete, (3) update patient location metadata (separate table), (4) resume writes to new region. Manual process; Cassandra has no 'move partition' command.

2. "What happens if US region loses internet connection to EU for 1 hour?"
   - Expected: Hinted Handoff buffers writes locally for the DC that's unreachable. When connection restores, hints are replayed. If hints buffer exceeds disk space (~50GB per node typically), old hints are dropped—permanent data loss. Solutions: increase hints directory disk, or reduce replication factor in unreachable region (accept loss).

3. "How would you implement geo-fencing (e.g., GDPR requires patient data in EU stay in EU only)?"
   - Expected: Requires partitioning by region and legal entity. Create separate keyspace per region (`hospital_system_eu`, `hospital_system_us`). Application logic enforces routing. Cassandra's token-based distribution can't enforce geographic boundaries natively.

### Interview Question 6: Query Efficiency, Materialized Views, and ALLOW FILTERING

**Question:**
"The GET /api/patients/{id}/events?days=30 endpoint uses filtering on the clustering key range. The endpoint works without ALLOW FILTERING, but a business requirement asks for: 'all Lab Results for patient within last 30 days.' Why does this require ALLOW FILTERING? Is it acceptable? How would you redesign?"

**Expected Answer - Level 1 (Good):**
"ALLOW FILTERING searches all events in the partition for matching event_type='LAB_RESULT'. Without it, Cassandra rejects the query because it can't efficiently filter without scanning everything. Acceptable for small partitions, bad for large ones."

**Expected Answer - Level 2 (Better):**
"Cassandra's query restriction: filters on non-key columns require ALLOW FILTERING
```cql
-- This works (filters on clustering key):
SELECT * FROM medical_events WHERE patient_id = ? AND event_timestamp > ?

-- This requires ALLOW FILTERING (filters on event_type, not a key column):
SELECT * FROM medical_events WHERE patient_id = ? AND event_type = 'LAB_RESULT'

-- Why? Clustering key (event_timestamp) is ordered on disk. Cassandra can binary search to 'first matching timestamp' then scan forward. But event_type is not ordered; Cassandra must scan ALL rows in partition to find matches.

**ALLOW FILTERING cost:**
- Small partition (100 events): scan 100 rows, return 10 labs—acceptable overhead
- Large partition (1M events, celebrity patient): scan 1M rows, return 100 labs—catastrophic (could take seconds)

**Acceptable:** For partitions expected <10K rows. For patients with high event volume, unacceptable.

**Redesign options:**
1. **Materialized View:**
   ```cql
   CREATE MATERIALIZED VIEW labs_by_patient_time AS
   SELECT * FROM medical_events 
   WHERE event_type = 'LAB_RESULT' AND patient_id IS NOT NULL AND event_timestamp IS NOT NULL
   PRIMARY KEY ((patient_id, event_type), event_timestamp DESC)
   ```
   Query: `SELECT * FROM labs_by_patient_time WHERE patient_id = ? AND event_timestamp > ? AND event_type = 'LAB_RESULT'`
   Cassandra maintains this view incrementally. Each write to medical_events automatically updates labs_by_patient_time. Query is efficient (no ALLOW FILTERING).

2. **Separate table:**
   ```cql
   CREATE TABLE medical_events_by_type (
     patient_id UUID,
     event_type TEXT,
     event_timestamp TIMESTAMP,
     event_id UUID,
     ...
     PRIMARY KEY ((patient_id, event_type), event_timestamp DESC, event_id)
   )
   ```
   Application inserts into both tables. Query is efficient but write complexity doubles.

3. **Search engine (secondary index):**
   Use Elasticsearch/Solr for complex queries. Medical_events table remains canonical, ES index is derived (eventual consistency acceptable)."

**Expected Answer - Level 3 (Expert):**
"ALLOW FILTERING is a performance anti-pattern in Cassandra because it violates the partition-first query model:

**Cassandra's query execution:**
1. Hash partition key → find node(s) containing partition
2. Read partition from disk
3. Apply clustering key range filter (binary search + forward scan)
4. Apply WHERE filter on non-keys (requires full partition scan)
5. Apply LIMIT

Step 4 is O(partition_size), not O(result_size). For patient with 1M events, querying 'LAB_RESULT' in last 30 days:
- Step 2: Read partition (1M rows, ~500MB) from disk into memory—~100ms
- Step 4: Scan 1M rows for event_type='LAB_RESULT'—~50ms (memory-bound)
- Result: 100 matching lab results returned in ~150ms

Compared to clustering-key-only query (10 rows, ~1ms), ALLOW FILTERING adds 100x latency.

**Real-world consequences:**
- Cassandra server-side: query goes from sub-millisecond to 100ms+, blocking coordinator node
- Cassandra driver: holding read window open 100ms, read-repair chances increase
- Application: 100ms per query * 1000 concurrent requests = 100 second response time (timeout failure)

**Materialized view trade-offs:**
- **Advantage:** Query efficient (no ALLOW FILTERING), read-only view automatically kept in sync
- **Disadvantage:** Write amplification. Insert medical_events → Cassandra writes to both medical_events AND labs_by_patient_time. If medical_events has 10 columns and we create 5 MV's (by type, by severity, by doctor, by symptom, by department), write becomes 6x slower. MV also consumes 6x disk space.
- **Limitation:** MV must include all primary key columns to be queryable. Can't filter on arbitrary columns post-query (no application-level post-filtering in view definition).

**Separate table approach:**
- Application explicitly inserts into both medical_events and medical_events_by_type
- Write cost: 2 INSERT statements per event
- Benefit: Full control over indexing strategy
- Danger: Consistency between tables. If second insert fails, orphaned data in first table (no distributed transactions in Cassandra)

**Search engine approach (recommended for complex queries):**
- Elasticsearch indexes all events with full-text search, faceting, complex filters
- Cassandra remains source of truth (no transactional consistency between ES and Cassandra)
- ES queries offloaded from Cassandra, reducing cluster load
- Trade-off: eventual consistency (event takes ~1 second to appear in ES after insert), dual-write complexity, operational overhead (maintain 2 systems)"

**Follow-up Probe Questions:**
1. "If you chose materialized views for lab results, what happens when you need to query by both event_type and severity?"
   - Expected: Create another MV `medical_events_by_type_severity` with PRIMARY KEY ((patient_id, event_type, severity), event_timestamp DESC). Now you have 2 MVs, each mirroring the source table. Write amplification: 3x (original + 2 MVs).

2. "How would you handle a query like 'all events from January 2025 that are LAB_RESULT type' without ALLOW FILTERING?"
   - Expected: Separate table with composite partition key: `((patient_id, year_month), event_type, event_timestamp DESC)`. Or store year_month in application layer as part of query: `SELECT * FROM medical_events_by_year_month WHERE patient_id = ? AND year_month = '2025-01'` + application filters event_type='LAB_RESULT'. Cassandra doesn't have efficient cross-partition filtering.

3. "What's the impact of 5 materialized views on a Cassandra cluster with 10B events?"
   - Expected: 6x write latency (source + 5 MV updates), 6x disk usage, 6x compaction workload. On 5-node cluster, requires 5*6=30 core-hours of compaction daily. Likely unsustainable. Better solution: application-level Kafka stream that writes to Elasticsearch, decoupling write path from complex indexing.

---

## Summary: Key Takeaways for Senior Engineers

1. **UUID over sequential IDs:** Client-side generated UUIDs distribute uniformly, enabling linear cluster scaling. Sequential IDs create temporal hotspots.

2. **Clustering key ordering:** Matches dominant query pattern (DESC for recent-first queries). Immutable once defined; changing requires table migration.

3. **Consistency level tuning:** LOCAL_ONE acceptable for eventual consistency patterns (patient registration, append-only events). LOCAL_QUORUM for critical operations. QUORUM for cross-DC strong consistency, at latency cost.

4. **Spring Data abstraction:** Appropriate for CRUD. Raw CQL required for batch atomicity, LWT, and large result streaming. Monitor N+1 query patterns.

5. **Multi-region scaling:** Shard by time bucket to prevent partition bloat. Use CDC for cross-region replication. Accept eventual consistency or implement application-level read-your-write guarantees.

6. **Complex queries:** ALLOW FILTERING works for small partitions only. For production queries, redesign with materialized views, separate tables, or search engines.

---

**Next: Deploying for Production**
- Cassandra cluster tuning (heap, GC, compaction)
- Monitoring (Prometheus, DataStax Insights)
- Backup/restore strategy
- Disaster recovery planning

---

**Document Last Updated:** January 11, 2026
**Application Version:** 1.0.0
