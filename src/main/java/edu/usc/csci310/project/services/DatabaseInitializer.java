package edu.usc.csci310.project.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class DatabaseInitializer {

    private final Connection connection;

    @Autowired
    public DatabaseInitializer(Connection connection) {
        this.connection = connection;
    }

    @PostConstruct
    public void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Create users table with correct auto-increment syntax for SQLite
            String createUserTableSQL =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT NOT NULL, " +
                            "password TEXT NOT NULL, " +
                            "is_public BOOLEAN NOT NULL DEFAULT 0)";
            stmt.executeUpdate(createUserTableSQL);
            System.out.println("User table created");

            // table holding all song info that has been searched to prevent too many api calls
            String createSongsTableSQL =
                    "CREATE TABLE IF NOT EXISTS songs (" +
                            "song_id INTEGER PRIMARY KEY, " +
                            "artist_id INTEGER NOT NULL, " +
                            "artist_name TEXT NOT NULL, " +
                            "song_name TEXT NOT NULL, " +
                            "lyrics TEXT NOT NULL, " +
                            "release_year INTEGER, " +
                            "UNIQUE(song_name, artist_id))";
            stmt.executeUpdate(createSongsTableSQL);
            System.out.println("Songs table created");

            // Create favorites table with a foreign key constraint
            String createFavoritesTableSQL =
                    "CREATE TABLE IF NOT EXISTS favorites (" +
                            "username TEXT NOT NULL, " +
                            "song_id TEXT NOT NULL, " +
                            "song_order INTEGER, " +
                            "CONSTRAINT fk_user FOREIGN KEY (username) REFERENCES users(username), " +
                            "CONSTRAINT fk_song FOREIGN KEY (song_id) REFERENCES songs(song_id), " +
                            "PRIMARY KEY (username, song_id))";
            stmt.executeUpdate(createFavoritesTableSQL);
            System.out.println("Favorites table created");
            try {
                stmt.executeUpdate("""
                    WITH ordered AS (
                        SELECT rowid, 
                               ROW_NUMBER() OVER (PARTITION BY username ORDER BY rowid) AS rn
                        FROM favorites
                        WHERE song_order IS NULL
                    )
                    UPDATE favorites
                    SET song_order = (
                        SELECT rn FROM ordered WHERE ordered.rowid = favorites.rowid
                    )
                    WHERE song_order IS NULL
                """);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to backfill song_order", e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing the database schema", e);
        }
    }
}