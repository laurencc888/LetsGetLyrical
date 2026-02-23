import React from "react";
import {render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import Search from "./Search";
import userEvent from "@testing-library/user-event";


jest.mock('react-d3-cloud', () => () => <div data-testid="mocked-word-cloud" />);

const user = userEvent.setup();

const mockArtistJson = [
    {
        id: 1,
        name: "Taylor Swift",
        imageUrl: "https://images.genius.com/df4a816f593b08bc8a361ad58a848640.1000x1000x1.jpg"
    }
];

const mockEmptyJson = [];

const mockSongsJson = [
    {
        songName: 'Blank Space',
        songId: 100,
        artistId: 1,
        lyrics: 'blah blah blah',
        releaseYear: 2025,
    }
];

const mockTwoSongsJson = [
    {
        songName: 'Blank Space',
        songId: 100,
        artistId: 1,
        lyrics: 'blah blah blah',
        releaseYear: 2025,
    },
    {
        songName: 'Love Story',
        songId: 101,
        artistId: 1,
        lyrics: 'blah blah blah',
        releaseYear: 2025,
    }
];

function renderPage() {
    render(
        <MemoryRouter>
            <Search />
        </MemoryRouter>
    )
}

// resetting mocking behavior
beforeEach(() => {
    jest.resetAllMocks();
    renderPage();
});

const searchArtist = async (name) => {
    await user.type(screen.getByPlaceholderText("Artist"), name);
    await user.click(screen.getByTestId("get-artists"));
};

const sortBySelect = async (byPopularity) => {
    await user.click(screen.getByText("Sort By"));

    if (byPopularity) {
        await user.click(screen.getByText("Popularity"));
    }
    else {
        await user.click(screen.getByText("Manual"));
    }
};

const doPopularSearch = async () => {
    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await sortBySelect(true);
    await inputNumSongs("1");
    await user.click(screen.getByTestId("search"));
};

const inputNumSongs = async (numSongs) => {
    await user.type(screen.getByPlaceholderText("# Songs"), numSongs);
};

test("Successfully generate Taylor top 5", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        });

    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await sortBySelect(true);
    await inputNumSongs("5");
    await user.click(screen.getByTestId("search"));

    expect(screen.getByPlaceholderText("Artist")).toHaveValue("Taylor Swift");
});

test("manually get 1 from taylor", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        });

    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await sortBySelect(false);
    await inputNumSongs("1");
    await user.click(screen.getByTestId("get-songs"));
    await user.click(screen.getByText("Blank Space"));
    expect(screen.getByText("Manual")).toBeInTheDocument();
});

test("close artist pop up", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        });

    await searchArtist("Taylor");
    await user.click(screen.getByTestId("close-artist-pop"));

    expect(screen.queryByText("Taylor Swift")).not.toBeInTheDocument();
});

test("no artists found", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockEmptyJson,
            ok: true,
        });

    await searchArtist("Taylor");
    expect(screen.getByText("No artists found")).toBeInTheDocument();
});

test("empty artist field", async () => {
    await user.click(screen.getByTestId("get-artists"));
    expect(screen.getByText("Please enter an artist name"));
});

test("error fetching artists", async () => {
    global.fetch = jest.fn().mockRejectedValue(new Error("fetching artist error"));

    await searchArtist("Taylor");
    expect(screen.getByText("Failed to fetch artists. Please try again.")).toBeInTheDocument();
});

test("handle invalid field search cases", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        });

    await user.click(screen.getByTestId("search"));
    expect(screen.getByText("Artist field is empty!")).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("Artist"), "Taylor");
    await user.click(screen.getByTestId("search"));
    expect(screen.getByText("Number of songs is empty!")).toBeInTheDocument();

    await inputNumSongs("2");
    await user.click(screen.getByTestId("search"));
    expect(screen.getByText("Please select a sort option!")).toBeInTheDocument();

    await sortBySelect(false);
    await user.type(screen.getByPlaceholderText("Artist"), "invalid artist");
    await user.click(screen.getByTestId("search"));
    expect(screen.getByText("Please select a valid artist!")).toBeInTheDocument();

    // await searchArtist("Taylor");
    // await user.click(screen.getByText("Taylor Swift"));
    // await user.type(screen.getByPlaceholderText("Artist"), "Taylor");
    // await user.click(screen.getByTestId("search"));
    // expect(screen.getByText("Please select the artist using Get Artists!")).toBeInTheDocument();

    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await user.click(screen.getByTestId("search"));
    expect(screen.getByText("Please select songs from the list!")).toBeInTheDocument();

    await user.click(screen.getByTestId("get-songs"));
    await user.click(screen.getByText("Blank Space"));
    await user.click(screen.getByText("Love Story"));
    await user.click(screen.getByTestId("search"));
    expect(screen.queryByText("Please select songs from the list!")).not.toBeInTheDocument();
});

