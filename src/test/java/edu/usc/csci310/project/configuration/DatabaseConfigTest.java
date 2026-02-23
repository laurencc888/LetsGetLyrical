package edu.usc.csci310.project.configuration;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class DatabaseConfigTest {
    @Test
    void sqliteConnection() throws SQLException {
        try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
            Connection mockedConnection = mock(Connection.class);
            DatabaseMetaData mockedMetaData = mock(DatabaseMetaData.class);

            when(mockedConnection.getMetaData()).thenReturn(mockedMetaData);
            String url = "jdbc:sqlite:musicWebApp.db";
            mocked.when(() -> DriverManager.getConnection(url)).thenReturn(mockedConnection);

            DatabaseConfig dc = new DatabaseConfig();
            Connection connection = dc.sqliteConnection();

            assertNotNull(connection);
            assertEquals(mockedConnection, connection);
        }
    }
}