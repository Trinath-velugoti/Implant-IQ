from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import mysql.connector
import os
import uuid
import random
import jwt
import datetime
import bcrypt
from flask_mail import Mail, Message
from dotenv import load_dotenv
from PIL import Image
from predict import ImplantPredictor

load_dotenv()

app = Flask(__name__)
CORS(app)

# CONFIG
SECRET_KEY = os.getenv('SECRET_KEY', 'SENTINEL_SECURE_PROTO_X_99')
app.config['SECRET_KEY'] = SECRET_KEY
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# MAIL CONFIG
app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = os.getenv('MAIL_USERNAME')
app.config['MAIL_PASSWORD'] = os.getenv('MAIL_PASSWORD')
app.config['MAIL_DEFAULT_SENDER'] = os.getenv('MAIL_USERNAME')
mail = Mail(app)

db_config = {
    "host": "localhost",
    "user": "root",
    "password": "WJ28@krhps",
    "database": "implantiq_db"
}

predictor = ImplantPredictor()

def get_db_connection():
    return mysql.connector.connect(**db_config)

def token_required(f):
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'message': 'Token is missing!'}), 401
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
            current_doctor_id = data['doctor_id']
        except:
            return jsonify({'message': 'Token is invalid!'}), 401
        return f(current_doctor_id, *args, **kwargs)
    decorated.__name__ = f.__name__
    return decorated

