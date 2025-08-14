from app.db_connection import get_db_connection
import joblib
import pandas as pd
import psycopg2

# Load models and scalers once when module is loaded
model_binary = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\final_binary_soft_voting_ensemble_compat.pkl")
# model_multi = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\final_multi_soft_voting_ensemble_compat.pkl")
model_multi = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\final_try.pkl")
scaler_binary = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\scaler_binary_6.pkl")
scaler_multi = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\scaler_multi_6.pkl")
label_encoder = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\label_encoder_6.pkl")

def get_reading_with_machine_name(reading_id):
    conn = psycopg2.connect(
        host="127.0.0.1",
        database="PrognosisEdge",
        user="postgres",
        password="1234",
        port="5432"
    )
    cursor = conn.cursor()
    cursor.execute("""
        SELECT mr.reading_id, mr.machine_id, m.name, mr.water_flow_rate,
               mr.pressure_stability_index, mr.detergent_level,
               mr.hydraulic_pressure, mr.temperature_fluctuation_index,
               mr.hydraulic_oil_temperature, mr.coolant_temperature
        FROM machinereadings mr
        JOIN machines m ON m.serial_number = mr.machine_id
        WHERE mr.reading_id = %s
    """, (reading_id,))
    row = cursor.fetchone()
    if row:
        return {
            "reading_id": row[0],
            "machine_id": row[1],
            "machine_name": row[2],
            "water_flow_rate": row[3],
            "pressure_stability_index": row[4],
            "detergent_level": row[5],
            "hydraulic_pressure": row[6],
            "temperature_fluctuation_index": row[7],
            "hydraulic_oil_temperature": row[8],
            "coolant_temperature": row[9]
        }
    return None

