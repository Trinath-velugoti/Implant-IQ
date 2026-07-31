import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score
import joblib
import os

# Load dataset
df = pd.read_csv("dataset/implant_dataset.csv")

# Features
X = df[['bone_density','bone_height','bone_width',
        'implant_length','implant_diameter']]

# Targets
y_years = df['survival_years']
y_rate  = df['success_rate']

# Split
X_train, X_test, yy_train, yy_test = train_test_split(
    X, y_years, test_size=0.2, random_state=42)
_, _, yr_train, yr_test = train_test_split(
    X, y_rate, test_size=0.2, random_state=42)

# Train models
model_years = RandomForestRegressor(n_estimators=100, random_state=42)
model_years.fit(X_train, yy_train)

model_rate = RandomForestRegressor(n_estimators=100, random_state=42)
model_rate.fit(X_train, yr_train)

# Evaluate
print(f"Survival Years MAE: {mean_absolute_error(yy_test, model_years.predict(X_test)):.2f}")
print(f"Success Rate  MAE:  {mean_absolute_error(yr_test, model_rate.predict(X_test)):.2f}")
print(f"Survival Years R2:  {r2_score(yy_test, model_years.predict(X_test)):.2f}")
print(f"Success Rate  R2:   {r2_score(yr_test, model_rate.predict(X_test)):.2f}")

# Save
os.makedirs("model", exist_ok=True)
joblib.dump(model_years, "model/model_years.pkl")
joblib.dump(model_rate,  "model/model_rate.pkl")
print("✅ Models saved successfully!")