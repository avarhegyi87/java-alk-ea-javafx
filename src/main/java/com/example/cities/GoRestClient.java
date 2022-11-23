package com.example.cities;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GoRestClient {
    static String token = "ad97ee814e6ec8cf60c67e2e37002e9d932c8139d6915787fab061f1d5415017";
    static HttpsURLConnection connection;

    static void setRequestProperties() {}
    static void setBufferedWriter() throws IOException {}
    static String getResponse(int code) throws IOException {
        int statusCode = connection.getResponseCode();
        System.out.println("status code: " + statusCode);
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
    static void POST(String name, String gender, String email, String status) throws IOException {}
    static void PUT(String Id, String name, String gender, String email, String status) throws IOException {}
    static void DELETE(String Id) throws IOException {}
}
