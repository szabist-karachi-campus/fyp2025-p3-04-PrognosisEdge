from app.db_connection import get_db_connection
import datetime
import psycopg2.extras
# import bcrypt

def create_user(name, email, password, role):
    """
    Create a new user with a unique email and a secure hashed password.
    """
    conn = None
    cursor = None
    try:
        # Validate role
        allowed_roles = ["System Supervisor", "Service Engineer", "User Administrator"]
        if role not in allowed_roles:
            return {"success": False, "message": "Invalid role. Allowed roles: System Supervisor, Service Engineer, User Administrator"}

        # Connect to the database
        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection error."}
        cursor = conn.cursor()

        # Check if email already exists
        cursor.execute("SELECT id FROM public.logincredentials WHERE email = %s", (email,))
        if cursor.fetchone():
            return {"success": False, "message": "Email already exists. Please enter a unique email."}

        hashed_password = password
        # hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

        # Insert new user into the database, setting prev_pwd to NULL,
        # is_active to True, and both created_at and updated_at to the current timestamp.
        cursor.execute("""
            INSERT INTO public.logincredentials (username, email, current_pwd, prev_pwd, role, is_active, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            RETURNING id
        """, (name, email, hashed_password, None, role, True, datetime.datetime.now(), datetime.datetime.now()))

        user_id = cursor.fetchone()[0]
        conn.commit()

        return {"success": True, "message": "User created successfully.", "user_id": user_id}

    except Exception as e:
        print(f"Error in create_user: {e}")
        return {"success": False, "message": "System temporarily unavailable. Please try again later."}

    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()


def get_user_by_id(user_id):
    """
    Retrieve user details by ID to pre-fill update form.
    """
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection error."}
        cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)

        cursor.execute("""
            SELECT id, username, email, role, is_active, created_at, updated_at
            FROM public.logincredentials
            WHERE id = %s
        """, (user_id,))
        row = cursor.fetchone()

        if not row:
            return {"success": False, "message": "User not found."}

        user_data = {
            "user_id": row["id"],
            "name": row["username"],
            "email": row["email"],
            "role": row["role"],
            "is_active": row["is_active"],
            "created_at": row["created_at"],
            "updated_at": row["updated_at"]
        }

        return {"success": True, "data": user_data}

    except Exception as e:
        print(f"Error in get_user_by_id: {e}")
        return {"success": False, "message": "System temporarily unavailable."}

    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()


def update_user(user_id, name, email, role):
    """
    Update user details including name, email, and role.
    """
    conn = None
    cursor = None
    try:
        allowed_roles = ["System Supervisor", "Service Engineer", "User Administrator"]
        if role not in allowed_roles:
            return {"success": False, "message": "Invalid role. Allowed roles: System Supervisor, Service Engineer, User Administrator"}

        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection error."}
        cursor = conn.cursor()

        # Check if the user exists
        cursor.execute("SELECT id FROM public.logincredentials WHERE id = %s", (user_id,))
        if not cursor.fetchone():
            return {"success": False, "message": "User not found."}

        # Check if the email is already taken by another user
        cursor.execute("SELECT id FROM public.logincredentials WHERE email = %s AND id != %s", (email, user_id))
        if cursor.fetchone():
            return {"success": False, "message": "Email already exists. Please enter a unique email."}

        # Update the record and update the updated_at column
        cursor.execute("""
            UPDATE public.logincredentials
            SET username = %s,
                email = %s,
                role = %s,
                updated_at = %s
            WHERE id = %s
        """, (name, email, role, datetime.datetime.now(), user_id))
        conn.commit()
        return {"success": True, "message": "User details updated successfully."}

    except Exception as e:
        print(f"Error in update_user: {e}")
        return {"success": False, "message": "System temporarily unavailable. Please try again later."}

    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()


def deactivate_user(user_id):
    """
    Deactivate an existing user by setting is_active to false.
    """
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection error."}
        cursor = conn.cursor()

        cursor.execute("SELECT id FROM public.logincredentials WHERE id = %s", (user_id,))
        if not cursor.fetchone():
            return {"success": False, "message": "User not found."}

        cursor.execute("""
            UPDATE public.logincredentials
            SET is_active = false,
                updated_at = %s
            WHERE id = %s
        """, (datetime.datetime.now(), user_id))
        conn.commit()
        return {"success": True, "message": "User deactivated successfully."}

    except Exception as e:
        print(f"Error in deactivate_user: {e}")
        return {"success": False, "message": "System temporarily unavailable. Please try again later."}

    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()


def get_all_users():
    """
    Retrieve all user records from the database.
    """
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection error."}
        cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
        cursor.execute("""
            SELECT id, username, email, current_pwd, prev_pwd, role, is_active, created_at, updated_at
            FROM public.logincredentials
        """)
        rows = cursor.fetchall()
        users = []
        for row in rows:
            users.append({
                "user_id": row["id"],
                "name": row["username"],
                "email": row["email"],
                "role": row["role"],
                "is_active": row["is_active"],
                "created_at": row["created_at"],
                "updated_at": row["updated_at"]
            })
        return {"success": True, "data": users}
    except Exception as e:
        print(f"Error in get_all_users: {e}")
        return {"success": False, "message": "System temporarily unavailable."}
    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()

