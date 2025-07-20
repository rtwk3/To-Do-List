from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

model = joblib.load("priority_model.pkl")
vectorizer = joblib.load("vectorizer.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()
    task_text = data["task"]
    features = vectorizer.transform([task_text])
    prediction = model.predict(features)[0]
    return jsonify(prediction)

if __name__ == "__main__":
    app.run(debug=True)
