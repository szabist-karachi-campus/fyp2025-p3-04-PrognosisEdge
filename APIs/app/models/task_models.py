from app.db_connection import get_db_connection
from datetime import datetime
import psycopg2
from psycopg2.extras import DictCursor
import datetime

def schedule_maintenance(title, machine_name, assigned_engineer, scheduled_at, notes):
    connection = get_db_connection()
    if not connection:
        return {"success": False, "message": "Database connection failed"}

    try:
        cursor = connection.cursor()

        # Fetch the machine_id based on machine_name
        cursor.execute("SELECT serial_number FROM Machines WHERE name = %s", (machine_name,))
        machine = cursor.fetchone()
        if not machine:
            return {"success": False, "message": "Machine not found"}

        machine_id = machine[0]

        # Default status to 'upcoming'
        default_status = 'upcoming'

        # Insert into MaintenanceTasks
        insert_task_query = """
        INSERT INTO MaintenanceTasks (machine_id, title, assigned_engineer, status)
        VALUES (%s, %s, %s, %s) RETURNING task_id
        """
        cursor.execute(insert_task_query, (machine_id, title, assigned_engineer, default_status))
        task_id = cursor.fetchone()[0]

        # Insert into TaskDates
        insert_date_query = """
        INSERT INTO TaskDates (task_id, scheduled_at)
        VALUES (%s, %s)
        """
        cursor.execute(insert_date_query, (task_id, scheduled_at))

        # Insert notes if provided
        if notes:
            insert_notes_query = """
            INSERT INTO TaskNotes (task_id, notes)
            VALUES (%s, %s)
            """
            cursor.execute(insert_notes_query, (task_id, notes))

        connection.commit()

        return {"success": True, "message": "Maintenance scheduled successfully"}

    except Exception as e:
        print(f"Error in schedule_maintenance: {e}")
        return {"success": False, "message": "Unexpected server error"}

    finally:
        cursor.close()
        connection.close()


def fetch_tasks_by_status(status=None):
    """
    Fetch tasks filtered by status.
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Query for fetching tasks based on status
        if status and status.lower() != "all":
            query = """
                SELECT t.task_id, t.title, t.assigned_engineer, t.status, td.scheduled_at, td.started_at, td.ended_at,
                       tn.notes, tn.comments
                FROM maintenancetasks t
                JOIN taskdates td ON t.task_id = td.task_id
                LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
                WHERE LOWER(t.status) = LOWER(%s)
                ORDER BY td.scheduled_at ASC;
            """
            cursor.execute(query, (status,))
        else:
            # Fetch all tasks, sort "Upcoming" by scheduled_at
            query = """
                SELECT t.task_id, t.title, t.assigned_engineer, t.status, td.scheduled_at, td.started_at, td.ended_at,
                       tn.notes, tn.comments
                FROM maintenancetasks t
                JOIN taskdates td ON t.task_id = td.task_id
                LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
                ORDER BY 
                    CASE WHEN LOWER(t.status) = 'upcoming' THEN td.scheduled_at END ASC,
                    td.scheduled_at ASC;
            """
            cursor.execute(query)

        rows = cursor.fetchall()
        conn.close()

        tasks = []
        for row in rows:
            scheduled_at = row[4]
            started_at = row[5]
            ended_at = row[6]

            # Convert datetime fields to ISO format if they are not None
            task = {
                "task_id": row[0],
                "title": row[1],
                "assigned_engineer": row[2],
                "status": row[3],
                "scheduled_date": scheduled_at.date().isoformat() if scheduled_at else None,
                "scheduled_time": scheduled_at.time().isoformat() if scheduled_at else None,
                "started_at": started_at.isoformat() if isinstance(started_at, datetime.datetime) else None,
                "ended_at": ended_at.isoformat() if isinstance(ended_at, datetime.datetime) else None,
                "notes": row[7],
                "comments": row[8]
            }
            tasks.append(task)

        return {"success": True, "data": tasks}

    except Exception as e:
        print(f"Error in fetch_tasks_by_status: {e}")
        return {"success": False, "message": "An error occurred while fetching tasks"}


def get_task_counts_by_status():
    """
    Get counts of tasks grouped by their status.
    """
    try:
        # Get database connection
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=DictCursor)  # Use DictCursor for dict-like rows

        # SQL query to count tasks by status
        query = """
            SELECT status, COUNT(*) as count
            FROM maintenancetasks
            GROUP BY status
        """
        cursor.execute(query)
        result = cursor.fetchall()

        # Close cursor and connection
        cursor.close()
        connection.close()

        # Process result into a dictionary
        status_counts = {row["status"]: row["count"] for row in result}
        return {"success": True, "data": status_counts}
    except Exception as e:
        print(f"Error in get_task_counts_by_status: {e}")
        return {"success": False, "message": "Failed to fetch task counts"}


def fetch_task_details(task_id):
    """
    Fetch detailed information of a specific task by task_id.
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Query to fetch task details
        query = """
            SELECT t.task_id, t.title, t.assigned_engineer, t.status, 
                   td.scheduled_at, td.started_at, td.ended_at,
                   tn.notes, tn.comments
            FROM maintenancetasks t
            JOIN taskdates td ON t.task_id = td.task_id
            LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
            WHERE t.task_id = %s;
        """
        cursor.execute(query, (task_id,))
        row = cursor.fetchone()

        conn.close()

        if not row:
            return {"success": False, "message": "Task not found."}

        scheduled_at = row[4]
        started_at = row[5]
        ended_at = row[6]

        # Convert datetime fields to separate date and time components
        task_details = {
            "task_id": row[0],
            "title": row[1],
            "assigned_engineer": row[2],
            "status": row[3],
            "scheduled_date": scheduled_at.date().isoformat() if scheduled_at else None,
            "scheduled_time": scheduled_at.time().isoformat() if scheduled_at else None,
            "started_at": started_at.isoformat() if isinstance(started_at, datetime.datetime) else None,
            "ended_at": ended_at.isoformat() if isinstance(ended_at, datetime.datetime) else None,
            "notes": row[7],
            "comments": row[8],
        }

        return {"success": True, "data": task_details}

    except Exception as e:
        print(f"Error in fetch_task_details: {e}")
        return {"success": False, "message": "An error occurred while fetching task details."}