# from app.db_connection import get_db_connection
# import datetime
# import psycopg2.extras
# # import bcrypt

# def create_user(name, email, password, role):
#     """
#     Create a new user with a unique email and a secure hashed password.
#     """
#     try:
#         # Validate role
#         allowed_roles = ["System Supervisor", "Service Engineer", "User Administrator"]
#         if role not in allowed_roles:
#             return {"success": False, "message": "Invalid role. Allowed roles: System Supervisor, Service Engineer, User Administrator"}

#         # Connect to the database
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         # Check if email already exists
#         cursor.execute("SELECT id FROM LoginCredentials WHERE email = %s", (email,))
#         if cursor.fetchone():
#             return {"success": False, "message": "Email already exists. Please enter a unique email."}

#         hashed_password = password
#         # Hash the password before storing it
#         # hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

#         # Insert new user into the database
#         cursor.execute("""
#             INSERT INTO LoginCredentials (username, email, current_pwd, role, created_at)
#             VALUES (%s, %s, %s, %s, %s)
#             RETURNING id
#         """, (name, email, hashed_password, role, datetime.datetime.now()))

#         user_id = cursor.fetchone()[0]
#         conn.commit()

#         return {"success": True, "message": "User created successfully.", "user_id": user_id}

#     except Exception as e:
#         print(f"Error in create_user: {e}")
#         return {"success": False, "message": "System temporarily unavailable. Please try again later."}

#     finally:
#         cursor.close()
#         conn.close()

# def get_user_by_id(user_id):
#     """
#     Retrieve user details by ID to pre-fill update form.
#     """
#     try:
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         cursor.execute("""
#             SELECT id, username, email, role
#             FROM LoginCredentials
#             WHERE id = %s
#         """, (user_id,))
#         row = cursor.fetchone()

#         if not row:
#             return {"success": False, "message": "User not found."}

#         user_data = {
#             "user_id": row[0],
#             "name": row[1],
#             "email": row[2],
#             "role": row[3]
#         }

#         return {"success": True, "data": user_data}

#     except Exception as e:
#         print(f"Error in get_user_by_id: {e}")
#         return {"success": False, "message": "System temporarily unavailable."}

#     finally:
#         cursor.close()
#         conn.close()


# def update_user(user_id, name, email, role):
#     """
#     Update user details including name, email, and role.
#     """
#     try:
#         # Validate role
#         allowed_roles = ["System Supervisor", "Service Engineer", "User Administrator"]
#         if role not in allowed_roles:
#             return {"success": False, "message": "Invalid role. Allowed roles: System Supervisor, Service Engineer, User Administrator"}

#         conn = get_db_connection()
#         cursor = conn.cursor()

#         # Check if the user exists
#         cursor.execute("SELECT id FROM LoginCredentials WHERE id = %s", (user_id,))
#         if not cursor.fetchone():
#             return {"success": False, "message": "User not found."}

#         # Check if the email is already taken by another user
#         cursor.execute("SELECT id FROM LoginCredentials WHERE email = %s AND id != %s", (email, user_id))
#         if cursor.fetchone():
#             return {"success": False, "message": "Email already exists. Please enter a unique email."}

#         # Perform the update
#         cursor.execute("""
#             UPDATE LoginCredentials
#             SET username = %s,
#                 email = %s,
#                 role = %s,
#                 updated_at = %s
#             WHERE id = %s
#         """, (name, email, role, datetime.datetime.now(), user_id))

#         conn.commit()
#         return {"success": True, "message": "User details updated successfully."}

#     except Exception as e:
#         print(f"Error in update_user: {e}")
#         return {"success": False, "message": "System temporarily unavailable. Please try again later."}

#     finally:
#         cursor.close()
#         conn.close()

# def deactivate_user(user_id):
#     """
#     Deactivate an existing user by setting is_active to false.
#     """
#     try:
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         # Check if the user exists
#         cursor.execute("SELECT id FROM LoginCredentials WHERE id = %s", (user_id,))
#         if not cursor.fetchone():
#             return {"success": False, "message": "User not found."}

#         # Deactivate the user (set is_active to false)
#         cursor.execute("""
#             UPDATE LoginCredentials
#             SET is_active = false,
#                 updated_at = %s
#             WHERE id = %s
#         """, (datetime.datetime.now(), user_id))

#         conn.commit()
#         return {"success": True, "message": "User deactivated successfully."}

#     except Exception as e:
#         print(f"Error in deactivate_user: {e}")
#         return {"success": False, "message": "System temporarily unavailable. Please try again later."}

#     finally:
#         cursor.close()
#         conn.close()

# def get_all_users():
#     """
#     Retrieve all user records from the database.
#     """
#     try:
#         conn = get_db_connection()
#         # Use DictCursor to easily reference columns by name
#         cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
#         cursor.execute("SELECT id, username, email, role, is_active FROM LoginCredentials")
#         rows = cursor.fetchall()
#         users = []
#         for row in rows:
#             users.append({
#                 "user_id": row["id"],
#                 "name": row["username"],
#                 "email": row["email"],
#                 "role": row["role"],
#                 "is_active": row["is_active"]
#             })
#         return {"success": True, "data": users}
#     except Exception as e:
#         print(f"Error in get_all_users: {e}")
#         return {"success": False, "message": "System temporarily unavailable."}
#     finally:
#         cursor.close()
#         conn.close()
