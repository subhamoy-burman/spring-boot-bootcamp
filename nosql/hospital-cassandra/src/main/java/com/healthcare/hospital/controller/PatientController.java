package com.healthcare.hospital.controller;

import com.healthcare.hospital.domain.MedicalEvent;
import com.healthcare.hospital.domain.Patient;
import com.healthcare.hospital.repository.MedicalEventRepository;
import com.healthcare.hospital.repository.PatientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller demonstrating Cassandra operations through 4 minimal endpoints.
 * 
 * Endpoints:
 * 1. POST /api/patients              → Create a new patient (save operation)
 * 2. GET /api/patients/{id}          → Retrieve patient by ID (findById with partition key)
 * 3. GET /api/patients/{id}/events   → Get medical events for patient (query by partition key + range)
 * 4. POST /api/patients/{id}/events  → Add medical event for patient
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "APIs for managing patients and their medical events in Cassandra")
public class PatientController {
    
    private final PatientRepository patientRepository;
    private final MedicalEventRepository medicalEventRepository;
    
    /**
     * ENDPOINT 1: Create a new patient
     * 
     * HTTP: POST /api/patients
     * Demonstrates: CassandraRepository.save() → UPSERT operation
     */
    @PostMapping
    @Operation(
        summary = "Create a new patient",
        description = "Creates a new patient and stores in Cassandra. PatientId is auto-generated as UUID.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Patient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid patient data")
        }
    )
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        // Auto-generate UUID if not provided
        if (patient.getPatientId() == null) {
            patient.setPatientId(UUID.randomUUID());
        }
        
        Patient savedPatient = patientRepository.save(patient);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedPatient);
    }
    
    /**
     * ENDPOINT 2: Get patient by ID
     * 
     * HTTP: GET /api/patients/{id}
     * Demonstrates: CassandraRepository.findById(UUID) → Partition key lookup
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get patient by ID",
        description = "Retrieves a patient by their unique ID (UUID). This is a fast operation because patientId is the partition key.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
        }
    )
    public ResponseEntity<Patient> getPatient(
        @PathVariable("id") 
        @Parameter(description = "Patient UUID - example: 550e8400-e29b-41d4-a716-446655440000")
        UUID patientId
    ) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        
        return patient
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * ENDPOINT 3: Get medical events for a patient (with optional time range)
     * 
     * HTTP: GET /api/patients/{id}/events
     * Query Parameters:
     *   - ?days=30 → Get events from last N days (default: 30)
     */
    @GetMapping("/{id}/events")
    @Operation(
        summary = "Get medical events for a patient",
        description = "Retrieves all medical events for a patient within the specified number of days. Uses partition key + clustering key range query.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
        }
    )
    public ResponseEntity<List<MedicalEvent>> getPatientEvents(
        @PathVariable("id") 
        @Parameter(description = "Patient UUID")
        UUID patientId,
        @RequestParam(defaultValue = "30") 
        @Parameter(description = "Number of days to look back (default: 30)")
        int days
    ) {
        // Verify patient exists
        if (!patientRepository.existsById(patientId)) {
            return ResponseEntity.notFound().build();
        }
        
        // Calculate time range
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(days, ChronoUnit.DAYS);
        
        // Query: All events for patient within last N days
        // Cassandra efficiently retrieves because:
        // 1. patientId routes to correct node
        // 2. eventTimestamp (DESC) pre-sorted, so query is efficient
        List<MedicalEvent> events = medicalEventRepository
            .findByPatientIdAndEventTimestampBetween(patientId, startTime, endTime);
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * BONUS: Add a medical event for a patient
     * 
     * HTTP: POST /api/patients/{id}/events
     * Demonstrates: MedicalEvent creation and save
     */
    @PostMapping("/{id}/events")
    @Operation(
        summary = "Add a medical event for a patient",
        description = "Creates a new medical event for a patient. Uses time-based UUID and composite clustering keys.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "400", description = "Invalid event data")
        }
    )
    public ResponseEntity<MedicalEvent> addMedicalEvent(
        @PathVariable("id") 
        @Parameter(description = "Patient UUID")
        UUID patientId,
        @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Medical event details")
        MedicalEvent event
    ) {
        // Verify patient exists
        if (!patientRepository.existsById(patientId)) {
            return ResponseEntity.notFound().build();
        }
        
        // Ensure event is associated with this patient
        event.setPatientId(patientId);
        
        // Auto-generate if needed
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getEventTimestamp() == null) {
            event.setEventTimestamp(Instant.now());
        }
        
        MedicalEvent savedEvent = medicalEventRepository.save(event);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedEvent);
    }
}
