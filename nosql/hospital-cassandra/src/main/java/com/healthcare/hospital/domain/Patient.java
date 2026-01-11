package com.healthcare.hospital.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Simple Entity demonstrating @Table and @PrimaryKey annotations.
 * Maps to Cassandra table: patients
 * 
 * Primary Key: patientId (partition key)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("patients")
public class Patient {
    
    @PrimaryKey
    private UUID patientId;
    
    @Column("patient_name")
    private String patientName;
    
    @Column("date_of_birth")
    private LocalDate dateOfBirth;  // Uses LocalDate for Cassandra DATE type
    
    @Column("blood_type")
    private String bloodType;  // A, B, AB, O
    
    private String email;
}
