package edu.usc.csci310.project.services;

import edu.usc.csci310.project.interfaces.Genius;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeniusService implements Genius {

    @Value("${GENIUS_TOKEN}")
    private String geniusToken;

    private HttpURLConnection connection;

    public HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    // fetching json for api call, return content as a string
    public String fetchJson(URL url) throws Exception {
        connection = createConnection(url);
        connection.setRequestProperty("Authorization", "Bearer " + geniusToken);
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();
        return content.toString();
    }

    @Override
    public String getArtistSearchJson(URL url) throws Exception {
        return fetchJson(url);
    }

    @Override
    public String getSongsByArtistJson(URL url) throws Exception {
        return fetchJson(url);
    }

    @Override
    public String getPopularSongsByArtistJson(URL url) throws Exception {
        return fetchJson(url);
    }
}