def update_task(task_id, new_status=None, updated_date=None, notes=None):
    """
    Update task details such as status, date, or notes.
    Supports simultaneous and independent updates.
    """
    import datetime

    try:
        # Ensure valid status transitions
        valid_transitions = {
            "Upcoming": ["In Progress", "Cancelled"],
            "Overdue": ["In Progress"],
            "In Progress": ["Completed"],
            "Cancelled": ["Upcoming"]
        }

        # Connect to the database
        conn = get_db_connection()
        if conn is None:
            return {"success": False, "message": "Database connection failed."}

        cursor = conn.cursor()

        # Fetch the current status of the task
        cursor.execute("SELECT status FROM maintenancetasks WHERE task_id = %s", (task_id,))
        result = cursor.fetchone()

        if not result:
            return {"success": False, "message": "Task not found."}

        # Fetch and clean the current status
        current_status = result[0].strip().title()  # Ensure consistent case
        if new_status:
            new_status = new_status.strip().title()

        # Initialize a flag to track changes
        changes_made = False

        # Validate and update status if it has changed
        if new_status and new_status != current_status:
            allowed_transitions = valid_transitions.get(current_status, [])
            if new_status not in allowed_transitions:
                return {"success": False, "message": f"Invalid transition from '{current_status}' to '{new_status}'."}

            # Update the task status
            cursor.execute(
                "UPDATE maintenancetasks SET status = %s WHERE task_id = %s",
                (new_status, task_id)
            )
            changes_made = True

            # Update fields in `taskdates` table for status transitions
            now = datetime.datetime.now()
            if current_status == "Upcoming" and new_status == "In Progress":
                cursor.execute(
                    "UPDATE taskdates SET started_at = %s, updated_at = %s WHERE task_id = %s",
                    (now, now, task_id)
                )
                changes_made = True

            if current_status == "In Progress" and new_status == "Completed":
                cursor.execute(
                    "UPDATE taskdates SET ended_at = %s, updated_at = %s WHERE task_id = %s",
                    (now, now, task_id)
                )
                changes_made = True

        # Update scheduled date/time if provided and the current status is "Upcoming" (unchanged)
        if updated_date and current_status == "Upcoming" and (not new_status or new_status == current_status):
            cursor.execute(
                "UPDATE taskdates SET scheduled_at = %s, updated_at = %s WHERE task_id = %s",
                (updated_date, datetime.datetime.now(), task_id)
            )
            changes_made = True

        # Add or update notes if provided
        if notes is not None:
            cursor.execute(
                "INSERT INTO tasknotes (task_id, notes) VALUES (%s, %s) "
                "ON CONFLICT (task_id) DO UPDATE SET notes = EXCLUDED.notes",
                (task_id, notes)
            )
            changes_made = True

        # Commit changes if any were made
        if changes_made:
            conn.commit()
            return {"success": True, "message": "Task updated successfully."}
        else:
            return {"success": False, "message": "No changes were made to the task."}

    except Exception as e:
        print(f"Error in update_task: {e}")
        return {"success": False, "message": "Unexpected server error."}

    finally:
        if 'cursor' in locals():
            cursor.close()
        if 'conn' in locals():
            conn.close()

