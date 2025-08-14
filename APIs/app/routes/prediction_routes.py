from flask import Blueprint, jsonify
from app.models import prediction_models
from app.db_connection import get_db_connection
import threading
import psycopg2
import select

prediction_blueprint = Blueprint("prediction_blueprint", __name__)

def listen_to_new_readings(socketio):
    from flask import current_app

    print("Prediction listener has started")
    seen_ids = set()

    try:
        conn = psycopg2.connect(
            host="127.0.0.1",
            database="PrognosisEdge",
            user="postgres",
            password="1234",
            port="5432"
        )
        conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()
        cursor.execute("LISTEN new_reading;")
        print(">>> Listening to new_reading channel...")

        while True:
            if select.select([conn], [], [], 5) == ([], [], []):
                continue

            conn.poll()
            while conn.notifies:
                notify = conn.notifies.pop(0)
                reading_id = int(notify.payload)

                if reading_id in seen_ids:
                    continue
                seen_ids.add(reading_id)

                print(f">>> Received notification for reading_id: {reading_id}")
                machine_data = prediction_models.get_reading_with_machine_name(reading_id)

                if machine_data:
                    result = prediction_models.predict_failure(machine_data)

                    if result.get("success"):
                        prediction_models.update_prediction_result(
                            reading_id,
                            result["machine_failure"],
                            result["failure_type"]
                        )
                        print(f">>> Prediction saved for reading_id: {reading_id}")

                        # Logging confirmation before sending
                        print(f">>> Preparing to emit prediction to app: machine={machine_data['machine_id']}, failure={result['failure_type']}")

                        socketio.emit("new_prediction", {
                            "reading_id": reading_id,
                            "machine_id": machine_data["machine_id"],
                            "machine_name": machine_data["machine_name"],
                            "machine_failure": result["machine_failure"],
                            "failure_type": result["failure_type"] or "No Failure"
                        }, namespace="/")

                        print(">>> Prediction broadcast sent to app via WebSocket.")
                        print(">>> Clients connected?", socketio.server.manager.get_participants('/', '/'))


    except Exception as e:
        print(f"[ERROR] Notification listener failed: {e}")


@prediction_blueprint.route("/test_prediction_listener", methods=["GET"])
def test_prediction_listener():
    return jsonify({"success": True, "message": "Prediction listener active."})

@prediction_blueprint.route("/fetch_all", methods=["GET"])
def fetch_latest_predictions_for_operating():
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        query = """
        SELECT DISTINCT ON (m.serial_number)
            m.serial_number AS machine_id,
            m.name AS machine_name,
            mr.machine_failure,
            mr.failure_type,
            mr.water_flow_rate,
            mr.pressure_stability_index,
            mr.detergent_level,
            mr.hydraulic_pressure,
            mr.temperature_fluctuation_index,
            mr.hydraulic_oil_temperature,
            mr.coolant_temperature
        FROM machines m
        JOIN machinereadings mr ON m.serial_number = mr.machine_id
        WHERE m.status = 'Operating'
        ORDER BY m.serial_number, mr.reading_id DESC
        """

        cursor.execute(query)
        rows = cursor.fetchall()

        results = []
        for row in rows:
            results.append({
                "machineId": row[0],
                "machineName": row[1],
                "machineFailure": row[2],
                "failureType": row[3],
                "waterFlowRate": row[4],
                "pressureStabilityIndex": row[5],
                "detergentLevel": row[6],
                "hydraulicPressure": row[7],
                "temperatureFluctuationIndex": row[8],
                "hydraulicOilTemperature": row[9],
                "coolantTemperature": row[10]
            })

        cursor.close()
        conn.close()

        return jsonify(results), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

