
from app.db_connection import get_db_connection
import random


def authenticate_user(username, password):
    connection = get_db_connection()
    if not connection:
        return {"success": False, "error": "Database connection failed"}

    try:
        cursor = connection.cursor()
        query = """
        SELECT role, is_active, email FROM LoginCredentials 
        WHERE username = %s AND current_pwd = %s
        """
        cursor.execute(query, (username, password))
        user = cursor.fetchone()
        cursor.close()
        connection.close()

        if user:
            role, is_active, email = user
            if not is_active:
                return {"success": False, "message": "Account is deactivated"}
            return {"success": True, "role": role, "message": "Login successful", "email": email}
        else:
            return {"success": False, "message": "Invalid username or password"}

    except Exception as e:
        return {"success": False, "error": str(e)}

def store_otp(username):
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Fetch user email
        query = "SELECT email FROM LoginCredentials WHERE username = %s"
        cursor.execute(query, (username,))
        user = cursor.fetchone()

        if not user:
            return {"success": False, "message": "User not found"}

        email = user[0]

        # Generate OTP
        otp = str(random.randint(100000, 999999))

        # Store OTP in the database
        insert_query = """
        INSERT INTO OTPStore (username, otp, expires_at)
        VALUES (%s, %s, NOW() + INTERVAL '5 minutes')
        ON CONFLICT (username) DO UPDATE SET otp = EXCLUDED.otp, expires_at = EXCLUDED.expires_at
        """
        cursor.execute(insert_query, (username, otp))
        connection.commit()

        cursor.close()
        connection.close()

        return {"success": True, "otp": otp, "email": email}

    except Exception as e:
        return {"success": False, "message": str(e)}

def verify_stored_otp(username, otp):
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()
        query = """
        SELECT otp, expires_at FROM OTPStore WHERE username = %s AND otp = %s AND expires_at > NOW()
        """
        cursor.execute(query, (username, otp))
        result = cursor.fetchone()
        cursor.close()
        connection.close()

        if result:
            return {"success": True}
        else:
            return {"success": False, "message": "Invalid or expired OTP"}
    except Exception as e:
        return {"success": False, "message": str(e)}



# from app.db_connection import get_db_connection
# import random
# import traceback

# def authenticate_user(username, password):
#     print("authenticate_user called with:", username, password)
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "error": "Database connection failed"}

#     try:
#         cursor = connection.cursor()
#         query = """
#         SELECT role, is_active, email FROM logincredentials 
#         WHERE username = %s AND current_pwd = %s
#         """
#         print("About to execute query.")
#         cursor.execute(query, (username, password))
#         print("Query executed.")
#         user = cursor.fetchone()
#         print("Query result:", user)
#         cursor.close()
#         connection.close()

#         if user:
#             role, is_active, email = user
#             if not is_active:
#                 return {"success": False, "message": "Account is deactivated"}
#             return {"success": True, "role": role, "message": "Login successful", "email": email}
#         else:
#             return {"success": False, "message": "Invalid username or password"}

#     except Exception as e:
#         print("Error in authenticate_user:", e)
#         return {"success": False, "error": str(e)}


# def store_otp(username):
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Fetch user email
#         query = "SELECT email FROM logincredentials WHERE username = %s"
#         cursor.execute(query, (username,))
#         user = cursor.fetchone()

#         if not user:
#             return {"success": False, "message": "User not found"}

#         email = user[0]

#         # Generate OTP
#         otp = str(random.randint(100000, 999999))

#         # Store OTP in the database using MySQL's upsert syntax
#         insert_query = """
#         INSERT INTO otpstore (username, otp, expires_at)
#         VALUES (%s, %s, NOW() + INTERVAL 5 MINUTE)
#         ON DUPLICATE KEY UPDATE otp = VALUES(otp), expires_at = VALUES(expires_at)
#         """
#         cursor.execute(insert_query, (username, otp))
#         connection.commit()

#         cursor.close()
#         connection.close()

#         return {"success": True, "otp": otp, "email": email}

#     except Exception as e:
#         return {"success": False, "message": str(e)}

# def verify_stored_otp(username, otp):
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()
#         query = """
#         SELECT otp, expires_at FROM otpstore WHERE username = %s AND otp = %s AND expires_at > NOW()
#         """
#         cursor.execute(query, (username, otp))
#         result = cursor.fetchone()
#         cursor.close()
#         connection.close()

#         if result:
#             return {"success": True}
#         else:
#             return {"success": False, "message": "Invalid or expired OTP"}
#     except Exception as e:
#         return {"success": False, "message": str(e)}
