from flask import Blueprint, request, jsonify
from app.db_connection import get_db_connection
from flask_cors import cross_origin
from app.models import admin_models
from app.models.admin_models import create_user

admin_blueprint = Blueprint('admin_blueprint', __name__)

@admin_blueprint.route("/create_user", methods=["POST"])
@cross_origin()
def create_user_route():
    """
    API endpoint for creating a new user.
    """
    try:
        data = request.get_json()
        name = data.get("name")
        email = data.get("email")
        password = data.get("password")
        role = data.get("role")

        # Validate input fields
        if not name or not email or not password or not role:
            return jsonify({"success": False, "message": "All fields (name, email, password, role) are required."}), 400

        # Call the model function
        result = admin_models.create_user(name, email, password, role)

        if result["success"]:
            return jsonify(result), 201  # Success: User created
        elif "email already exists" in result.get("message", "").lower():
            return jsonify(result), 409  # Conflict: Email already exists
        else:
            return jsonify(result), 500  # Internal Server Error

    except Exception as e:
        print(f"Error in /create_user: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500

@admin_blueprint.route("/get_user", methods=["POST"])
@cross_origin()
def get_user_route():
    try:
        data = request.get_json()
        user_id = data.get("user_id")

        if not user_id:
            return jsonify({"success": False, "message": "User ID is required."}), 400

        result = admin_models.get_user_by_id(user_id)

        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 404

    except Exception as e:
        print(f"Error in /get_user: {e}")
        return jsonify({"success": False, "message": "Unexpected server error."}), 500

@admin_blueprint.route("/update_user", methods=["PUT"])
@cross_origin()
def update_user_route():
    try:
        data = request.get_json()
        user_id = data.get("user_id")
        name = data.get("name")
        email = data.get("email")
        role = data.get("role")

        if not user_id or not name or not email or not role:
            return jsonify({"success": False, "message": "All fields (user_id, name, email, role) are required."}), 400

        result = admin_models.update_user(user_id, name, email, role)

        if result["success"]:
            return jsonify(result), 200
        elif "email already exists" in result.get("message", "").lower():
            return jsonify(result), 409
        elif "user not found" in result.get("message", "").lower():
            return jsonify(result), 404
        else:
            return jsonify(result), 500

    except Exception as e:
        print(f"Error in /update_user: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500

@admin_blueprint.route("/deactivate_user", methods=["PUT"])
@cross_origin()
def deactivate_user_route():
    """
    API endpoint for deactivating a user account (isActive = false).
    """
    try:
        data = request.get_json()
        user_id = data.get("user_id")

        # Validate input
        if not user_id:
            return jsonify({"success": False, "message": "user_id is required."}), 400

        # Call the model function
        result = admin_models.deactivate_user(user_id)

        if result["success"]:
            return jsonify(result), 200
        elif "user not found" in result.get("message", "").lower():
            return jsonify(result), 404
        else:
            # Any other error from the model function
            return jsonify(result), 500

    except Exception as e:
        print(f"Error in /deactivate_user: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500

@admin_blueprint.route("/get_all_users", methods=["GET"])
@cross_origin()
def get_all_users_route():
    """
    API endpoint to retrieve all users.
    """
    try:
        result = admin_models.get_all_users()
        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 500
    except Exception as e:
        print(f"Error in /get_all_users: {e}")
        return jsonify({"success": False, "message": "Unexpected server error."}), 500
