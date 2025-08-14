from flask import Blueprint, request, jsonify
from flask_cors import cross_origin  # Import for route-specific CORS
from app.models.Reset_Password_model import reset_password

reset_password_blueprint = Blueprint("reset_password", __name__)

@reset_password_blueprint.route("/reset_password", methods=["POST"])
@cross_origin()  # Apply CORS to this route
def reset_password_route():
    try:
        # Parse the request JSON
        data = request.get_json()
        username = data.get("username")
        new_password = data.get("new_password")

        # Validate inputs
        if not username or not new_password:
            return jsonify({"success": False, "message": "Username and new password are required."}), 400

        if len(new_password) < 8:
            return jsonify({"success": False, "message": "Password must be at least 8 characters long."}), 400

        # Call the model to reset the password
        result = reset_password(username, new_password)
        # Return the result to the client
        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 400
    except Exception as e:
        return jsonify({"success": False, "message": "System temporarily unavailable. Please try again later."}), 500
