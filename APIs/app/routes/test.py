from flask import Blueprint, jsonify

test_blueprint = Blueprint("test", __name__)

@test_blueprint.route("/test", methods=["GET"])
def test_endpoint():
    return jsonify({"success": True, "message": "Test endpoint is working!"}), 200


from app.db_connection import get_db_connection

@test_blueprint.route("/db_test", methods=["GET"])
def db_test():
    try:
        connection = get_db_connection()
        if not connection:
            return jsonify({"success": False, "message": "Database connection failed"}), 500
        cursor = connection.cursor()
        cursor.execute("SELECT 1")
        result = cursor.fetchone()
        cursor.close()
        connection.close()
        return jsonify({"success": True, "result": result}), 200
    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500
