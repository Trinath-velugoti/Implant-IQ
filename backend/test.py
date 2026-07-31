import requests
import json

BASE_URL = "http://localhost:5000"

def test_api():
    print("--- Testing ImplantIQ Unified Backend ---")

    # 1. Test Home
    try:
        r = requests.get(f"{BASE_URL}/")
        print(f"[GET /] Success: {r.text}")
    except:
        print("[GET /] Failed: Server not running.")
        return

    # 2. Test Stats
    r = requests.get(f"{BASE_URL}/stats")
    print(f"[GET /stats] Total Patients: {r.json().get('total_patients')}")

    # 3. Test Prediction
    payload = {
        "bone_density": 1.5,
        "bone_height": 12.0,
        "bone_width": 6.0,
        "implant_length": 10.0,
        "implant_diameter": 4.5
    }
    r = requests.post(f"{BASE_URL}/predict", json=payload)
    print(f"[POST /predict] Result: {json.dumps(r.json(), indent=2)}")

if __name__ == "__main__":
    test_api()
