from app.db_connection import get_db_connection
from psycopg2.extras import RealDictCursor

# ---------- SYSTEM SUPERVISOR REPORTS ----------
def generate_and_fetch_machinereports(start_date, end_date, username):
    try:
        print(f"=== SS REPORT FUNCTION DEBUG ===")
        print(f"Input params: start_date={start_date}, end_date={end_date}, username={username}")
        
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=RealDictCursor)
        print("Database connection established")

        # Step 1: Check for existing reports
        print("--- Step 1: Checking existing machine reports ---")
        check_query = """
        SELECT * FROM machinereports
        WHERE date_range_start = %s AND date_range_end = %s
        """
        cursor.execute(check_query, (start_date, end_date))
        existing_report = cursor.fetchone()
        print(f"Existing report check: {existing_report}")
        
        if existing_report:
            print("Machine report already exists - fetching all reports for user")
            # FIXED: Fetch all existing reports for this user (same as SE pattern)
            fetch_query = """
            SELECT report_id, serial_number, title, created_by, date_range_start, date_range_end,
                   total_readings, failure_count, no_failure_count,
                   detergent_level_low_count, pressure_drop_count, temperature_anomaly_count,
                   water_flow_issue_count, avg_water_flow_rate, avg_pressure_stability_index,
                   avg_detergent_level, avg_hydraulic_pressure, avg_temperature_fluctuation_index,
                   avg_hydraulic_oil_temperature, avg_coolant_temperature, failure_prediction_rate,
                   created_at
            FROM machinereports 
            WHERE created_by = %s 
            ORDER BY created_at DESC
            """
            cursor.execute(fetch_query, (username,))
            all_reports = cursor.fetchall()
            cursor.close()
            connection.close()
            return {"success": True, "report_stored": False, "message": "Machine report already exists for this date range.", "reports": all_reports}

        # Step 2: Get aggregated machine data (only if report doesn't exist)
        print("--- Step 2: Getting aggregated machine data ---")
        aggregation_query = """
        SELECT machine_id,
               COUNT(*) AS total_readings,
               COUNT(*) FILTER (WHERE machine_failure) AS failure_count,
               COUNT(*) FILTER (WHERE NOT machine_failure) AS no_failure_count,
               COUNT(*) FILTER (WHERE failure_type = 'Detergent Level Low') AS detergent_level_low_count,
               COUNT(*) FILTER (WHERE failure_type = 'Pressure Drop') AS pressure_drop_count,
               COUNT(*) FILTER (WHERE failure_type = 'Temperature Anomaly') AS temperature_anomaly_count,
               COUNT(*) FILTER (WHERE failure_type = 'Water Flow Issue') AS water_flow_issue_count,
               AVG(water_flow_rate) AS avg_water_flow_rate,
               AVG(pressure_stability_index) AS avg_pressure_stability_index,
               AVG(detergent_level) AS avg_detergent_level,
               AVG(hydraulic_pressure) AS avg_hydraulic_pressure,
               AVG(temperature_fluctuation_index) AS avg_temperature_fluctuation_index,
               AVG(hydraulic_oil_temperature) AS avg_hydraulic_oil_temperature,
               AVG(coolant_temperature) AS avg_coolant_temperature,
               (COUNT(*) FILTER (WHERE machine_failure)::float / COUNT(*)) * 100.0 AS failure_prediction_rate
        FROM machinereadings
        WHERE timestamp >= %s AND timestamp < (%s::date + INTERVAL '1 day')
        GROUP BY machine_id
        """
        
        print(f"Executing aggregation query...")
        cursor.execute(aggregation_query, (start_date, end_date))
        data = cursor.fetchall()
        print(f"Query executed. Found {len(data)} machines")
        
        if not data:
            print(" No data found for date range")
            cursor.close()
            connection.close()
            return {"success": False, "message": "No machine data found for the specified date range"}

        # Step 3: Insert machine reports
        print("--- Step 3: Inserting machine reports ---")
        insert_query = """
        INSERT INTO machinereports (
            serial_number, title, created_by, date_range_start, date_range_end,
            total_readings, failure_count, no_failure_count,
            detergent_level_low_count, pressure_drop_count, temperature_anomaly_count,
            water_flow_issue_count, avg_water_flow_rate, avg_pressure_stability_index,
            avg_detergent_level, avg_hydraulic_pressure, avg_temperature_fluctuation_index,
            avg_hydraulic_oil_temperature, avg_coolant_temperature, failure_prediction_rate
        ) VALUES (
            %s, 'Auto Generated Report', %s, %s, %s,
            %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s, %s, %s
        ) RETURNING *
        """

        reports = []
        print(f"Starting to insert {len(data)} reports...")
        
        for i, row in enumerate(data):
            print(f"Inserting report {i+1} for machine_id: {row['machine_id']}")
            try:
                cursor.execute(insert_query, (
                    row["machine_id"], username, start_date, end_date,
                    row["total_readings"], row["failure_count"], row["no_failure_count"],
                    row["detergent_level_low_count"], row["pressure_drop_count"], row["temperature_anomaly_count"],
                    row["water_flow_issue_count"], row["avg_water_flow_rate"], row["avg_pressure_stability_index"],
                    row["avg_detergent_level"], row["avg_hydraulic_pressure"], row["avg_temperature_fluctuation_index"],
                    row["avg_hydraulic_oil_temperature"], row["avg_coolant_temperature"], row["failure_prediction_rate"]
                ))
                inserted_report = cursor.fetchone()
                reports.append(inserted_report)
                print(f"Successfully inserted report {i+1}")
            except Exception as insert_error:
                print(f" Error inserting report {i+1}: {str(insert_error)}")
                raise insert_error

        print("--- Step 4: Committing transaction ---")
        connection.commit()
        print(f"Successfully committed {len(reports)} reports")
        
        # Step 5: Fetch all reports for this user (same as SE pattern)
        print("--- Step 5: Fetching all reports for user ---")
        fetch_query = """
        SELECT report_id, serial_number, title, created_by, date_range_start, date_range_end,
               total_readings, failure_count, no_failure_count,
               detergent_level_low_count, pressure_drop_count, temperature_anomaly_count,
               water_flow_issue_count, avg_water_flow_rate, avg_pressure_stability_index,
               avg_detergent_level, avg_hydraulic_pressure, avg_temperature_fluctuation_index,
               avg_hydraulic_oil_temperature, avg_coolant_temperature, failure_prediction_rate,
               created_at
        FROM machinereports 
        WHERE created_by = %s 
        ORDER BY created_at DESC
        """
        cursor.execute(fetch_query, (username,))
        all_reports = cursor.fetchall()
        
        cursor.close()
        connection.close()
        
        return {"success": True, "report_stored": True, "message": "New machine reports generated successfully", "reports": all_reports}

    except Exception as e:
        print(f"ERROR in generate_and_fetch_machinereports: {str(e)}")
        print(f"Error type: {type(e).__name__}")
        import traceback
        print("Full traceback:")
        traceback.print_exc()
        return {"success": False, "message": f"Error generating machine report: {str(e)}"}

    finally:
        if 'cursor' in locals():
            cursor.close()
        if 'connection' in locals():
            connection.close()
        print("=== SS REPORT FUNCTION COMPLETED ===")


