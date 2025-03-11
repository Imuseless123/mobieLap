package com.example.week1_ex2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "AIzaSyDMqgBQPtxn3dnc4Qe_bDDU4YlL_agXvKU"; // Replace with your actual API key
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    private TextView responseTextView; // UI element to display response
    private TextView textView;
    private String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseTextView = findViewById(R.id.responseTextView); // Make sure you have this in activity_main.xml
        textView = findViewById(R.id.input);
        // Run network request in background

    }

    private void fetchAIResponse(String text) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + text + "\" }] }] }";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();
                // Update UI on main thread
                runOnUiThread(() -> {
                    try {
                        Log.i("MYWTF",this.JSONParser(response.toString()));
                        responseTextView.setText(this.JSONParser(response.toString()));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                runOnUiThread(() -> responseTextView.setText("Error: " + responseCode));
            }

//            textView.setText(this.text);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> responseTextView.setText("Request failed!"));
        }
    }

    private String JSONParser(String jsonString) throws JSONException {
        // Convert string to JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);

        // Navigate to the "text" field
        JSONArray candidates = jsonObject.getJSONArray("candidates");
        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        String text = parts.getJSONObject(0).getString("text");

        // Print the extracted text
        return text;
    }

    public void sendMessage(View view) {
        this.text = this.textView.getText().toString();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> fetchAIResponse(this.text));
    }

}
