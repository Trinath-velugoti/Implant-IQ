import mysql.connector
import random
import bcrypt
from datetime import datetime, timedelta

# Database configuration
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "WJ28@krhps"
}

def init_database():
    try:
        # Connect to MySQL
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()

        # Create Database
        cursor.execute("DROP DATABASE IF EXISTS implantiq_db")
        cursor.execute("CREATE DATABASE implantiq_db")
        cursor.execute("USE implantiq_db")

        print("--- SENTINEL SECURE: Initializing Clinical Database ---")

        # 1. Create Doctors Table
        cursor.execute("""
            CREATE TABLE doctors (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100),
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                phone VARCHAR(20),
                is_verified BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # 2. Create Patients Table (Isolated per Doctor)
        cursor.execute("""
            CREATE TABLE patients (
                id INT AUTO_INCREMENT PRIMARY KEY,
                doctor_id INT NOT NULL,
                patient_id VARCHAR(20) UNIQUE,
                name VARCHAR(100),
                prediction_date DATE,
                grade VARCHAR(5),
                survival_years FLOAT,
                success_rate FLOAT,
                FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
            )
        """)

        # 3. Create OTP System
        cursor.execute("""
            CREATE TABLE otps (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(100) NOT NULL,
                otp_code VARCHAR(6) NOT NULL,
                expires_at DATETIME NOT NULL,
                is_used BOOLEAN DEFAULT FALSE
            )
        """)

        # 4. Create Main Admin Doctor
        hashed_pass = bcrypt.hashpw("Doctor@123".encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
        cursor.execute("INSERT INTO doctors (username, email, password_hash, is_verified) VALUES (%s, %s, %s, TRUE)",
                       ("Dr. Trinath", "doctor@implantiq.com", hashed_pass))
        admin_id = cursor.lastrowid

        # 5. Generate 128 Mock Patients for the Admin
        names = ["Aditi", "Rajesh", "Sneha", "Vikram", "Priya", "Arjun", "Anjali", "Rohan", "Kavita", "Suresh"]
        last_names = ["Rao", "Kumar", "Kapoor", "Singh", "Sharma", "Verma", "Gupta", "Mehta", "Patel", "Reddy"]

        mock_data = []
        start_date = datetime.now() - timedelta(days=365)

        for i in range(1, 129):
            pid = f"PID-{1000 + i}"
            name = f"{random.choice(names)} {random.choice(last_names)}"
            p_date = (start_date + timedelta(days=random.randint(0, 365))).strftime('%Y-%m-%d')
            success = round(random.uniform(65, 98), 1)

            if success >= 90: grade = "A+"
            elif success >= 80: grade = "A"
            elif success >= 70: grade = "B"
            else: grade = "C"

            survival = round(random.uniform(4, 15), 1)
            mock_data.append((admin_id, pid, name, p_date, grade, survival, success))

        insert_query = """
            INSERT INTO patients (doctor_id, patient_id, name, prediction_date, grade, survival_years, success_rate)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """
        cursor.executemany(insert_query, mock_data)

        conn.commit()
        print(f"✅ Success: Database initialized with {cursor.rowcount} patients for Admin ID: {admin_id}")

        cursor.close()
        conn.close()

    except Exception as err:
        print(f"❌ Error: {err}")

if __name__ == "__main__":
    init_database()
