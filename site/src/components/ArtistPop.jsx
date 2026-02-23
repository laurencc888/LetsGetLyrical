import React from 'react';

// Popup component to display a list of artists
const ArtistPop = ({ filteredResults, onArtistSelect, onClose }) => {
    return (
        <div className="popup-container absolute top-24 left-1/2 transform -translate-x-1/2 bg-white shadow-lg rounded-lg w-[250px] p-4">
            <div className="popup-header flex justify-between items-center">
                <h3 className="text-xl font-bold">Select Artist</h3>
                <button
                    tabIndex="0"
                    aria-label="close list of artists"
                    onClick={onClose}
                    data-testid={"close-artist-pop"}
                    className="text-red-500">X</button>
            </div>
            <ul className="mt-4 max-h-[300px] overflow-y-auto">
                {filteredResults.map((artist, index) => (
                    <li tabIndex="0"
                        aria-label={artist.name}
                        key={index}
                        data-testid={`artist-item-${index}`}
                        className="flex items-center mb-4 cursor-pointer"
                        onClick={() => onArtistSelect(artist.id, artist.name)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                onArtistSelect(artist.id, artist.name);
                            }
                        }}
                    >
                        <img
                            id={artist.id}
                            src={artist.imageUrl}
                            alt={artist.name}
                            className="w-[30px] h-[30px] rounded-full mr-3"
                        />
                        <span>{artist.name}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ArtistPop;
