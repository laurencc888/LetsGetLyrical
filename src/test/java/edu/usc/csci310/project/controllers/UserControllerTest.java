package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.UserRequest;
import edu.usc.csci310.project.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {
    UserService us;
    UserRequest ur;
    UserController uc;

    @BeforeEach
    void setUp() {
        ur = new UserRequest();
        ur.setUsername("Jane Doe");
        ur.setPassword("Pw123");

        us = mock(UserService.class);
        uc = new UserController(us);
    }

    @Test
    void testLoginSuccess() {
        when(us.doesUsernameExist(ur)).thenReturn(1);
        when(us.isValidUsernamePasswordCombination(ur)).thenReturn(1);

        ResponseEntity<?> response = uc.login(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLoginDatabaseErrorWhenCheckingUsername() {
        UserRequest ur = new UserRequest();
        ur.setUsername("Jane Doe");
        ur.setPassword("Pw123");

        when(us.doesUsernameExist(ur)).thenReturn(-2);

        ResponseEntity<?> response = uc.login(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testLoginUsernameDoesNotExist() {
        when(us.doesUsernameExist(ur)).thenReturn(0);

        ResponseEntity<?> response = uc.login(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoginInvalidUsernamePassword() {
        when(us.doesUsernameExist(ur)).thenReturn(1);
        when(us.isValidUsernamePasswordCombination(ur)).thenReturn(0);

        ResponseEntity<?> response = uc.login(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoginDatabaseError() {
        UserRequest ur = new UserRequest();
        ur.setUsername("Jane Doe");
        ur.setPassword("Pw123");

        when(us.doesUsernameExist(ur)).thenReturn(1);
        when(us.isValidUsernamePasswordCombination(ur)).thenReturn(-2);

        ResponseEntity<?> response = uc.login(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testRegisterSuccess() {
        when(us.doesUsernameExist(ur)).thenReturn(0);
        when(us.addUser(ur)).thenReturn(1);

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRegisterDatabaseErrorWhenCheckingUsername() {
        when(us.doesUsernameExist(ur)).thenReturn(-2);

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testRegisterUsernameAlreadyExist() {
        when(us.doesUsernameExist(ur)).thenReturn(1);

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testRegisterDatabaseError() {
        when(us.doesUsernameExist(ur)).thenReturn(0);
        when(us.addUser(ur)).thenReturn(-2);

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testRegisterInvalidPassword() {
        ur.setPassword("bad password");

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testRegisterNullPassword() {
        ur.setPassword(null);

        ResponseEntity<?> response = uc.register(ur);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}