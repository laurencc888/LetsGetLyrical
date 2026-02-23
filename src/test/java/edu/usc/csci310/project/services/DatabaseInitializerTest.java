package edu.usc.csci310.project.services;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseInitializerTest {
    @Test
    void testInitializeDatabase() throws SQLException {
        Connection c = mock(Connection.class);
        try (Statement s = mock(Statement.class)) {
            when(c.createStatement()).thenReturn(s);
            when(s.executeUpdate(anyString())).thenReturn(0);
        }

        DatabaseInitializer di = new DatabaseInitializer(c);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        PrintStream ps = System.out;
        System.setOut(new PrintStream(boas));

        di.initializeDatabase();
        String confirmation = "User table created" + System.lineSeparator() + "Songs table created" + System.lineSeparator() + "Favorites table created" + System.lineSeparator();
        assertEquals(confirmation, boas.toString());

        System.setOut(ps);
    }

    @Test
    void testInitializeDatabaseRuntimeException() throws SQLException {
        Connection c = mock(Connection.class);
        when(c.createStatement()).thenThrow(new SQLException());
        DatabaseInitializer di = new DatabaseInitializer(c);
        RuntimeException re = assertThrows(RuntimeException.class, () -> di.initializeDatabase());

        assertEquals("Error initializing the database schema", re.getMessage());
    }

    @Test
    void testInitializeDatabaseSQLErrorBranch1() throws SQLException {
        Connection c = mock(Connection.class);
        try (Statement s = mock(Statement.class)) {
            when(c.createStatement()).thenReturn(s);
            when(s.executeUpdate(anyString())).thenReturn(0);
            when(s.executeUpdate("""
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
                """)).thenThrow(new SQLException());
        }

        DatabaseInitializer di = new DatabaseInitializer(c);
        RuntimeException re = assertThrows(RuntimeException.class, () -> di.initializeDatabase());

        assertEquals("Failed to backfill song_order", re.getMessage());
    }

}