from flask import Flask
from flask_mail import Mail
from app.config import MAIL_CONFIG
from app.routes import machines_routes
from app.routes import login_routes
from app.routes import otp_routes
from app.routes import reset_pass_route
from app.routes import schedule_routes
from app.routes import task_routes
from app.routes import test
from app.routes import maintenancerecord_routes
from app.routes.maintenancerecord_routes import maintenancerecord_blueprint
from app.routes.admin_routes import admin_blueprint
from app.routes import admin_routes
from app.routes import prediction_routes
from app.routes.prediction_routes import listen_to_new_readings
from app.routes import report_routes
from flask_cors import CORS
import threading
from flask_socketio import SocketIO
import ssl


app = Flask(__name__)

# Load mail configuration
app.config.update(MAIL_CONFIG)

# Initialize Flask-Mail
mail = Mail(app)

# Initialize CORS
CORS(app)

# Initialize SocketIO
socketio = SocketIO(app, cors_allowed_origins="*", async_mode="threading")

# Register blueprints
app.register_blueprint(login_routes.login_blueprint, url_prefix="/api")
app.register_blueprint(reset_pass_route.reset_password_blueprint, url_prefix="/api")
app.register_blueprint(otp_routes.otp_blueprint, url_prefix="/api/otp")
app.register_blueprint(machines_routes.machine_blueprint, url_prefix="/api/machines")
app.register_blueprint(schedule_routes.schedule_blueprint, url_prefix="/api/task")
app.register_blueprint(task_routes.task_blueprint, url_prefix="/api/task")
app.register_blueprint(maintenancerecord_routes.maintenancerecord_blueprint, url_prefix="/api/records")
app.register_blueprint(admin_routes.admin_blueprint, url_prefix="/api/admin")
app.register_blueprint(prediction_routes.prediction_blueprint, url_prefix="/api/predict")
app.register_blueprint(report_routes.report_bp, url_prefix="/api/reports")

# Start prediction listener inside Flask context
def start_listener():
    with app.app_context():
        listen_to_new_readings(socketio)

socketio.start_background_task(start_listener)

# threading.Thread(target=listen_to_new_readings, args=(socketio,), daemon=True).start()


# Start app with SSL 
if __name__ == "__main__":
    socketio.run(
        app,
        host="0.0.0.0",
        port=5000,
        debug=True,
        ssl_context="adhoc"
    )