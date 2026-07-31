import joblib
import pandas as pd
import os

class ImplantPredictor:
    def __init__(self):
        self.model_years_path = os.path.join("model", "model_years.pkl")
        self.model_rate_path = os.path.join("model", "model_rate.pkl")
        self.model_years = None
        self.model_rate = None
        self.load_models()

    def load_models(self):
        try:
            self.model_years = joblib.load(self.model_years_path)
            self.model_rate = joblib.load(self.model_rate_path)
            print("AI Models loaded successfully.")
        except Exception as e:
            print(f"Error loading models: {e}")

    def predict(self, bone_density, bone_height, bone_width, implant_length, implant_diameter):
        if not self.model_years or not self.model_rate:
            return None

        data = pd.DataFrame([[
            bone_density, bone_height, bone_width, implant_length, implant_diameter
        ]], columns=['bone_density', 'bone_height', 'bone_width', 'implant_length', 'implant_diameter'])

        years = float(self.model_years.predict(data)[0])
        rate = float(self.model_rate.predict(data)[0])

        grade = "A+" if rate >= 90 else "A" if rate >= 80 else "B" if rate >= 70 else "C"

        return {
            "survival_years": round(years, 1),
            "success_rate": round(rate, 1),
            "grade": grade
        }
