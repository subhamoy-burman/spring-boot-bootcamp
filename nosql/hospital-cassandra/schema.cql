USE hospital_system;

CREATE TABLE IF NOT EXISTS patients (
    patientid uuid PRIMARY KEY,
    patient_name text,
    date_of_birth date,
    blood_type text,
    email text
);

CREATE TABLE IF NOT EXISTS medical_events (
    patient_id uuid,
    event_timestamp timestamp,
    event_id uuid,
    event_type text,
    description text,
    diagnosis_codes list<text>,
    created_by text,
    PRIMARY KEY (patient_id, event_timestamp, event_id)
) WITH CLUSTERING ORDER BY (event_timestamp DESC, event_id ASC);
