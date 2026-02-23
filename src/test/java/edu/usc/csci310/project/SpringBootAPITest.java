package edu.usc.csci310.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SpringBootAPITest {

    SpringBootAPI api;

    @BeforeEach
    void setUp() {
        api = new SpringBootAPI();
    }

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> sb = Mockito.mockStatic(SpringApplication.class)) {
            assertDoesNotThrow(() -> SpringBootAPI.main(new String[]{}));
        }
    }

    @Test
    void testRedirect() {
        assertEquals("forward:/", api.redirect());
    }
}
