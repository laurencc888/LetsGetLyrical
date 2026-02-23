package edu.usc.csci310.project.services;

import edu.usc.csci310.project.requests.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import java.sql.*;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    UserService us;
    UserRequest request;

    @Mock
    Connection mockConnection;

    @Mock
    PreparedStatement mockPreparedStatement;

    @Mock
    Statement mockStatement;

    @Mock
    ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        us = new UserService(mockConnection);
        request = new UserRequest();
    }

    private UserRequest createTestUser() {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");
        return request;
    }

    private void mockDatabaseSetup(boolean success) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);
        when(mockResultSet.next()).thenReturn(success);
        when(mockStatement.executeQuery("SELECT last_insert_rowid()")).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    private void setupUsernameQueryMock(String passwordInDB, boolean resultSetNext, boolean throwSQLException) throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "SELECT * FROM users WHERE username = ?";

        if (throwSQLException) {
            when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());
            return;
        }

        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(resultSetNext);
        if (passwordInDB != null) {
            when(mockResultSet.getString("password")).thenReturn(passwordInDB);
        }
    }

    private int mockDatabaseSetup_select() throws SQLException {
        setupUsernameQueryMock(null, false, false);
        return us.isValidUsernamePasswordCombination(request);
    }


    private String SetValidUserInfo() {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");
        return "SELECT * FROM users WHERE username = ?";
    }

    void MockClassesBehavior(String sql) throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);
    }

    @Test
    void testAddUserSuccess() throws SQLException {
        UserRequest request = createTestUser();

        mockDatabaseSetup(true);
        int result = us.addUser(request);
        assertEquals(1, result);
    }

    @Test
    void testAddUserErrorExecutingConfirmation() throws SQLException {
        UserRequest request = createTestUser();
        mockDatabaseSetup(false);
        int result = us.addUser(request);
        assertEquals(-1, result);
    }

    @Test
    void testAddUserErrorInsertion() throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);

        int result = us.addUser(request);
        assertEquals(-1, result);
    }

    @Test
    void testAddUserDatabaseError() throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        int result = us.addUser(request);
        assertEquals(-2, result);
    }

    @Test
    void testUsernameExists() throws SQLException {
        String sql = SetValidUserInfo();
        MockClassesBehavior(sql);
        int result = us.doesUsernameExist(request);
        assertEquals(1, result);
    }

    @Test
    void testUsernameDoesNotExists() throws SQLException {
        setupUsernameQueryMock(null, false, false);
        int result = us.doesUsernameExist(request);
        assertEquals(0, result);
    }


    @Test
    void testUsernameExistsDatabaseError() throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "SELECT * FROM users WHERE username = ?";
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        int result = us.doesUsernameExist(request);
        assertEquals(-2, result);
    }

    @Test
    void testValidCombo() throws SQLException {
        String sql = SetValidUserInfo();
        MockClassesBehavior(sql);
        when(mockResultSet.getString("password")).thenReturn("Pw123");


        int result = us.isValidUsernamePasswordCombination(request);
        assertEquals(1, result);
    }

    @Test
    void testInvalidCombo() throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "SELECT * FROM users WHERE username = ?";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn("NotPw123");


        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);

        int result = us.isValidUsernamePasswordCombination(request);
        assertEquals(0, result);
    }

    @Test
    void testComboErrorExecuting() throws SQLException {
        int function_result = mockDatabaseSetup_select();
        assertEquals(-1, function_result);
    }

    @Test
    void testComboDatabaseError() throws SQLException {
        request.setUsername("Jane Doe");
        request.setPassword("Pw123");

        String sql = "SELECT * FROM users WHERE username = ?";
        when(mockConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        int result = us.isValidUsernamePasswordCombination(request);
        assertEquals(-2, result);
    }
}