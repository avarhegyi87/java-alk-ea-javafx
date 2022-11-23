package com.example.cities;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class GoRestClient {
    static String token = "ad97ee814e6ec8cf60c67e2e37002e9d932c8139d6915787fab061f1d5415017";
    static HttpsURLConnection connection;

    static void setRequestProperties() {
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
    }
    static void setBufferedWriter(String params) throws IOException {
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        wr.write(params);
        wr.close();
        connection.connect();
    }
    static String getResponse(int code) throws IOException {
        int statusCode = connection.getResponseCode();
        String resp;
        if (statusCode == code) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer jsonResponseData = new StringBuffer();
            String readLine = null;
            while ((readLine = in.readLine()) != null) {
                jsonResponseData.append(readLine);
            }
            in.close();
            resp = jsonResponseData.toString();
        } else {
            resp = "Hiba!";
        }
        connection.disconnect();
        return resp;
    }

    static String GET(String Id) throws IOException {
        String url = "https://gorest.co.in/public/v1/users";
        if(Id != null) {
            url += "/" + Id;
        }
        URL userUrl = new URL(url);
        connection = (HttpsURLConnection) userUrl.openConnection();
        connection.setRequestMethod("GET");
        if (Id != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }
        return getResponse(HttpsURLConnection.HTTP_OK);
    }
    static String POST(String name, String gender, String email, String status) throws IOException {
        URL postUrl = new URL("https://gorest.co.in/public/v1/users");
        connection = (HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        setRequestProperties();
        String params = "{\"name\":\""+name+"\", \"gender\":\""+gender+"\", \"email\":\""+email+"\", \"status\":\""+status+"\"}";
        setBufferedWriter(params);
        return getResponse(HttpsURLConnection.HTTP_CREATED);
    }
    static String PUT(String Id, String name, String gender, String email, String status) throws IOException {
        String url = "https://gorest.co.in/public/v1/users"+"/"+Id;
        URL postUrl = new URL(url);
        connection = (HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("PUT");
        setRequestProperties();
        String params = "{\"name\":\""+name+"\", \"gender\":\""+gender+"\", \"email\":\""+email+"\", \"status\":\""+status+"\"}";
        setBufferedWriter(params);
        return getResponse(HttpsURLConnection.HTTP_OK);
    }
    static String DELETE(String Id) throws IOException {
        String url = "https://gorest.co.in/public/v1/users"+"/"+Id;
        URL postUrl = new URL(url);
        connection = (HttpsURLConnection) postUrl.openConnection();
        connection.setRequestMethod("DELETE");
        setRequestProperties();
        return getResponse(HttpsURLConnection.HTTP_NO_CONTENT);
    }
}
