package edu.usc.csci310.project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistTest {

    private Artist artist;

    @BeforeEach
    void setUp() {
        artist = new Artist(1, "test", "url");
    }

    @Test
    void testId() {
        artist.setId(2);
        assertEquals(2, artist.getId());
    }

    @Test
    void testName() {
        artist.setName("newName");
        assertEquals("newName", artist.getName());
    }

    @Test
    void testUrl() {
        artist.setImageUrl("newUrl");
        assertEquals("newUrl", artist.getImageUrl());
    }

}