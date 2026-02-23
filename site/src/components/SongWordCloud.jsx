import React, { useState, useMemo, useEffect } from 'react';
import WordCloud from 'react-d3-cloud';
import { scaleOrdinal } from 'd3-scale';
import { schemeCategory10 } from 'd3-scale-chromatic';
import PopUp from "./PopUp";

const fillerWords = new Set([
  'a', 'an', 'the', 'and', 'or', 'but', 'if', 'in', 'on', 'at', 'by', 'for', 'with',
  'to', 'of', 'is', 'it', 'so', 'do', 'us', 'get', 'got', 'just', 'be', 'as', 'oh',
  'ah', 'ya', 'ooh', 'are', 'no', 'yes', 'were', 'was', 'like', 'uh', 'my',
  'from', 'they', 'into', "don't", 'oh', 'too', 'go', 'had', 'not',
  "i'm", "you're", "it'd", "i'll", "you'll", "i'd", "'til'", 'he', "i've", "on", "it'll", "it's", "it'd", 'getting', "we're", 'can'
]);

const exceptions = new Set([
  'nothing', 'something', 'anything', 'everything', 'loving', 'caring',
  'waiting', 'doing'
]);

const cleanAndTokenize = (text) => {
  const knownSuffixes = ['ing', 'es', 'ed', 's', 'ies'];

  // const isLongVowelWord = (word) => {
  //   const vowels = ['a', 'e', 'i', 'o', 'u'];
  //   const lastChar = word[word.length - 1];
  //   const secondLastChar = word[word.length - 2];
  //   return vowels.includes(secondLastChar) && !vowels.includes(lastChar);
  // };

  // Function to re-add the 'e' in cases like "dancing" -> "dance"
  const addEToStem = (stem) => {
    if (stem.endsWith('c') || stem.endsWith('v')) {
      return stem + 'e';
    }
    return stem;
  };

  const stripSuffix = (word) => {
    if (word.length <= 3) return word;

    if (word.endsWith('ing') && word.length > 5 && !exceptions.has(word)) {
      let stem = word.slice(0, -3);

      // Handle special cases for adding 'e' back when needed
      stem = addEToStem(stem);

      // Remove doubled consonants (e.g., "getting" -> "get")
      if (/([a-z])\1$/.test(stem)) {
        stem = stem.slice(0, -1);
      }
      return stem;
    } else if (word.endsWith('ed') && word.length > 4) {
      let stem = word.slice(0, -2);
      if (/([a-z])\1$/.test(stem)) {
        stem = stem.slice(0, -1);
      }
      return stem;
    } else if (word.endsWith('ies') && word.length > 4) {
      return word.slice(0, -3) + 'y';
    } else if (word.endsWith('es') && word.length > 4 && !/(sses|ches|shes|xes|zes)$/.test(word)) {
      return word.slice(0, -2);
    } else if (word.endsWith('s') && word.length > 3 && !/(ss|us|is|as)$/.test(word)) {
      return word.slice(0, -1);
    }

    return word;
  };

  const normalizeApostrophes = (word) => {
    word = word.replace(/^'+|'+$/g, '');

    // Remove common contractions
    word = word.replace(/n't$/, '');     // can't -> can
    word = word.replace(/'ll$/, '');     // you'll -> you
    word = word.replace(/'ve$/, '');     // they've -> they
    word = word.replace(/'re$/, '');     // they're -> they
    word = word.replace(/'d$/, '');      // she'd -> she
    word = word.replace(/'m$/, '');      // I'm -> I
    word = word.replace(/'s$/, '');      // John's -> John
    word = word.replace(/^'til$/, 'til'); // 'til -> til

    return word;
  };

  const originalWords = text.toLowerCase().replace(/[^a-z\s']/g, '').split(/\s+/);

  const normalizedWords = originalWords.map(word => {
    word = normalizeApostrophes(word);
    return stripSuffix(word);
  });

  const filtered = normalizedWords.filter(word => {
    return word && !fillerWords.has(word);
  });


  return filtered;
};

// Counts frequency of words
const countWords = (wordCloudData) => {
  const frequency = {};
  wordCloudData.forEach(song => {
    const words = cleanAndTokenize(song.lyrics);
    words.forEach(word => {
      frequency[word] = (frequency[word] || 0) + 1;
    });
  });

  const wordCloud = Object.entries(frequency)
    .map(([text, value]) => ({ text, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 100);

  console.log("Words in word cloud:", wordCloud.map(w => w.text));
  console.log("Total number of words in word cloud:", wordCloud.length);

  return wordCloud;
};

// word must be highlighted in lyrics from song selected
const highlightWordInLyrics = (lyrics, wordToHighlight) => {
    const regex = new RegExp(`\\b(${wordToHighlight})\\b`, 'gi');
    return lyrics.replace(regex, '<span class="bg-yellow-300 font-bold">$1</span>');
};


const SongWordCloud = ({ artist, wordCloudData }) => {
    const [render, setRender] = useState('cloud');
    const [selectedWord, setSelectedWord] = useState(null);
    const [modalData, setModalData] = useState([]);
    const [favorites, setFavorites] = useState([]);
    const [showPopup, setShowPopup] = useState(false);
    const [matchingSongs, setMatchingSongs] = useState([]);
    const [songDetails, setSongDetails] = useState(null);
    const [showToast, setShowToast] = useState(false);
    const [toastMessage, setToastMessage] = useState('');
    const [isToastError, setIsToastError] = useState(false);

    const wordData = useMemo(() => countWords(wordCloudData), [wordCloudData]);
    const colorScale = scaleOrdinal(schemeCategory10);

    const handleWordClick = (word) => {
        const matching = wordCloudData.filter(song =>
            cleanAndTokenize(song.lyrics).includes(word.toLowerCase())
        ).map(song => ({
            title: song.songName,
            count: cleanAndTokenize(song.lyrics).filter(w => w === word).length,
            details: song
        }));

        setSelectedWord(word);
        setMatchingSongs(matching);
        setModalData(matching.map(song => ({
            song: song.title,
            frequency: song.count,
            details: song.details
        })));
        setShowPopup(true);
    };

    const closeModal = () => {
        setShowPopup(false);
        setSongDetails(null);
    };

    const handleFavoriteClick = async (songDetails) => {
        setFavorites(prev => [...prev, songDetails]);

        const username = localStorage.getItem("username");
        const requestData = { username: username, songId: songDetails.songId };

        try {
            const response = await fetch('/add-to-favorite-songs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });

            const message = await response.text();

            if (response.ok) {
                setToastMessage('Successfully added to favorites.');
                setIsToastError(false);
                setShowToast(true);
            } else if (message.includes('already')) {
                setToastMessage('Song is already in favorites.');
                setIsToastError(true);
                setShowToast(true);
            } else {
                setToastMessage('Error adding song to favorites.');
                setIsToastError(true);
                setShowToast(true);
            }
        } catch (error) {
            console.error('Adding to favorites song failed:', error.message);
            setToastMessage('Unexpected error.');
            setIsToastError(true);
            setShowToast(true);
        }
    };

    const handleSongClick = (songDetails) => {
        setSongDetails(songDetails);
    };

    if (!wordCloudData || wordCloudData.length === 0) {
        return <div className="text-center mt-6">No songs to display.</div>;
    }



    return (
        <div className="flex flex-col items-center relative">
            {render === 'cloud' && (
                <div className="wordcloud-container mt-4" style={{ width: '800px', height: '500px', backgroundColor: 'white' }}>
                    <WordCloud
                        data={wordData}
                        width={800}
                        height={500}
                        font="Impact"
                        fontSize={(word) => Math.sqrt(word.value) * 7}
                        spiral="archimedean"
                        rotate={0}
                        padding={3}
                        random={Math.random}
                        fill={(d, i) => colorScale(i)}
                        onWordClick={(event, d) => handleWordClick(d.text)}
                    />
                </div>
            )}

            {render === 'table' && (
                <div className="mt-4 max-h-[400px] overflow-y-auto w-full">
                    <table className="min-w-full border-collapse table-auto border border-gray-300 bg-white"
                        aria-label="word frequency table">
                        <thead>
                        <tr>
                            <th className="py-2 px-4 border-b border-gray-300">Word</th>
                            <th className="py-2 px-4 border-b border-gray-300">Frequency</th>
                        </tr>
                        </thead>
                        <tbody>
                        {wordData.map((item, index) => (
                            <tr key={index}>
                                <td
                                    tabIndex="0"
                                    aria-label={`word: ${item.text}`}
                                    className="py-2 px-4 border-b border-gray-300 text-blue-500 cursor-pointer hover:underline"
                                    onClick={() => handleWordClick(item.text)}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") {
                                            handleWordClick(item.text);
                                        }
                                    }}
                                >
                                    {item.text}
                                </td>
                                <td className="py-2 px-4 border-b border-gray-300">{item.value}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

            <div className="flex gap-4 mt-4">
                <button onClick={() => setRender('table')} className="p-2 px-4 bg-blue-500 text-white rounded hover:bg-blue-600">Table</button>
                <button onClick={() => setRender('cloud')} className="p-2 px-4 bg-blue-500 text-white rounded hover:bg-blue-600">Word Cloud</button>
            </div>

            {/* Word Modal */}
            {showPopup && (
                <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50 animate-fade" onClick={closeModal}>
                    <div className="bg-white p-6 rounded-lg shadow-lg w-[90%] max-w-md" onClick={(e) => e.stopPropagation()}>
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold">Songs with "{selectedWord}"</h2>
                            <button
                                onClick={closeModal}
                                data-testid={"close-song-source-list"}
                                className="text-xl text-gray-500 hover:text-black">&times;</button>
                        </div>
                        <ul className="max-h-60 overflow-y-auto">
                            {modalData.map((item, index) => (
                                <div key={index} className="flex justify-between items-center group">
                                    <li
                                        tabIndex="0"
                                        aria-label={`song: ${item.song}, uses ${item.frequency} times`}
                                        className="text-sm cursor-pointer text-blue-500 hover:text-blue-700"
                                        onClick={() => handleSongClick(matchingSongs[index].details)}
                                        onKeyDown={(e) => {
                                            if (e.key === "Enter") {
                                                handleSongClick(matchingSongs[index].details);
                                            }
                                        }}
                                    >
                                        {item.frequency} : {item.song}
                                    </li>
                                    <button
                                        tabIndex="0"
                                        data-testid="add-to-favorites"
                                        aria-label={`add ${item.song} to favorites`}
                                        onClick={() => handleFavoriteClick(item.details)}
                                        className="text-red-500 opacity-0 group-hover:opacity-100 transition duration-200"
                                    >
                                        ♥ Add to Favorites
                                    </button>
                                </div>
                            ))}
                        </ul>
                    </div>
                </div>
            )}

            {/* Lyrics Modal */}
            {songDetails && (
                <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50 animate-fade"
                     onClick={closeModal}>
                    <div className="bg-white p-6 rounded-lg shadow-lg w-[90%] max-w-full overflow-x-auto"
                         onClick={(e) => e.stopPropagation()}>
                        <h2 className="text-xl font-bold mb-2">{songDetails.songName}</h2>
                        <p className="text-sm text-gray-600 mb-2">by {songDetails.artistName}</p>
                        <p className="text-xs text-gray-400 mb-4">{songDetails.releaseYear}</p>
                        <div className="text-sm" style={{ paddingBottom: '10px' }} dangerouslySetInnerHTML={{ __html: highlightWordInLyrics(songDetails.lyrics, selectedWord) }} />
                        <button onClick={closeModal} className="mt-4 p-2 bg-red-500 text-white rounded hover:bg-red-600">Close</button>
                    </div>
                </div>
            )}

            <PopUp
                showPopup={showToast}
                errorMessage={toastMessage}
                setShowPopup={setShowToast}
                isError={isToastError}
            />
        </div>
    );
};

export default SongWordCloud;
