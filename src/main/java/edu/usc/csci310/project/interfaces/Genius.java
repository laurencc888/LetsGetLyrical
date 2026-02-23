package edu.usc.csci310.project.interfaces;

import java.net.URL;

public interface Genius {
    String getArtistSearchJson(URL url) throws Exception;
    String getSongsByArtistJson(URL url) throws Exception;
    String getPopularSongsByArtistJson(URL url) throws Exception;
}