package edu.usc.csci310.project.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeniusServiceTest {

    @Mock
    private URL mockURL;

    @Mock
    private HttpURLConnection mockConnection;

    @InjectMocks
    private GeniusService geniusService;

    @BeforeEach
    void setUp() throws IOException {
        geniusService = new GeniusService();
        when(mockURL.openConnection()).thenReturn(mockConnection);
        mockAPICall(mockConnection);
    }

    void mockAPICall(HttpURLConnection mockConnection) throws IOException {
        InputStream mockStream = new ByteArrayInputStream(("""
                {
                    "response": {
                        "hits": []
                    }
                }
                """).getBytes());

        lenient().when(mockConnection.getInputStream()).thenReturn(mockStream);
    }

    @Test
    void testGetArtist() throws Exception {
        String result = geniusService.getArtistSearchJson(mockURL);
        assertTrue(result.contains("hits"));
    }

    @Test
    void testGetSongs() throws Exception {
        String result = geniusService.getSongsByArtistJson(mockURL);
        assertTrue(result.contains("hits"));
    }

    @Test
    void testGetPopularSongs() throws Exception {
        String result = geniusService.getPopularSongsByArtistJson(mockURL);
        assertTrue(result.contains("hits"));
    }

    @Test
    void testCreateConnection() throws Exception {
        assertNotNull(geniusService.createConnection(mockURL));
    }

    @Test
    void testFetchJson() throws Exception {
        assertTrue(geniusService.fetchJson(mockURL).contains("hits"));
    }
}