# Hospital Cassandra System - Setup Guide

## Complete Setup Instructions (Start to End)

### Prerequisites

Before starting, ensure you have the following installed:

1. **Java Development Kit (JDK) 21**
   - Download: https://www.oracle.com/java/technologies/downloads/#java21
   - Verify installation: `java -version`
   - Should output: `java version "21.0.x"`

2. **Apache Maven 3.8+**
   - Download: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`
   - Should output Maven version and Java version

3. **Docker Desktop**
   - Download: https://www.docker.com/products/docker-desktop
   - Verify installation: `docker --version` and `docker ps`
   - Ensure Docker daemon is running

4. **IDE (Recommended: VS Code or IntelliJ IDEA)**
   - VS Code: Install "Extension Pack for Java" and "Spring Boot Extension Pack"
   - IntelliJ: Ultimate Edition recommended for full Spring Boot support

---

## Step 1: Clone/Download the Project

```powershell
# Navigate to your workspace
cd C:\Users\<your-username>\projects\Spring-Projects

# If cloning from repository (example)
git clone <repository-url> nosql/hospital-cassandra

# Or if extracting from archive
# Extract to: C:\Users\<your-username>\projects\Spring-Projects\nosql\hospital-cassandra
```

---

## Step 2: Start Cassandra Database

### 2.1 Navigate to Project Directory

```powershell
cd C:\Users\<your-username>\projects\Spring-Projects\nosql\hospital-cassandra
```

### 2.2 Start Cassandra Container using Docker Compose

```powershell
docker-compose up -d
```

**Expected Output:**
```
Creating network "hospital-cassandra_default" with the default driver
Creating hospital-cassandra ... done
```

### 2.3 Verify Cassandra is Running

```powershell
docker ps
```

**Expected Output:**
```
CONTAINER ID   IMAGE          COMMAND                  STATUS         PORTS                    NAMES
<container-id> cassandra:4.1  "docker-entrypoint.s…"   Up 30 seconds  0.0.0.0:9042->9042/tcp   hospital-cassandra
```

### 2.4 Wait for Cassandra to Initialize (Important!)

Cassandra takes 30-60 seconds to fully initialize. Check logs:

```powershell
docker logs hospital-cassandra
```

Look for this line indicating Cassandra is ready:
```
INFO  [main] ... Startup complete
```

---

## Step 3: Create Cassandra Keyspace and Tables

### 3.1 Verify Keyspace Exists

```powershell
docker exec hospital-cassandra cqlsh -e "DESCRIBE KEYSPACES;"
```

If `hospital_system` is **not** in the list, create it:

```powershell
docker exec hospital-cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS hospital_system WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
```

### 3.2 Create Tables

The project includes a schema.cql file. Execute it:

```powershell
# Copy schema to container
docker cp schema.cql hospital-cassandra:/tmp/schema.cql

# Execute schema
docker exec hospital-cassandra cqlsh -f /tmp/schema.cql
```

### 3.3 Verify Tables Created

```powershell
docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLES;"
```

**Expected Output:**
```
medical_events  patients
```

### 3.4 Inspect Table Schema (Optional)

```powershell
docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLE patients;"
docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLE medical_events;"
```

---

## Step 4: Build the Application

### 4.1 Clean and Package

```powershell
# From project root directory
cd C:\Users\<your-username>\projects\Spring-Projects\nosql\hospital-cassandra

mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

### 4.2 Verify JAR File Created

```powershell
dir target\*.jar
```

**Expected Output:**
```
hospital-cassandra-1.0.0.jar
```

---

## Step 5: Run the Application

### Option A: Run from IDE (Recommended for Development)

#### Using VS Code:
1. Open the project folder in VS Code
2. Open `HospitalApplication.java` (located in `src/main/java/com/healthcare/hospital/`)
3. Press **F5** or click "Run and Debug" → "Debug (Launch) - HospitalApplication"
4. Wait for "Started HospitalApplication in X seconds" message in console

#### Using IntelliJ IDEA:
1. Open the project in IntelliJ
2. Right-click on `HospitalApplication.java`
3. Select "Run 'HospitalApplication.main()'"
4. Wait for "Started HospitalApplication in X seconds" message in Run console

### Option B: Run from Command Line (JAR)

```powershell
java -jar target\hospital-cassandra-1.0.0.jar
```

