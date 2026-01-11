package com.healthcare.hospital.config;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Force Cassandra session initialization on startup to catch configuration errors early.
 * This prevents the 500 error when the first request comes in.
 */
@Slf4j
@Component
public class CassandraSessionInitializer {
    
    private final CqlSession cqlSession;
    
    public CassandraSessionInitializer(CqlSession cqlSession) {
        this.cqlSession = cqlSession;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // Force session initialization by executing a simple query
            var metadata = cqlSession.getMetadata();
            log.info("✓ Cassandra session initialized successfully");
            log.info("  - Connected nodes: {}", metadata.getNodes().size());
            log.info("  - Keyspace: hospital_system");
        } catch (Exception e) {
            log.error("✗ Failed to initialize Cassandra session: {}", e.getMessage());
            throw new RuntimeException("Cassandra initialization failed", e);
        }
    }
}
