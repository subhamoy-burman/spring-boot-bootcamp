package com.healthcare.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Hospital Cassandra System.
 * 
 * @SpringBootApplication enables:
 * 1. Component scanning for @Repository, @Service, @Controller
 * 2. Auto-configuration (DataStax Java Driver, Spring Data Cassandra)
 * 3. Embedded servlet container (Tomcat)
 * 
 * Spring Data Cassandra Autoconfiguration will:
 * - Read application.yml for connection properties
 * - Create CqlSession to keyspace 'hospital_system'
 * - Create table schemas (if schema-action: recreate)
 * - Register repositories as beans
 */
@SpringBootApplication
public class HospitalApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}