test("clicking through sort by menu", async () => {
    await sortBySelect(true);
    await user.click(screen.getByText("Popularity"));
    await user.click(screen.getByText("Manual"));

    await user.click(screen.getByText("Manual"));
    await user.click(screen.getByText("Popularity"));

    expect(screen.getByText("Popularity")).toBeInTheDocument();
});

test("no songs for artist", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockEmptyJson,
            ok: true,
        });

    await doPopularSearch();
    expect(screen.getByText("No songs found for this artist.")).toBeInTheDocument();
});

test("error fetching songs", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockRejectedValueOnce(
            new Error("fetching songs error")
        );

    await doPopularSearch();
    expect(screen.queryByText("blah")).not.toBeInTheDocument();
});

test("searching from fav and get error", async () => {
    localStorage.getItem = jest.fn().mockReturnValue("username");

    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        })
        .mockRejectedValueOnce(
            new Error("fetching songs error")
        );

    await user.click(screen.getByTestId("search-from-favorite"));
    await user.click(screen.getByTestId("search-from-favorite"));
    expect(screen.getByText("Failed to fetch favorite songs. Please try again.")).toBeInTheDocument();
});

test("manual pick but no artist selected or songs returned", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockEmptyJson,
            ok: true,
        });

    await sortBySelect(false);

    await user.click(screen.getByTestId("get-songs"));
    expect(screen.getByText("No artist selected!")).toBeInTheDocument();

    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));

    await user.click(screen.getByTestId("get-songs"));
    expect(screen.getByText("Number of songs is empty!")).toBeInTheDocument();

    await inputNumSongs("1");
    await user.click(screen.getByTestId("get-songs"));
    expect(screen.getByText("No songs found for this artist.")).toBeInTheDocument();
    await user.click(screen.getByTestId("get-songs"));
});

test("manually picking same song", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        });

    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await sortBySelect(false);
    await inputNumSongs("2");
    await user.click(screen.getByTestId("get-songs"));
    await user.click(screen.getByText("Blank Space"));
    await user.click(screen.getByText("Blank Space"));
    expect(screen.getByText("Blank Space")).toBeInTheDocument();
});

test("adding from favorite", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        });

    await user.click(screen.getByTestId("add-from-favorite"));
    expect(screen.queryByText("Failed to fetch favorite songs. Please try again.")).not.toBeInTheDocument();
});

test("adding to word cloud from fav", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        });

    await doPopularSearch();
    await user.click(screen.getByTestId("add-from-favorite"));
    expect(screen.getByTestId("mocked-word-cloud")).toBeInTheDocument();
});

test("adding to results successfully", async () => {
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Need a valid word cloud!")).toBeInTheDocument();

    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        });

    await user.click(screen.getByTestId("search-from-favorite"));

    // checking edge cases
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Artist field is empty!")).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("Artist"), "Taylor");
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Number of songs is empty!")).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("# Songs"), "1");
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Please select a sort option!")).toBeInTheDocument();

    await sortBySelect(false);
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Please select a valid artist!")).toBeInTheDocument();

    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        });
    await searchArtist("Taylor");
    await user.click(screen.getByText("Taylor Swift"));
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.getByText("Please select enough songs!"));

    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockTwoSongsJson,
            ok: true,
        });
    await user.click(screen.getByTestId("get-songs"));
    await user.click(screen.getByText("Love Story"));
    await user.click(screen.getByTestId("add-to-results"));
    expect(screen.queryByText("Please select enough songs!")).not.toBeInTheDocument();
});

test("adding to results by popularity", async () => {
    global.fetch = jest.fn()
        .mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        }).mockResolvedValueOnce({
            json: async () => mockArtistJson,
            ok: true,
        })
        .mockResolvedValueOnce({
            json: async () => mockSongsJson,
            ok: true,
        });

    await doPopularSearch();
    await user.click(screen.getByTestId("add-to-results"));

    expect(screen.getByTestId("mocked-word-cloud")).toBeInTheDocument();

});

test("no user means go to login from search page", async () => {
    Storage.prototype.getItem = jest.fn(() => null);

    delete window.location; // delete the default location object
    window.location = { href: '' }; // assign mock object

    Object.defineProperty(window, "localStorage", {
        value: {
            getItem: jest.fn(() => null),
            setItem: jest.fn(),
            removeItem: jest.fn(),
        },
        writable: true,
    });

    render(
        <MemoryRouter>
            <Search/>
        </MemoryRouter>
    );
    expect(window.location.href).toBe('/');
});
