package com.healthcare.hospital.repository;

import com.healthcare.hospital.domain.MedicalEvent;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Advanced Repository demonstrating custom query methods for Cassandra.
 * 
 * Key Concepts:
 * 1. Method Name Derivation: Spring parses method names to generate CQL
 * 2. Partition Key Required: Must include patientId in queries (distributed requirement)
 * 3. Clustering Key Ranges: Can use Between/Ordering on clustering keys
 * 4. @Query Annotation: For complex CQL or projections
 */
@Repository
public interface MedicalEventRepository extends CassandraRepository<MedicalEvent, UUID> {
    
    /**
     * Find all events for a patient.
     * 
     * Method Name Derivation:
     *   findBy + PatientId → WHERE patient_id = ?
     * 
     * Generated CQL:
     *   SELECT * FROM medical_events WHERE patient_id = ?0
     * 
     * @param patientId Partition key - REQUIRED (tells Cassandra which node to query)
     * @return All medical events for the patient, ordered newest-first (from table definition)
     */
    List<MedicalEvent> findByPatientId(UUID patientId);
    
    /**
     * Find events within a date range for a patient.
     * 
     * Method Name Derivation:
     *   findBy + PatientId +And+ EventTimestamp +Between
     *   → WHERE patient_id = ? AND event_timestamp >= ? AND event_timestamp <= ?
     * 
     * Generated CQL:
     *   SELECT * FROM medical_events 
     *   WHERE patient_id = ?0 
     *   AND event_timestamp >= ?1 
     *   AND event_timestamp <= ?2
     * 
     * Why Between works: eventTimestamp is a CLUSTERING KEY
     * (Cassandra only allows range queries on clustering keys)
     * 
     * @param patientId Partition key (required)
     * @param startTime Start of range (inclusive)
     * @param endTime End of range (inclusive)
     * @return Events within the time range
     */
    List<MedicalEvent> findByPatientIdAndEventTimestampBetween(
        UUID patientId,
        Instant startTime,
        Instant endTime
    );
    
    /**
     * Custom CQL query with explicit LIMIT.
     * 
     * @Query annotation allows raw CQL when method name derivation is insufficient.
     * 
     * CQL:
     *   SELECT * FROM medical_events WHERE patient_id = ?0 LIMIT 10
     * 
     * @param patientId Partition key
     * @return Up to 10 most recent events
     */
    @Query("SELECT * FROM medical_events WHERE patient_id = ?0 LIMIT 10")
    List<MedicalEvent> findTop10EventsByPatientId(UUID patientId);
    
    /**
     * Check if patient has any events.
     * 
     * Generated CQL (optimized to COUNT):
     *   SELECT COUNT(*) FROM medical_events WHERE patient_id = ?0
     * 
     * @param patientId Partition key
     * @return true if any events exist, false otherwise
     */
    boolean existsByPatientId(UUID patientId);
}
