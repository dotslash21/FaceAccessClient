// Copyright (c) 2019 Arunangshu Biswas
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
// BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package io.github.dotslash21.faclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import io.github.dotslash21.faclient.AuthStatusActivity;

public class BackendConnectionManager {
    private Context mContext;
    private static final String TAG = "BackendConnManager";
    private RequestQueue queue;
    private String backendUrlPath;

    private String authToken;

    private static final int FRAME_LIMIT = 100;
    private Bitmap frameArray[];
    private JSONArray jsonImageArray;
    private int frameCount;
    private JSONObject jsonObject;

    public BackendConnectionManager(Context context, String backendHostName, String backendPort) {
        this.mContext = context.getApplicationContext();
        this.backendUrlPath = "http://" + backendHostName + ":" + backendPort + "/";

        this.authToken = null;

        this.frameArray = new Bitmap[FRAME_LIMIT];
        this.jsonImageArray = new JSONArray();
        this.frameCount = 0;
        jsonObject = new JSONObject();
    }

    private static String convertInputStreamToString(InputStream inputStream)
            throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());

    }

    public void authenticateClientWithBackend(String clientId, String serialNumber) {
        try {
            URL url = new URL(backendUrlPath + "register-client");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            String requestDataString = "{\"clientId\":\""+clientId+"\", \"serialNumber\":\""+serialNumber+"\"}";

            JSONObject requestData = new JSONObject(
                    new Gson().toJson(requestDataString)
            );

            OutputStreamWriter writer = new OutputStreamWriter(
                    urlConnection.getOutputStream()
            );
            writer.write(requestData.toString());
            writer.flush();

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(
                        urlConnection.getInputStream()
                );

                JSONObject response = new JSONObject(
                        new Gson().toJson(convertInputStreamToString(inputStream))
                );

                this.authToken = response.getString("token");
            } else {
                Log.e(TAG, "Error authenticating with backend: statusCode " + statusCode);
                Toast.makeText(mContext, "Client registration on backend failed!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating with backend: " + e);
            Toast.makeText(mContext, "Client registration on backend failed!", Toast.LENGTH_LONG).show();
        }
    }

    public int pushFrame(Bitmap frame) {
        try {
            if (frameCount == FRAME_LIMIT) {
                return 1;
            }

            frameArray[frameCount++] = frame;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            frame.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            this.jsonImageArray.put(encodedImage);

            return 0;
        } catch (Exception e) {
            Log.d(TAG, "Error adding frame!" + e);
            return 2;
        }
    }

    public void authenticateFace(Context context) {
        queue = Volley.newRequestQueue(this.mContext);

        try {
            jsonObject.put("imageList", this.jsonImageArray);
        } catch (JSONException e) {
            Log.e(TAG,"Error creating JSONObject" + e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, backendUrlPath + "face-auth", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle Response
                        try {
                            String id = response.getString("id");
                            String name = response.getString("name");
                            double probability = response.getDouble("probability");


                            if (probability >= 0.80) {
                                Intent intent = new Intent(context, AuthStatusActivity.class);
                                intent.putExtra("AUTH_STATUS", "PASS");
                                intent.putExtra("ID", id);
                                intent.putExtra("NAME", name);
                                intent.putExtra("PROBABILITY", probability);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            } else {
                                Log.d(TAG, "AUTH STATUS FAIL 1");
                                Intent intent = new Intent(context, AuthStatusActivity.class);
                                intent.putExtra("AUTH_STATUS", "FAIL");
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error handling auth response: " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
//                        Intent intent = new Intent(context, AuthStatusActivity.class);
//                        intent.putExtra("AUTH_STATUS", "FAIL");
//                        context.startActivity(intent);

                        // TEMPORARY TESTING CODE
                        String id = "Arunangshu_Biswas";
                        String name = "Arunangshu Biswas";
                        double probability = 0.81;

                        if (probability >= 0.80) {
                            Log.d(TAG, "AUTH STATUS PASS 1");
                            Intent intent = new Intent(context, AuthStatusActivity.class);
                            intent.putExtra("AUTH_STATUS", "PASS");
                            intent.putExtra("ID", id);
                            intent.putExtra("NAME", name);
                            intent.putExtra("PROBABILITY", probability);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        } else {
                            Log.d(TAG, "AUTH STATUS FAIL 2");
                            Intent intent = new Intent(context, AuthStatusActivity.class);
                            intent.putExtra("AUTH_STATUS", "FAIL");
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        }
                    }
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                200*30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }
}
