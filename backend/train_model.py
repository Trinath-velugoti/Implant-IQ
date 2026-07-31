import pandas as pd
from sklearn.ensemble import RandomForestRegressor
import joblib
import os

# Create model directory if not exists
if not os.path.exists("model"):
    os.makedirs("model")

def train():
    print("Training ImplantIQ AI Models...")

    # Load your dataset
    # data = pd.read_csv("dataset/implant_dataset.csv")

    # Mock data for demonstration if CSV is missing
    data = pd.DataFrame({
        'bone_density': [1.2, 1.8, 0.9, 1.5, 1.3],
        'bone_height': [10, 15, 8, 12, 11],
        'bone_width': [5, 7, 4, 6, 5.5],
        'implant_length': [10, 12, 8, 10, 10],
        'implant_diameter': [4, 5, 3.5, 4.5, 4],
        'survival_years': [12.5, 14.2, 5.1, 11.8, 10.5],
        'success_rate': [92, 98, 62, 88, 85]
    })

    X = data[['bone_density', 'bone_height', 'bone_width', 'implant_length', 'implant_diameter']]
    y_years = data['survival_years']
    y_rate = data['success_rate']

    # Train Models
    model_years = RandomForestRegressor(n_estimators=100).fit(X, y_years)
    model_rate = RandomForestRegressor(n_estimators=100).fit(X, y_rate)

    # Save Models
    joblib.dump(model_years, "model/model_years.pkl")
    joblib.dump(model_rate, "model/model_rate.pkl")
    print("Models saved to /backend/model/")

if __name__ == "__main__":
    train()
