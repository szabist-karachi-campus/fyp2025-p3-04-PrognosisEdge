from app.db_connection import get_db_connection

def reset_password(username, new_password):
    try:
        # Connect to the database
        connection = get_db_connection()
        cursor = connection.cursor()

        # Check if the username exists
        cursor.execute("SELECT current_pwd, prev_pwd FROM LoginCredentials WHERE username = %s", (username,))
        user = cursor.fetchone()

        if not user:
            return {"success": False, "message": "Username not found. Please try again."}

        current_pwd, prev_pwd = user

        # Check if the new password matches the previous password
        if prev_pwd and prev_pwd == new_password:
            return {"success": False, "message": "New password cannot be the same as the previous password. Please try a different password."}

        # Update the password: Set current_pwd to prev_pwd, and new_password to current_pwd
        update_query = """
        UPDATE LoginCredentials
        SET prev_pwd = current_pwd, current_pwd = %s
        WHERE username = %s
        """
        cursor.execute(update_query, (new_password, username))
        connection.commit()

        return {"success": True, "message": "Password reset successfully."}
    except Exception as e:
        return {"success": False, "message": f"System error occurred: {str(e)}"}
    finally:
        cursor.close()
        connection.close()