### 5.1 Verify Application Started Successfully

Look for these log messages in the console:

```
INFO --- [main] c.h.hospital.HospitalApplication     : Started HospitalApplication in 3.857 seconds
INFO --- [main] c.h.h.c.CassandraSessionInitializer  : ✓ Cassandra session initialized successfully
INFO --- [main] c.h.h.c.CassandraSessionInitializer  :   - Connected nodes: 1
INFO --- [main] c.h.h.c.CassandraSessionInitializer  :   - Keyspace: hospital_system
INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port 8080 (http)
```

---

## Step 6: Access Swagger UI and Test Endpoints

### 6.1 Open Swagger UI in Browser

Navigate to: **http://localhost:8080/swagger-ui.html**

You should see the Swagger documentation interface with all API endpoints.

### 6.2 Test the Application (Complete Workflow)

#### Test 1: Create a Patient (POST /api/patients)

1. In Swagger UI, expand **POST /api/patients**
2. Click "Try it out"
3. Replace the example JSON with:

```json
{
  "patientName": "John Doe",
  "dateOfBirth": "1990-05-15",
  "bloodType": "O+",
  "email": "john.doe@hospital.com"
}
```

4. Click "Execute"
5. **Expected Response: 201 Created**

```json
{
  "patientId": "3b4264f5-c6e5-45fd-afda-1c1318f2c207",
  "patientName": "John Doe",
  "dateOfBirth": "1990-05-15",
  "bloodType": "O+",
  "email": "john.doe@hospital.com"
}
```

6. **Copy the `patientId` UUID** - you'll need it for subsequent tests

#### Test 2: Retrieve Patient (GET /api/patients/{patientId})

1. In Swagger UI, expand **GET /api/patients/{patientId}**
2. Click "Try it out"
3. Paste the UUID from Test 1 into the `patientId` field
4. Click "Execute"
5. **Expected Response: 200 OK** with the patient details

#### Test 3: Add Medical Event (POST /api/patients/{patientId}/events)

1. In Swagger UI, expand **POST /api/patients/{patientId}/events**
2. Click "Try it out"
3. Paste the patient UUID into `patientId` field
4. Replace the request body with:

```json
{
  "eventType": "LAB_RESULT",
  "description": "COVID-19 PCR Test - Negative",
  "diagnosisCodes": ["B97.29"],
  "createdBy": "Dr. Sarah Smith"
}
```

5. Click "Execute"
6. **Expected Response: 201 Created**

```json
{
  "patientId": "3b4264f5-c6e5-45fd-afda-1c1318f2c207",
  "eventTimestamp": "2026-01-11T15:45:30.123Z",
  "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "eventType": "LAB_RESULT",
  "description": "COVID-19 PCR Test - Negative",
  "diagnosisCodes": ["B97.29"],
  "createdBy": "Dr. Sarah Smith",
  "urgent": false
}
```

#### Test 4: Add More Events (to demonstrate time-series ordering)

Repeat Test 3 with different data:

**Event 2:**
```json
{
  "eventType": "ADMISSION",
  "description": "Emergency admission - chest pain",
  "diagnosisCodes": ["R07.9"],
  "createdBy": "Dr. Michael Chen"
}
```

**Event 3:**
```json
{
  "eventType": "DISCHARGE",
  "description": "Patient discharged - stable condition",
  "diagnosisCodes": [],
  "createdBy": "Dr. Sarah Smith"
}
```

#### Test 5: Query Events (GET /api/patients/{patientId}/events)

1. In Swagger UI, expand **GET /api/patients/{patientId}/events**
2. Click "Try it out"
3. Paste the patient UUID
4. Set `days` parameter to `30` (retrieves events from last 30 days)
5. Click "Execute"
6. **Expected Response: 200 OK** with array of events **ordered newest-first** (DESC by timestamp)

```json
[
  {
    "patientId": "3b4264f5-...",
    "eventTimestamp": "2026-01-11T15:47:00.000Z",
    "eventType": "DISCHARGE",
    ...
  },
  {
    "patientId": "3b4264f5-...",
    "eventTimestamp": "2026-01-11T15:46:00.000Z",
    "eventType": "ADMISSION",
    ...
  },
  {
    "patientId": "3b4264f5-...",
    "eventTimestamp": "2026-01-11T15:45:00.000Z",
    "eventType": "LAB_RESULT",
    ...
  }
]
```

