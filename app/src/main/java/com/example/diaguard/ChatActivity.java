package com.example.diaguard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {
    private LinearLayout chatContainer;
    private EditText messageInput;
    private Button sendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chatContainer);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String userMessage = messageInput.getText().toString();
            if (!userMessage.isEmpty()) {
                addMessage("You: " + userMessage);
                sendMessageToGPT(userMessage);
                messageInput.setText("");
            }
        });
    }

    private void addMessage(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        chatContainer.addView(textView);
    }


    private void sendMessageToGPT(String message) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openai.com/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "gpt-3.5-turbo");
                jsonBody.put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", message)));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes());
                    os.flush();
                }

                // Читаем ответ сервера (успешный или с ошибкой)
                InputStream inputStream;
                if (conn.getResponseCode() >= 400) {
                    inputStream = conn.getErrorStream(); // Ошибка API
                } else {
                    inputStream = conn.getInputStream(); // Успешный ответ
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String response;
                StringBuilder responseStrBuilder = new StringBuilder();
                while ((response = in.readLine()) != null) {
                    responseStrBuilder.append(response);
                }
                in.close();

                String responseString = responseStrBuilder.toString();
                System.out.println("Server Response: " + responseString); // Лог ответа

                if (conn.getResponseCode() >= 400) {
                    runOnUiThread(() -> addMessage("Ошибка: " + responseString));
                    return;
                }

                JSONObject jsonResponse = new JSONObject(responseString);
                String reply = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                runOnUiThread(() -> addMessage("ChatGPT: " + reply));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> addMessage("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

}

