package edu.usc.csci310.project.services;

import edu.usc.csci310.project.requests.SongRequest;
import edu.usc.csci310.project.models.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    MusicService ms;
    SongRequest sr;

    @Mock
    private URL mockURL;

    @Mock
    private HttpURLConnection mockHTTPConnection;

    @Mock
    Connection mockConnection;

    @Mock
    PreparedStatement mockPreparedStatement;

    @Mock
    Statement mockStatement;

    @Mock
    ResultSet mockResultSet;

    void mockFavDB() throws SQLException {
        when(mockResultSet.getInt("song_id")).thenReturn(1);
        when(mockResultSet.getString("song_name")).thenReturn("test");
        when(mockResultSet.getString("lyrics")).thenReturn("lyrics");;
        when(mockResultSet.getString("artist_name")).thenReturn("testName");
        when(mockResultSet.getInt("release_year")).thenReturn(2025);
        when(mockResultSet.getInt("artist_id")).thenReturn(1);
    }

    void mockSoulmateEnemyDB() throws SQLException {
        when(mockResultSet.getString("username")).thenReturn("user2").thenReturn("user3").thenReturn("user4");
        when(mockResultSet.getString("all_lyrics")).thenReturn("lyric1 lyric2 lyric3").thenReturn("lyric4 lyric5").thenReturn("lyric2 lyric3");
    }

    void mockAPICall(HttpURLConnection mockConnection) throws IOException {
        InputStream mockStream = new ByteArrayInputStream(("""
        {
            "lyrics": "Hello, it's me\\nI was wondering..."
        }
        """).getBytes());

        when(mockConnection.getInputStream()).thenReturn(mockStream);
    }

    void mockMoveSongSetUp() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.getInt("song_order")).thenReturn(2);
    }

    void mockStatementExecute() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    }

    @BeforeEach
    void setUp() {
        ms = new MusicService(mockConnection);
        sr = new SongRequest(0, 0, "", "", "", 0);
    }

    @Test
    void testSongExists() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        assertTrue(ms.songExists(1));
    }

    @Test
    void testSongDoesNotExist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        assertFalse(ms.songExists(1));
    }

    @Test
    void testSongExistThrowsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(SQLException.class);
        assertFalse(ms.songExists(1));
    }

    @Test
    void testSaveSongValidReleaseYear() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        assertEquals(1, ms.saveSong(sr));
    }

    @Test
    void testSaveSongInvalidReleaseYear() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        sr.setReleaseYear(null);
        assertEquals(1, ms.saveSong(sr));
    }

    @Test
    void testSaveSongException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(SQLException.class);
        assertEquals(0, ms.saveSong(sr));
    }

    @Test
    void testGetFavoriteSongs() throws SQLException {
        mockStatementExecute();

        mockFavDB();
        when(mockResultSet.wasNull()).thenReturn(false);

        List<Song> favSongs = ms.getFavoriteSongs("test");
        assertEquals(1, favSongs.get(0).getSongId());
    }

    @Test
    void testGetFavoriteSongsNoYear() throws SQLException {
        mockStatementExecute();

        mockFavDB();
        when(mockResultSet.wasNull()).thenReturn(true);

        List<Song> favSongs = ms.getFavoriteSongs("test");
        assertEquals(1, favSongs.size());
    }

    @Test
    void testGetFavoriteSongsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertEquals(0, ms.getFavoriteSongs("test").size());
    }

    @Test
    void testDeleteFavoriteSongValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        int code = ms.deleteFavoriteSong("test", "1");
        assertEquals(1, code);
    }

    @Test
    void testDeleteFavoriteSongInvalid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        int code = ms.deleteFavoriteSong("test", "1");
        assertEquals(0, code);
    }

    @Test
    void testDeleteAllFavoriteSongsValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        int code = ms.deleteAllFavoriteSongs("test");
        assertEquals(1, code);
    }

    @Test
    void testDeleteAllFavoriteSongsInvalid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        int code = ms.deleteAllFavoriteSongs("test");
        assertEquals(0, code);
    }

    @Test
    void testFetchLyrics() throws IOException {
        when(mockURL.openConnection()).thenReturn(mockHTTPConnection);
        mockAPICall(mockHTTPConnection);

        assertEquals("Hello, it's me\nI was wondering...", ms.fetchLyricsFromOVH(mockURL));
    }

    @Test
    void testFetchLyricsException() throws IOException {
        when(mockURL.openConnection()).thenReturn(mockHTTPConnection);
        when(mockHTTPConnection.getInputStream()).thenThrow(IOException.class);

        assertEquals("(Could not fetch lyrics)", ms.fetchLyricsFromOVH(mockURL));
    }

    @Test
    void testSetVisibilityFavoriteSongsValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        int code = ms.setVisibility("test", 0);
        assertEquals(1, code);
    }

    @Test
    void testSetVisibilityFavoriteSongsInvalid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        int code = ms.setVisibility("test", 0);
        assertEquals(0, code);
    }

    @Test
    void testCreateOVHConnection() throws IOException {
        when(mockURL.openConnection()).thenReturn(mockHTTPConnection);

        assertNotNull(ms.createOVHConnection(mockURL));
    }

    @Test
    void testMoveSongUpDown() throws SQLException {
        mockMoveSongSetUp();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("song_id")).thenReturn(100);

        assertDoesNotThrow(() -> ms.moveSong("username", 1, "up"));
        assertDoesNotThrow(() -> ms.moveSong("username", 1, "down"));
    }

    @Test
    void testMoveSongNoSong() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertDoesNotThrow(() -> ms.moveSong("username", 1, "up"));
    }

    @Test
    void testMoveSongEdgeCase() throws SQLException {
        mockMoveSongSetUp();
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);

        assertDoesNotThrow(() -> ms.moveSong("username", 1, "up"));
    }

    @Test
    void testMoveSongException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> ms.moveSong("username", 1, "up"));
    }

    @Test
    void testAddFavoriteSongValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false).thenReturn(true);

        assertEquals(1, ms.addFavoriteSong("username", 1));
    }

    @Test
    void testAddFavoriteSongEmpty() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false).thenReturn(false);

        assertEquals(1, ms.addFavoriteSong("username", 1));
    }

    @Test
    void testAddFavoriteSongError() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertEquals(0, ms.addFavoriteSong("username", 1));
    }

    @Test
    void testSongAlreadyInFavorite() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        assertEquals(2, ms.addFavoriteSong("username", 1));
    }

    @Test
    void testGetUserInfoValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("username")).thenReturn("username");
        when(mockResultSet.getBoolean("is_public")).thenReturn(true);
        mockFavDB();

        assertNotNull(ms.getUserInfo("username"));
    }
                                                           
    @Test                                                       
    void testGetSoulmate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // first true + true + + false to exit first while loop in getSoulmate method
        // second true + false to exit while loop in getFavoriteSongs method
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);

        mockFavDB();
        mockSoulmateEnemyDB();

        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(true).when(spyMs).isSoulmateMatching(anyString(), anyString(), anyMap());

        Map<String, Object> soulmate = new HashMap<>();
        int code = spyMs.getSoulmate("user1", soulmate);

        assertEquals(1, code);
        assertEquals("user2", soulmate.get("name"));
        assertEquals(List.of("test"), soulmate.get("songs"));
        assertEquals("true", soulmate.get("match"));
    }

    @Test
    void testGetSoulmateNoMatch() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // first true + true + + false to exit first while loop in getSoulmate method
        // second true + false to exit while loop in getFavoriteSongs method
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);

        mockFavDB();
        mockSoulmateEnemyDB();

        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isSoulmateMatching(anyString(), anyString(), anyMap());

        Map<String, Object> soulmate = new HashMap<>();
        int code = spyMs.getSoulmate("user1", soulmate);

        assertEquals(1, code);
        assertEquals("user2", soulmate.get("name"));
        assertEquals(List.of("test"), soulmate.get("songs"));
        assertEquals("false", soulmate.get("match"));
    }

    @Test
     void testGetSoulmateNone() throws SQLException {
         when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
         when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
         when(mockResultSet.next()).thenReturn(false);
 
         MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
         doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
 
         Map<String, Object> soulmate = new HashMap<>();
         int code = spyMs.getSoulmate("user1", soulmate);
 
         assertEquals(0, code);
     }
  
    @Test
    void testGetUserInfoInvalid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
      
        assertNull(ms.getUserInfo("username"));
    }

    @Test
    void testGetUserInfoException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> ms.getUserInfo("username"));
    }

    @Test
    void testGetSoulmateThrowsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        Map<String, Object> soulmate = new HashMap<>();
        assertEquals(0, ms.getSoulmate("user1", soulmate));
    }

    @Test
    void testGetEnemy() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // first true + true + true + false to exit first while loop in getEnemy method
        // second true + false to exit while loop in getFavoriteSongs method
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);

        mockFavDB();
        mockSoulmateEnemyDB();

        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(true).when(spyMs).isEnemyMatching(anyString(), anyString(), anyMap());

        Map<String, Object> enemy = new HashMap<>();
        int code = spyMs.getEnemy("user1", enemy);

        assertEquals(1, code);
        assertEquals("user3", enemy.get("name"));
        assertEquals(List.of("test"), enemy.get("songs"));
        assertEquals("true", enemy.get("match"));
    }

    @Test
    void testGetEnemyNoMatch() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // first true + true + true + false to exit first while loop in getEnemy method
        // second true + false to exit while loop in getFavoriteSongs method
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);

        mockFavDB();
        mockSoulmateEnemyDB();

        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isEnemyMatching(anyString(), anyString(), anyMap());

        Map<String, Object> enemy = new HashMap<>();
        int code = spyMs.getEnemy("user1", enemy);

        assertEquals(1, code);
        assertEquals("user3", enemy.get("name"));
        assertEquals(List.of("test"), enemy.get("songs"));
        assertEquals("false", enemy.get("match"));
    }

    @Test
    void testGetEnemyNone() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");

        Map<String, Object> enemy = new HashMap<>();
        int code = spyMs.getEnemy("user1", enemy);

        assertEquals(0, code);
    }

    @Test
    void testGetEnemyThrowsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        Map<String, Object> enemy = new HashMap<>();
        assertEquals(0, ms.getEnemy("user1", enemy));
    }

    @Test
    void testIsSoulmateMatching() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();
        lyricMap.put("user2", new HashSet<>(Set.of("lyric1", "lyric2", "lyric3")));
        lyricMap.put("user3", new HashSet<>(Set.of("lyric4", "lyric5")));

        assertTrue(spyMs.isSoulmateMatching("user1", "user2", lyricMap));
    }

    @Test
    void testIsSoulmateMatchingNot() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();
        lyricMap.put("user2", new HashSet<>(Set.of("lyric3", "lyric4", "lyric5")));
        lyricMap.put("user3", new HashSet<>(Set.of("lyric4", "lyric5")));

        assertFalse(spyMs.isSoulmateMatching("user1", "user3", lyricMap));
    }

    @Test
    void testIsSoulmateMatchingPrivate() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(true).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();

        assertFalse(spyMs.isSoulmateMatching("user1", "user2", lyricMap));
    }

    @Test
    void testIsEnemyMatching() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();
        lyricMap.put("user2", new HashSet<>(Set.of("lyric1", "lyric2", "lyric3")));
        lyricMap.put("user3", new HashSet<>(Set.of("lyric4", "lyric5")));
        lyricMap.put("user4", new HashSet<>(Set.of("lyric1", "lyric2", "lyric3")));

        assertTrue(spyMs.isEnemyMatching("user1", "user3", lyricMap));
    }

    @Test
    void testIsEnemyMatchingNot() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(Set.of("lyric1", "lyric2", "lyric4")).when(spyMs).getUserLyrics("user1");
        doReturn(false).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();
        lyricMap.put("user2", new HashSet<>(Set.of("lyric1", "lyric2", "lyric3")));
        lyricMap.put("user3", new HashSet<>(Set.of("lyric4", "lyric5")));

        assertFalse(spyMs.isEnemyMatching("user1", "user3", lyricMap));
    }

    @Test
    void testIsEnemyMatchingPrivate() throws SQLException {
        MusicService spyMs = Mockito.spy(new MusicService(mockConnection));
        doReturn(true).when(spyMs).isUserPrivate(anyString());

        Map<String, Set<String>> lyricMap = new HashMap<>();

        assertFalse(spyMs.isEnemyMatching("user1", "user2", lyricMap));
    }

    @Test
    void testIsUserPrivateIsPrivate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(anyString())).thenReturn(0); // branch: true, 1

        assertTrue(ms.isUserPrivate("username"));
    }

    @Test
    void testIsUserPrivateIsPublic() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(anyString())).thenReturn(1);

        assertFalse(ms.isUserPrivate("username"));
    }

    @Test
    void testIsUserPrivateNoResult() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        assertFalse(ms.isUserPrivate("username"));
    }


    @Test
    void testIsUserPrivateThrowsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        Map<String, Object> soulmate = new HashMap<>();
        assertFalse(ms.isUserPrivate("username"));
    }

    @Test
    void testGetUserLyricValid() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("all_lyrics")).thenReturn("lyric1 lyric2 lyric3");

        Set<String> results = ms.getUserLyrics("username");
        assertEquals(Set.of("lyric1", "lyric2", "lyric3"), results);
    }

    @Test
    void testGetUserLyricNoResults() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Set<String> results = ms.getUserLyrics("username");
        assertEquals(0, results.size());
    }

    @Test
    void testGetUserLyricThrowsException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(SQLException.class);
        Set<String> results = ms.getUserLyrics("username");
        assertEquals(0, results.size());
    }

    @Test
    void testGetUserInfoError() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> ms.getUserInfo("username"));
    }
}
