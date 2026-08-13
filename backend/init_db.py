import mysql.connector
import bcrypt
import random
from datetime import datetime, timedelta

db_config = {
    "host": "localhost",
    "user": "root",
    "password": "WJ28@krhps"
}

def init_database():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        cursor.execute("CREATE DATABASE IF NOT EXISTS implantiq_db")
        cursor.execute("USE implantiq_db")

        # Drop tables for fresh reset if requested, or just ensure they exist
        cursor.execute("DROP TABLE IF EXISTS predictions, extractions, xray_studies, patients, otp_verifications, doctors")

        # 1. Doctors Table
        cursor.execute("""
            CREATE TABLE doctors (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100),
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                is_verified BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # 2. Patients Table
        cursor.execute("""
            CREATE TABLE patients (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id VARCHAR(20) UNIQUE NOT NULL,
                doctor_id INT NOT NULL,
                name VARCHAR(100) NOT NULL,
                age INT,
                gender VARCHAR(10),
                dob DATE,
                phone VARCHAR(20),
                email VARCHAR(100),
                medical_history TEXT,
                allergies TEXT,
                dental_conditions TEXT,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (doctor_id) REFERENCES doctors(id)
            )
        """)

        # 3. X-Ray Studies Table
        cursor.execute("""
            CREATE TABLE xray_studies (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id VARCHAR(20) NOT NULL,
                file_url VARCHAR(255),
                file_name VARCHAR(255),
                mime_type VARCHAR(50),
                validation_status VARCHAR(20) DEFAULT 'PENDING',
                confidence FLOAT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
            )
        """)

        # 4. Extractions Table
        cursor.execute("""
            CREATE TABLE extractions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                xray_id INT NOT NULL,
                bone_density FLOAT,
                bone_height FLOAT,
                bone_width FLOAT,
                implant_length FLOAT,
                implant_diameter FLOAT,
                status VARCHAR(20) DEFAULT 'COMPLETE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (xray_id) REFERENCES xray_studies(id)
            )
        """)

        # 5. Predictions Table
        cursor.execute("""
            CREATE TABLE predictions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                xray_id INT NOT NULL,
                implant_suitability VARCHAR(50),
                confidence FLOAT,
                risk_level VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (xray_id) REFERENCES xray_studies(id)
            )
        """)

        # 6. OTP Verifications
        cursor.execute("""
            CREATE TABLE otp_verifications (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(100) NOT NULL,
                otp_code VARCHAR(6) NOT NULL,
                expires_at DATETIME NOT NULL,
                is_used BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # Default Admin Doctor
        hashed_pass = bcrypt.hashpw("Doctor@123".encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
        cursor.execute("INSERT INTO doctors (username, email, password_hash, is_verified) VALUES (%s, %s, %s, TRUE)",
                       ("Dr. Trinath", "doctor@implantiq.com", hashed_pass))
        admin_id = cursor.lastrowid

        # Generate 128 Mock Patients for Admin
        names = ["Rahul", "Anjali", "Suresh", "Priya", "Amit", "Sneha", "Vikram", "Kavita", "Rohan", "Deepa"]
        for i in range(1, 129):
            pid = f"PAT-2026-{100000 + i}"
            p_name = f"{random.choice(names)} {i}"
            cursor.execute("""
                INSERT INTO patients (patient_id, doctor_id, name, age, gender)
                VALUES (%s, %s, %s, %s, %s)
            """, (pid, admin_id, p_name, random.randint(20, 70), random.choice(['Male', 'Female'])))

        conn.commit()
        print("✅ Clinical Database Rebuilt with 128 Managed Patients for Admin.")
        cursor.close()
        conn.close()

    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    init_database()
