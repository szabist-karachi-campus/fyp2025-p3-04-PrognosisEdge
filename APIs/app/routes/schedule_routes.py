from flask import Blueprint, request, jsonify
from flask_cors import cross_origin
from app.models import task_models

schedule_blueprint = Blueprint("schedule", __name__)
@schedule_blueprint.route("/schedule_maintenance", methods=["POST"])
@cross_origin()
def schedule_maintenance_route():
    try:
        data = request.get_json()
        print(f"Received Data: {data}")  # Log received data for debugging

        # Extract required fields
        title = data.get("title")
        machine_name = data.get("machine_name")
        assigned_engineer = data.get("assigned_engineer")
        scheduled_at = data.get("scheduled_at")
        notes = data.get("notes", None)  # Notes are optional

        # Check for missing fields and log them
        missing_fields = []
        if not title:
            missing_fields.append("title")
        if not machine_name:
            missing_fields.append("machine_name")
        if not assigned_engineer:
            missing_fields.append("assigned_engineer")
        if not scheduled_at:
            missing_fields.append("scheduled_at")

        if missing_fields:
            print(f"Validation Error: Missing fields - {', '.join(missing_fields)}")
            return jsonify({"success": False, "message": f"Missing fields: {', '.join(missing_fields)}"}), 400

        # Proceed with scheduling logic if validation passes
        result = task_models.schedule_maintenance(
            title, machine_name, assigned_engineer, scheduled_at, notes
        )

        return jsonify(result), (200 if result["success"] else 500)

    except Exception as e:
        print(f"Error in /schedule_maintenance: {e}")
        return jsonify({"success": False, "message": "Unexpected server error"}), 500