def predict_failure(machine_data):
    
    try:
        binary_df = pd.DataFrame([[
            machine_data["water_flow_rate"],
            machine_data["temperature_fluctuation_index"],
            machine_data["pressure_stability_index"],
            machine_data["detergent_level"],
            machine_data["hydraulic_pressure"],
            machine_data["coolant_temperature"]
        ]], columns=[
            "Water_Flow_Rate", "Temperature_Fluctuation_Index", "Pressure_Stability_Index",
            "Detergent_Level", "Hydraulic_Pressure", "Coolant_Temperature"
        ])

        multi_df = pd.DataFrame([[
            machine_data["water_flow_rate"],
            machine_data["temperature_fluctuation_index"],
            machine_data["pressure_stability_index"],
            machine_data["detergent_level"],
            machine_data["hydraulic_pressure"],
            machine_data["hydraulic_oil_temperature"]
        ]], columns=[
            "Water_Flow_Rate", "Temperature_Fluctuation_Index", "Pressure_Stability_Index",
            "Detergent_Level", "Hydraulic_Pressure", "Hydraulic_Oil_Temperature"
        ])

        binary_scaled = scaler_binary.transform(binary_df)
        multi_scaled = scaler_multi.transform(multi_df)

        # Get probabilities
        binary_proba = model_binary.predict_proba(binary_scaled)[0]
        multi_proba = model_multi.predict_proba(multi_scaled)[0]
        
        # Get class labels and indices
        failure_classes = label_encoder.classes_
        no_failure_idx = list(failure_classes).index("No Failure")
        
        # Binary decision
        binary_threshold = 0.5
        binary_pred = 1 if binary_proba[1] > binary_threshold else 0
        binary_confidence = binary_proba[1] if binary_pred == 1 else binary_proba[0]
        
        # Get RANKED multiclass predictions (best to worst)
        ranked_indices = multi_proba.argsort()[::-1]  # Descending order
        
        # Extract top predictions with their info
        predictions_ranked = []
        for i, idx in enumerate(ranked_indices):
            predictions_ranked.append({
                'rank': i + 1,
                'index': idx,
                'label': failure_classes[idx],
                'probability': multi_proba[idx],
                'is_no_failure': idx == no_failure_idx
            })
        
        print(f"[PREDICTION] Binary: {binary_pred} (conf: {binary_confidence:.3f})")
        print(f"[MULTICLASS RANKING]:")
        for pred in predictions_ranked[:4]:  # Show top 4
            print(f"  {pred['rank']}. {pred['label']}: {pred['probability']:.3f}")
        
        # ENHANCED DECISION LOGIC - FIRST CHOICE ONLY
        if binary_pred == 0:
            # Binary says no failure - generally trust it unless multiclass is very confident about a specific failure
            top_pred = predictions_ranked[0]
            if not top_pred['is_no_failure'] and top_pred['probability'] > 0.85:
                # Very high confidence multiclass failure overrides binary
                final_binary_pred = 1
                final_failure_type = top_pred['label']
                decision_reason = f"Very high confidence multiclass ({top_pred['probability']:.3f}) overrides binary"
            else:
                final_binary_pred = 0
                final_failure_type = "No Failure"
                decision_reason = f"Binary no failure (conf: {binary_confidence:.3f})"
                
        else:  # binary_pred == 1
            # Binary says failure - use top multiclass prediction
            top_pred = predictions_ranked[0]
            
            if top_pred['is_no_failure']:
                # Problem: Top prediction is "No Failure" but binary says failure
                print(f"[CONFLICT] Binary=1 but top multiclass is 'No Failure'")
                
                # Find best NON-"No Failure" prediction (first one only)
                best_failure_pred = None
                for pred in predictions_ranked:
                    if not pred['is_no_failure']:
                        best_failure_pred = pred
                        break
                
                if best_failure_pred is None:
                    # This shouldn't happen but safety check
                    final_binary_pred = 0
                    final_failure_type = "No Failure"
                    decision_reason = "Safety fallback: no failure types available"
                    
                elif binary_confidence > 0.75 and best_failure_pred['probability'] > 0.15:
                    # High confidence binary + reasonable failure type probability
                    final_binary_pred = 1
                    final_failure_type = best_failure_pred['label']
                    decision_reason = f"High conf binary ({binary_confidence:.3f}) + first failure type: {best_failure_pred['label']} ({best_failure_pred['probability']:.3f})"
                    
                elif binary_confidence > 0.6 and best_failure_pred['probability'] > 0.2:
                    # Medium confidence binary + decent failure type probability
                    final_binary_pred = 1
                    final_failure_type = best_failure_pred['label']
                    decision_reason = f"Medium conf binary ({binary_confidence:.3f}) + failure type: {best_failure_pred['label']} ({best_failure_pred['probability']:.3f})"
                    
                else:
                    # Low confidence scenario - trust the "No Failure" prediction
                    final_binary_pred = 0
                    final_failure_type = "No Failure"
                    decision_reason = f"Low confidence, trusting multiclass 'No Failure'"
                        
            else:
                # Top prediction is a specific failure type
                if top_pred['probability'] > 0.6:
                    # Good confidence in top failure type
                    final_binary_pred = 1
                    final_failure_type = top_pred['label']
                    decision_reason = f"Good confidence in top prediction: {top_pred['label']} ({top_pred['probability']:.3f})"
                    
                elif binary_confidence > 0.7:
                    # High confidence binary but low confidence multiclass - use top prediction anyway
                    final_binary_pred = 1
                    final_failure_type = top_pred['label']
                    decision_reason = f"High conf binary, using top prediction: {top_pred['label']} ({top_pred['probability']:.3f})"
                        
                else:
                    # Both models have medium/low confidence
                    if top_pred['probability'] > 0.25:
                        final_binary_pred = 1
                        final_failure_type = top_pred['label']
                        decision_reason = f"Medium confidence consensus: {top_pred['label']} ({top_pred['probability']:.3f})"
                    else:
                        # Very low confidence - be conservative
                        final_binary_pred = 0
                        final_failure_type = "No Failure"
                        decision_reason = f"Low confidence across all predictions, defaulting to No Failure"

        # FINAL CONSISTENCY CHECKS
        if final_binary_pred == 1 and final_failure_type == "No Failure":
            print(f"[ERROR] Still inconsistent after logic!")
            final_binary_pred = 0
            final_failure_type = "No Failure"
            decision_reason = "Consistency enforcement: forced to No Failure"
            
        if final_binary_pred == 0 and final_failure_type != "No Failure":
            print(f"[ERROR] Opposite inconsistency detected!")
            final_failure_type = "No Failure"
            decision_reason = "Consistency enforcement: forced failure type to No Failure"

        print(f"[DECISION] {decision_reason}")
        print(f"[FINAL] Binary: {final_binary_pred}, Failure Type: {final_failure_type}")

        return {
            "machine_failure": bool(final_binary_pred),
            "failure_type": final_failure_type,
            "success": True,
            "confidence_scores": {
                "binary_confidence": float(binary_confidence),
                "multiclass_confidence": float(predictions_ranked[0]['probability']),
                "decision_reason": decision_reason
            },
            "debug_info": {
                "binary_proba": binary_proba.tolist(),
                "multi_proba": multi_proba.tolist(),
                "failure_classes": failure_classes.tolist(),
                "ranked_predictions": [
                    {
                        "rank": p['rank'],
                        "label": p['label'], 
                        "probability": float(p['probability'])
                    } for p in predictions_ranked[:5]
                ],
                "final_binary": int(final_binary_pred),
                "final_failure_type": final_failure_type
            }
        }
        
    except Exception as e:
        print(f"[ERROR] Prediction failed: {str(e)}")
        import traceback
        traceback.print_exc()
        return {
            "success": False,
            "message": f"Prediction error: {str(e)}"
        }
    
