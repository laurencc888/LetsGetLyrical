package edu.usc.csci310.project.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usc.csci310.project.models.Artist;
import edu.usc.csci310.project.models.Song;
import edu.usc.csci310.project.requests.SongRequest;
import edu.usc.csci310.project.services.GeniusService;
import edu.usc.csci310.project.services.MusicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@RestController
public class MusicController {

    private final MusicService musicService;
    private final GeniusService geniusService;

    public MusicController(MusicService musicService, GeniusService geniusService) {
        this.musicService = musicService;
        this.geniusService = geniusService;
    }

    public List<Song> parseSongInfo(String json, int artistId) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode songs = root.path("response").path("songs");

        // track song info: id, song name, main artist, lyrics
        Map<Integer, Song> songMap = new HashMap<>();

        Song newSong;
        for (JsonNode song : songs) {
            // skip if artist id is not correct
            int artistIDFromSong = song.path("primary_artist").path("id").asInt();
            if (artistIDFromSong == artistId) {
                int songId = song.path("id").asInt();
                String title = song.path("title").asText();
                String artistName = song.path("primary_artist").path("name").asText();

                // setting up lyric api
                System.out.println(artistName);
                System.out.println(title);

                String encodedArtist = java.net.URLEncoder.encode(artistName, "UTF-8");
                String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");

                String apiUrl = "https://api.lyrics.ovh/v1/" + encodedArtist + "/" + encodedTitle;
                URL ovhUrl = new URL(apiUrl);
                String lyrics = musicService.fetchLyricsFromOVH(ovhUrl);

                // if it could not find lyrics, don't bother adding to the database
                if (lyrics.equals("(Could not fetch lyrics)"))
                    continue;

                Integer releaseYear = song.path("release_date_components").path("year").asInt(0);
                System.out.println("song id: " + songId);

                // avoiding duplicates
                if (!songMap.containsKey(songId)) {
                    newSong = new Song(songId, title, lyrics, releaseYear, artistName, artistId);
                    songMap.put(songId, newSong);
                }

                // checking if we need to save to database
                if (!musicService.songExists(songId)) {
                    SongRequest sr = new SongRequest(songId, artistIDFromSong, artistName, title, lyrics, releaseYear);
                    musicService.saveSong(sr);
                }
            }
        }
        return new ArrayList<>(songMap.values());
    }

    // searching for artist using inputted term:
    // response: id, name, image_url
    @GetMapping("/search-artist")
    public ResponseEntity<List<Artist>> searchArtist(@RequestParam String name) {
        try {
            String apiUrl = "https://api.genius.com/search?q=" + java.net.URLEncoder.encode(name, "UTF-8");
            URL url = new URL(apiUrl);
            String json = geniusService.getArtistSearchJson(url);

            // using Jackson to parse Json info
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode hits = root.path("response").path("hits");

            // track MAIN artist info: id, name, image (for search table)
            Map<Integer, Artist> artistMap = new HashMap<>();
            Artist newArtist;
            for (JsonNode hit : hits) {
                JsonNode artist = hit.path("result").path("primary_artist");
                int id = artist.path("id").asInt();
                String artistName = artist.path("name").asText();
                String imageUrl = artist.path("image_url").asText();

                // avoid duplicates
                if (!artistMap.containsKey(id)) {
                    newArtist = new Artist(id, artistName, imageUrl);
                    artistMap.put(id, newArtist);
                }
            }

            List<Artist> result = new ArrayList<>(artistMap.values());
            return ResponseEntity.ok().body(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/get-songs-by-artist")
    public ResponseEntity<List<Song>> getSongsByArtist(@RequestParam int artistId) {
        List<Song> allSongs = new ArrayList<>();
        int page = 1;
        int perPage = 25;
        int maxSongs = 25; // setting maximum to 25

        try {
            while (allSongs.size() < maxSongs) {
                String apiURL = "https://api.genius.com/artists/" + artistId + "/songs?per_page=" + perPage + "&page=" + page;
                URL url = new URL(apiURL);
                String json = geniusService.getSongsByArtistJson(url);

                List<Song> songsWithLyrics = parseSongInfo(json, artistId);

                if (songsWithLyrics.isEmpty())
                    break; // no more to fetch

                allSongs.addAll(songsWithLyrics);
                page++;
            }

            if (allSongs.size() > maxSongs) {
                allSongs = allSongs.subList(0, maxSongs);
            }

            return ResponseEntity.ok().body(allSongs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/get-popular-songs-by-artist")
    public ResponseEntity<List<Song>> getPopularSongsByArtist(@RequestParam int artistId, @RequestParam int numSongs) {
        List<Song> allSongs = new ArrayList<>();
        int page = 1;

        try {
            while (allSongs.size() < numSongs) {
                String apiUrl = "https://api.genius.com/artists/" + artistId + "/songs?sort=popularity&per_page=" + numSongs + "&page=" + page;
                URL url = new URL(apiUrl);
                String json = geniusService.getPopularSongsByArtistJson(url);
                List<Song> songsFromPage = parseSongInfo(json, artistId);

                if (songsFromPage.isEmpty())
                    break;

                allSongs.addAll(songsFromPage);
                page++;
            }

            // reduce to requested number of songs
            if (allSongs.size() > numSongs) {
                allSongs = allSongs.subList(0, numSongs);
            }

            return ResponseEntity.ok().body(allSongs);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/get-favorite-songs")
    public ResponseEntity<List<Song>> getFavoriteSongs(@RequestParam String username) {
        return ResponseEntity.ok().body(musicService.getFavoriteSongs(username));
    }

    @PostMapping("/move-song")
    public ResponseEntity<String> moveSong(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        int songId = (int) body.get("songId");
        String direction = (String) body.get("direction"); // "up" or "down"

        try {
            musicService.moveSong(username, songId, direction);
            return ResponseEntity.ok("Song moved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to move song");
        }
    }

    @PostMapping("/delete-favorite-song")
    public ResponseEntity<?> deleteFavoriteSong(@RequestBody Map<String, String> requestData) {
        String username = requestData.get("username");
        String songOrder = requestData.get("songOrder");

        if (musicService.deleteFavoriteSong(username, songOrder) == 1) {
            System.out.println("Song deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body("successfully deleted song");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error deleting song");
        }
    }

    @PostMapping("/delete-all-favorite-songs")
    public ResponseEntity<?> deleteAllFavoriteSongs(@RequestBody Map<String, String> requestData) {
        String username = requestData.get("username");
        if (musicService.deleteAllFavoriteSongs(username) == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("successfully deleted all songs");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error deleting all songs");
        }
    }

    @PostMapping("/add-to-favorite-songs")
    public ResponseEntity<String> addToFavoriteSongs(@RequestBody Map<String, String> requestData) {
        String username = requestData.get("username");
        String songIdString = requestData.get("songId");

        int songId = Integer.parseInt(songIdString);
        int result = musicService.addFavoriteSong(username, songId);

        if (result == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("successfully added song");
        }
        else if (result == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error adding song");
        }
        // if result == 2
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("song already in favorites");
        }
    }


    @PostMapping("/set-visibility")
    public ResponseEntity<String> setVisibility(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String visibility = body.get("visibility");     // "public" or "private"

        int value;
        if (visibility.equals("public")) {
            value = 1;
        }
        else {
            value = 0;
        }

        if (musicService.setVisibility(username, value) == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("successfully set visibility");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error setting visibility");
        }
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<?> getUserInfo(@RequestParam String username) {
        Map<String, Object> userInfo = musicService.getUserInfo(username);

        if (userInfo != null) {
            return ResponseEntity.ok().body(userInfo);
        }
        // if the user doesn't exist
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }
    }
  
    @PostMapping("/get-soulmate")
    public ResponseEntity<Map<String, Object>> getSoulmate(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        Map<String, Object> soulmate = new HashMap<>();
        if (musicService.getSoulmate(username, soulmate) == 1) {
            return ResponseEntity.status(HttpStatus.OK).body(soulmate);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/get-enemy")
    public ResponseEntity<Map<String, Object>> getEnemy(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        Map<String, Object> enemy = new HashMap<>();
        if (musicService.getEnemy(username, enemy) == 1) {
            return ResponseEntity.status(HttpStatus.OK).body(enemy);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}