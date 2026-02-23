package edu.usc.csci310.project.requests;

public class SongRequest {
    private int songIdVal;
    private int artistIdVal;
    private String artistNameVal;
    private String songNameVal;
    private String lyricsVal;
    private Integer releaseYearVal;

    public SongRequest(int songIdVal, int artistIdVal, String artistNameVal, String songNameVal, String lyricsVal, Integer releaseYearVal) {
        this.songIdVal = songIdVal;
        this.artistIdVal = artistIdVal;
        this.artistNameVal = artistNameVal;
        this.songNameVal = songNameVal;
        this.lyricsVal = lyricsVal;
        this.releaseYearVal = releaseYearVal;
    }

    public int getSongId() {
        return songIdVal;
    }

    public void setSongId(int songIdItem) {
        this.songIdVal = songIdItem;
    }

    public int getArtistId() {
        return artistIdVal;
    }

    public void setArtistId(int artistIdItem) {
        this.artistIdVal = artistIdItem;
    }

    public String getArtistName() {
        return artistNameVal;
    }

    public void setArtistName(String artistNameItem) {
        this.artistNameVal = artistNameItem;
    }

    public String getSongName() {
        return songNameVal;
    }

    public void setSongName(String songNameItem) {
        this.songNameVal = songNameItem;
    }

    public String getLyrics() {
        return lyricsVal;
    }

    public void setLyrics(String lyricsItem) {
        this.lyricsVal = lyricsItem;
    }

    public Integer getReleaseYear() {
        return releaseYearVal;
    }

    public void setReleaseYear(Integer releaseYearItem) {
        this.releaseYearVal = releaseYearItem;
    }
}