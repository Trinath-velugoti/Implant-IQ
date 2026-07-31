import mysql.connector
import random
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
        cursor.execute("CREATE DATABASE IF NOT EXISTS implantiq_db")
        cursor.execute("USE implantiq_db")

        # Create Patients Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS patients (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id VARCHAR(20) UNIQUE,
                name VARCHAR(100),
                prediction_date DATE,
                grade VARCHAR(5),
                survival_years FLOAT,
                success_rate FLOAT
            )
        """)

        # Create Users Table for Auth
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100),
                email VARCHAR(100) UNIQUE,
                password VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # Add a default doctor account
        cursor.execute("INSERT IGNORE INTO users (username, email, password) VALUES (%s, %s, %s)",
                       ("Dr. Trinath", "doctor@implantiq.com", "Doctor@123"))

        # Clear existing data for fresh start
        cursor.execute("DELETE FROM patients")

        # Generate 128 Mock Patients
        names = ["Aditi", "Rajesh", "Sneha", "Vikram", "Priya", "Arjun", "Anjali", "Rohan", "Kavita", "Suresh"]
        last_names = ["Rao", "Kumar", "Kapoor", "Singh", "Sharma", "Verma", "Gupta", "Mehta", "Patel", "Reddy"]

        mock_data = []
        start_date = datetime.now() - timedelta(days=365)

        print("Generating 128 mock patients...")
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

            mock_data.append((pid, name, p_date, grade, survival, success))

        # Bulk Insert
        insert_query = """
            INSERT INTO patients (patient_id, name, prediction_date, grade, survival_years, success_rate)
            VALUES (%s, %s, %s, %s, %s, %s)
        """
        cursor.executemany(insert_query, mock_data)

        conn.commit()
        print(f"Successfully inserted {cursor.rowcount} patients into MySQL!")

        cursor.close()
        conn.close()

    except mysql.connector.Error as err:
        print(f"Error: {err}")

if __name__ == "__main__":
    init_database()
