-- Drop table if exists to ensure clean setup
DROP TABLE IF EXISTS patient CASCADE;

CREATE TABLE patient (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  date_of_birth DATE,
  medical_record_number VARCHAR(100) UNIQUE NOT NULL
);
