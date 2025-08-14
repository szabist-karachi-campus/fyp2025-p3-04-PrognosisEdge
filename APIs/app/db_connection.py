# import mysql.connector
from app.config import DATABASE_CONFIG
import psycopg2

def get_db_connection():
    try:
        connection = psycopg2.connect(
            dbname=DATABASE_CONFIG["dbname"],
            user=DATABASE_CONFIG["user"],
            password=DATABASE_CONFIG["password"],
            host=DATABASE_CONFIG["host"],
            port=DATABASE_CONFIG["port"],
        )
        return connection
    except Exception as e:
        print(f"Database connection error: {e}")
        return None