package com.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Medical Appointment System");
        try {
            Long patientCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM patient", Long.class);
            health.put("database", "Connected");
            health.put("patients", patientCount);
        } catch (Exception e) {
            health.put("database", "Error: " + e.getMessage());
        }
        return health;
    }
}
