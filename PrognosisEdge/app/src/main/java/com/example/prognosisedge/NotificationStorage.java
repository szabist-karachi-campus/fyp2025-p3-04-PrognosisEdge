package com.example.prognosisedge;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String PREF_NAME = "notification_pref";
    private static final String KEY_NOTIFICATIONS = "notifications";

    //  Save notifications to SharedPreferences
    public static void saveNotifications(Context context, List<AppNotification> notificationList) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonArray = new JSONArray();
        for (AppNotification n : notificationList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("category", n.getCategory());
                obj.put("title", n.getTitle());
                obj.put("description", n.getDescription());
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString(KEY_NOTIFICATIONS, jsonArray.toString());
        editor.apply();
    }

    // Load notifications from SharedPreferences
    public static List<AppNotification> loadNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_NOTIFICATIONS, null);
        List<AppNotification> list = new ArrayList<>();

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    list.add(new AppNotification(
                            obj.getString("category"),
                            obj.getString("title"),
                            obj.getString("description")
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}
