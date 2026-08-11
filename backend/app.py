from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
import os
import random
from predict import ImplantPredictor
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Database Configuration
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "WJ28@krhps",
    "database": "implantiq_db"
}

# Initialize AI Predictor
predictor = ImplantPredictor()

def get_db_connection():
    return mysql.connector.connect(**db_config)

@app.route('/api/health', methods=['GET'])
def health():
    db_status = "disconnected"
    try:
        conn = get_db_connection()
        if conn.is_connected():
            db_status = "connected"
        conn.close()
    except:
        db_status = "error"

    return jsonify({
        "status": "ok",
        "version": "1.0",
        "database": db_status
    })

@app.route('/api/stats', methods=['GET'])
def get_stats():
    user_id = request.args.get('user_id')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        # Base query depends on user_id
        where_clause = "WHERE doctor_id = %s" if user_id else ""
        params = (user_id,) if user_id else ()

        # 1. Total Unique Patients
        cursor.execute(f"SELECT COUNT(DISTINCT patient_id) as total FROM patients {where_clause}", params)
        total_patients = cursor.fetchone()['total']

        # 2. Avg Success Rate
        cursor.execute(f"SELECT AVG(success_rate) as avg_rate FROM patients {where_clause}", params)
        avg_rate = cursor.fetchone()['avg_rate'] or 0.0

        # 3. Active Predictions (Today)
        today = datetime.now().strftime('%Y-%m-%d')
        today_where = f"WHERE prediction_date = %s"
        today_params = [today]
        if user_id:
            today_where += " AND doctor_id = %s"
            today_params.append(user_id)

        cursor.execute(f"SELECT COUNT(*) as total FROM patients {today_where}", tuple(today_params))
        today_preds = cursor.fetchone()['total']

        # 4. Critical Risks
        risk_where = f"WHERE (grade = 'C' OR success_rate < 70)"
        risk_params = []
        if user_id:
            risk_where += " AND doctor_id = %s"
            risk_params.append(user_id)

        cursor.execute(f"SELECT COUNT(*) as total FROM patients {risk_where}", tuple(risk_params))
        critical_risks = cursor.fetchone()['total']

        cursor.close()
        conn.close()

        return jsonify({
            "totalPatients": total_patients,
            "successRate": round(float(avg_rate), 1),
            "activePredictions": today_preds,
            "criticalRisks": critical_risks,
            "serverToday": datetime.now().strftime('%d/%m/%Y')
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/analyze-xray', methods=['POST'])
def analyze_xray():
    """
    Simulates AI extraction only for valid X-Ray files.
    """
    if 'file' not in request.files:
        return jsonify({"error": "No image uploaded", "status": "rejected"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "Empty filename", "status": "rejected"}), 400

    # Logic: Accept only image types
    if not file.content_type.startswith('image/'):
        return jsonify({"error": "Invalid clinical format. Please upload a valid X-Ray image.", "status": "rejected"}), 400

    # Simulate processing
    import time
    time.sleep(1.5)

    return jsonify({
        "boneDensity": round(random.uniform(0.8, 2.2), 2),
        "boneHeight": round(random.uniform(8.0, 20.0), 1),
        "boneWidth": round(random.uniform(4.0, 10.0), 1),
        "implantLength": random.choice([8.0, 10.0, 11.5, 13.0, 15.0]),
        "implantDiameter": random.choice([3.3, 3.8, 4.2, 4.5, 5.0]),
        "status": "success"
    })

@app.route('/api/reset-password', methods=['POST'])
def reset_password():
    data = request.json
    email = data.get('email')
    # In a real app, send email here
    return jsonify({"status": "success", "message": f"Reset link sent to {email}"})

@app.route('/api/export/patients', methods=['GET'])
def export_patients():
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM patients")
        patients = cursor.fetchall()
        cursor.close()
        conn.close()

        # Generate CSV string
        if not patients:
            return "No data available", 404

        header = ",".join(patients[0].keys())
        rows = [",".join(map(str, p.values())) for p in patients]
        csv_content = header + "\n" + "\n".join(rows)

        from flask import Response
        return Response(
            csv_content,
            mimetype="text/csv",
            headers={"Content-disposition": "attachment; filename=patients_export.csv"}
        )
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/recent', methods=['GET'])
def get_recent():
    user_id = request.args.get('user_id')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        query = """
            SELECT patient_id, name, DATE_FORMAT(prediction_date, '%d %b %Y') as date,
            grade as result, survival_years, success_rate
            FROM patients
        """
        params = []
        if user_id:
            query += " WHERE doctor_id = %s"
            params.append(user_id)

        query += " ORDER BY id DESC LIMIT 5"

        cursor.execute(query, tuple(params))
        recent = cursor.fetchall()

        # Ensure numeric values are floats
        for p in recent:
            p['survival_years'] = float(p['survival_years'])
            p['success_rate'] = float(p['success_rate'])

        cursor.close()
        conn.close()
        return jsonify(recent)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/patients', methods=['GET'])
def get_patients():
    user_id = request.args.get('user_id')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        query = "SELECT patient_id, name, DATE_FORMAT(prediction_date, '%d/%m/%Y') as prediction_date, grade FROM patients"
        params = []
        if user_id:
            query += " WHERE doctor_id = %s"
            params.append(user_id)

        query += " ORDER BY id DESC"

        cursor.execute(query, tuple(params))
        patients = cursor.fetchall()
        cursor.close()
        conn.close()
        return jsonify(patients)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/patients/<pid>', methods=['GET'])
def get_patient_detail(pid):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        # Fetch detailed info including history if needed, but for now just the patient row
        cursor.execute("SELECT patient_id, name, DATE_FORMAT(prediction_date, '%d/%m/%Y') as prediction_date, grade, survival_years, success_rate FROM patients WHERE patient_id = %s", (pid,))
        patient = cursor.fetchone()
        cursor.close()
        conn.close()

        if patient:
            patient['success_rate'] = float(patient['success_rate'])
            patient['survival_years'] = float(patient['survival_years'])
            return jsonify(patient), 200
        else:
            return jsonify({"error": "Patient not found"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/signup', methods=['POST'])
def signup():
    data = request.json
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    if not all([username, email, password]):
        return jsonify({"error": "Missing fields"}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO users (username, email, password) VALUES (%s, %s, %s)",
                       (username, email, password))
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"status": "success", "message": "User registered"}), 201
    except mysql.connector.Error as err:
        if err.errno == 1062: # Duplicate entry
            return jsonify({"error": "Email already registered"}), 409
        return jsonify({"error": str(err)}), 500

@app.route('/api/login', methods=['POST'])
def login():
    data = request.json
    email = data.get('email')
    password = data.get('password')

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM users WHERE email = %s AND password = %s", (email, password))
        user = cursor.fetchone()
        cursor.close()
        conn.close()

        if user:
            return jsonify({"status": "success", "user": user}), 200
        else:
            return jsonify({"error": "Invalid email or password"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/predict', methods=['POST'])
def predict():
    data = request.json
    patient_name = data.get('patientName', 'New Patient')
    doctor_id = data.get('doctor_id', 1) # Default to 1 if not provided

    # Run AI Analysis using the predictor module
    result = predictor.predict(
        data['boneDensity'],
        data['boneHeight'],
        data['boneWidth'],
        data['implantLength'],
        data['implantDiameter']
    )

    if not result:
        return jsonify({"error": "AI models not loaded"}), 500

    # Save to Database
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if patient exists by name for THIS doctor
        cursor.execute("SELECT patient_id FROM patients WHERE name = %s AND doctor_id = %s", (patient_name, doctor_id))
        row = cursor.fetchone()

        if row:
            pid = row[0]
            cursor.execute("""
                UPDATE patients
                SET prediction_date = CURDATE(), grade = %s, survival_years = %s, success_rate = %s
                WHERE patient_id = %s
            """, (result['grade'], result['survival_years'], result['success_rate'], pid))
        else:
            cursor.execute("SELECT MAX(id) FROM patients")
            max_id_row = cursor.fetchone()
            next_id = (max_id_row[0] if max_id_row and max_id_row[0] else 1000) + 1
            pid = f"PID-{next_id}"
            cursor.execute("""
                INSERT INTO patients (patient_id, name, prediction_date, grade, survival_years, success_rate, doctor_id)
                VALUES (%s, %s, CURDATE(), %s, %s, %s, %s)
            """, (pid, patient_name, result['grade'], result['survival_years'], result['success_rate'], doctor_id))

        conn.commit()
        cursor.close()
        conn.close()

        return jsonify({
            "survival_years": result['survival_years'],
            "success_rate": result['success_rate'],
            "grade": result['grade'],
            "patient_id": pid,
            "prediction_date": datetime.now().strftime('%d/%m/%Y')
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
