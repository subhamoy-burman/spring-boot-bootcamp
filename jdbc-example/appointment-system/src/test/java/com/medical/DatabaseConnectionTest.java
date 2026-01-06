package com.medical;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM patient", Long.class);
        assertThat(count).isNotNull();
        System.out.println("Found patients: " + count);
    }
}
