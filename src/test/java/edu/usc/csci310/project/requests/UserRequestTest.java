package edu.usc.csci310.project.requests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRequestTest {
    private static UserRequest ur;

    @BeforeEach
    void setUp() {
        ur = new UserRequest();
    }

    @Test
    void testGetUsername() {
        ur.setUsername("Jane Doe");
        assertEquals("Jane Doe", ur.getUsername());
    }

    @Test
    void testSetUsername() {
        ur.setUsername("Jane Doe");
        assertEquals("Jane Doe", ur.getUsername());
    }

    @Test
    void testGetPassword() {
        ur.setPassword("Pw123");
        assertEquals("Pw123", ur.getPassword());
    }

    @Test
    void testSetPassword() {
        ur.setPassword("Pw123");
        assertEquals("Pw123", ur.getPassword());
    }

}