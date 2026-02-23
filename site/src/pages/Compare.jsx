import React, {useEffect, useRef, useState} from "react";
import SearchNav from '../components/SearchNav';
import Register from "./Register";

function Compare() {
    const [onCompareSubmit, setOnCompareSubmit] = useState(false);
    const [sortOrder, setSortOrder] = useState("");
    const [invalidFriends, setInvalidFriends] = useState([]);
    const [privateFriends, setPrivateFriends] = useState(false); // To track if there are private favorites
    const [selectedSong, setSelectedSong] = useState(null);
    const [selectedSongUsers, setSelectedSongUsers] = useState([]);
    const [friendsFavorites, setFriendsFavorites] = useState([]);


    const mockUsername = "username";
    const hoverTime = useRef(null);

    //Redirect to Login page if no user logged in
    useEffect(() => {
        const username = localStorage.getItem("username");
        if (!username) {
            window.location.href = "/";
        }

    }, []);

    const handleCompareClick = async (e) => {

        e.preventDefault();

        const myUser = localStorage.getItem("username") || mockUsername;
        const input = (document.getElementById('first_name').value) + "," + myUser;
        const friends = input.split(',').map(name => name.trim());

        console.log('friends: ', friends);

        let invalid = []; // invalid users
        let privateUsers = []; // private users
        let friendsData = []; // public data

        for (const name of friends) {
            try {
                const response = await fetch(`/get-user-info?username=${encodeURIComponent(name)}`);
                if (!response.ok) {
                    throw new Error('error getting user details');
                }
                const data = await response.json();
                console.log('fetched data:', data);

                if (!data || Object.keys(data).length === 0) {
                    invalid.push(name);
                    console.log("pushed into invalid: ", name);
                }
                else if (!data.isPublic && name !== myUser) { // private user and it's not myself
                    privateUsers.push(name);
                    console.log("pushed into private: ", name);
                }
                else { // valid user!
                    friendsData.push({
                        name: name,
                        favorites: data.favorites
                    });
                    console.log('friendsData:', friendsData);
                    console.log('favorites data:', data.favorites);
                }

            } catch (error) {
                console.error(`Error fetching info for ${name}:`, error);
                invalid.push(name);
            }
        }
        setInvalidFriends(invalid);
        setPrivateFriends(privateUsers.length > 0);

        console.log('invalid friends:', invalidFriends);
        console.log('private friends:', privateFriends);

        if (invalid.length === 0 && privateUsers.length === 0) {
            setFriendsFavorites(friendsData);
            setOnCompareSubmit(true);
            setSortOrder("desc");  // default to "most to least frequent"
        }

    };

    const handleSortChange = (order) => {
        setSortOrder(order);
    };

    // getting correlating users for songs
    const openSongUsers = (songName) => {
        console.log("Checking users for song:", songName);
        const users = friendsFavorites
            .filter(user => user.favorites.some(song => song.songName.toLowerCase() === songName.toLowerCase()))
            .map(user => user.name);

        console.log("Users favorited this song:", users);
        setSelectedSongUsers(users);
    };


    const getSortedSongs = () => {
        const songMap = new Map(); // songName : value={ details, count }

        friendsFavorites.forEach(friend => {
            friend.favorites.forEach(song => {
                if (!songMap.has(song.songName)) {
                    songMap.set(song.songName, { ...song, count: 1 });
                } else {
                    const existing = songMap.get(song.songName);
                    existing.count += 1;
                    songMap.set(song.songName, existing);
                }
            });
        });

        const sorted = Array.from(songMap.values())
            .sort((a, b) => sortOrder === "desc" ? b.count - a.count : a.count - b.count);

        return sorted;
    };

    const openSongDetails = (song) => {
        setSelectedSong(song);
    };

    const closePopup = () => {

        setSelectedSong(null);
        setSelectedSongUsers([]); // Clear the users list when closing the popup
    };

    return (
        <div className="w-full min-h-screen bg-customBlue pb-20">
            <SearchNav/>

            <div className="w-full flex items-center justify-center pt-20 px-4">
                <div
                    className="box-content p-8 max-w-[50rem] w-full bg-customDark rounded-xl shadow-lg flex flex-col items-center">
                    <h1 className="text-white text-2xl sm:text-3xl font-bold text-center mb-6">
                        Compare Favorites with Friends
                    </h1>

                    <form className="w-full flex flex-col sm:flex-row gap-4 justify-center items-center">
                        <input
                            type="text"
                            id="first_name"
                            tabIndex="0"
                            aria-label="Friend usernames for comparison input"
                            className="flex-grow bg-gray-50 border border-gray-300 text-gray-900 text-sm focus:ring-blue-500 focus:border-blue-500 block p-2.5 h-[42px] rounded"
                            placeholder="Input the username of friend(s)..."

                            required
                        />
                        <button
                            type="submit"
                            tabIndex="0"
                            aria-label="Compare Favorites with Friends button"
                            onClick={handleCompareClick}
                            className="bg-buttonDark hover:bg-blue-700 text-white font-bold px-6 h-[42px] rounded"
                        >
                            Compare Lists
                        </button>
                    </form>

                    {invalidFriends.length > 0 && (
                        <div
                            className="fixed bottom-8 left-1/2 transform -translate-x-1/2 bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-lg shadow-lg flex items-center gap-3 z-50">
                            <svg className="w-6 h-6 text-red-700" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                                 xmlns="http://www.w3.org/2000/svg" tabIndex="0"
                                 aria-label="Error message for nonexistent user">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                      d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                            <p className="text-sm">
                                Error comparing lists. One or more usernames do not exist.
                            </p>
                        </div>
                    )}

                    {privateFriends && (
                        <div
                            className="fixed bottom-8 left-1/2 transform -translate-x-1/2 bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-lg shadow-lg flex items-center gap-3 z-50">
                            <svg className="w-6 h-6 text-red-700" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                                 xmlns="http://www.w3.org/2000/svg" tabIndex="0"
                                 aria-label="error messsage for private user">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                      d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                            <p className="text-sm">
                                Error comparing lists. One or more user’s favorite lists are private.
                            </p>
                        </div>
                    )}

                    {onCompareSubmit && !privateFriends && (
                        <>
                            <form className="flex mt-6 gap-8">
                                <div className="flex items-center">
                                    <h3 className="font-bold mr-6 text-white"> Sort By: </h3>
                                    <input
                                        tabIndex="0"
                                        type="radio"
                                        name="sort-option"
                                        id="most-frequent"
                                        aria-label="Sort by Most to Least frequent"
                                        checked={sortOrder === "desc"}
                                        onChange={() => handleSortChange("desc")}
                                        className="shrink-0 mt-0.5 border-gray-200 rounded-full text-blue-600 focus:ring-blue-500"
                                    />
                                    <label htmlFor="most-frequent" className="text-sm text-gray-300 ml-2 font-bold">
                                        Most to Least Frequent Favorite Song
                                    </label>
                                </div>

                                <div className="flex items-center mt-2">
                                    <input
                                        tabIndex="0"
                                        type="radio"
                                        name="sort-option"
                                        id="least-frequent"
                                        aria-label="Sort by Least to Most frequent"
                                        checked={sortOrder === "asc"}
                                        onChange={() => handleSortChange("asc")}
                                        className="shrink-0 mt-0.5 border-gray-200 rounded-full text-blue-600 focus:ring-blue-500"
                                    />
                                    <label htmlFor="least-frequent" className="text-sm text-gray-300 ml-2 font-bold">
                                        Least to Most Frequent Favorite Song
                                    </label>
                                </div>
                            </form>
                        </>
                    )}
                </div>
            </div>

            {sortOrder && !privateFriends && (
                <div className="w-full flex justify-center mt-10 px-4">
                    <div className="max-w-[50rem] w-full bg-white p-8 rounded-xl shadow-lg overflow-x-auto">
                        <table className="w-full text-black border-collapse">
                            <thead>
                            <tr>
                                <th className="border-b-2 p-4 text-white bg-buttonDark text-left">Song</th>
                                <th className="border-b-2 p-4 text-white bg-buttonDark text-left">Frequency</th>
                            </tr>
                            </thead>
                            <tbody>
                            {getSortedSongs().map((song, index) => (
                                <tr
                                    key={index}
                                    className="hover:bg-gray-100 cursor-pointer"
                                    onClick={() => openSongDetails(song)}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            openSongUsers(song.songName);

                                        }
                                    }}
                                >
                                    <td className="p-4" tabIndex="0" aria-label={song.songName}>{song.songName}</td>
                                    <td
                                        className="p-4 cursor-pointer hover:underline hover:text-blue-500"
                                        tabIndex="0"
                                        aria-label={song.count}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            openSongUsers(song.songName);

                                        }}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter') {
                                                e.preventDefault();
                                                e.stopPropagation();
                                                openSongUsers(song.songName);

                                            }
                                        }}
                                        onMouseEnter={() => {
                                            hoverTime.current = setTimeout(() => {
                                                openSongUsers(song.songName);
                                            }, 1000);

                                        }}
                                        onMouseLeave={() => {
                                            clearTimeout(hoverTime.current);
                                            // setSelectedSongUsers([]);
                                        }}
                                    >
                                        {song.count}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {selectedSong && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-white rounded-lg p-8 shadow-lg w-96 relative">
                        <button
                            onClick={closePopup}
                            tabIndex="0"
                            aria-label="close song details"
                            className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
                        >
                            ✖
                        </button>

                        <h2 className="text-2xl font-bold mb-4">{selectedSong.songName}</h2>
                        <p><strong>Artist:</strong> {selectedSong.artistName}</p>
                        <p><strong>Year of Recording:</strong> {selectedSong.releaseYear}</p>
                    </div>
                </div>
            )}

            {selectedSongUsers.length > 0 && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-white rounded-lg p-8 shadow-lg w-96 relative">
                        <button
                            onClick={() => setSelectedSongUsers([])}
                            tabIndex="0"
                            aria-label="close list of those who favorited this song"
                            className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
                        >
                            ✖
                        </button>

                        <h2 className="text-2xl font-bold mb-4">Users who favorited this song</h2>
                        <ul className="list-disc list-inside">
                            {selectedSongUsers.map((user, index) => (
                                <li key={index}>{user}</li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}
            <p
                className="fixed bottom-4 left-1/2 transform -translate-x-1/2 text-4xl font-sans leading-none tracking-tight text-black"
                tabIndex="0"
                aria-label="Team 9 Footer"
            >
                Team 9
            </p>
        </div>
    );
}

export default Compare;