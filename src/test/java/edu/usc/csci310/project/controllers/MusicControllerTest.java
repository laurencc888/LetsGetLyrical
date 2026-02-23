package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.services.GeniusService;
import edu.usc.csci310.project.services.MusicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicControllerTest {

    private String artistJson = """
            {
                "response": {
                    "hits": [
                        {
                            "result": {
                                "primary_artist": {
                                    "id": 1,
                                    "name": "Taylor Swift",
                                    "image_url": "https://image.url/taylor.jpg"
                                }
                            }
                        }
                    ]
                }
            }
            """;

    private String dupeArtistJson = """
            {
                "response": {
                    "hits": [
                        {
                            "result": {
                                "primary_artist": {
                                    "id": 1,
                                    "name": "Taylor Swift",
                                    "image_url": "https://image.url/taylor.jpg"
                                }
                            }
                        },
                        {
                            "result": {
                                "primary_artist": {
                                    "id": 1,
                                    "name": "Taylor Swift",
                                    "image_url": "https://image.url/taylor.jpg"
                                }
                            }
                        }
                    ]
                }
            }
            """;

    private static final String FORTNIGHT_JSON = """
        {
            "id": 102,
            "title": "Fortnight",
            "primary_artist": {
                "id": 1,
                "name": "Taylor Swift"
            },
            "url": "https://genius.com/Song-Two-lyrics",
            "release_date_components": {
                "year": 2025,
                "month": 11,
                "day": 12
            }
        }
        """;

    private String songJson = """
        {
            "response": {
                "songs": [
                    {
                        "id": 101,
                        "title": "Song One",
                        "primary_artist": {
                            "id": 1,
                            "name": "Taylor Swift"
                        },
                        "url": "https://genius.com/Song-One-lyrics",
                        "release_date_components": {
                            "year": 2025,
                            "month": 11,
                            "day": 12
                        }
                    },
                    """ + FORTNIGHT_JSON + """
                ]
            }
        }
        """;

    private String emptySongsJson = """
        {
            "response": {
                "songs": [
                ]
            }
        }
        """;

    private String wrongArtistJson = """
        {
            "response": {
                "songs": [
                    {
                        "id": 101,
                        "title": "Song One",
                        "primary_artist": {
                            "id": 2,
                            "name": "Taylor Swift"
                        },
                        "url": "https://genius.com/Song-One-lyrics",
                        "release_date_components": {
                            "year": 2025,
                            "month": 11,
                            "day": 12
                        }
                    }
                ]
            }
        }
        """;

    private String dupeSongJson = """
        {
            "response": {
                "songs": [
                    """ + FORTNIGHT_JSON + "," + FORTNIGHT_JSON + """
                ]
            }
        }
        """;

    private Map<String, String> makeFavReq() {
        Map<String, String> map = new HashMap<>();
        map.put("username", "username");
        map.put("songId", "1");
        return map;
    }

    final private List<Song> songList = new ArrayList<>();

    @Mock
    private MusicService musicService;

    @Mock
    private GeniusService geniusService;

    @InjectMocks
    private MusicController musicController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSearchArtist() throws Exception {
        when(geniusService.getArtistSearchJson(any())).thenReturn(artistJson);

        ResponseEntity<List<Artist>> response = musicController.searchArtist("Taylor");
        Artist artist = response.getBody().get(0);
        assertEquals(1, artist.getId());
        assertEquals("Taylor Swift", artist.getName());
        assertEquals("https://image.url/taylor.jpg", artist.getImageUrl());
    }

    @Test
    void testSearchArtistHasDuplicate() throws Exception {
        when(geniusService.getArtistSearchJson(any())).thenReturn(dupeArtistJson);

        ResponseEntity<List<Artist>> response = musicController.searchArtist("Taylor");
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testSearchArtistException() throws Exception {
        when(geniusService.getArtistSearchJson(any())).thenThrow(new Exception());

        ResponseEntity<List<Artist>> response = musicController.searchArtist("Taylor");
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testGetSongsByArtist() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenReturn(songJson);
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics").thenReturn("(Could not fetch lyrics)");

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSongsByArtistException() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenThrow(new Exception());

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testIncorrectArtistID() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenReturn(wrongArtistJson);

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testDuplicateSong() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenReturn(dupeSongJson).thenReturn(emptySongsJson);
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics");
        when(musicService.songExists(102)).thenReturn(true).thenReturn(false);

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testReachMaxSongs() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenReturn(dupeSongJson);
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics");
        when(musicService.songExists(102)).thenReturn(false);

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(25, response.getBody().size());
    }

    @Test
    void testTrimToMaxSongs() throws Exception {
        when(geniusService.getSongsByArtistJson(any())).thenReturn(songJson);
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics");
        when(musicService.songExists(101)).thenReturn(false);
        when(musicService.songExists(102)).thenReturn(false);

        ResponseEntity<List<Song>> response = musicController.getSongsByArtist(1);
        assertEquals(25, response.getBody().size());
    }

    @Test
    void testGetPopularSongsByArtist() throws Exception {
        when(geniusService.getPopularSongsByArtistJson(any())).thenReturn(songJson);
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics");

        ResponseEntity<List<Song>> response = musicController.getPopularSongsByArtist(1, 1);
        assertEquals(1, response.getBody().size());

        response = musicController.getPopularSongsByArtist(1, 2);
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetNoPopularSongsByArtist() throws Exception {
        when(geniusService.getPopularSongsByArtistJson(any())).thenReturn(emptySongsJson);

        ResponseEntity<List<Song>> response = musicController.getPopularSongsByArtist(1, 1);
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testGetPopularSongsByArtistException() throws Exception {
        when(geniusService.getPopularSongsByArtistJson(any())).thenThrow(new Exception());

        ResponseEntity<List<Song>> response = musicController.getPopularSongsByArtist(1, 1);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testGetFavoriteSongs() {
        when(musicService.getFavoriteSongs("test")).thenReturn(songList);

        ResponseEntity<List<Song>> response = musicController.getFavoriteSongs("test");
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testDeleteFavoriteSongValid() {
        when(musicService.deleteFavoriteSong("test", "1")).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");
        map.put("songOrder", "1");

        ResponseEntity<?> response = musicController.deleteFavoriteSong(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteFavoriteSongInvalid() {
        when(musicService.deleteFavoriteSong("test", "1")).thenReturn(0);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");
        map.put("songOrder", "1");

        ResponseEntity<?> response = musicController.deleteFavoriteSong(map);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testDeleteAllFavoriteSongsValid() {
        when(musicService.deleteAllFavoriteSongs("test")).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.deleteAllFavoriteSongs(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteAllFavoriteSongsInvalid() {
        when(musicService.deleteAllFavoriteSongs("test")).thenReturn(0);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.deleteAllFavoriteSongs(map);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testSetVisibilityPrivateFavoriteSongsValid() {
        when(musicService.setVisibility("test", 0)).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");
        map.put("visibility", "private");

        ResponseEntity<?> response = musicController.setVisibility(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testSetVisibilityPublicFavoriteSongsValid() {
        when(musicService.setVisibility("test", 1)).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");
        map.put("visibility", "public");

        ResponseEntity<?> response = musicController.setVisibility(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void testSetVisibilityPrivateFavoriteSongsInvalid() {
        when(musicService.setVisibility("test", 0)).thenReturn(0);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");
        map.put("visibility", "private");

        ResponseEntity<?> response = musicController.setVisibility(map);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testParseSongInfo() throws IOException {
        when(musicService.fetchLyricsFromOVH(any())).thenReturn("fake lyrics");
        when(musicService.songExists(101)).thenReturn(true);
        when(musicService.songExists(102)).thenReturn(true);

        assertEquals(2, musicController.parseSongInfo(songJson, 1).size());
    }

    @Test
    void testMoveSongWithError() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "myUsername");
        requestBody.put("songId", 123);
        requestBody.put("direction", "up");

        assertEquals("Song moved successfully", musicController.moveSong(requestBody).getBody());

        // testing error
        doThrow(new RuntimeException("error")).when(musicService).moveSong("username", 123, "up");

        assertEquals("Failed to move song", musicController.moveSong(requestBody).getBody());
    }

    @Test
    void testAddToFavoriteSongValid() {
        when(musicService.addFavoriteSong("username", 1)).thenReturn(1);

        assertEquals("successfully added song", musicController.addToFavoriteSongs(makeFavReq()).getBody());
    }

    @Test
    void testAddToFavoriteSongError() {
        when(musicService.addFavoriteSong("username", 1)).thenReturn(0);

        assertEquals("error adding song", musicController.addToFavoriteSongs(makeFavReq()).getBody());
    }

    @Test
    void testAddExistingSong() {
        when(musicService.addFavoriteSong("username", 1)).thenReturn(2);

        assertEquals("song already in favorites", musicController.addToFavoriteSongs(makeFavReq()).getBody());
    }

    @Test
    void testGetUserInfo() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("username", "test");
        when(musicService.getUserInfo("user")).thenReturn(mockInfo);

        assertEquals(mockInfo, musicController.getUserInfo("user").getBody());
    }

    @Test
    void testGetUserInfoInvalid() {
        when(musicService.getUserInfo("user")).thenReturn(null);

        assertEquals("user not found", musicController.getUserInfo("user").getBody());
    }
  
    @Test
    void testGetSoulmateValid() {
        when(musicService.getSoulmate(anyString(), anyMap())).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.getSoulmate(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetSoulmateInvalid() {
        when(musicService.getSoulmate(anyString(), anyMap())).thenReturn(0);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.getSoulmate(map);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetEnemyValid() {
        when(musicService.getEnemy(anyString(), anyMap())).thenReturn(1);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.getEnemy(map);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetEnemyInvalid() {
        when(musicService.getEnemy(anyString(), anyMap())).thenReturn(0);

        Map<String, String> map = new HashMap<>();
        map.put("username", "test");

        ResponseEntity<?> response = musicController.getEnemy(map);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
