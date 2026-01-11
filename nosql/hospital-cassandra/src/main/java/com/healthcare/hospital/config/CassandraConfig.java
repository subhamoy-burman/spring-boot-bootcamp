package com.healthcare.hospital.config;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cassandra configuration to explicitly set the local datacenter and keyspace.
 * This resolves configuration issues with the DataStax driver.
 */
@Configuration
public class CassandraConfig {
    
    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;
    
    @Value("${spring.data.cassandra.keyspace-name:hospital_system}")
    private String keyspace;
    
    /**
     * Customize the CqlSessionBuilder to explicitly set the local datacenter and keyspace.
     * This is the recommended approach in Spring Boot 3.x.
     */
    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
        return (CqlSessionBuilder builder) -> builder
                .withLocalDatacenter(localDatacenter)
                .withKeyspace(keyspace);
    }
}
