from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
import os
import random
import jwt
import datetime
import bcrypt
from flask_mail import Mail, Message
from predict import ImplantPredictor

app = Flask(__name__)
CORS(app)

# SECURITY CONFIG
SECRET_KEY = "SENTINEL_SECURE_PROTO_X_99"
app.config['SECRET_KEY'] = SECRET_KEY

# MAIL CONFIGURATION
app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = 'your-clinical-email@gmail.com' # CHANGE THIS
app.config['MAIL_PASSWORD'] = 'your-app-password'            # CHANGE THIS
app.config['MAIL_DEFAULT_SENDER'] = 'your-clinical-email@gmail.com'

mail = Mail(app)

def send_otp_email(target_email, otp_code):
    try:
        msg = Message("ImplantIQ - Clinical Verification Code",
                      recipients=[target_email])
        msg.body = f"""
        Hello Doctor,

        Your 6-digit clinical verification code for ImplantIQ Pro is: {otp_code}

        This code will expire in 5 minutes.
        If you did not request this, please secure your account immediately.

        Regards,
        ImplantIQ AI Lab Security Team
        """
        mail.send(msg)
        return True
    except Exception as e:
        print(f"Mail Error: {e}")
        return False

db_config = {
    "host": "localhost",
    "user": "root",
    "password": "WJ28@krhps",
    "database": "implantiq_db"
}

predictor = ImplantPredictor()

def get_db_connection():
    return mysql.connector.connect(**db_config)

def get_doctor_id(token):
    try:
        data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
        return data['doctor_id']
    except:
        return None

# --- AUTHENTICATION ENDPOINTS ---

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    data = request.json
    email = data.get('email')
    password = data.get('password')
    username = data.get('username')

    if not all([email, password, username]):
        return jsonify({"error": "Missing clinical credentials"}), 400

    # Removed strict domain check to allow testing with any valid email (Gmail, etc.)
    if "@" not in email or "." not in email:
        return jsonify({"error": "Valid clinical email required"}), 400

    # 2. Hash password
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if exists
        cursor.execute("SELECT id FROM doctors WHERE email = %s", (email,))
        if cursor.fetchone():
            return jsonify({"error": "Doctor already registered"}), 409

        # Generate OTP
        otp = str(random.randint(100000, 999999))
        expiry = datetime.datetime.now() + datetime.timedelta(minutes=5)

        # Store Doctor (Unverified)
        cursor.execute("INSERT INTO doctors (username, email, password_hash, is_verified) VALUES (%s, %s, %s, FALSE)",
                       (username, email, hashed.decode('utf-8')))

        # Store OTP
        cursor.execute("INSERT INTO otps (email, otp_code, expires_at) VALUES (%s, %s, %s)",
                       (email, otp, expiry))

        conn.commit()
        cursor.close()
        conn.close()

        # SEND REAL EMAIL
        sent = send_otp_email(email, otp)

        return jsonify({
            "status": "otp_sent",
            "message": "Verification code has been sent to your clinical inbox.",
            "mail_status": "success" if sent else "failed"
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/auth/resend-otp', methods=['POST'])
def resend_otp():
    data = request.json
    email = data.get('email')

    if not email: return jsonify({"error": "Email required"}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Generate NEW OTP
        otp = str(random.randint(100000, 999999))
        expiry = datetime.datetime.now() + datetime.timedelta(minutes=5)

        # Invalidate old ones
        cursor.execute("UPDATE otps SET is_used = TRUE WHERE email = %s", (email,))

        # Store NEW OTP
        cursor.execute("INSERT INTO otps (email, otp_code, expires_at) VALUES (%s, %s, %s)",
                       (email, otp, expiry))

        conn.commit()
        cursor.close()
        conn.close()

        # SEND REAL EMAIL
        sent = send_otp_email(email, otp)

        return jsonify({
            "status": "otp_sent",
            "message": "New verification code sent",
            "mail_status": "success" if sent else "failed"
        }), 200
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
        cursor.execute("SELECT * FROM otps WHERE email = %s AND otp_code = %s AND expires_at > NOW() AND is_used = FALSE",
                       (email, otp))
        valid_otp = cursor.fetchone()

        if valid_otp:
            cursor.execute("UPDATE doctors SET is_verified = TRUE WHERE email = %s", (email,))
            cursor.execute("UPDATE otps SET is_used = TRUE WHERE id = %s", (valid_otp['id'],))
            conn.commit()
            return jsonify({"status": "success", "message": "Account activated"}), 200
        else:
            return jsonify({"error": "Invalid or expired OTP"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.json
    email = data.get('email')
    password = data.get('password')

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM doctors WHERE email = %s", (email,))
        user = cursor.fetchone()

        if user and bcrypt.checkpw(password.encode('utf-8'), user['password_hash'].encode('utf-8')):
            if not user['is_verified']:
                return jsonify({"error": "Account not verified. Please check your email."}), 403

            token = jwt.encode({
                'doctor_id': user['id'],
                'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24)
            }, app.config['SECRET_KEY'])

            return jsonify({
                "status": "success",
                "token": token,
                "user": {"id": user['id'], "username": user['username'], "email": user['email']}
            }), 200
        else:
            return jsonify({"error": "Invalid email or password"}), 401
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# --- SECURE DATA ISOLATION ENDPOINTS ---

@app.route('/api/stats', methods=['GET'])
def get_stats():
    token = request.headers.get('Authorization')
    doctor_id = get_doctor_id(token)
    if not doctor_id: return jsonify({"error": "Unauthorized"}), 401

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        # TOTAL PATIENTS (OWNED ONLY)
        cursor.execute("SELECT COUNT(*) as total FROM patients WHERE doctor_id = %s", (doctor_id,))
        total_patients = cursor.fetchone()['total']

        # AVG SUCCESS
        cursor.execute("SELECT AVG(success_rate) as avg_rate FROM patients WHERE doctor_id = %s", (doctor_id,))
        avg_rate = cursor.fetchone()['avg_rate'] or 0.0

        cursor.close()
        conn.close()

        return jsonify({
            "totalPatients": total_patients,
            "successRate": round(float(avg_rate), 1),
            "activePredictions": 0, # Filtered today can be added here
            "criticalRisks": 0
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/patients', methods=['GET'])
def get_patients():
    token = request.headers.get('Authorization')
    doctor_id = get_doctor_id(token)
    if not doctor_id: return jsonify({"error": "Unauthorized"}), 401

    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT patient_id, name, prediction_date, grade FROM patients WHERE doctor_id = %s ORDER BY id DESC", (doctor_id,))
        patients = cursor.fetchall()
        cursor.close()
        conn.close()
        return jsonify(patients)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
