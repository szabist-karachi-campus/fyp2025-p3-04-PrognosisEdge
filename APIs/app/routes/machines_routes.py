from flask import Blueprint, request, jsonify
from app.db_connection import get_db_connection
from flask_cors import cross_origin
from app.models import machine_models

machine_blueprint = Blueprint("machines", __name__)

@machine_blueprint.route("/get_all_machines", methods=["GET"])
@cross_origin()
def get_all_machines_route():
    try:
        result = machine_models.get_all_machines()
        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 500
    except Exception as e:
        print(f"Error in /get_all_machines: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500


@machine_blueprint.route("/add_machine", methods=["POST"])
@cross_origin()
def add_machine_route():
    try:
        data = request.get_json()
        print(f"Received Data: {data}")  # Log incoming data
        name = data.get("name")
        machine_type = data.get("type")
        serial_number = data.get("serial_number")
        location = data.get("location")
        status = data.get("status")  

        # Validate input
        if not all([name, machine_type, serial_number, location, status]):
            return jsonify({"success": False, "message": "All fields are required"}), 400

        # Call the model function to add the machine
        result = machine_models.add_machine(name, machine_type, serial_number, location, status)
        if result["success"]:
            return jsonify(result), 200
        elif "already exists" in result.get("message", "").lower():
            return jsonify(result), 409
        else:
            return jsonify(result), 500
    except Exception as e:
        print(f"Error in /add_machine: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500

@machine_blueprint.route("/edit_machine", methods=["PUT"])
@cross_origin()
def edit_machine_route():
    try:
        data = request.get_json()
        serial_number = data.get("serial_number")  # Required for identifying the machine
        name = data.get("name")
        machine_type = data.get("type")
        location = data.get("location")
        status = data.get("status")

        # Validate input
        if not serial_number:
            return jsonify({"success": False, "message": "Serial number is required"}), 400
        if not all([name, machine_type, location, status]):
            return jsonify({"success": False, "message": "All fields are required"}), 400

        # Call the model function to update the machine
        result = machine_models.edit_machine(serial_number, name, machine_type, location, status)
        if result["success"]:
            return jsonify(result), 200
        elif result["message"] == "Machine not found":
            return jsonify(result), 404
        elif "already exists" in result["message"].lower():
            return jsonify(result), 409
        else:
            return jsonify(result), 500

    except Exception as e:
        print(f"Error in /edit_machine: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500

@machine_blueprint.route("/delete_machine", methods=["DELETE"])
@cross_origin()
def delete_machine_route():
    """
    API endpoint to delete a machine based on its serial number.
    """
    try:
        serial_number = request.args.get("serial_number")  # Extract serial_number from query params

        if not serial_number:
            return jsonify({"success": False, "message": "Serial number is required"}), 400

        # Call the model function to delete the machine
        result = machine_models.delete_machine(serial_number)
        if result["success"]:
            return jsonify(result), 200  # Success
        elif "not found" in result.get("message", "").lower():
            return jsonify(result), 404  # Machine not found
        else:
            return jsonify(result), 500  # Internal server error

    except Exception as e:
        print(f"Error in /delete_machine: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500


@machine_blueprint.route("/get_machines_by_type", methods=["GET"])
@cross_origin()
def get_machines_by_type_route():
    """
    API endpoint to fetch machines based on their type.
    """
    try:
        machine_type = request.args.get("type")  # Get machine type from query parameters
        if not machine_type:
            return jsonify({"success": False, "message": "Machine type is required"}), 400

        # Call the model function to fetch machines by type
        result = machine_models.get_machines_by_type(machine_type)
        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 500

    except Exception as e:
        print(f"Error in /get_machines_by_type: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500
