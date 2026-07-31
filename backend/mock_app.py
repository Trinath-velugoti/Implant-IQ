from flask import Flask, jsonify
from flask_cors import CORS
import random
from datetime import datetime

app = Flask(__name__)
CORS(app)

@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "version": "1.0", "database": "connected"})

@app.route('/api/stats', methods=['GET'])
def get_stats():
    return jsonify({
        "totalPatients": 128,
        "successRate": 85.5,
        "activePredictions": 5,
        "criticalRisks": 12,
        "serverToday": datetime.now().strftime('%d/%m/%Y')
    })

@app.route('/api/patients', methods=['GET'])
def get_patients():
    return jsonify([
        {"patient_id": "PID-1001", "name": "Aditi Rao", "prediction_date": "29/07/2026", "grade": "A+"},
        {"patient_id": "PID-1002", "name": "Rajesh Kumar", "prediction_date": "29/07/2026", "grade": "B"}
    ])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