# --- AUTH ---

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    data = request.json
    email = data.get('email')
    password = data.get('password')
    username = data.get('username')
    if not all([email, password, username]):
        return jsonify({"error": "Missing credentials"}), 400

    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM doctors WHERE email = %s", (email,))
        if cursor.fetchone():
            return jsonify({"error": "Email already exists"}), 409

        otp = str(random.randint(100000, 999999))
        expiry = datetime.datetime.now() + datetime.timedelta(minutes=5)

        cursor.execute("INSERT INTO doctors (username, email, password_hash) VALUES (%s, %s, %s)",
                       (username, email, hashed.decode('utf-8')))
        cursor.execute("INSERT INTO otp_verifications (email, otp_code, expires_at) VALUES (%s, %s, %s)",
                       (email, otp, expiry))
        conn.commit()

        # Real mail or log
        print(f"OTP for {email}: {otp}")
        return jsonify({"status": "otp_sent", "demo_otp": otp}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/auth/verify-otp', methods=['POST'])
def verify_otp():
    data = request.json
    email = data.get('email')
    otp = data.get('otp')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM otp_verifications WHERE email = %s AND otp_code = %s AND expires_at > NOW() AND is_used = FALSE",
                       (email, otp))
        if cursor.fetchone():
            cursor.execute("UPDATE doctors SET is_verified = TRUE WHERE email = %s", (email,))
            cursor.execute("UPDATE otp_verifications SET is_used = TRUE WHERE email = %s", (email,))
            conn.commit()
            return jsonify({"status": "success"}), 200
        return jsonify({"error": "Invalid/Expired OTP"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.json
    user, pwd = data.get('email'), data.get('password')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM doctors WHERE email = %s", (user,))
        doctor = cursor.fetchone()
        if doctor and bcrypt.checkpw(pwd.encode('utf-8'), doctor['password_hash'].encode('utf-8')):
            if not doctor['is_verified']:
                return jsonify({"error": "Verify email first"}), 403
            token = jwt.encode({'doctor_id': doctor['id'], 'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24)}, app.config['SECRET_KEY'])
            return jsonify({"status": "success", "token": token, "user": {"username": doctor['username']}}), 200
        return jsonify({"error": "Invalid email/password"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# --- PATIENTS ---

@app.route('/api/patients', methods=['POST'])
@token_required
def add_patient(doctor_id):
    data = request.json
    # Generate unique ID: PAT-2026-XXXXXX
    year = datetime.datetime.now().year
    suffix = str(random.randint(100000, 999999))
    patient_id = f"PAT-{year}-{suffix}"

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO patients (patient_id, doctor_id, name, age, gender, dob, phone, email, medical_history, allergies, dental_conditions, notes)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (patient_id, doctor_id, data['name'], data.get('age'), data.get('gender'), data.get('dob'),
              data.get('phone'), data.get('email'), data.get('medicalHistory'), data.get('allergies'),
              data.get('dentalConditions'), data.get('notes')))
        conn.commit()
        return jsonify({"patientId": patient_id, "name": data['name'], "status": "CREATED"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/patients', methods=['GET'])
@token_required
def list_patients(doctor_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT patient_id, name, age, created_at FROM patients WHERE doctor_id = %s", (doctor_id,))
        return jsonify(cursor.fetchall()), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/patients/<pid>', methods=['GET'])
@token_required
def get_patient(doctor_id, pid):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM patients WHERE patient_id = %s AND doctor_id = %s", (pid, doctor_id))
        patient = cursor.fetchone()
        if not patient: return jsonify({"error": "Not found"}), 404
        return jsonify(patient), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# --- X-RAY WORKFLOW ---

@app.route('/api/xray/upload', methods=['POST'])
@token_required
def upload_xray(doctor_id):
    if 'file' not in request.files: return jsonify({"error": "No file"}), 400
    file = request.files['file']
    pid = request.form.get('patientId')

    filename = f"{uuid.uuid4()}_{file.filename}"
    filepath = os.path.join(UPLOAD_FOLDER, filename)
    file.save(filepath)

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO xray_studies (patient_id, file_url, file_name, mime_type) VALUES (%s, %s, %s, %s)",
                       (pid, filepath, file.filename, file.content_type))
        conn.commit()
        return jsonify({"xrayId": cursor.lastrowid, "previewUrl": f"/api/uploads/{filename}"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/xray/validate', methods=['POST'])
@token_required
def validate_xray(doctor_id):
    xid = request.json.get('xrayId')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT file_url FROM xray_studies WHERE id = %s", (xid,))
        study = cursor.fetchone()

        # Real logic: Aspect ratio check for Panoramic
        img = Image.open(study['file_url'])
        w, h = img.size
        ratio = w/h
        is_valid = ratio > 1.4 # Panoramic X-rays are typically wide
        conf = 0.95 if is_valid else 0.1

        status = 'VALID' if is_valid else 'INVALID'
        cursor.execute("UPDATE xray_studies SET validation_status = %s, confidence = %s WHERE id = %s",
                       (status, conf, xid))
        conn.commit()
        return jsonify({"valid": is_valid, "type": "PANORAMIC_DENTAL_XRAY" if is_valid else "UNKNOWN", "confidence": conf}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/xray/extract', methods=['POST'])
@token_required
def extract_features(doctor_id):
    xid = request.json.get('xrayId')
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT validation_status FROM xray_studies WHERE id = %s", (xid,))
        status = cursor.fetchone()
        if status['validation_status'] != 'VALID':
            return jsonify({"error": "INVALID_DENTAL_XRAY", "message": "Validate first"}), 400

        # Mock Extraction
        data = {
            "bone_density": round(random.uniform(0.8, 2.2), 2),
            "bone_height": round(random.uniform(10, 20), 1),
            "bone_width": round(random.uniform(5, 10), 1),
            "implant_length": 11.5,
            "implant_diameter": 4.2
        }
        cursor.execute("""
            INSERT INTO extractions (xray_id, bone_density, bone_height, bone_width, implant_length, implant_diameter)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (xid, data['bone_density'], data['bone_height'], data['bone_width'], data['implant_length'], data['implant_diameter']))
        conn.commit()
        return jsonify(data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/predictions', methods=['POST'])
@token_required
def predict(doctor_id):
    data = request.json
    xid = data.get('xrayId')
    # Call ML model
    res = predictor.predict(data['bone_density'], data['bone_height'], data['bone_width'], data['implant_length'], data['implant_diameter'])
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO predictions (xray_id, implant_suitability, confidence, risk_level) VALUES (%s, %s, %s, %s)",
                       (xid, "HIGH" if res['success_rate'] > 80 else "MEDIUM", res['success_rate']/100, res['grade']))
        conn.commit()
        return jsonify({"predictionId": cursor.lastrowid, "riskLevel": res['grade'], "confidence": res['success_rate'], "suitability": "HIGH"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/uploads/<filename>')
def serve_upload(filename):
    return send_from_directory(UPLOAD_FOLDER, filename)

# --- DASHBOARD STATS ---

@app.route('/api/dashboard/statistics', methods=['GET'])
@token_required
def dashboard_stats(doctor_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT COUNT(*) as count FROM patients WHERE doctor_id = %s", (doctor_id,))
        total = cursor.fetchone()['count']
        return jsonify({"totalPatients": total, "successRate": "85%", "activeToday": 0, "criticalRisks": 0}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
