import React, { useState, useEffect } from 'react';
import '../styles/index.css';
import SearchNav from '../components/SearchNav';
import ArtistPop from '../components/ArtistPop';  // Import the ArtistPop component
import PopUp from "../components/PopUp";
import SongWordCloud from '../components/SongWordCloud';

const NUM_SONGS = 50;  // max songs api will fetch

function Search() {
    const [numSongs, setNumSongs] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [artist, setArtist] = useState('');
    const [artistId, setArtistId] = useState(null);
    const [searchType, setSearchType] = useState('');
    const [hasSearched, setHasSearched] = useState(false);
    const [artistFound, setArtistFound] = useState(true);
    const [filteredResults, setFilteredResults] = useState([]);
    const [popupVisible, setPopupVisible] = useState(false);  // State to control popup visibility
    // const [addToResultsError, setAddToResultsError] = useState('');  // Error for "Add to Results"
    const [selectedArtist, setSelectedArtist] = useState(null);  // State to store selected artist
    const [wordCloudData, setWordCloudData] = useState([]); // State to store data for word cloud
    const [showGetSongsButton, setShowGetSongsButton] = useState(false);
    const [favoriteSongs, setFavoriteSongs] = useState([]); // state to store favorite songs from search
    const [fetchedSongs, setFetchedSongs] = useState([]);  // State to store the fetched songs
    const [selectedSongs, setSelectedSongs] = useState([]); // State to store selected songs
    const [showWordCloud, setShowWordCloud] = useState(false);
    const [songsShow, setSongsShow] = useState(false);
    const [artistSelectedFromPopup, setArtistSelectedFromPopup] = useState(false);

    // for toast
    const [showToast, setShowToast] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [isError, setIsError] = useState(false);



    //Redirect to Login page if no user logged in
    useEffect(() => {
        const username = localStorage.getItem("username");
        if (!username) {
            window.location.href = "/";
        }





    }, []);





















    // Function to check if the artist exists
    const checkArtistExists = (name) => {
        const artist = filteredResults.find(item => item.name.toLowerCase() === name.toLowerCase());
        return artist ? true : false;
    };

    const fetchArtistData = async () => {

        if (!artist.trim()) {
            setErrorMessage("Please enter an artist name");
            setShowToast(true);
            setIsError(true);
            return;
        }

        try {
            const response = await fetch(`/search-artist?name=${encodeURIComponent(artist)}`);
            const artistResults = await response.json();

            if (artistResults.length === 0) {
                setErrorMessage("No artists found");
                setShowToast(true);
                setIsError(true);
                setPopupVisible(false);
            } else {
                setFilteredResults(artistResults);
                setErrorMessage('');
                setPopupVisible(true); // show artist picker
            }
        } catch (error) {
            console.error('Error fetching artist:', error);
            setErrorMessage("Failed to fetch artists. Please try again.");
            setShowToast(true);
            setIsError(true);
        }
    };

    const handleSearch = async () => {

        // Validation
        if (!artist.trim()) {
            setErrorMessage('Artist field is empty!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!numSongs.trim()) {
            setErrorMessage('Number of songs is empty!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!searchType) {
            setErrorMessage('Please select a sort option!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!checkArtistExists(artist)) {
            setErrorMessage('Please select a valid artist!');
            setArtistFound(false);
            setShowToast(true);
            setIsError(true);
            return;
        }
        // if (!artistSelectedFromPopup) {
        //     setErrorMessage('Please select the artist using Get Artists!');
        //     setShowToast(true);
        //     setIsError(true);
        //     return;
        // }
        if (searchType === 'Manual' && selectedSongs.length !== parseInt(numSongs)) {
            setErrorMessage('Please select songs from the list!');
            setShowToast(true);
            setIsError(true);
            return;
        }

        setErrorMessage('');
        setIsError(false);
        setShowToast(false);
        setArtistFound(true);
        setHasSearched(true);

        if (searchType === 'Manual') {
            console.log('Manually selected songs:', selectedSongs);
            setWordCloudData(selectedSongs);
        } else if (searchType === 'Popularity') {
            console.log('Fetching popular songs...');
            const popularSongs = await getPopularSongs();
            setWordCloudData(popularSongs);
        }

        setShowWordCloud(true);
    };

    const getPopularSongs = async () => {
        try {
            const response = await fetch(`/get-popular-songs-by-artist?artistId=${encodeURIComponent(artistId)}&numSongs=${encodeURIComponent(numSongs)}`);
            const songResults = await response.json();

            console.log(songResults);

            if (songResults.length === 0) {
                setErrorMessage("No songs found for this artist.");
                setShowToast(true);
                setIsError(true);
                return [];
            } else {
                console.log('Songs fetched:', songResults);
                return songResults; // return the array instead of setting word cloud here
            }
        } catch (error) {
            console.error('Error fetching songs:', error);
            setErrorMessage("Failed to fetch songs. Please try again.");
            return [];
        }
    };

    const handleAddToResults = async () => {

        console.log('Checking Add to Results...');

        if (!wordCloudData || wordCloudData.length === 0) {
            setErrorMessage("Need a valid word cloud!");
            setShowToast(true);
            setIsError(true);
            return;
        }

        // Validation checks
        if (!artist.trim()) {
            setErrorMessage('Artist field is empty!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!numSongs) {
            setErrorMessage('Number of songs is empty!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!searchType) {
            setErrorMessage('Please select a sort option!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!checkArtistExists(artist)) {
            setErrorMessage('Please select a valid artist!');
            setArtistFound(false);
            setShowToast(true);
            setIsError(true);
            return;
        }
        // if (!artistSelectedFromPopup) {
        //     setErrorMessage('Please select the artist using Get Artists!');
        //     setShowToast(true);
        //     setIsError(true);
        //     return;
        // }

        console.log('Selected Songs:', selectedSongs.length);
        console.log('Required Songs:', numSongs);

        if (searchType === 'Manual' && selectedSongs.length < numSongs) {
            setErrorMessage('Please select enough songs!');
            setShowToast(true);
            setIsError(true);
            return;
        }

        let songsToAdd = [];

        if (searchType === 'Manual') {
            songsToAdd = selectedSongs;
        } else if (searchType === 'Popularity') {
            songsToAdd = await getPopularSongs();
        }

        const combined = [...wordCloudData];
        songsToAdd.forEach(song => {
            if (!combined.some(existing => existing.songName === song.songName)) {
                combined.push(song);
            }
        });

        setWordCloudData(combined);
        setShowWordCloud(true);

        setErrorMessage('');
        setShowToast(false);
        setIsError(false);
        console.log('Successfully added to word cloud.');
    };

    const getFavorites = async () => {
        try {
            const username = localStorage.getItem("username");
            const response = await fetch(`/get-favorite-songs?username=${encodeURIComponent(username)}`);
            const favResults = await response.json();
            return favResults;
        } catch (error) {
            console.error('Error fetching favorite songs:', error);
            setErrorMessage("Failed to fetch favorite songs. Please try again.");
            setShowToast(true);
            setIsError(true);
            return [];
        }
    };

    const handleSearchFromFavorite = async () => {

        const favResults = await getFavorites();
        setWordCloudData(favResults);
        setShowWordCloud(true);
    };

    const handleAddFromFavorite = async () => {

        if (!wordCloudData || wordCloudData.length === 0) {
            console.log('Error: No word cloud data');
            setErrorMessage("Need a valid word cloud!");
            setShowToast(true);
            setIsError(true);
            return;
        }

        const favResults = await getFavorites();

        const combined = [...wordCloudData];
        favResults.forEach(song => {
            if (!combined.some(s => s.songName === song.songName)) {
                combined.push(song);
            }
        });

        setWordCloudData(combined); // update with the bigger list
        setShowWordCloud(true);
    };

    // Function to handle the dropdown selection
    const handleDropdownSelect = (type) => {

        console.log('Dropdown selected:', type);
        setSearchType(type);
        setIsOpen(false);
        setShowGetSongsButton(type === 'Manual');
        setSelectedSongs([]);
    };

    // only used to manually pick songs
    const searchSongs = async () => {

        if (!selectedArtist) {
            setErrorMessage('No artist selected!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        if (!numSongs) {
            setErrorMessage('Number of songs is empty!');
            setShowToast(true);
            setIsError(true);
            return;
        }
        try {
            const response = await fetch(`/get-songs-by-artist?artistId=${encodeURIComponent(artistId)}`);
            const songResults = await response.json();

            console.log(songResults);

            if (songResults.length === 0) {
                setErrorMessage("No songs found for this artist.");
                setShowToast(true);
                setIsError(true);

            } else {
                console.log('Songs fetched:', songResults);
                setFetchedSongs(songResults);
            }
        } catch (error) {
            console.error('Error fetching songs:', error);
            setErrorMessage("Failed to fetch songs. Please try again.");
        }
    };

    const handleCloseSongsPopup = () => {

        console.log("number of selected songs: ", selectedSongs.length);
        console.log("selectedSongs: ", selectedSongs);
        if (selectedSongs.length === parseInt(numSongs)) {
            setFetchedSongs([]);
            setPopupVisible(false);
        }
    };

    // automatically close song selection menu
    useEffect(() => {
        if (selectedSongs.length === parseInt(numSongs)) {
            handleCloseSongsPopup();
        }
    }, [selectedSongs, numSongs]);

    const handleSongSelection = (song) => {

        setSelectedSongs(prev => {
            const isAlreadySelected = prev.some(s => s.songName === song.songName);

            if (isAlreadySelected) {
                return prev.filter(s => s.songName !== song.songName);
            }

            if (prev.length < NUM_SONGS) {
                return [...prev, song];
            }
        });
    };

    // Function to handle artist selection and update artist input
    const handleArtistSelect = (id, name) => {

        console.log('ArtistSelect:', name);
        console.log(filteredResults);
        const selected = filteredResults.find(item => item.id === id);
        setSelectedArtist(selected);  // Store the selected artist
        setArtist(name);  // Set the artist name in the input field
        setArtistId(id); // saving id of artist
        setArtistSelectedFromPopup(true); // User selected artist
        setPopupVisible(false);  // Close the popup after selection
    };

    // Function to close the artist popup
    const closePopup = () => {

        setPopupVisible(false);
    };

    return (
        <div>
            <PopUp showPopup={showToast} errorMessage={errorMessage} setShowPopup={setShowToast} isError={isError}/>
            <SearchNav/>

            <div className="w-full h-screen bg-customBlue flex justify-center items-center">
                <div className="bg-customDark p-6 sm:p-10 rounded-lg shadow-xl w-full max-w-6xl">
                    <div className="flex flex-col items-center w-full max-w-[888px] mx-auto">
                        <h3 className="text-2xl sm:text-4xl text-white mb-4 sm:mb-8 text-center" tabIndex="0" aria-label="search page title">SEARCH</h3>

                        <div className="flex flex-wrap justify-center gap-4 w-full" aria-label="search input fields">
                            <div className="flex flex-col items-center space-y-2">
                                <div className="bg-white border border-gray-300 rounded px-2 py-1">
                                    <input
                                        id="artistName"
                                        tabIndex="0"
                                        type="text"
                                        placeholder="Artist"
                                        aria-label="Artist Name Input"
                                        className="text-center bg-white outline-none w-[120px] py-2"
                                        value={artist}
                                        onChange={(e) => {
                                            setArtist(e.target.value);
                                            setArtistSelectedFromPopup(false);
                                            setSelectedSongs([]);

                                        }}
                                    />
                                </div>
                                <button
                                    tabIndex="0"
                                    onClick={fetchArtistData}
                                    data-testid={"get-artists"}
                                    className="bg-buttonDark text-white px-3 py-1.5 rounded-md hover:bg-blue-800 w-[140px]"
                                    aria-label="Get Artist Button"
                                >
                                    Get Artists
                                </button>
                            </div>

                            <div className="relative">
                                <button
                                    tabIndex="0"
                                    onClick={() => setIsOpen(!isOpen)}
                                    aria-label="Sort By Dropdown"
                                    className="text-white bg-buttonDark hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-3 py-1.5 text-center inline-flex items-center h-[40px]"
                                    type="button"
                                >
                                    {searchType || 'Sort By'}
                                </button>

                                {isOpen && (
                                    <div className="absolute mt-2 z-10 bg-white divide-y divide-gray-100 rounded-lg shadow-sm w-44" aria-label="Sort Options Menu">
                                        <ul className="py-2 text-sm text-gray-700">
                                            <li>
                                                <a
                                                    href="#"
                                                    tabIndex="0"
                                                    aria-label="Sort by Manual"
                                                    className={`block px-4 py-2 hover:bg-gray-100 ${searchType === 'Manual' ? 'bg-gray-200' : ''}`}
                                                    onClick={() => handleDropdownSelect('Manual')}
                                                >
                                                    Manual
                                                </a>
                                            </li>
                                            <li>
                                                <a
                                                    href="#"
                                                    tabIndex="0"
                                                    aria-label="Sort by Popularity"
                                                    className={`block px-4 py-2 hover:bg-gray-100 ${searchType === 'Popularity' ? 'bg-gray-200' : ''}`}
                                                    onClick={() => handleDropdownSelect('Popularity')}
                                                >
                                                    Popularity
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                )}
                            </div>

                            <div className="flex flex-col items-center space-y-2">
                                <div className="bg-white border border-gray-300 rounded px-2 py-1">
                                    <input
                                        id="numSongs"
                                        type="text"
                                        placeholder="# Songs"
                                        aria-label="Number of Songs Input"
                                        className="text-center bg-white outline-none w-[120px] py-2"
                                        value={numSongs}
                                        onChange={(e) => {
                                            setNumSongs(e.target.value);
                                            setSelectedSongs([]);

                                        }}
                                    />
                                </div>
                                {showGetSongsButton && (
                                    <button
                                        onClick={searchSongs}
                                        data-testid={"get-songs"}
                                        aria-label="get songs button"
                                        className="bg-buttonDark text-white px-3 py-1.5 rounded-md hover:bg-blue-800 w-[140px]"
                                    >
                                        Get Songs
                                    </button>
                                )}
                            </div>

                            <button
                                onClick={handleSearch}
                                data-testid={"search"}
                                aria-label="search button"
                                className="bg-buttonDark text-white rounded px-3 py-1.5 hover:bg-gray-100 w-[100px] h-[60px]"
                            >
                                Search
                            </button>

                            <button
                                onClick={handleAddToResults}
                                data-testid={"add-to-results"}
                                aria-label="add to results button"
                                className="bg-buttonDark text-white rounded px-4 py-2 w-[100px] h-[60px]"
                            >
                                Add to Results
                            </button>

                            <button
                                onClick={handleSearchFromFavorite}
                                data-testid={"search-from-favorite"}
                                aria-label="generate from favorites button"
                                className="bg-buttonDark text-white rounded px-3 py-1.5 hover:bg-gray-100 w-[140px] h-[60px]"
                            >
                                Generate From Favorites
                            </button>

                            <button
                                onClick={handleAddFromFavorite}
                                data-testid={"add-from-favorite"}
                                aria-label="add from favorites button"
                                className="bg-buttonDark text-white rounded px-3 py-1.5 hover:bg-gray-100 w-[100px]"
                            >
                                Add From Favorites
                            </button>
                        </div>

                        {popupVisible && (
                            <ArtistPop
                                filteredResults={filteredResults}
                                onArtistSelect={handleArtistSelect}
                                onClose={closePopup}
                            />
                        )}

                        {fetchedSongs.length > 0 && searchType === 'Manual' && (
                            <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50" aria-label="Song Selection Modal">
                                <div className="bg-white p-6 rounded-lg w-[300px] max-h-[400px] overflow-auto">
                                    <h3 className="text-xl font-semibold mb-4" tabIndex="0">Song Results</h3>
                                    <ul className="space-y-2">
                                        {fetchedSongs.map((song, index) => (
                                            <li key={index} className="flex justify-between">
                                                <button
                                                    tabIndex="0"
                                                    aria-label={`select song ${song.songName}`}
                                                    onClick={() => handleSongSelection(song)}
                                                    className={`${
                                                        selectedSongs.some((s) => s.songName === song.songName) ? 'bg-gray-300' : ''
                                                    } w-full text-left p-2`}
                                                >
                                                    {song.songName}
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            </div>
                        )}

                        {showWordCloud && (
                            <SongWordCloud artist={artist} wordCloudData={wordCloudData} />
                        )}
                    </div>
                </div>
            </div>
            <p
                className="mb-4 text-4xl font-sans leading-none tracking-tight text-center text-black"
                tabIndex="0"
                aria-label="Team 9 Footer"
            >
                Team 9
            </p>
        </div>
    );
}


export default Search;