package com.aircheck.scraper;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class Scraper {

private final String API_KEY = "b06c7b5f09d3a79aa795615537b676c5";
private final String API_KEY_2 = "0320e3284b492358bcc9752cd5796e03";
private final String url;
private String lat;
private String lon;
private String query;

public Scraper(String lat, String lon) throws UnsupportedEncodingException {
        url = "https://api.openweathermap.org/data/2.5/weather";
        this.lat = lat;
        this.lon = lon;
    query = String.format("lat=%s&lon=%s&appid=%s",
            URLEncoder.encode(lat, String.valueOf(StandardCharsets.UTF_8)),
            URLEncoder.encode(lon, String.valueOf(StandardCharsets.UTF_8)),
            URLEncoder.encode(API_KEY, String.valueOf(StandardCharsets.UTF_8)));
        }

public void Scrap() throws IOException, JSONException {
        String temp = url + "?" + query;
        URLConnection connection = new URL(url + "?" + query).openConnection();
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
        String responseBody = scanner.useDelimiter("\\A").next();
        System.out.println(responseBody);
        JSONObject obj = new JSONObject(responseBody);
//            https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
        }
    }
}
