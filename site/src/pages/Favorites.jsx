import { React, useState } from 'react';
import '../styles/index.css';
import Nav from '../components/Nav';
import SearchNav from '../components/SearchNav';
import { useEffect } from 'react';
import thumbsUp from '../images/thumbsUp.png';
import thumbsDown from '../images/thumbsDown.png';
import "../styles/index.css";

export default function FavoriteSongs({ mockUsername }) {

    const [songs, setSongs] = useState([]);

    const [visibility, setVisibility] = useState(() => {
        const username = localStorage.getItem("username");
        return localStorage.getItem(`visibility_${username}`) ?? 'private';
    });

    useEffect(() => {
        const username = localStorage.getItem("username");
        if (username) {
            localStorage.setItem(`visibility_${username}`, visibility);
        }
    }, [visibility]);

    const [hoveredSong, setHoveredSong] = useState(null);
    const [selectedSong, setSelectedSong] = useState(null);
    const [showConfirmDeleteOne, setShowConfirmDeleteOne] = useState(false);
    const [showConfirmDeleteAll, setShowConfirmDeleteAll] = useState(false);
    const [songToDelete, setSongToDelete] = useState(null);
    const [statusMessage, setStatusMessage] = useState(null);

    useEffect(() => {
        const username = mockUsername ?? localStorage.getItem("username");
        if (!username) {
            window.location.href = "/";
            return;
        }

        fetch(`/get-favorite-songs?username=${encodeURIComponent(username)}`)
            .then(res => res.json())
            .then(data => {
              const formatted = data.map(song => ({
                id: song.songId,
                title: song.songName,
                artist: song.artistName,
                year: song.releaseYear
              }));
              setSongs(formatted);
            })

            .catch(err => {
                console.error("Failed to fetch favorites", err);
            });
    }, []);

    const [pageTimer, setPageTimer] = useState(null);

    //Use Effect for inactivity timer
    useEffect(() => {
        //Once window.location.href switches to this page (changes to /search), set timer for user activity
        //Set timer for user activity
        setPageTimer(setTimeout(() => {
            handleLogOutAndRedirectToLogin();
            }, 60000));
    }, []);

    const handleLogOutAndRedirectToLogin = () => {
        //Log user out and redirect them to the login page
        const username = localStorage.getItem("username");
        localStorage.removeItem("username");
        window.location.href = "/";
    }

    //Reset timer if activity on page
    const resetTimerWithActivity = () => {
        //Reset inactivity timer since activity occured
        if (pageTimer){
            clearTimeout(pageTimer);

            //Set timer again for user activity
            setPageTimer(setTimeout(() => {
                handleLogOutAndRedirectToLogin();
            }, 60000));
        }
    }

    async function deleteSong() {
        const username = mockUsername ?? localStorage.getItem("username");
        console.log("" + (songToDelete + 1) + " <- song to delete");
        const requestData = { username: username, songOrder: "" + (songToDelete + 1)};
        console.log("" + (songToDelete + 1) + " <- song to delete");

        try {
            const response = await fetch('/delete-favorite-song', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }
        } catch (error) {
            console.error('Deleting song failed:', error.message);
        }
    }

    async function deleteAllSongs() {
        const username = mockUsername ?? localStorage.getItem("username");
        const requestData = { username: username};

        try {
            const response = await fetch('/delete-all-favorite-songs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }
        } catch (error) {
            console.error('Deleting song failed:', error.message);
        }
    }

    const handleMoveUp = async (index) => {
        const newSongs = [...songs];
        [newSongs[index - 1], newSongs[index]] = [newSongs[index], newSongs[index - 1]];
        setSongs(newSongs);

        await fetch('/move-song', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: mockUsername ?? localStorage.getItem("username"),
                songId: newSongs[index - 1].id,
                direction: 'up',
            }),
        });
    };

    const handleMoveDown = async (index) => {
      const newSongs = [...songs];
      [newSongs[index], newSongs[index + 1]] = [newSongs[index + 1], newSongs[index]];
      setSongs(newSongs);

      await fetch('/move-song', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: mockUsername ?? localStorage.getItem("username"),
          songId: newSongs[index + 1].id,
          direction: 'down',
        }),
      });
    };

    const handleVisibilityChange = async (value) => {
        setVisibility(value);
        const response = await fetch('/set-visibility', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: mockUsername ?? localStorage.getItem("username"),
                visibility: value,
            }),
        });
    };

    const requestDelete = (index) => {
        setSongToDelete(index);
        setShowConfirmDeleteOne(true);
    };

    const confirmDelete = async () => {
        await deleteSong();

        const newSongs = songs.filter((_, i) => i !== songToDelete);
        setSongs(newSongs);
        setShowConfirmDeleteOne(false);
        setSongToDelete(null);
    };

    const cancelDelete = () => {
        setShowConfirmDeleteOne(false);
        setSongToDelete(null);
    };

    const requestDeleteAll = () => {
        setShowConfirmDeleteAll(true);
    };

    const confirmDeleteAll = async () => {
        setSongs([]);
        setShowConfirmDeleteAll(false);

        await deleteAllSongs();
    };

    const cancelDeleteAll = () => {
        setShowConfirmDeleteAll(false);
    };

    const handleSoulmate = async () => {
        const response = await fetch('/get-soulmate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: mockUsername ?? localStorage.getItem("username"),
            }),
        });

        if (!response.ok) {
            setStatusMessage({
                error: 'true',
                name: 'No Soulmate Found.',
            });
            return;
        }

        const data = await response.json();
        console.log(data); ///////////////////////////////////////////////////////////
        setStatusMessage({
            error: 'false',
            type: 'soulmate',
            name: data.name,
            songs: data.songs,
            match: data.match === 'true'
        });
    };

    const handleEnemy = async () => {
        const response = await fetch('/get-enemy', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: mockUsername ?? localStorage.getItem("username"),
            }),
        });

        if (!response.ok) {
            setStatusMessage({
                error: 'true',
                name: 'No Enemy Found.',
            });
            return;
        }

        const data = await response.json();
        console.log(data); ///////////////////////////////////////////////////////////
        setStatusMessage({
            error: 'false',
            type: 'enemy',
            name: data.name,
            songs: data.songs,
            match: data.match === 'true'
        });
    };

    const resetSoulmateEnemyMatch = () => {
        setStatusMessage(prevState => ({
            ...prevState,
            match: false,
        }));
    };

    useEffect(() => {
        if (statusMessage?.match === true) {
            const timer = setTimeout(resetSoulmateEnemyMatch, 1000); // 1 sec timeout
            return () => clearTimeout(timer);
        }
    }, [statusMessage?.match, setStatusMessage]);

    return (
        <div onClick={resetTimerWithActivity} onMouseMove={resetTimerWithActivity} onKeyDown={resetTimerWithActivity}>
            <SearchNav/>
            <div className="w-full h-screen bg-customBlue flex justify-center items-center">
                <div className="bg-[#7079b7] p-10 rounded-lg shadow-lg w-full max-w-3xl flex gap-6 mt-10">

                    {/*{statusMessage?.type === "soulmate" ? (*/}
                    {/*  <img src={thumbsUp} alt="Thumbs Up Sign" className="thumbsUp" />*/}
                    {/*) : statusMessage?.type === "enemy" ? (*/}
                    {/*  <img src={thumbsDown} alt="Thumbs Down Sign" className="thumbsDown" />*/}
                    {/*) : null}*/}

                    {statusMessage?.match === true ? (
                        statusMessage.type === "soulmate" ? (
                            <img
                                key={`thumbs-up-${Date.now()}`}
                                src={thumbsUp}
                                alt="Thumbs Up Sign"
                                className="thumbsUp"
                                tabIndex={0}
                            />
                        ) : (
                            <img
                                key={`thumbs-down-${Date.now()}`}
                                src={thumbsDown}
                                alt="Thumbs Down Sign"
                                className="thumbsDown"
                                tabIndex={0}
                            />
                        )
                    ) : null}

                        {/* Left: Soulmate & Enemy */}
                        <div
                            className="bg-[#dbe0f5] p-4 rounded-md flex flex-col gap-4 items-center shadow-md self-start">
                            <button onClick={handleSoulmate}
                                    className="bg-[#1f254f] text-white py-2 px-4 rounded-md text-lg">
                                Soulmate
                            </button>
                            <button onClick={handleEnemy}
                                    className="bg-[#1f254f] text-white py-2 px-4 rounded-md text-lg">
                                Enemy
                            </button>


                            {statusMessage && (
                                <div
                                    className="text-[#1f254f] text-center mt-2 flex flex-col items-center font-semibold">
                                    {statusMessage.error === "true" ? (
                                        <span className="font-bold">{statusMessage.name}</span>
                                    ) : (
                                        <>

                                            <button
                                                className="text-xl leading-none hover:text-black"
                                                onClick={() => setStatusMessage(null)}
                                            >
                                                ✖
                                            </button>

                                            <span>
                                  Your Lyrical {statusMessage.type === 'soulmate' ? 'Soulmate' : 'Enemy'} is:
                                </span>
                                            <span className="font-bold">{statusMessage.name}</span>
                                            <div className="flex flex-col mt-2 gap-1">
                                                {statusMessage?.songs?.map((title, idx) => (
                                                    <div key={idx}
                                                         className="bg-white px-3 py-1 rounded shadow text-sm">
                                                        {title}
                                                    </div>
                                                ))}
                                            </div>
                                        </>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Center: Song List */}
                        <div className="flex-1 flex flex-col items-start gap-4">
                            <h1 className="text-white text-2xl font-semibold text-left mb-2">My Favorite Songs</h1>

                            {songs.length === 0 ? (
                                <p className="text-white">No Favorite Songs</p>
                            ) : (
                                songs.map((song, index) => (
                                    <div
                                        key={index}
                                        className={`bg-white w-full p-2 rounded flex justify-between items-center shadow ${
                                            hoveredSong === index ? 'bg-yellow-100' : ''
                                        }`}
                                        onMouseEnter={() => setHoveredSong(index)}
                                        onMouseLeave={() => setHoveredSong(null)}
                                    >
                  <span onClick={() => setSelectedSong(song)} className="cursor-pointer underline">
                    {song.title}
                  </span>
                                        {hoveredSong === index && (
                                            <div className="flex gap-2">
                                                <button
                                                    onClick={() => handleMoveUp(index)}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                    disabled={index === 0}
                                                >
                                                    <span className="text-lg">⬆️</span>
                                                </button>
                                                <button
                                                    onClick={() => handleMoveDown(index)}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                    disabled={index === songs.length - 1}
                                                >
                                                    <span className="text-lg">⬇️</span>
                                                </button>
                                                <button
                                                    onClick={() => requestDelete(index)}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                >
                                                    <span className="text-lg">❌</span>
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                ))
                            )}
                        </div>

                        {/* Right: Visibility + Delete All */}
                        <div className="flex flex-col justify-between">
                            <div className="text-white mb-6">
                                <h3 className="font-semibold mb-2">List Visibility</h3>
                                <div className="flex flex-col gap-2">
                                    <label className="flex items-center space-x-2">
                                        <input
                                            aria-label="Public button"
                                            type="radio"
                                            name="visibility"
                                            value="public"
                                            checked={visibility === 'public'}
                                            onChange={() => handleVisibilityChange('public')}
                                        />
                                        <span>Public</span>
                                    </label>
                                    <label className="flex items-center space-x-2">
                                        <input
                                            type="radio"
                                            name="visibility"
                                            value="private"
                                            aria-label="Public"
                                            checked={visibility === 'private'}
                                            onChange={() => handleVisibilityChange('private')}
                                        />
                                        <span>Private</span>
                                    </label>
                                </div>
                            </div>

                            <button
                                onClick={requestDeleteAll}
                                aria-label="Button to request delete all the songs"
                                className="bg-[#1f254f] text-white py-2 px-4 rounded font-semibold shadow hover:bg-[#2b3269]"
                            >
                                Delete All Songs
                            </button>
                        </div>
                    </div>
                </div>

                {/* Modal: Confirm delete one */}
                {showConfirmDeleteOne && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-[#dbe0f5] p-6 rounded shadow-md w-80 text-center">
                        <p className="text-lg font-semibold mb-4">Are you sure you want to delete your favorite
                            song?</p>
                        <div className="flex justify-center gap-4">
                            <button onClick={confirmDelete}
                            aria-label="Confirm the delete for the show confirm"
                                    className="bg-[#1f254f] text-white px-4 py-2 rounded hover:bg-[#2a2f60]">
                                Yes, delete
                            </button>
                            <button onClick={cancelDelete}
                            aria-label="Canceling the delete for show confirm"
                                    className="bg-[#1f254f] text-white px-4 py-2 rounded hover:bg-[#2a2f60]">
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Modal: Confirm delete all */}
            {showConfirmDeleteAll && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-[#dbe0f5] p-6 rounded shadow-md w-80 text-center">
                        <p className="text-lg font-semibold mb-4">Are you sure you want to delete all your favorite
                            songs?</p>
                        <div className="flex justify-center gap-4">
                            <button
                                onClick={confirmDeleteAll}
                                aria-label="Confirms the delete option"
                                className="bg-[#1f254f] text-white px-4 py-2 rounded hover:bg-[#2a2f60]"
                            >
                                Yes, delete all
                            </button>
                            <button
                                onClick={cancelDeleteAll}
                                aria-label="Cancels the delete the option"
                                className="bg-[#1f254f] text-white px-4 py-2 rounded hover:bg-[#2a2f60]"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Modal: Song details */}
            {selectedSong && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-[#dbe0f5] p-6 rounded shadow-md w-96 text-center relative">
                        <button
                            onClick={() => setSelectedSong(null)}
                            aria-label="Pick word for song button"
                            className="absolute top-2 right-2 text-xl font-bold text-[#1f254f] hover:text-black"
                        >
                            &times;
                        </button>
                        <h2 className="text-xl font-semibold underline mb-4">{selectedSong.title}</h2>
                        <p className="mb-2 font-medium">Artist: {selectedSong.artist}</p>
                        <p className="font-medium">Year of Recording: {selectedSong.year}</p>
                    </div>
                </div>
            )}

            <p className="mb-4 text-4xl font-sans leading-none tracking-tight text-center text-black">
                Team 9
            </p>
        </div>
    );
}