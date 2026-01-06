package com.medical.domain;

import java.time.LocalDate;

public class Patient {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String medicalRecordNumber;

    public Patient() {}

    public Patient(Long id, String name, LocalDate dateOfBirth, String medicalRecordNumber) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.medicalRecordNumber = medicalRecordNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getMedicalRecordNumber() { return medicalRecordNumber; }
    public void setMedicalRecordNumber(String medicalRecordNumber) { this.medicalRecordNumber = medicalRecordNumber; }
}
