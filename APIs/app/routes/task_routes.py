from flask import Blueprint, request, jsonify
from app.db_connection import get_db_connection
from app.models import task_models
from app.models.task_models import fetch_tasks_by_status
from app.models.task_models import get_task_counts_by_status
from app.models.task_models import fetch_task_details
from app.models.task_models import *
from flask_cors import cross_origin

task_blueprint = Blueprint("tasks", __name__)

@task_blueprint.route("/fetch_tasks_by_status", methods=["GET"])
def fetch_tasks_by_status_route():
    """
    API endpoint to fetch tasks by status.
    """
    try:
        status = request.args.get("status", None)  # Get status filter if provided
        tasks = fetch_tasks_by_status(status)  # Fetch tasks by status

        # Debug logs for responses
        print(f"Tasks Response: {tasks}")

        # Validate tasks response
        if not tasks["success"]:
            return jsonify({
                "success": False,
                "message": tasks.get("message", "Failed to fetch tasks")
            }), 500

        # Safely access data
        tasks_data = tasks.get("data", [])

        return jsonify({
            "success": True,
            "message": "Tasks fetched successfully",
            "tasks": tasks_data
        }), 200

    except Exception as e:
        # Log the exception for debugging
        print(f"Error in /fetch_tasks_by_status: {e}")
        return jsonify({
            "success": False,
            "message": "Unexpected server error"
        }), 500


@task_blueprint.route("/fetch_task_counts", methods=["GET"])
def fetch_task_counts_route():
    """
    API endpoint to fetch task counts by status.
    """
    try:
        task_counts = get_task_counts_by_status()  # Fetch task counts by status

        # Debug logs for responses
        print(f"Counts Response: {task_counts}")

        # Validate counts response
        if not task_counts["success"]:
            return jsonify({
                "success": False,
                "message": task_counts.get("message", "Failed to fetch task counts")
            }), 500

        # Safely access data
        counts_data = task_counts.get("data", {})

        return jsonify({
            "success": True,
            "message": "Task counts fetched successfully",
            "counts": counts_data
        }), 200

    except Exception as e:
        # Log the exception for debugging
        print(f"Error in /fetch_task_counts: {e}")
        return jsonify({
            "success": False,
            "message": "Unexpected server error"
        }), 500


@task_blueprint.route("/<int:task_id>", methods=["GET"])
@cross_origin()
def fetch_task_details_route(task_id):
    """
    API endpoint to fetch details of a specific task.
    """
    try:
        # Call the model function to fetch task details
        result = fetch_task_details(task_id)

        if result["success"]:
            return jsonify(result), 200  # Success: Task details fetched
        elif "not found" in result.get("message", "").lower():
            return jsonify(result), 404  # Not Found: Task doesn't exist
        else:
            return jsonify(result), 500  # Internal Server Error

    except Exception as e:
        print(f"Error in /<task_id>: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500


@task_blueprint.route("/update_task/<int:task_id>", methods=["PUT"])
@cross_origin()
def update_task_route(task_id):
    try:
        data = request.get_json()
        new_status = data.get("status")
        updated_date = data.get("updated_date")  # Date and time combined
        notes = data.get("notes")  # Notes field

        # Validate input
        if not new_status:
            return jsonify({"success": False, "message": "New status is required."}), 400

        # Call the model function to update the task
        result = task_models.update_task(task_id, new_status, updated_date, notes)

        if result["success"]:
            return jsonify(result), 200  # Success: Task updated
        elif "invalid transition" in result.get("message", "").lower():
            return jsonify(result), 400  # Bad Request: Invalid transition
        elif "not found" in result.get("message", "").lower():
            return jsonify(result), 404  # Not Found: Task doesn't exist
        else:
            return jsonify(result), 500  # Internal Server Error

    except Exception as e:
        print(f"Error in /update_task/<task_id>: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500

    except Exception as e:
        print(f"Error in /update_task: {e}")
        return jsonify({"success": False, "message": "Unexpected server error. Please try again later."}), 500
