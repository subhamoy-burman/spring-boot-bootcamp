package com.healthcare.hospital.domain;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Complex Entity demonstrating @PrimaryKeyColumn with composite keys and clustering order.
 * This represents time-series medical events for patients.
 * 
 * Maps to Cassandra table: medical_events
 * 
 * Partition Key: patientId (routes to specific node based on patient)
 * Clustering Keys: eventTimestamp (DESC), eventId (ensures uniqueness)
 * 
 * Query Pattern: "Get all events for patient X, ordered newest-first"
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("medical_events")
public class MedicalEvent {
    
    /**
     * PARTITION KEY: All events for one patient are stored together
     * Enables efficient query: "Get all events for patient X"
     */
    @PrimaryKeyColumn(
        name = "patient_id",
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID patientId;
    
    /**
     * CLUSTERING KEY 1: Primary sort within partition (newest first)
     * ordinal = 0 means this is the first clustering key
     * ordering = DESCENDING ensures recent events come first
     */
    @PrimaryKeyColumn(
        name = "event_timestamp",
        ordinal = 0,
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.DESCENDING
    )
    private Instant eventTimestamp;
    
    /**
     * CLUSTERING KEY 2: Secondary sort for uniqueness
     * ordinal = 1 means this is applied after eventTimestamp
     * Solves problem: Two events at same millisecond would overwrite without this key
     */
    @PrimaryKeyColumn(
        name = "event_id",
        ordinal = 1,
        type = PrimaryKeyType.CLUSTERED
    )
    private UUID eventId;
    
    @Column("event_type")
    private String eventType;  // LAB_RESULT, PRESCRIPTION, VISIT, SURGERY
    
    private String description;
    
    @Column("diagnosis_codes")
    private List<String> diagnosisCodes = new ArrayList<>();  // e.g., ["J20.9", "R05.9"]
    
    @Column("created_by")
    private String createdBy;  // Doctor or healthcare provider name
    
    /**
     * NOT PERSISTED: Computed at application layer
     * Example of @Transient usage - field exists in Java but not in Cassandra
     */
    @Transient
    private boolean isUrgent;
    
    /**
     * Convenience constructor for creating new events
     */
    public MedicalEvent(UUID patientId, String eventType, String description) {
        this.patientId = patientId;
        this.eventTimestamp = Instant.now();
        this.eventId = Uuids.timeBased();  // Time-based UUID for natural ordering
        this.eventType = eventType;
        this.description = description;
    }
}
