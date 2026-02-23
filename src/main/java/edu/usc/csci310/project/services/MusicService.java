package edu.usc.csci310.project.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.requests.SongRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicService {

    private final Connection connection;

    private HttpURLConnection httpURLConnection;

    public MusicService(Connection connection) {
        this.connection = connection;
    }

    public HttpURLConnection createOVHConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    // check if the song already exists in the database
    // true: user exists
    // false: user doesn't exist
    public boolean songExists(int songId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM songs WHERE song_id = ?")) {
            stmt.setInt(1, songId);
            ResultSet rs = stmt.executeQuery();

            // rs.next is null if it doesn't exist (false)
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // saving song info in the database, given that it's not already there
    // 1: successfully added
    // 0: unsuccessfully added
    // using Integer for releaseYear since Genius sometimes does not have it provided
    public int saveSong(SongRequest songRequest) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO songs (song_id, artist_id, artist_name, song_name, lyrics, release_year) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, songRequest.getSongId());
            stmt.setInt(2, songRequest.getArtistId());
            stmt.setString(3, songRequest.getArtistName());
            stmt.setString(4, songRequest.getSongName());
            stmt.setString(5, songRequest.getLyrics());
//            stmt.setInt(6, songRequest.getReleaseYear());

            Integer releaseYear = songRequest.getReleaseYear();
            if (releaseYear != null) {
                stmt.setInt(6, releaseYear);
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
            return 1; // 1 = success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 0 = failure
        }
    }

    public List<Song> getFavoriteSongs(String username) {
        List<Song> favoriteSongs = new ArrayList<>();

        String sql = "SELECT s.song_id, s.song_name, s.lyrics, s.release_year, s.artist_name, s.artist_id, f.song_order " +
                "FROM favorites f " +
                "JOIN songs s ON f.song_id = s.song_id " +
                "WHERE f.username = ?" +
                "ORDER BY f.song_order";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);





            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int songId = rs.getInt("song_id");
                String name = rs.getString("song_name");
                String lyrics = rs.getString("lyrics");
                Integer releaseYear = rs.getInt("release_year");
                if (rs.wasNull()) releaseYear = null;
                int artistId = rs.getInt("artist_id");
                String artistName = rs.getString("artist_name");

                // Only retrieve it if Song class has a field for it
                Integer songOrder = rs.getInt("song_order");
                if (rs.wasNull()) songOrder = null;

                Song song = new Song(songId, name, lyrics, releaseYear, artistName, artistId);
                // If your Song class has a setter or constructor overload:
                // song.setSongOrder(songOrder);

                favoriteSongs.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return favoriteSongs;
    }

    public void moveSong(String username, int songId, String direction) {
        try {
            // Get current song_order
            String getOrderSQL = "SELECT song_order FROM favorites WHERE username = ? AND song_id = ?";
            int currentOrder;

            PreparedStatement stmt = connection.prepareStatement(getOrderSQL);
            stmt.setString(1, username);






            stmt.setInt(2, songId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return; // song not found
            currentOrder = rs.getInt("song_order");


            int newOrder = direction.equalsIgnoreCase("up") ? currentOrder - 1 : currentOrder + 1;

            // Find the neighbor song with newOrder
            String findNeighborSQL = "SELECT song_id FROM favorites WHERE username = ? AND song_order = ?";
            Integer neighborSongId = null;

            stmt = connection.prepareStatement(findNeighborSQL);
            stmt.setString(1, username);
            stmt.setInt(2, newOrder);
            rs = stmt.executeQuery();
            if (rs.next()) {
                neighborSongId = rs.getInt("song_id");
            } else {
                return; // Can't move up or down, edge case
            }

            // Swap the song_order values
            String updateSQL = "UPDATE favorites SET song_order = ? WHERE username = ? AND song_id = ?";
            stmt = connection.prepareStatement(updateSQL);

            // Swap current song to newOrder
            stmt.setInt(1, newOrder);
            stmt.setString(2, username);
            stmt.setInt(3, songId);
            stmt.addBatch();

            // Swap neighbor song to currentOrder
            stmt.setInt(1, currentOrder);
            stmt.setString(2, username);
            stmt.setInt(3, neighborSongId);
            stmt.addBatch();

            stmt.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to move song order", e);
        }
    }


    // deleting user/song info in the favorites database, given that it's already there
    // 1: successfully deleted
    // 0: unsuccessfully deleted
    public int deleteFavoriteSong(String username, String songOrder) {
        String sql = "DELETE FROM favorites WHERE username = ? AND song_order = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);






            stmt.setString(2, songOrder);
            stmt.executeUpdate();

            sql = "UPDATE favorites SET song_order = song_order - 1 WHERE username = ? AND song_order > ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(sql)) {
                updateStmt.setString(1, username);
                updateStmt.setString(2, songOrder);
                updateStmt.executeUpdate();
            }

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // deleting all user info in the favorites database, given that it's already there
    // 1: successfully deleted
    // 0: unsuccessfully deleted
    public int deleteAllFavoriteSongs(String username) {
        String sql = "DELETE FROM favorites WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);





            stmt.executeUpdate();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 1: successfully set visibility
    // 0: unsuccessfully set visibility
    public int setVisibility(String username, int value) {
        String sql = "UPDATE users SET is_public = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setString(2, username);






            stmt.executeUpdate();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 1: successfully found soulmate
    // 0: unsuccessfully found soulmate
    public int getSoulmate(String username, Map<String, Object> soulmate) {
        String sql = "SELECT u.username, GROUP_CONCAT(s.lyrics, ' ') AS all_lyrics " +
                "FROM users u " +
                "JOIN favorites f ON u.username = f.username " +
                "JOIN songs s ON f.song_id = s.song_id " +
                "WHERE u.is_public = 1 AND u.username != ? " +
                "GROUP BY u.username";

        System.out.println(sql);

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);






            ResultSet rs = stmt.executeQuery();
            Map<String, Set<String>> lyricMap = new HashMap<>();

            while (rs.next()) {
                String user = rs.getString("username");
                System.out.println(user);
                String lyrics = rs.getString("all_lyrics");

                String[] words = lyrics.split("[\\s\\n\\r]+");
                Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
                lyricMap.put(user, uniqueWords);
            }

            int maxOverlap = -1;
            String soulmateUser = null;
            Set<String> userLyrics = getUserLyrics(username);

            for (Map.Entry<String, Set<String>> entry : lyricMap.entrySet()) {
                String user = entry.getKey();
                Set<String> lyrics = entry.getValue();

                Set<String> intersection = new HashSet<>(userLyrics);
                intersection.retainAll(lyrics);

                int overlapCount = intersection.size();
                System.out.println(user + ": " + overlapCount);
                if (overlapCount > maxOverlap) {
                    maxOverlap = overlapCount;
                    soulmateUser = user;
                }
            }

            if (soulmateUser != null) {
                soulmate.put("name", soulmateUser);
                System.out.println(soulmateUser);

                List<Song> favoriteSongs = getFavoriteSongs(soulmateUser);
                List<String> songs = favoriteSongs.stream()
                        .map(Song::getSongName)
                        .collect(Collectors.toList());

                soulmate.put("songs", songs);

                if (isSoulmateMatching(username, soulmateUser, lyricMap)) {
                    soulmate.put("match", "true");
                    System.out.println("matching");
                }
                else {
                    soulmate.put("match", "false");
                    System.out.println("NOT matching");
                }

                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // helper function for getting soulmate
    // given the user and soulmate's username, checks if the soulmate's soulmate is the user (for triggering animation)
    public boolean isSoulmateMatching(String username, String soulmate, Map<String, Set<String>> lyricMap) {
        if (isUserPrivate(username)) {
            return false;
        }

        Set<String> userLyrics = getUserLyrics(username);
        lyricMap.put(username, userLyrics);
        int maxOverlap = -1;

        String soulmateUser = null;
        Set<String> soulmateLyrics = lyricMap.get(soulmate);
        lyricMap.remove(soulmate);

        for (Map.Entry<String, Set<String>> entry : lyricMap.entrySet()) {
            String user = entry.getKey();
            Set<String> lyrics = entry.getValue();

            Set<String> intersection = new HashSet<>(soulmateLyrics);
            intersection.retainAll(lyrics);

            int overlapCount = intersection.size();

            if (overlapCount > maxOverlap) {
                maxOverlap = overlapCount;
                soulmateUser = user;
            }
        }

        return soulmateUser.equals(username);
    }

    // 1: successfully found enemy
    // 0: unsuccessfully found enemy
    public int getEnemy(String username, Map<String, Object> enemy) {
        String sql = "SELECT u.username, GROUP_CONCAT(s.lyrics, ' ') AS all_lyrics " +
                "FROM users u " +
                "JOIN favorites f ON u.username = f.username " +
                "JOIN songs s ON f.song_id = s.song_id " +
                "WHERE u.is_public = 1 AND u.username != ? " +
                "GROUP BY u.username";

        System.out.println(sql);

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);






            ResultSet rs = stmt.executeQuery();
            Map<String, Set<String>> lyricMap = new HashMap<>();

            while (rs.next()) {
                String user = rs.getString("username");
                System.out.println(user);
                String lyrics = rs.getString("all_lyrics");

                String[] words = lyrics.split("[\\s\\n\\r]+");
                Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
                lyricMap.put(user, uniqueWords);
            }

            int minOverlap = Integer.MAX_VALUE;
            String enemyUser = null;
            Set<String> userLyrics = getUserLyrics(username);

            for (Map.Entry<String, Set<String>> entry : lyricMap.entrySet()) {
                String user = entry.getKey();
                Set<String> lyrics = entry.getValue();

                Set<String> intersection = new HashSet<>(userLyrics);
                intersection.retainAll(lyrics);

                int overlapCount = intersection.size();
                System.out.println(user + ": " + overlapCount);
                if (overlapCount < minOverlap) {
                    minOverlap = overlapCount;
                    enemyUser = user;
                }
            }

            if (enemyUser != null) {
                enemy.put("name", enemyUser);
                System.out.println("enemy: " + enemyUser);

                List<Song> favoriteSongs = getFavoriteSongs(enemyUser);
                List<String> songs = favoriteSongs.stream()
                        .map(Song::getSongName)
                        .collect(Collectors.toList());

                enemy.put("songs", songs);

                if (isEnemyMatching(username, enemyUser, lyricMap)) {
                    enemy.put("match", "true");
                    System.out.println("matching");
                }
                else {
                    enemy.put("match", "false");
                    System.out.println("NOT matching");
                }

                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("IN CATCH BODY");
        }

        return 0;
    }

    // helper function for getting enemy
    // given the user and enemy's username, checks if the enemy's enemy is the user (for triggering animation)
    public boolean isEnemyMatching(String username, String enemy, Map<String, Set<String>> lyricMap) {
        if (isUserPrivate(username)) {
            return false;
        }

        Set<String> userLyrics = getUserLyrics(username);
        lyricMap.put(username, userLyrics);
        int minOverlap = Integer.MAX_VALUE;

        String enemyUser = null;
        Set<String> enemyLyrics = lyricMap.get(enemy);
        lyricMap.remove(enemy);

        for (Map.Entry<String, Set<String>> entry : lyricMap.entrySet()) {
            String user = entry.getKey();
            Set<String> lyrics = entry.getValue();

            Set<String> intersection = new HashSet<>(enemyLyrics);
            intersection.retainAll(lyrics);

            int overlapCount = intersection.size();

            if (overlapCount < minOverlap) {
                minOverlap = overlapCount;
                enemyUser = user;
            }
        }

        return enemyUser.equals(username);
    }


    // helper function for getting soulmate/enemy
    // check if the given user is private
    public boolean isUserPrivate(String username) {
        String sql = "SELECT is_public FROM users WHERE username = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);





            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_public") == 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // helper function for getting soulmate
    // given username, returns a set of unique lyrics words from all the user's favorite songs
    public Set<String> getUserLyrics(String username) {
        String sql = "SELECT GROUP_CONCAT(s.lyrics, ' ') AS all_lyrics " +
                "FROM users u " +
                "JOIN favorites f ON u.username = f.username " +
                "JOIN songs s ON f.song_id = s.song_id " +
                "WHERE u.username = ?";

        Set<String> uniqueLyrics = new HashSet<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);





            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lyrics = rs.getString("all_lyrics");
                String[] words = lyrics.split("[\\s\\n\\r]+");
                uniqueLyrics.addAll(Arrays.asList(words));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueLyrics;
    }

    // helper method to parse lyrics from OHV API
    public String fetchLyricsFromOVH(URL url) {
        try {
            httpURLConnection = createOVHConnection(url);
            httpURLConnection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();

            String json = content.toString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            return root.path("lyrics").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "(Could not fetch lyrics)";
        }
    }

    // 1 = added to fav
    // 0 = couldn't add to fav
    // 2 = already in fav
    // song order is automatically next available row
    public int addFavoriteSong(String username, int songId) {
        try {
            // checking if in favorites
            String checkSql = "SELECT 1 FROM favorites WHERE username = ? AND song_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, username);






            checkStmt.setInt(2, songId);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return 2; // already exists
            }

            // want to be last in favorites list
            int nextOrder = 0;
            // watch for 0 songs in list
            String findMaxOrderSql = "SELECT COALESCE(MAX(song_order), 0) + 1 AS next_order FROM favorites WHERE username = ?";
            PreparedStatement orderStmt = connection.prepareStatement(findMaxOrderSql);
            orderStmt.setString(1, username);

            ResultSet orderRs = orderStmt.executeQuery();
            if (orderRs.next()) {
                nextOrder = orderRs.getInt("next_order");
            }

            // put in favorites list
            String insertSql = "INSERT INTO favorites (username, song_id, song_order) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertSql);
            insertStmt.setString(1, username);

            insertStmt.setInt(2, songId);
            insertStmt.setInt(3, nextOrder);

            insertStmt.executeUpdate();
            return 1;


        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<String, Object> getUserInfo(String username) {
        Map<String, Object> userInfo = new HashMap<>();

        String userSql = "SELECT username, is_public FROM users WHERE username = ?";

        try {
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();

            // user doesn't exist
            if (!userRs.next()) {
                return null;
            }

            userInfo.put("username", userRs.getString("username"));
            userInfo.put("isPublic", userRs.getBoolean("is_public"));

            List<Song> favoriteSongs = getFavoriteSongs(username);

            // adding song details
            List<Map<String, Object>> favoriteSongDetails = new ArrayList<>();
            for (Song song : favoriteSongs) {
                Map<String, Object> songData = new HashMap<>();
                songData.put("songName", song.getSongName());
                songData.put("artistName", song.getArtistName());
                songData.put("releaseYear", song.getReleaseYear());
                favoriteSongDetails.add(songData);
            }

            userInfo.put("favorites", favoriteSongDetails);

            return userInfo;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving user info", e);
        }
    }
}