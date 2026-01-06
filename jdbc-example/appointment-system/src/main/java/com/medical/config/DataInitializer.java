package com.medical.config;

import com.medical.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    private final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner showCounts(PatientService service) {
        return args -> {
            long count = service.findAll().size();
            log.info("Startup: found {} patients", count);
        };
    }
}
