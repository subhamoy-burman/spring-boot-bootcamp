package com.medical.service;

import com.medical.domain.Patient;
import com.medical.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository repo;

    public PatientService(PatientRepository repo) {
        this.repo = repo;
    }

    public List<Patient> findAll() {
        return repo.findAll();
    }

    public Optional<Patient> findByMrn(String mrn) {
        return repo.findByMrn(mrn);
    }

    public Patient create(Patient p) {
        Long id = repo.save(p);
        p.setId(id);
        return p;
    }

    public Optional<Patient> findById(Long id) {
        return repo.findById(id);
    }

    public boolean update(Patient p) {
        int rows = repo.update(p);
        return rows > 0;
    }

    public boolean delete(Long id) {
        int rows = repo.deleteById(id);
        return rows > 0;
    }
}
