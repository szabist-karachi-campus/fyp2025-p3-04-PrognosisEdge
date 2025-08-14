from app.db_connection import get_db_connection

def get_all_machines():
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()
        query = "SELECT serial_number, name, type, location, status FROM Machines"
        cursor.execute(query)
        machines = cursor.fetchall()

        if not machines:
            return {"success": True, "data": [], "message": "No machines available"}

        # Format data as a list of dictionaries
        machine_list = [
            {
                "serial_number": row[0],
                "name": row[1],
                "type": row[2],
                "location": row[3],
                "status": row[4]
            }
            for row in machines
        ]

        return {"success": True, "data": machine_list, "message": "Machines fetched successfully"}

    except Exception as e:
        print(f"Database error: {e}")  # Log the actual error
        return {"success": False, "message": "An unexpected error occurred while fetching machines"}

    finally:
        cursor.close()
        connection.close()


def add_machine(name, machine_type, serial_number, location, status):
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Check for duplicate serial number
        check_query = "SELECT serial_number FROM Machines WHERE serial_number = %s"
        cursor.execute(check_query, (serial_number,))
        if cursor.fetchone():
            return {"success": False, "message": "A machine with this serial number already exists"}

        # Insert the machine into the database
        insert_query = """
        INSERT INTO Machines (serial_number, name, type, location, status)
        VALUES (%s, %s, %s, %s, %s)
        """
        cursor.execute(insert_query, (serial_number, name, machine_type, location, status))
        connection.commit()

        return {"success": True, "message": "Machine added successfully"}

    except Exception as e:
        print(f"Database error: {e}")  # Log the actual error
        return {"success": False, "message": "An unexpected error occurred while adding the machine"}

    finally:
        cursor.close()
        connection.close()

def edit_machine(serial_number, name, machine_type, location, status):
    """
    Update machine details.
    """
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Check if the machine exists
        check_query = "SELECT * FROM Machines WHERE serial_number = %s"
        cursor.execute(check_query, (serial_number,))
        machine = cursor.fetchone()
        if not machine:
            return {"success": False, "message": "Machine not found"}

        # Update machine details
        update_query = """
        UPDATE Machines
        SET name = %s, type = %s, location = %s, status = %s
        WHERE serial_number = %s
        """
        cursor.execute(update_query, (name, machine_type, location, status, serial_number))
        connection.commit()

        return {"success": True, "message": "Machine details updated successfully"}

    except Exception as e:
        print(f"Error in edit_machine: {e}")
        return {"success": False, "message": str(e)}

    finally:
        cursor.close()
        connection.close()


def delete_machine(serial_number):
    """
    Delete a machine from the database based on its serial number.
    """
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Check if the machine exists
        check_query = "SELECT * FROM Machines WHERE serial_number = %s"
        cursor.execute(check_query, (serial_number,))
        machine = cursor.fetchone()
        if not machine:
            return {"success": False, "message": "Machine not found"}

        # Delete the machine
        delete_query = "DELETE FROM Machines WHERE serial_number = %s"
        cursor.execute(delete_query, (serial_number,))
        connection.commit()

        return {"success": True, "message": "Machine deleted successfully"}

    except Exception as e:
        print(f"Error in delete_machine: {e}")
        return {"success": False, "message": str(e)}

    finally:
        cursor.close()
        connection.close()

def get_machines_by_type(machine_type):
    """
    Fetch machines based on their type from the database.
    """
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Fetch machines by type
        query = "SELECT name FROM Machines WHERE type = %s"
        cursor.execute(query, (machine_type,))
        machines = cursor.fetchall()
        print(f"Query result: {machines}")  # Debug log

        # Extract machine names from the result
        machine_names = [machine[0] for machine in machines]

        return {"success": True, "data": machine_names}

    except Exception as e:
        print(f"Error in get_machines_by_type: {e}")
        return {"success": False, "message": str(e)}

    finally:
        cursor.close()
        connection.close()


# from app.db_connection import get_db_connection

