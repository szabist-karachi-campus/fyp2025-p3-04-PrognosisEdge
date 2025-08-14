from flask import Blueprint, request, jsonify
from app.models.maintenancerecord_models import fetch_maintenance_records
from app.models.maintenancerecord_models import fetch_engineers
from flask_cors import cross_origin

maintenancerecord_blueprint = Blueprint("maintenancerecord", __name__)

@maintenancerecord_blueprint.route("/fetch_engineers", methods=["GET"])
@cross_origin()
def fetch_engineers_route():
    """
    API endpoint to fetch the list of engineers.

    Returns:
        JSON response containing the list of engineers or an error message.
    """
    try:
        # Call the model function to fetch engineers
        result = fetch_engineers()

        # Check if the fetch was successful
        if result["success"]:
            return jsonify({
                "success": True,
                "message": "Engineers fetched successfully",
                "data": result["data"]
            }), 200
        else:
            return jsonify({
                "success": False,
                "message": result.get("message", "Failed to fetch engineers")
            }), 500

    except Exception as e:
        # Handle unexpected exceptions
        print(f"Error in /fetch_engineers: {e}")
        return jsonify({
            "success": False,
            "message": "Unexpected server error. Please try again later."
        }), 500


@maintenancerecord_blueprint.route("/fetch_maintenance_history", methods=["GET"])
@cross_origin()
def fetch_maintenance_history():
    """
    API endpoint to fetch maintenance history.
    """
    try:
        # Retrieve optional filters from query parameters
        machine = request.args.get("machine")  # Filter by machine name
        status = request.args.get("status")    # Filter by task status (Completed, Cancelled)
        engineer = request.args.get("engineer")  # Filter by assigned engineer

        # Fetch maintenance records using the provided filters
        records = fetch_maintenance_records(machine=machine, status=status, engineer=engineer)

        # Debugging: Print the response for maintenance records
        print(f"Maintenance Records Response: {records}")

        # If fetching records fails, return an error response
        if not records["success"]:
            return jsonify({
                "success": False,
                "message": records.get("message", "Failed to fetch maintenance history")
            }), 500

        # Return the fetched records as a success response
        return jsonify({
            "success": True,
            "message": "Maintenance history fetched successfully",
            "data": records["data"]
        }), 200

    except Exception as e:
        # Log and handle unexpected errors
        print(f"Error in /fetch_maintenance_history: {e}")
        return jsonify({
            "success": False,
            "message": "Unexpected server error. Please try again later."
        }), 500


from flask import Blueprint, request, jsonify
from app.db_connection import get_db_connection
from app.models.maintenancerecord_models import fetch_maintenance_logs

@maintenancerecord_blueprint.route("/fetch_logs", methods=["GET"])
def fetch_logs_route():
    """
    API endpoint to fetch all maintenance logs with optional filters.
    """
    try:
        # Get filters from query parameters
        machine = request.args.get("machine", None)
        status = request.args.get("status", None)
        engineer = request.args.get("engineer", None)

        # Call the model function
        result = fetch_maintenance_logs(machine, status, engineer)

        if result["success"]:
            return jsonify(result), 200  # Success: Logs fetched
        else:
            return jsonify(result), 500  # Failure: Error message in response

    except Exception as e:
        print(f"Error in /fetch_logs route: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500
    
from app.models.maintenancerecord_models import update_comment

@maintenancerecord_blueprint.route("/update_comment", methods=["POST"])
@cross_origin()
def update_comment_route():
    try:
        data = request.get_json()
        task_id = data.get("task_id")
        comment = data.get("comment")

        if not task_id or not comment:
            return jsonify({"success": False, "message": "Task ID and comment are required."}), 400

        result = update_comment(task_id, comment)

        if result["success"]:
            return jsonify(result), 200
        else:
            return jsonify(result), 500

    except Exception as e:
        print(f"Exception in /update_comment: {e}")
        return jsonify({"success": False, "message": "Internal server error"}), 500