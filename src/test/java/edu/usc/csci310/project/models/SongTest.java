package edu.usc.csci310.project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {

    private Song song;

    @BeforeEach
    void setUp() {
        song = new Song(1, "test", "lyrics", 1, "blah", 1);
    }

    @Test
    void testId() {
        song.setSongId(2);
        assertEquals(2, song.getSongId());
    }

    @Test
    void testName() {
        song.setSongName("newName");
        assertEquals("newName", song.getSongName());
    }

    @Test
    void testLyrics() {
        song.setLyrics("newLyrics");
        assertEquals("newLyrics", song.getLyrics());
    }

    @Test
    void testReleaseYear() {
        song.setReleaseYear(2025);
        assertEquals(2025, song.getReleaseYear());
    }

    @Test
    void testArtistName() {
        song.setArtistName("newArtistName");
        assertEquals("newArtistName", song.getArtistName());
    }

    @Test
    void testArtistId() {
        song.setArtistId(2);
        assertEquals(2, song.getArtistId());
    }

}