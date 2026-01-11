package com.healthcare.hospital.repository;

import com.healthcare.hospital.domain.Patient;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Basic Repository demonstrating Spring Data Cassandra CRUD operations.
 * 
 * Extends CassandraRepository<Patient, UUID> where:
 *   - Patient is the entity type
 *   - UUID is the primary key type
 * 
 * Inherited methods from CrudRepository:
 *   - save(Patient)        → UPSERT
 *   - findById(UUID)       → Single lookup (fast - partition key)
 *   - findAll()            → Scan all tables (SLOW - avoid in production)
 *   - count()              → Count all rows (SLOW - full table scan)
 *   - deleteById(UUID)     → Delete by ID
 */
@Repository
public interface PatientRepository extends CassandraRepository<Patient, UUID> {
    // Spring Data generates implementations automatically!
}