# from app.db_connection import get_db_connection
# from datetime import datetime
# import datetime

# def schedule_maintenance(title, machine_name, assigned_engineer, scheduled_at, notes):
#     connection = get_db_connection()
#     if not connection:
#         return {"success": False, "message": "Database connection failed"}

#     try:
#         cursor = connection.cursor()

#         # Fetch the machine_id based on machine_name
#         cursor.execute("SELECT serial_number FROM machines WHERE name = %s", (machine_name,))
#         machine = cursor.fetchone()
#         if not machine:
#             return {"success": False, "message": "Machine not found"}

#         machine_id = machine[0]

#         # Default status to 'upcoming'
#         default_status = 'upcoming'

#         # Insert into maintenancetasks; MySQL doesn't support RETURNING so use lastrowid
#         insert_task_query = """
#         INSERT INTO maintenancetasks (machine_id, title, assigned_engineer, status)
#         VALUES (%s, %s, %s, %s)
#         """
#         cursor.execute(insert_task_query, (machine_id, title, assigned_engineer, default_status))
#         task_id = cursor.lastrowid

#         # Insert into taskdates
#         insert_date_query = """
#         INSERT INTO taskdates (task_id, scheduled_at)
#         VALUES (%s, %s)
#         """
#         cursor.execute(insert_date_query, (task_id, scheduled_at))

#         # Insert notes if provided
#         if notes:
#             insert_notes_query = """
#             INSERT INTO tasknotes (task_id, notes)
#             VALUES (%s, %s)
#             """
#             cursor.execute(insert_notes_query, (task_id, notes))

#         connection.commit()

#         return {"success": True, "message": "Maintenance scheduled successfully"}

#     except Exception as e:
#         print(f"Error in schedule_maintenance: {e}")
#         return {"success": False, "message": "Unexpected server error"}

#     finally:
#         cursor.close()
#         connection.close()


# def fetch_tasks_by_status(status=None):
#     """
#     Fetch tasks filtered by status.
#     """
#     try:
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         # Query for fetching tasks based on status
#         if status and status.lower() != "all":
#             query = """
#                 SELECT t.task_id, t.title, t.assigned_engineer, t.status, td.scheduled_at, td.started_at, td.ended_at,
#                        tn.notes, tn.comments
#                 FROM maintenancetasks t
#                 JOIN taskdates td ON t.task_id = td.task_id
#                 LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
#                 WHERE LOWER(t.status) = LOWER(%s)
#                 ORDER BY td.scheduled_at ASC;
#             """
#             cursor.execute(query, (status,))
#         else:
#             # Fetch all tasks, sort "upcoming" by scheduled_at
#             query = """
#                 SELECT t.task_id, t.title, t.assigned_engineer, t.status, td.scheduled_at, td.started_at, td.ended_at,
#                        tn.notes, tn.comments
#                 FROM maintenancetasks t
#                 JOIN taskdates td ON t.task_id = td.task_id
#                 LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
#                 ORDER BY 
#                     CASE WHEN LOWER(t.status) = 'upcoming' THEN td.scheduled_at END ASC,
#                     td.scheduled_at ASC;
#             """
#             cursor.execute(query)

#         rows = cursor.fetchall()
#         conn.close()

#         tasks = []
#         for row in rows:
#             scheduled_at = row[4]
#             started_at = row[5]
#             ended_at = row[6]

#             task = {
#                 "task_id": row[0],
#                 "title": row[1],
#                 "assigned_engineer": row[2],
#                 "status": row[3],
#                 "scheduled_date": scheduled_at.date().isoformat() if scheduled_at else None,
#                 "scheduled_time": scheduled_at.time().isoformat() if scheduled_at else None,
#                 "started_at": started_at.isoformat() if isinstance(started_at, datetime.datetime) else None,
#                 "ended_at": ended_at.isoformat() if isinstance(ended_at, datetime.datetime) else None,
#                 "notes": row[7],
#                 "comments": row[8]
#             }
#             tasks.append(task)

#         return {"success": True, "data": tasks}

#     except Exception as e:
#         print(f"Error in fetch_tasks_by_status: {e}")
#         return {"success": False, "message": "An error occurred while fetching tasks"}


# def get_task_counts_by_status():
#     """
#     Get counts of tasks grouped by their status.
#     """
#     try:
#         connection = get_db_connection()
#         cursor = connection.cursor(dictionary=True)

#         query = """
#             SELECT status, COUNT(*) as count
#             FROM maintenancetasks
#             GROUP BY status
#         """
#         cursor.execute(query)
#         result = cursor.fetchall()

#         cursor.close()
#         connection.close()