# def get_all_machines():
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()
#         query = "SELECT serial_number, name, type, location, status FROM machines"
#         cursor.execute(query)
#         machines = cursor.fetchall()

#         if not machines:
#             return {"success": True, "data": [], "message": "No machines available"}

#         # Format data as a list of dictionaries
#         machine_list = [
#             {
#                 "serial_number": row[0],
#                 "name": row[1],
#                 "type": row[2],
#                 "location": row[3],
#                 "status": row[4]
#             }
#             for row in machines
#         ]

#         return {"success": True, "data": machine_list, "message": "Machines fetched successfully"}

#     except Exception as e:
#         print(f"Database error: {e}")  # Log the actual error
#         return {"success": False, "message": "An unexpected error occurred while fetching machines"}

#     finally:
#         cursor.close()
#         connection.close()


# def add_machine(name, machine_type, serial_number, location, status):
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Check for duplicate serial number
#         check_query = "SELECT serial_number FROM machines WHERE serial_number = %s"
#         cursor.execute(check_query, (serial_number,))
#         if cursor.fetchone():
#             return {"success": False, "message": "A machine with this serial number already exists"}

#         # Insert the machine into the database
#         insert_query = """
#         INSERT INTO machines (serial_number, name, type, location, status)
#         VALUES (%s, %s, %s, %s, %s)
#         """
#         cursor.execute(insert_query, (serial_number, name, machine_type, location, status))
#         connection.commit()

#         return {"success": True, "message": "Machine added successfully"}

#     except Exception as e:
#         print(f"Database error: {e}")  # Log the actual error
#         return {"success": False, "message": "An unexpected error occurred while adding the machine"}

#     finally:
#         cursor.close()
#         connection.close()


# def edit_machine(serial_number, name, machine_type, location, status):
#     """
#     Update machine details.
#     """
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Check if the machine exists
#         check_query = "SELECT * FROM machines WHERE serial_number = %s"
#         cursor.execute(check_query, (serial_number,))
#         machine = cursor.fetchone()
#         if not machine:
#             return {"success": False, "message": "Machine not found"}

#         # Update machine details
#         update_query = """
#         UPDATE machines
#         SET name = %s, type = %s, location = %s, status = %s
#         WHERE serial_number = %s
#         """
#         cursor.execute(update_query, (name, machine_type, location, status, serial_number))
#         connection.commit()

#         return {"success": True, "message": "Machine details updated successfully"}

#     except Exception as e:
#         print(f"Error in edit_machine: {e}")
#         return {"success": False, "message": str(e)}

#     finally:
#         cursor.close()
#         connection.close()


# def delete_machine(serial_number):
#     """
#     Delete a machine from the database based on its serial number.
#     """
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Check if the machine exists
#         check_query = "SELECT * FROM machines WHERE serial_number = %s"
#         cursor.execute(check_query, (serial_number,))
#         machine = cursor.fetchone()
#         if not machine:
#             return {"success": False, "message": "Machine not found"}

#         # Delete the machine
#         delete_query = "DELETE FROM machines WHERE serial_number = %s"
#         cursor.execute(delete_query, (serial_number,))
#         connection.commit()

#         return {"success": True, "message": "Machine deleted successfully"}

#     except Exception as e:
#         print(f"Error in delete_machine: {e}")
#         return {"success": False, "message": str(e)}

#     finally:
#         cursor.close()
#         connection.close()


# def get_machines_by_type(machine_type):
#     """
#     Fetch machines based on their type from the database.
#     """
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Fetch machines by type
#         query = "SELECT name FROM machines WHERE type = %s"
#         cursor.execute(query, (machine_type,))
#         machines = cursor.fetchall()
#         print(f"Query result: {machines}")  # Debug log

#         # Extract machine names from the result
#         machine_names = [machine[0] for machine in machines]

#         return {"success": True, "data": machine_names}

#     except Exception as e:
#         print(f"Error in get_machines_by_type: {e}")
#         return {"success": False, "message": str(e)}

#     finally:
#         cursor.close()
#         connection.close()
