from flask import Flask
from flask_cors import CORS
from flask_mail import Mail

mail = Mail()

def create_app():
    app = Flask(__name__)
    CORS(app)  # Enable CORS for external access

    # Mailjet configuration
    from app.config import MAIL_CONFIG
    app.config.update(MAIL_CONFIG)

    # Initialize Flask-Mail
    mail.init_app(app)

    # Register blueprints
    from .routes.login_routes import login_blueprint
    from .routes.otp_routes import otp_blueprint

    app.register_blueprint(login_blueprint, url_prefix="/api")
    app.register_blueprint(otp_blueprint, url_prefix="/api/otp")

    return app
