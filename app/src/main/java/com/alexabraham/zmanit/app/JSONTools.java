package com.alexabraham.zmanit.app;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: alexabraham
 * Date: 12/6/13
 * Time: 9:38 AM
 */

public class JSONTools {

    private JSONParser parser;

    public JSONTools() {
        parser = new JSONParser();
    }

    public Map getFromIP() throws IOException, ParseException {
        String jsonString = parseJSON("http://freegeoip.net/json/");
        Map jsonData = (Map) parser.parse(jsonString);

        return jsonData;

    }

    public String getFromMap(Map location, String value) {
        String result = location.get(value).toString();
        return result;
    }

    private String parseJSON(String urlString) throws IOException {
        String jsonString = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            jsonString = buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }

        return jsonString;
    }
}