# ---------- SERVICE ENGINEER REPORTS ----------
def generate_and_fetch_workorderreports(start_date, end_date, username):
    try:
        print(f"=== SE REPORT FUNCTION DEBUG ===")
        print(f"Input params: start_date={start_date}, end_date={end_date}, username={username}")
        
        connection = get_db_connection()
        cursor = connection.cursor(cursor_factory=RealDictCursor)
        print("Database connection established")

        # Step 1: Check for existing reports
        print("--- Step 1: Checking existing work order reports ---")
        check_query = """
        SELECT * FROM workorderreports
        WHERE created_by = %s AND date_range_start = %s AND date_range_end = %s
        """
        cursor.execute(check_query, (username, start_date, end_date))
        existing_report = cursor.fetchone()
        print(f"Existing report check: {existing_report}")
        
        if existing_report:
            print("Work order report already exists - fetching all reports for user")
            # Fetch all existing reports for this user (same as SS pattern) with proper serialization
            fetch_query = """
            SELECT report_id, title, created_by, date_range_start, date_range_end,
                   total_work_orders, completed_work_orders, in_progress_work_orders,
                   cancelled_work_orders, overdue_work_orders, 
                   EXTRACT(EPOCH FROM average_completion_time)/3600.0 AS average_completion_time,
                   created_at
            FROM workorderreports 
            WHERE created_by = %s 
            ORDER BY created_at DESC
            """
            cursor.execute(fetch_query, (username,))
            all_reports = cursor.fetchall()
            return {"success": True, "report_stored": False, "message": "Work order report already exists.", "reports": all_reports}

        # Step 2: Get aggregated work order data
        print("--- Step 2: Getting aggregated work order data ---")
        aggregation_query = """
        SELECT COUNT(*) AS total_work_orders,
               COUNT(*) FILTER (WHERE status = 'Completed') AS completed_work_orders,
               COUNT(*) FILTER (WHERE status = 'In Progress') AS in_progress_work_orders,
               COUNT(*) FILTER (WHERE status = 'Cancelled') AS cancelled_work_orders,
               COUNT(*) FILTER (WHERE status = 'Upcoming (Not Started)' AND scheduled_at < NOW()) AS overdue_work_orders,
               CASE 
                   WHEN AVG(EXTRACT(EPOCH FROM (ended_at - started_at))/3600.0) IS NOT NULL 
                   THEN INTERVAL '1 hour' * AVG(EXTRACT(EPOCH FROM (ended_at - started_at))/3600.0)
                   ELSE INTERVAL '0 hours'
               END AS average_completion_time
        FROM maintenancetasks t
        JOIN taskdates d ON t.task_id = d.task_id
        WHERE t.assigned_engineer = %s 
        AND d.created_at >= %s::date 
        AND d.created_at < (%s::date + INTERVAL '1 day')
        """
        
        print(f"Executing aggregation query for engineer: {username}")
        cursor.execute(aggregation_query, (username, start_date, end_date))
        row = cursor.fetchone()
        print(f"Aggregation complete. Found data: {dict(row)}")
        
        if row["total_work_orders"] == 0:
            print(" No work orders found for this engineer in the date range")
            return {"success": False, "message": "No work orders found for the specified date range"}

        # Step 3: Insert work order report
        print("--- Step 3: Inserting work order report ---")
        insert_query = """
        INSERT INTO workorderreports (
            title, created_by, date_range_start, date_range_end,
            total_work_orders, completed_work_orders, in_progress_work_orders,
            cancelled_work_orders, overdue_work_orders, average_completion_time
        ) VALUES (
            'Auto Generated Work Order Report', %s, %s, %s,
            %s, %s, %s, %s, %s, %s
        ) RETURNING *
        """
        
        try:
            print(f"Inserting report for engineer: {username}")
            cursor.execute(insert_query, (
                username, start_date, end_date,
                row["total_work_orders"], row["completed_work_orders"],
                row["in_progress_work_orders"], row["cancelled_work_orders"],
                row["overdue_work_orders"], row["average_completion_time"]
            ))
            inserted_report = cursor.fetchone()
            print(f"Successfully inserted work order report")
        except Exception as insert_error:
            print(f" Error inserting work order report: {str(insert_error)}")
            raise insert_error

        # Step 4: Fetch all reports for this user (same as SS pattern)
        print("--- Step 4: Fetching all reports for user ---")
        fetch_query = """
        SELECT report_id, title, created_by, date_range_start, date_range_end,
               total_work_orders, completed_work_orders, in_progress_work_orders,
               cancelled_work_orders, overdue_work_orders, 
               EXTRACT(EPOCH FROM average_completion_time)/3600.0 AS average_completion_time,
               created_at
        FROM workorderreports 
        WHERE created_by = %s 
        ORDER BY created_at DESC
        """
        cursor.execute(fetch_query, (username,))
        all_reports = cursor.fetchall()

        print("--- Step 5: Committing transaction ---")
        connection.commit()
        print(f"Transaction committed. Work order report created successfully")
        
        return {"success": True, "report_stored": True, "reports": all_reports}

    except Exception as e:
        print(f"ERROR in generate_and_fetch_workorderreports: {str(e)}")
        print(f"Error type: {type(e).__name__}")
        import traceback
        print("Full traceback:")
        traceback.print_exc()
        return {"success": False, "message": f"Error generating work order report: {str(e)}"}

    finally:
        if 'cursor' in locals():
            cursor.close()
        if 'connection' in locals():
            connection.close()
        print("=== SE REPORT FUNCTION COMPLETED ===")