---

## Step 7: Verify Data in Cassandra

### 7.1 Query Patients Table

```powershell
docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT * FROM patients;"
```

**Expected Output:**
```
 patientid                            | blood_type | date_of_birth | email                    | patient_name
--------------------------------------+------------+---------------+--------------------------+--------------
 3b4264f5-c6e5-45fd-afda-1c1318f2c207 |        O+  |    1990-05-15 | john.doe@hospital.com    | John Doe

(1 rows)
```

### 7.2 Query Medical Events Table

```powershell
docker exec hospital-cassandra cqlsh -e "USE hospital_system; SELECT patient_id, event_timestamp, event_type, description FROM medical_events WHERE patient_id = 3b4264f5-c6e5-45fd-afda-1c1318f2c207;"
```

**Note:** Replace the UUID with your actual patient ID.

**Expected Output** (showing DESC ordering by timestamp):
```
 patient_id                           | event_timestamp                 | event_type  | description
--------------------------------------+---------------------------------+-------------+---------------------------
 3b4264f5-c6e5-45fd-afda-1c1318f2c207 | 2026-01-11 15:47:00.000000+0000 | DISCHARGE   | Patient discharged...
 3b4264f5-c6e5-45fd-afda-1c1318f2c207 | 2026-01-11 15:46:00.000000+0000 | ADMISSION   | Emergency admission...
 3b4264f5-c6e5-45fd-afda-1c1318f2c207 | 2026-01-11 15:45:00.000000+0000 | LAB_RESULT  | COVID-19 PCR Test...

(3 rows)
```

---

## Step 8: Alternative Testing with curl

If you prefer command-line testing:

### Create Patient:
```powershell
curl -X POST http://localhost:8080/api/patients `
  -H "Content-Type: application/json" `
  -d '{\"patientName\":\"Jane Smith\",\"dateOfBirth\":\"1985-03-20\",\"bloodType\":\"A+\",\"email\":\"jane.smith@hospital.com\"}'
```

### Get Patient:
```powershell
curl http://localhost:8080/api/patients/<patient-uuid>
```

### Add Event:
```powershell
curl -X POST http://localhost:8080/api/patients/<patient-uuid>/events `
  -H "Content-Type: application/json" `
  -d '{\"eventType\":\"LAB_RESULT\",\"description\":\"Blood work completed\",\"diagnosisCodes\":[],\"createdBy\":\"Dr. Jones\"}'
```

### Query Events:
```powershell
curl "http://localhost:8080/api/patients/<patient-uuid>/events?days=30"
```

---

## Troubleshooting Common Issues

### Issue 1: "Connection refused" or "Unable to connect to Cassandra"

**Symptoms:**
```
ERROR: All host(s) tried for query failed
```

**Solution:**
1. Verify Cassandra container is running: `docker ps`
2. Check Cassandra logs: `docker logs hospital-cassandra`
3. Wait 30-60 seconds after starting container
4. Restart container: `docker-compose restart`

---

### Issue 2: "local DC must be explicitly set"

**Symptoms:**
```
ERROR: Since you provided explicit contact points, the local DC must be explicitly set
```

**Solution:**
This is fixed in the current configuration via `CassandraConfig.java`. If you encounter this:
1. Verify `CassandraConfig.java` exists in `src/main/java/com/healthcare/hospital/config/`
2. Check `application.yml` has `local-datacenter: datacenter1`
3. Rebuild: `mvn clean package -DskipTests`

---

### Issue 3: "table patients does not exist"

**Symptoms:**
```
ERROR: Query; CQL [...]; table patients does not exist
```

**Solution:**
1. Verify keyspace exists: `docker exec hospital-cassandra cqlsh -e "DESCRIBE KEYSPACES;"`
2. Verify tables exist: `docker exec hospital-cassandra cqlsh -e "USE hospital_system; DESCRIBE TABLES;"`
3. If missing, run Step 3 again to create tables
4. Check schema.cql file exists in project root

---

### Issue 4: "Codec not found for requested operation: [DATE <-> java.lang.String]"

**Symptoms:**
```
ERROR: CodecNotFoundException: Codec not found for requested operation: [DATE <-> java.lang.String]
```

