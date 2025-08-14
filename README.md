# PrognosisEdge

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Retrofit](https://img.shields.io/badge/Retrofit-0052CC?style=for-the-badge&logo=square&logoColor=white)](https://square.github.io/retrofit/)
[![MPAndroidChart](https://img.shields.io/badge/MPAndroidChart-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://github.com/PhilJay/MPAndroidChart)
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white)](https://flask.palletsprojects.com/)
[![Flask-SocketIO](https://img.shields.io/badge/Flask--SocketIO-4B8BBE?style=for-the-badge&logo=python&logoColor=white)](https://flask-socketio.readthedocs.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Scikit-learn](https://img.shields.io/badge/Scikit--learn-F7931E?style=for-the-badge&logo=scikit-learn&logoColor=white)](https://scikit-learn.org/stable/)
[![Pandas](https://img.shields.io/badge/Pandas-150458?style=for-the-badge&logo=pandas&logoColor=white)](https://pandas.pydata.org/)
[![NumPy](https://img.shields.io/badge/NumPy-013243?style=for-the-badge&logo=numpy&logoColor=white)](https://numpy.org/)
[![SMOTE-ENN](https://img.shields.io/badge/SMOTE--ENN-FF69B4?style=for-the-badge&logo=scikit-learn&logoColor=white)](https://imbalanced-learn.org/stable/references/generated/imblearn.combine.SMOTEENN.html)
[![Feature Selection](https://img.shields.io/badge/Feature%20Selection-00A86B?style=for-the-badge&logo=python&logoColor=white)](#)
![License](https://img.shields.io/badge/license-Private-red)

> **AI-Driven Predictive Maintenance for Industrial Machines**

---

## 📌 Project Overview

**PrognosisEdge** is a full-stack predictive maintenance system developed for the manufacturing industry. It uses historical IoT sensor data and supervised ML models to predict machine failures and generate maintenance reports.

This repository contains the **Android App**, **AI models**, **Flask APIs**, **database scripts**, **notebooks**, and all supporting **final documentation**.

---

## 📂 Folder Structure
```
PrognosisEdge/
├── AI/                   # ML models, SHAP plots, evaluation notebooks
├── APIs/                 # Flask backend: REST + WebSocket
├── Documentation/        # SRS, SDS, Test Cases, Proposal, User Manual, etc.
├── PrognosisEdge/        # Android Studio project
├── Resources/            # Icons, fonts, assets, report templates
├── DB Script.sql         # PostgreSQL schema for users, machines, logs, etc.
├── README.md             # This file
└── .gitignore
```

---

## 🧠 Machine Learning

Trained ML models use historical machine sensor data for:

- `machine_failure` (binary classification)
- `failure_type` (multiclass classification: temperature, pressure, detergent, flow)

**Models Used:**

- Logistic Regression  
- Random Forest  
- MLP
- KNN
- Decision Tree
- LightGBM  
- XGBoost

**Tools:**

- SHAP for explainability  
- GridSearchCV + HalvingCV  
---

## 📱 Android Application

An Android app (built in Java/XML) is provided for both:

- **System Supervisors**: Generate reports, add machines, assign tasks  
- **Service Engineers**: Update work orders, view predictions, schedule maintenance

> Uses Retrofit for API calls, Live WebSocket updates, custom UI with Orbitron and Proxima Nova fonts.

---

## 🔌 Backend: Flask APIs + WebSocket

The backend provides:

- Secure login (OTP via Mailjet)  
- Work order & machine endpoints  
- Prediction listener via DB trigger  
- WebSocket server for real-time notifications

---

## 🏗️ System Architecture
```plaintext
┌──────────────────────────────────┐
│          Android App              │
│  (Java/XML, Retrofit, Charts)     │
└─────▲───────────────┬─────────────┘
      │               │
      │ REST API       │ WebSocket
      │               │
┌─────┴───────────────▼─────────────┐
│           Flask Backend            │
│  (REST, WebSocket, ML Integration) │
└─────▲─────────────────────────────┘
      │
      │
┌─────┴─────────────────────────────┐
│        Machine Learning Models     │
│ (Scikit-learn, TensorFlow, MLflow) │
└─────▲─────────────────────────────┘
      │
      │
┌─────┴─────────────────────────────┐
│          PostgreSQL Database       │
└────────────────────────────────────┘

```
---

## 🛠️ Technology Stack

| Layer                 | Technologies |
|-----------------------|--------------|
| **Frontend (Mobile)** | Java, XML, Retrofit, MPAndroidChart |
| **Backend**           | Python, Flask, Flask-SocketIO |
| **Database**          | PostgreSQL |
| **Machine Learning**  | Scikit-learn, TensorFlow, MLflow |
| **Data Preprocessing**| Pandas, NumPy, SMOTE-ENN, Feature Selection |
| **Hosting**           | Local server (development), Cloud-ready architecture |


PrognosisEdge/
├── AI/                  # ML models, SHAP plots, evaluation notebooks
├── APIs/                 # Flask backend: REST + WebSocket
├── PrognosisEdge/        # Android Studio project
├── DB Script.sql         # PostgreSQL schema
├── README.md             # Project overview
└── .gitignore

---

## 🔑 Keywords
`Predictive Maintenance`, `Machine Learning`, `Industrial IoT`, `Android App`, `PrognosisEdge`

---

## ✨ Authors
- **Ghazal E Ashar & Shahzeb Ahmed Iqbal**

