package edu.usc.csci310.project.models;

public class Song {
    private int songId;
    private String songName;
    private String lyrics;
    private Integer releaseYear;

    private String artistName;
    private int artistId;

    public Song(int songId, String songName, String lyrics, Integer releaseYear, String artistName, int artistId) {
        this.songId = songId;
        this.songName = songName;
        this.lyrics = lyrics;
        this.releaseYear = releaseYear;
        this.artistName = artistName;
        this.artistId = artistId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }
}

