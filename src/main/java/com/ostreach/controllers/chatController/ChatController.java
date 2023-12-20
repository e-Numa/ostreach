package com.ostreach.controllers.chatController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatController {
    private static String CHAT_ENGINE_PROJECT_ID = "38943d99-a9ed-43f5-b41c-edb2142b8ee1";
    private static String CHAT_ENGINE_PRIVATE_KEY = "41121325-d2fe-4204-84ff-dc11bae9a6d7";

    @CrossOrigin
    @RequestMapping(path = "/chat/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getLogin(@RequestBody HashMap<String, String> request) {

        HttpURLConnection con = null;

        try {

            // Create GET request
            URL url = new URL("https://api.chatengine.io/users/me");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // Set headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Project-ID", CHAT_ENGINE_PROJECT_ID);
            con.setRequestProperty("User-Name", request.get("email"));
            con.setRequestProperty("User-Secret", request.get("email"));

            // Generate response String
            StringBuilder responseStr = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    responseStr.append(responseLine.trim());
                }
            }

            // Jsonify + return response
            Map<String, Object> response = new Gson().fromJson(
                    responseStr.toString(), new TypeToken<HashMap<String, Object>>() {
                    }.getType());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {

            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        } finally {

            if (con != null) {
                con.disconnect();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/chat/signup", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> newSignup(@RequestBody HashMap<String, String> request) {

        HttpURLConnection con = null;

        try {

            // Create POST request
            URL url = new URL("https://api.chatengine.io/users");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            // Set headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Private-Key", CHAT_ENGINE_PRIVATE_KEY);

            // Add request body
            con.setDoOutput(true);

            Map<String, String> body = new HashMap<String, String>();
            body.put("username", request.get("email"));
            body.put("secret", request.get("email"));
            body.put("email", request.get("email"));
            body.put("first_name", request.get("firstName"));
            body.put("last_name", request.get("lastName"));
            String jsonInputString = new JSONObject(body).toString();

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Generate response String
            StringBuilder responseStr = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    responseStr.append(responseLine.trim());
                }
            }

            // Jsonify + return response
            Map<String, Object> response = new Gson().fromJson(
                    responseStr.toString(), new TypeToken<HashMap<String, Object>>() {
                    }.getType());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {

            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}

