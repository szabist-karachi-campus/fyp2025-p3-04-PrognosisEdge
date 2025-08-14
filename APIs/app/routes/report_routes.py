from flask import Blueprint, request, jsonify
from app.models.report_models import (
    generate_and_fetch_machinereports,
    generate_and_fetch_workorderreports
)

report_bp = Blueprint('report_bp', __name__)

# ---------- SYSTEM SUPERVISOR ROUTE ----------
@report_bp.route('/machines', methods=['POST'])
def generate_machine_report():
    try:
        print("=== FLASK ROUTE DEBUG ===")
        print(f"Request method: {request.method}")
        print(f"Request headers: {dict(request.headers)}")
        
        # Check if we're getting JSON data
        if not request.is_json:
            print("Request is not JSON")
            return jsonify({"success": False, "message": "Request must be JSON"}), 400
        
        data = request.get_json()
        print(f"Received data: {data}")
        
        start_date = data.get("start_date")
        end_date = data.get("end_date")
        username = data.get("username")
        
        print(f"Parsed fields - start_date: {start_date}, end_date: {end_date}, username: {username}")

        if not all([start_date, end_date, username]):
            missing_fields = []
            if not start_date: missing_fields.append("start_date")
            if not end_date: missing_fields.append("end_date")
            if not username: missing_fields.append("username")
            
            print(f"Missing fields: {missing_fields}")
            return jsonify({"success": False, "message": f"Missing required fields: {missing_fields}"}), 400

        print("All fields present, calling generate_and_fetch_machinereports...")
        result = generate_and_fetch_machinereports(start_date, end_date, username)
        print(f"Function returned: {result}")
        
        status_code = 200 if result["success"] else 500
        print(f"Returning with status code: {status_code}")
        
        return jsonify(result), status_code

    except Exception as e:
        print(f"FLASK ROUTE ERROR: {str(e)}")
        print(f"Error type: {type(e).__name__}")
        import traceback
        print("Full traceback:")
        traceback.print_exc()
        return jsonify({"success": False, "message": f"Server error: {str(e)}"}), 500


# ---------- SERVICE ENGINEER ROUTE ----------
@report_bp.route('/workorders', methods=['POST'])
def generate_workorder_report():
    try:
        data = request.get_json()
        start_date = data.get("start_date")
        end_date = data.get("end_date")
        username = data.get("username")

        if not all([start_date, end_date, username]):
            return jsonify({"success": False, "message": "Missing required fields."}), 400

        result = generate_and_fetch_workorderreports(start_date, end_date, username)
        return jsonify(result), 200 if result["success"] else 500

    except Exception as e:
        return jsonify({"success": False, "message": f"Server error: {str(e)}"}), 500
