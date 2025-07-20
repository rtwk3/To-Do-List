# 📝 Smart To-Do List (Java + Flask)

A modern desktop To-Do List application built using **Java Swing** with features like smart priority prediction via a **Flask REST API**, task reminders, sorting, filtering, and persistent storage.

---

## 🚀 Features

- ✅ **Add, remove, complete, and clear tasks**
- 🌟 **Smart Priority Prediction** (`High`, `Medium`, `Low`) using a Flask API
- 📅 **Set task reminders** with date & time input
- 📂 **Persistent task saving** to a local file (`tasks.txt`)
- 🔃 **Sort tasks** by:
  - A-Z
  - Done / Pending first
  - Priority (High → Low / Low → High)
- 🔎 **Filter tasks**: All / Completed / Pending
- 🎨 **Color-coded UI** with icons and task styling

---

## 🧠 Smart Priority Prediction (via Flask API)

The app connects to a locally running **Flask server** that predicts priority based on task text using an ML model.

### Example Request
```json
POST /predict
{
  "task": "Complete DBMS assignment"
}
