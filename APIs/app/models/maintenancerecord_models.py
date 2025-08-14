from app.db_connection import get_db_connection
import psycopg2.extras

def fetch_engineers():
    """
    Fetch active engineers from the logincredentials table.
    Only includes users with the 'engineer' role and active accounts.

    Returns:
        dict: Success status and data or error message.
    """
    try:
        # Get database connection
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=psycopg2.extras.DictCursor)

        # SQL query to fetch engineers
        query = """
            SELECT username
            FROM logincredentials
            WHERE LOWER(role) = 'service engineer' AND is_active = TRUE
        """
        cursor.execute(query)
        rows = cursor.fetchall()

        # Close cursor and connection
        cursor.close()
        connection.close()

        # Extract usernames from the query results
        engineers = [row['username'] for row in rows]

        return {"success": True, "data": engineers}

    except Exception as e:
        # Log and handle exceptions
        print(f"Error in fetch_engineers: {e}")
        return {"success": False, "message": "Failed to fetch engineers"}


def fetch_maintenance_records(machine=None, status=None, engineer=None):
    """
    Fetch maintenance history based on optional filters.
    Only 'Completed' and 'Cancelled' statuses are included.
    """
    try:
        # Get database connection
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=psycopg2.extras.DictCursor)

        # Base query
        query = """
            SELECT m.name AS machine_name, t.task_id, t.title, t.status, t.assigned_engineer,
                   td.scheduled_at, td.started_at, td.ended_at, tn.notes, tn.comments
            FROM maintenancetasks t
            JOIN machines m ON t.machine_id = m.serial_number
            JOIN taskdates td ON t.task_id = td.task_id
            LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
            WHERE LOWER(t.status) IN ('completed', 'cancelled')
        """

        # Filters
        filters = []
        params = []

        if machine:
            filters.append("LOWER(m.name) = LOWER(%s)")
            params.append(machine)

        if status:
            filters.append("LOWER(t.status) = LOWER(%s)")
            params.append(status)

        if engineer:
            filters.append("LOWER(t.assigned_engineer) = LOWER(%s)")
            params.append(engineer)

        # Apply filters
        if filters:
            query += " AND " + " AND ".join(filters)

        # Sorting
        query += " ORDER BY td.scheduled_at DESC"

        # Execute query
        cursor.execute(query, params)
        rows = cursor.fetchall()

        # Close resources
        cursor.close()
        connection.close()

        # Convert rows to list of dictionaries
        records = []
        for row in rows:
            records.append({
                "machine_name": row["machine_name"],
                "task_id": row["task_id"],
                "title": row["title"],
                "status": row["status"],
                "assigned_engineer": row["assigned_engineer"],
                "scheduled_at": row["scheduled_at"].isoformat() if row["scheduled_at"] else None,
                "started_at": row["started_at"].isoformat() if row["started_at"] else None,
                "ended_at": row["ended_at"].isoformat() if row["ended_at"] else None,
                "notes": row["notes"],
                "comments": row["comments"]
            })

        return {"success": True, "data": records}

    except Exception as e:
        print(f"Error in fetch_maintenance_records: {e}")
        return {"success": False, "message": "Failed to fetch maintenance history"}


def fetch_maintenance_logs(machine=None, status=None, engineer=None):
    """
    Fetch all maintenance logs based on optional filters.
    Includes all statuses unless filters are applied.
    """
    try:
        # Get database connection
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=psycopg2.extras.DictCursor)

        # Base query to include all statuses
        query = """
            SELECT m.name AS machine_name, t.task_id, t.title, t.status, t.assigned_engineer,
                   td.scheduled_at, td.started_at, td.ended_at, tn.notes, tn.comments
            FROM maintenancetasks t
            JOIN machines m ON t.machine_id = m.serial_number
            JOIN taskdates td ON t.task_id = td.task_id
            LEFT JOIN tasknotes tn ON t.task_id = tn.task_id
        """

        # Filters
        filters = []
        params = []

        if machine:
            filters.append("LOWER(m.name) = LOWER(%s)")
            params.append(machine)

        if status:
            filters.append("LOWER(t.status) = LOWER(%s)")
            params.append(status)

        if engineer:
            filters.append("LOWER(t.assigned_engineer) = LOWER(%s)")
            params.append(engineer)

        # Apply filters if any
        if filters:
            query += " WHERE " + " AND ".join(filters)

        # Sorting
        query += " ORDER BY td.scheduled_at DESC"

        # Execute query
        cursor.execute(query, params)
        rows = cursor.fetchall()

        # Close resources
        cursor.close()
        connection.close()

        # Convert rows to list of dictionaries
        records = []
        for row in rows:
            records.append({
                "machine_name": row["machine_name"],
                "task_id": row["task_id"],
                "title": row["title"],
                "status": row["status"],
                "assigned_engineer": row["assigned_engineer"],
                "scheduled_at": row["scheduled_at"].isoformat() if row["scheduled_at"] else None,
                "started_at": row["started_at"].isoformat() if row["started_at"] else None,
                "ended_at": row["ended_at"].isoformat() if row["ended_at"] else None,
                "notes": row["notes"],
                "comments": row["comments"]
            })

        return {"success": True, "data": records}

    except Exception as e:
        print(f"Error in fetch_maintenance_logs: {e}")
        return {"success": False, "message": "Failed to fetch maintenance logs"}

def update_comment(task_id, comment):
    """
    Update the comments for a given task in the tasknotes table.
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if task already has a notes entry
        cursor.execute("SELECT note_id FROM public.tasknotes WHERE task_id = %s", (task_id,))
        existing = cursor.fetchone()

        if existing:
            # Update existing comment
            cursor.execute("""
                UPDATE public.tasknotes
                SET comments = %s
                WHERE task_id = %s
            """, (comment, task_id))
        else:
            # Insert new entry if it doesn't exist
            cursor.execute("""
                INSERT INTO public.tasknotes (task_id, notes, comments)
                VALUES (%s, NULL, %s)
            """, (task_id, comment))

        conn.commit()
        cursor.close()
        conn.close()

        return {"success": True, "message": "Comment updated successfully."}

    except Exception as e:
        print(f"Error in update_comment: {e}")
        return {"success": False, "message": "Failed to update comment."}