def update_prediction_result(reading_id, machine_failure, failure_type):
    try:
        print(f"[Update Attempt] reading_id={reading_id}, failure={machine_failure}, type={failure_type}")
        print(f"[Debug] machine_failure type: {type(machine_failure)}, value: {machine_failure}")
        print(f"[Debug] failure_type type: {type(failure_type)}, value: '{failure_type}'")
        
        conn = get_db_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT 1 FROM machinereadings WHERE reading_id = %s", (reading_id,))
        if cursor.fetchone() is None:
            print(f"No reading found with reading_id={reading_id}. Skipping update.")
            cursor.close()
            conn.close()
            return False

        # ENSURE CORRECT TYPES - Convert boolean properly
        machine_failure_bool = bool(machine_failure)
        failure_type_str = str(failure_type) if failure_type else "No Failure"
        
        print(f"[Debug] Converting to: machine_failure_bool={machine_failure_bool}, failure_type_str='{failure_type_str}'")
        
        # Check for inconsistency before database update
        if machine_failure_bool and failure_type_str == "No Failure":
            print(f"[CRITICAL] Database update inconsistency detected!")
            print(f"  machine_failure_bool: {machine_failure_bool}")
            print(f"  failure_type_str: '{failure_type_str}'")
            print(f"  → Forcing consistency: machine_failure=False")
            machine_failure_bool = False
            failure_type_str = "No Failure"

        cursor.execute("""
            UPDATE machinereadings
            SET machine_failure = %s,
                failure_type = %s
            WHERE reading_id = %s
        """, (machine_failure_bool, failure_type_str, reading_id))

        conn.commit()
        print(f"Updated reading_id={reading_id} → machine_failure={machine_failure_bool}, failure_type='{failure_type_str}'")

        # VERIFY THE UPDATE - Read back what was actually saved
        cursor.execute("""
            SELECT machine_failure, failure_type 
            FROM machinereadings 
            WHERE reading_id = %s
        """, (reading_id,))
        
        saved_result = cursor.fetchone()
        if saved_result:
            saved_failure, saved_type = saved_result
            print(f"[VERIFICATION] Database contains: machine_failure={saved_failure}, failure_type='{saved_type}'")
            
            # Check if what we saved matches what we intended
            if saved_failure != machine_failure_bool or saved_type != failure_type_str:
                print(f"[ERROR] Database save mismatch!")
                print(f"  Intended: failure={machine_failure_bool}, type='{failure_type_str}'")
                print(f"  Actual:   failure={saved_failure}, type='{saved_type}'")

        cursor.close()
        conn.close()
        return True

    except Exception as e:
        print(f"Error updating prediction result for reading_id={reading_id}: {e}")
        import traceback
        traceback.print_exc()
        return False
    
# from app.db_connection import get_db_connection
# import joblib
# import pandas as pd
# import psycopg2

# # Load models and scalers once when module is loaded
# model_binary = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\final_binary_soft_voting_ensemble_compat.pkl")
# model_multi = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\final_multi_soft_voting_ensemble_compat.pkl")
# scaler_binary = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\scaler_binary_6.pkl")
# scaler_multi = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\scaler_multi_6.pkl")
# label_encoder = joblib.load(r"C:\Users\ghaza\Desktop\FYP\Application\AI\models\label_encoder.pkl")

