# ImplantIQ: AI-Based Dental Implant Survival Prediction System

ImplantIQ is a professional full-stack medical application designed to predict the survival years and success rates of dental implants based on patient bone measurements. This project features a unified architecture combining a Native Android App, a Web Dashboard, and a Python-powered AI Backend.

## 🚀 Project Architecture

The project is organized into three main modules:

1.  **Android App (`/app`)**: A native Java-based mobile application for clinical use on-the-go.
2.  **Web Dashboard (`/web`)**: A professional HTML/JS dashboard for desktop-based patient management and analysis.
3.  **Unified Backend (`/backend`)**: A Flask-based API that serves both mobile and web frontends, processes AI predictions, and manages the MySQL database.

---

## 🛠️ Prerequisites

Before running the project, ensure you have the following installed:

*   **Android Studio**: For running and managing the mobile application.
*   **Python 3.x**: For the AI backend.
*   **MySQL Server**: To store patient records.
*   **VS Code (Optional)**: For backend development.

---

## ⚙️ Setup Instructions

### 1. Database Setup
1.  Ensure MySQL is running.
2.  Update the credentials in `backend/init_db.py` if different from:
    *   **User**: `root`
    *   **Password**: `WJ28@krhps`
3.  Install dependencies: `pip install mysql-connector-python`
4.  Run the initialization script: `python backend/init_db.py`
    *   *This will create the `implantiq_db` and insert 128 initial patient records.*

### 2. Backend Setup
1.  Navigate to the backend folder: `cd backend`
2.  Install requirements: `pip install flask flask-cors joblib pandas scikit-learn`
3.  **Important**: Ensure your AI models (`model_years.pkl` and `model_rate.pkl`) are placed in the `backend/model/` directory.
4.  Start the server: `python app.py`
    *   The server will run on `http://localhost:5000` (web) and `http://10.0.2.2:5000` (emulator).

---

## 📱 Usage

### Running the Android App
1.  Open the `ImplantIQ` project in Android Studio.
2.  Click the **Run** button to install the app on your phone or emulator.
3.  **Login Credentials**:
    *   **Email**: `doctor@implantiq.com`
    *   **Password**: `Doctor@123`

### Accessing the Web Dashboard
1.  Simply open `web/index.html` in any modern web browser (Chrome, Edge, Safari).
2.  The dashboard will automatically sync with the live MySQL database.

---

## ✨ Features

*   **AI-Powered Predictions**: Predicts implant survival using bone density, height, width, and implant dimensions.
*   **Professional UI**: High-end dark theme design using Google's Material Components.
*   **Real-Time Sync**: Live data updates across both mobile and web platforms.
*   **Immersive Experience**: Full-screen clinical interface on mobile.
*   **Registration System**: Fully functional Signup and Login workflow.

---

**Developed by VELGOTI TRINATH**
*Final Year Project Submission*