#         status_counts = {row["status"]: row["count"] for row in result}
#         return {"success": True, "data": status_counts}
#     except Exception as e:
#         print(f"Error in get_task_counts_by_status: {e}")
#         return {"success": False, "message": "Failed to fetch task counts"}


# def fetch_task_details(task_id):
#     """
#     Fetch detailed information of a specific task by task_id.
#     """
#     try:
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         query = """
#             SELECT t.task_id, t.title, t.assigned_engineer, t.status, 
#                    td.scheduled_at, td.started_at, td.ended_at,
#                    tn.notes, tn.comments
#             FROM maintenancetasks t
#             JOIN taskdates td ON t.task_id = td.task_id
#             LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
#             WHERE t.task_id = %s;
#         """
#         cursor.execute(query, (task_id,))
#         row = cursor.fetchone()

#         conn.close()

#         if not row:
#             return {"success": False, "message": "Task not found."}

#         scheduled_at = row[4]
#         started_at = row[5]
#         ended_at = row[6]

#         task_details = {
#             "task_id": row[0],
#             "title": row[1],
#             "assigned_engineer": row[2],
#             "status": row[3],
#             "scheduled_date": scheduled_at.date().isoformat() if scheduled_at else None,
#             "scheduled_time": scheduled_at.time().isoformat() if scheduled_at else None,
#             "started_at": started_at.isoformat() if isinstance(started_at, datetime.datetime) else None,
#             "ended_at": ended_at.isoformat() if isinstance(ended_at, datetime.datetime) else None,
#             "notes": row[7],
#             "comments": row[8],
#         }

#         return {"success": True, "data": task_details}

#     except Exception as e:
#         print(f"Error in fetch_task_details: {e}")
#         return {"success": False, "message": "An error occurred while fetching task details."}


# def update_task(task_id, new_status=None, updated_date=None, notes=None):
#     """
#     Update task details such as status, date, or notes.
#     Supports simultaneous and independent updates.
#     """
#     import datetime

#     try:
#         valid_transitions = {
#             "Upcoming": ["In Progress", "Cancelled"],
#             "Overdue": ["In Progress"],
#             "In Progress": ["Completed"],
#             "Cancelled": ["Upcoming"]
#         }

#         conn = get_db_connection()
#         if conn is None:
#             return {"success": False, "message": "Database connection failed."}

#         cursor = conn.cursor()

#         # Fetch the current status of the task
#         cursor.execute("SELECT status FROM maintenancetasks WHERE task_id = %s", (task_id,))
#         result = cursor.fetchone()

#         if not result:
#             return {"success": False, "message": "Task not found."}

#         current_status = result[0].strip().title()  # Normalize status
#         if new_status:
#             new_status = new_status.strip().title()

#         changes_made = False

#         if new_status and new_status != current_status:
#             allowed_transitions = valid_transitions.get(current_status, [])
#             if new_status not in allowed_transitions:
#                 return {"success": False, "message": f"Invalid transition from '{current_status}' to '{new_status}'."}

#             cursor.execute(
#                 "UPDATE maintenancetasks SET status = %s WHERE task_id = %s",
#                 (new_status, task_id)
#             )
#             changes_made = True

#             now = datetime.datetime.now()
#             if current_status == "Upcoming" and new_status == "In Progress":
#                 cursor.execute(
#                     "UPDATE taskdates SET started_at = %s, updated_at = %s WHERE task_id = %s",
#                     (now, now, task_id)
#                 )
#                 changes_made = True

#             if current_status == "In Progress" and new_status == "Completed":
#                 cursor.execute(
#                     "UPDATE taskdates SET ended_at = %s, updated_at = %s WHERE task_id = %s",
#                     (now, now, task_id)
#                 )
#                 changes_made = True

#         if updated_date and current_status == "Upcoming" and (not new_status or new_status == current_status):
#             cursor.execute(
#                 "UPDATE taskdates SET scheduled_at = %s, updated_at = %s WHERE task_id = %s",
#                 (updated_date, datetime.datetime.now(), task_id)
#             )
#             changes_made = True

#         if notes is not None:
#             cursor.execute(
#                 "INSERT INTO tasknotes (task_id, notes) VALUES (%s, %s) ON DUPLICATE KEY UPDATE notes = VALUES(notes)",
#                 (task_id, notes)
#             )
#             changes_made = True

#         if changes_made:
#             conn.commit()
#             return {"success": True, "message": "Task updated successfully."}
#         else:
#             return {"success": False, "message": "No changes were made to the task."}

#     except Exception as e:
#         print(f"Error in update_task: {e}")
#         return {"success": False, "message": "Unexpected server error."}

#     finally:
#         if 'cursor' in locals():
#             cursor.close()
#         if 'conn' in locals():
#             conn.close()