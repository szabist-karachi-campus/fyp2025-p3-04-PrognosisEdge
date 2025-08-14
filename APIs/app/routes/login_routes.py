from flask import Blueprint, request, jsonify
from app.models.login_otp_models import authenticate_user
from flask_cors import cross_origin  # Import for route-specific CORS


login_blueprint = Blueprint("login", __name__)

@login_blueprint.route("/login", methods=["POST"])
@cross_origin()  # Apply CORS to this route
def login():
    data = request.get_json()
    username = data.get("username")
    password = data.get("password")

    if not username or not password:
        return jsonify({"success": False, "message": "Username and password are required"}), 400

    result = authenticate_user(username, password)

    if result.get("error"):
        return jsonify({"success": False, "message": "Internal server error", "error": result["error"]}), 500

    if not result["success"]:
        return jsonify({"success": False, "message": "Invalid Credentials"}), 401

    return jsonify(result), 200
