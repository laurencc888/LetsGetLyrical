package edu.usc.csci310.project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {

    private static final String DATABASE_URL = "jdbc:sqlite:musicWebApp.db";

    @Bean
    public Connection sqliteConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        System.out.println("DB URL: " + connection.getMetaData().getURL());
        return connection;
    }

}

