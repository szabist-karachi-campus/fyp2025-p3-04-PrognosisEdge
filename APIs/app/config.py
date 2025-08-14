
# DATABASE_CONFIG = {
#     "dbname": "prognosisedge",
#     "user": "root",
#     "password": "1234",
#     "host": "localhost",  # Dynamically fetch the host IP; consider using 'localhost' for local development
#     "port": 3306,  # Use integer port
# }

import socket

def get_host_ip():
    try:
        # Get the current IP address of the host
        return socket.gethostbyname(socket.gethostname())
    except Exception as e:
        print(f"Error getting host IP: {e}")

DATABASE_CONFIG = {
    "dbname": "PrognosisEdge",
    "user": "postgres",
    "password": "1234",
    "host": get_host_ip(),  # Dynamically fetch the host IP
    "port": "5432",
}


MAIL_CONFIG = {
    "MAIL_SERVER": "in-v3.mailjet.com",
    "MAIL_PORT": 587,
    "MAIL_USE_SSL": False,
    "MAIL_USERNAME": "9785e3d94da4ded6c5667a09b6fb9ac6",  # Mailjet API Key
    "MAIL_PASSWORD": "55b3c10c3254f12728ae43e265656925",  #  Mailjet Secret Key
    "MAIL_DEFAULT_SENDER": "prognosisedge@gmail.com"  #  Verified sender email
}