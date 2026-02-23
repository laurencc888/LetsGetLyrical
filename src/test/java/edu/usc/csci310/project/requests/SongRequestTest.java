package edu.usc.csci310.project.requests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongRequestTest {
    private SongRequest sr;

    @BeforeEach
    void setUp() {
        sr = new SongRequest(0, 0, "", "", "", 0);
    }

    @Test
    void testGetSetSongId() {
        sr.setSongId(100);
        assertEquals(100, sr.getSongId());
    }

    @Test
    void testGetSetArtistId() {
        sr.setArtistId(100);
        assertEquals(100, sr.getArtistId());
    }

    @Test
    void testGetSetArtistName() {
        sr.setArtistName("test");
        assertEquals("test", sr.getArtistName());
    }

    @Test
    void testGetSetSongName() {
        sr.setSongName("test");
        assertEquals("test", sr.getSongName());
    }

    @Test
    void testGetSetLyrics() {
        sr.setLyrics("blah blah blah");
        assertEquals("blah blah blah", sr.getLyrics());
    }

    @Test
    void testGetSetReleaseYear() {
        sr.setReleaseYear(2025);
        assertEquals(2025, sr.getReleaseYear());
    }
}