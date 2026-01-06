package com.medical.repository;

import com.medical.domain.Patient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Patient> findAll() {
        return jdbcTemplate.query(
            "SELECT id, name, date_of_birth, medical_record_number FROM patient ORDER BY id",
            this::mapRowToPatient
        );
    }

    public Optional<Patient> findByMrn(String mrn) {
        List<Patient> results = jdbcTemplate.query(
            "SELECT id, name, date_of_birth, medical_record_number FROM patient WHERE medical_record_number = ?",
            this::mapRowToPatient,
            mrn
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Patient> findById(Long id) {
        List<Patient> results = jdbcTemplate.query(
            "SELECT id, name, date_of_birth, medical_record_number FROM patient WHERE id = ?",
            this::mapRowToPatient,
            id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Long save(Patient p) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO patient (name, date_of_birth, medical_record_number) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, p.getName());
            if (p.getDateOfBirth() != null) {
                ps.setDate(2, Date.valueOf(p.getDateOfBirth()));
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, p.getMedicalRecordNumber());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(Patient p) {
        return jdbcTemplate.update(
            "UPDATE patient SET name = ?, date_of_birth = ?, medical_record_number = ? WHERE id = ?",
            p.getName(),
            p.getDateOfBirth() != null ? Date.valueOf(p.getDateOfBirth()) : null,
            p.getMedicalRecordNumber(),
            p.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM patient WHERE id = ?", id);
    }

    private Patient mapRowToPatient(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        Date dob = rs.getDate("date_of_birth");
        LocalDate localDob = dob != null ? dob.toLocalDate() : null;
        String mrn = rs.getString("medical_record_number");
        return new Patient(id, name, localDob, mrn);
    }
}
