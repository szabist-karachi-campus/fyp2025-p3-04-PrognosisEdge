from flask import Blueprint, request, jsonify
from flask_mail import Message
from flask_cors import cross_origin  # Import for route-specific CORS
from app import mail
from app.models.login_otp_models import store_otp, verify_stored_otp

otp_blueprint = Blueprint("otp", __name__)

@otp_blueprint.route("/send_otp", methods=["POST"])
@cross_origin()  # Apply CORS to this route
def send_otp():
    data = request.get_json()
    username = data.get("username")

    # Generate OTP and send email
    result = store_otp(username)
    if not result["success"]:
        return jsonify(result), 400

    try:
        msg = Message(
            "PrognosisEdge OTP Code",
            recipients=[result["email"]],  # Fetch recipient from the database
            body=f"OTP for PrognosisEdge is: {result['otp']}. It is valid for 5 minutes."
        )
        mail.send(msg)
        return jsonify({"success": True, "message": "OTP sent successfully"}), 200
    except Exception as e:
        return jsonify({"success": False, "message": "Failed to send OTP", "error": str(e)}), 500


@otp_blueprint.route("/verify_otp", methods=["POST"])
def verify_otp():
    data = request.get_json()
    username = data.get("username")
    otp = data.get("otp")

    result = verify_stored_otp(username, otp)
    if result["success"]:
        return jsonify({"success": True, "message": "OTP verified successfully"}), 200
    else:
        return jsonify(result), 400
