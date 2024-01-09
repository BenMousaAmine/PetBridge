package com.example.petbridge.messaging;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMNotification {

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "BMKvjoHBUMenSIjS-TjoaSxlw4HYwM8bxXgxgKfFwwIo49M6w2pgG_LwRVqMjkWRAl-mB8GgEgiN0c5euODceBI";

    public static void sendNotification(Context context , String token, String message, String title) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject json = new JSONObject();
        try {
            json.put("to", token);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", title);
            notificationObj.put("body", message);

            json.put("notification", notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_API, json,
                    response -> {

                    },
                    error -> {

                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + SERVER_KEY);
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