# def get_reading_with_machine_name(reading_id):
#     conn = psycopg2.connect(
#         host="127.0.0.1",
#         database="PrognosisEdge",
#         user="postgres",
#         password="1234",
#         port="5432"
#     )
#     cursor = conn.cursor()
#     cursor.execute("""
#         SELECT mr.reading_id, mr.machine_id, m.name, mr.water_flow_rate,
#                mr.pressure_stability_index, mr.detergent_level,
#                mr.hydraulic_pressure, mr.temperature_fluctuation_index,
#                mr.hydraulic_oil_temperature, mr.coolant_temperature
#         FROM machinereadings mr
#         JOIN machines m ON m.serial_number = mr.machine_id
#         WHERE mr.reading_id = %s
#     """, (reading_id,))
#     row = cursor.fetchone()
#     if row:
#         return {
#             "reading_id": row[0],
#             "machine_id": row[1],
#             "machine_name": row[2],
#             "water_flow_rate": row[3],
#             "pressure_stability_index": row[4],
#             "detergent_level": row[5],
#             "hydraulic_pressure": row[6],
#             "temperature_fluctuation_index": row[7],
#             "hydraulic_oil_temperature": row[8],
#             "coolant_temperature": row[9]
#         }
#     return None


# def predict_failure(machine_data):
#     try:
#         binary_df = pd.DataFrame([[
#             machine_data["water_flow_rate"],
#             machine_data["temperature_fluctuation_index"],
#             machine_data["pressure_stability_index"],
#             machine_data["detergent_level"],
#             machine_data["hydraulic_pressure"],
#             machine_data["coolant_temperature"]
#         ]], columns=[
#             "Water_Flow_Rate", "Temperature_Fluctuation_Index", "Pressure_Stability_Index",
#             "Detergent_Level", "Hydraulic_Pressure", "Coolant_Temperature"
#         ])

#         multi_df = pd.DataFrame([[
#             machine_data["water_flow_rate"],
#             machine_data["temperature_fluctuation_index"],
#             machine_data["pressure_stability_index"],
#             machine_data["detergent_level"],
#             machine_data["hydraulic_pressure"],
#             machine_data["hydraulic_oil_temperature"]
#         ]], columns=[
#             "Water_Flow_Rate", "Temperature_Fluctuation_Index", "Pressure_Stability_Index",
#             "Detergent_Level", "Hydraulic_Pressure", "Hydraulic_Oil_Temperature"
#         ])

#         binary_scaled = scaler_binary.transform(binary_df)
#         multi_scaled = scaler_multi.transform(multi_df)

#         binary_pred = model_binary.predict(binary_scaled)[0]

#         if binary_pred == 1:
#             multi_encoded = model_multi.predict(multi_scaled)[0]
#             failure_type = label_encoder.inverse_transform([multi_encoded])[0]
#         else:
#             failure_type = "No Failure"

#         return {
#             "machine_failure": bool(binary_pred),
#             "failure_type": failure_type,
#             "success": True
#         }
#     except Exception as e:
#         return {
#             "success": False,
#             "message": f"Prediction error: {str(e)}"
#         }

# def update_prediction_result(reading_id, machine_failure, failure_type):
#     try:
#         print(f"[Update Attempt] reading_id={reading_id}, failure={machine_failure}, type={failure_type}")
#         conn = get_db_connection()
#         cursor = conn.cursor()

#         cursor.execute("SELECT 1 FROM machinereadings WHERE reading_id = %s", (reading_id,))
#         if cursor.fetchone() is None:
#             print(f"No reading found with reading_id={reading_id}. Skipping update.")
#             cursor.close()
#             conn.close()
#             return False

#         cursor.execute("""
#             UPDATE machinereadings
#             SET machine_failure = %s,
#                 failure_type = %s
#             WHERE reading_id = %s
#         """, (machine_failure, failure_type, reading_id))

#         conn.commit()
#         print(f"Updated reading_id={reading_id} → machine_failure={machine_failure}, failure_type='{failure_type}'")

#         cursor.close()
#         conn.close()
#         return True

#     except Exception as e:
#         print(f"Error updating prediction result for reading_id={reading_id}: {e}")
#         return False