**Solution:**
This means Java entity type doesn't match Cassandra column type. Fixed in current version:
- `Patient.dateOfBirth` is `LocalDate` (Java) → `date` (Cassandra)
- If you see this, verify `Patient.java` imports `java.time.LocalDate`

---

### Issue 5: Application doesn't start (port already in use)

**Symptoms:**
```
ERROR: Port 8080 was already in use
```

**Solution:**
1. Stop other applications using port 8080
2. Or change port in `application.yml`:
   ```yaml
   server:
     port: 8081
   ```
3. Rebuild and restart

---

### Issue 6: Maven build fails

**Symptoms:**
```
[ERROR] Failed to execute goal
```

**Solution:**
1. Verify Java 21: `java -version`
2. Verify Maven 3.8+: `mvn -version`
3. Clean Maven cache: `mvn clean`
4. Delete `target` folder manually and rebuild
5. Check internet connection (Maven downloads dependencies)

---

## Stopping the Application

### Stop Spring Boot Application:
- **IDE:** Click the red "Stop" button or press `Ctrl+C` in the terminal
- **JAR:** Press `Ctrl+C` in the terminal running the JAR

### Stop Cassandra Container:
```powershell
docker-compose down
```

To stop but keep data:
```powershell
docker stop hospital-cassandra
```

To start again later:
```powershell
docker start hospital-cassandra
```

---

## Cleanup (Complete Removal)

### Remove Docker Container and Volumes:
```powershell
docker-compose down -v
```

**Warning:** This deletes all data in Cassandra.

### Remove Docker Image:
```powershell
docker rmi cassandra:4.1
```

### Delete Project:
```powershell
rm -r C:\Users\<your-username>\projects\Spring-Projects\nosql\hospital-cassandra
```

---

## Configuration Files Reference

### 1. `application.yml` (Spring Boot Configuration)
- Location: `src/main/resources/application.yml`
- Purpose: Configures Cassandra connection, logging, and Swagger
- Key settings:
  - `keyspace-name: hospital_system`
  - `contact-points: 127.0.0.1`
  - `port: 9042`
  - `local-datacenter: datacenter1`

### 2. `docker-compose.yml` (Cassandra Container)
- Location: Project root
- Purpose: Defines Cassandra 4.1 container configuration
- Ports: 9042 (Cassandra CQL)

### 3. `schema.cql` (Database Schema)
- Location: Project root
- Purpose: Creates tables in Cassandra
- Tables: `patients`, `medical_events`

### 4. `pom.xml` (Maven Dependencies)
- Location: Project root
- Purpose: Defines project dependencies and build configuration
- Key dependencies:
  - Spring Boot 3.3.0
  - Spring Data Cassandra
  - Springdoc OpenAPI (Swagger)

---

## Quick Start Checklist

- [ ] Java 21 installed
- [ ] Maven 3.8+ installed
- [ ] Docker Desktop installed and running
- [ ] Project downloaded/cloned
- [ ] Cassandra container started: `docker-compose up -d`
- [ ] Waited 60 seconds for Cassandra to initialize
- [ ] Tables created: `docker exec hospital-cassandra cqlsh -f /tmp/schema.cql`
- [ ] Application built: `mvn clean package -DskipTests`
- [ ] Application started (IDE or JAR)
- [ ] Swagger UI accessible: http://localhost:8080/swagger-ui.html
- [ ] POST /api/patients tested successfully
- [ ] GET /api/patients/{id} tested successfully
- [ ] POST /api/patients/{id}/events tested successfully
- [ ] GET /api/patients/{id}/events tested successfully

---

## Next Steps

Once setup is complete, proceed to **APPLICATION_WALKTHROUGH.md** for:
- Deep technical dive into application architecture
- Request flow analysis
- Debug points and troubleshooting techniques
- Senior Engineer interview questions and answers

---

## Support and Resources

- **Project Structure:** See `PROJECT_STRUCTURE.md`
- **Cassandra Documentation:** https://cassandra.apache.org/doc/latest/
- **Spring Data Cassandra:** https://docs.spring.io/spring-data/cassandra/docs/current/reference/html/
- **Docker Documentation:** https://docs.docker.com/

---

**Last Updated:** January 11, 2026
**Application Version:** 1.0.0
**Tested Environment:** Windows 11, Java 21, Maven 3.9, Docker Desktop 4.x
