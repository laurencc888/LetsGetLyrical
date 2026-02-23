package edu.usc.csci310.project.services;

import edu.usc.csci310.project.requests.UserRequest;
import org.springframework.stereotype.Service;


import java.sql.*;





@Service
public class UserService {
    private final Connection connection;
    public UserService(Connection connection) {
        this.connection = connection;
    }

    /*
    given that the username doesn't exist, insert user into database
         1: successful execution
        -1: error executing statement
        -2: error connecting to db
     */
    public int addUser(UserRequest request) {
        int code =-3;
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, request.getUsername());
            stmt.setString(2, request.getPassword());





            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (Statement statement = connection.createStatement();
                     ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        code = 1; // success
                    }
                    else {
                        code = -1; // error executing statement
                    }
                }
            } else {
                code = -1; // error executing/inserting
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            code = -2; // error connecting to database
        }
        return code;
    }

    /*
    check if username exists in database
         1: username exists
         0: username does not exist
        -2: error connecting to db
     */
    public int doesUsernameExist(UserRequest request) {
        int code = -1;
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, request.getUsername());



            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    code = 1; // username exists
                }
                else {
                    code = 0; // username does not exist
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            code = -2; // error connecting to database
        }
        return code;
    }

    /*
    given that the username exists, check if username-password combination is valid
         1: username-password combination is valid
         0: username password combination is not valid
        -1: error executing statement
        -2: error connecting to db
     */
    public int isValidUsernamePasswordCombination(UserRequest request) {
        int code = -3;
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, request.getUsername());



            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (request.getPassword().equals(storedPassword)) {

                        code = 1; // valid combination
                    }
                    else {
                        code = 0; // invalid combination
                    }
                } else {
                    code = -1; // error executing statement
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            code = -2; // error connecting to database
        }
        return code;
    }
}